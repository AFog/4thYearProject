package com.aaronfogarty.huh;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import co.devcenter.androiduilibrary.ChatView;
import co.devcenter.androiduilibrary.ChatViewEventListener;
import co.devcenter.androiduilibrary.SendButton;
import co.devcenter.androiduilibrary.models.ChatMessage;

import static com.aaronfogarty.huh.HuhConnectionService.OFFLINE_MESSAGE_ARRAYLIST;
import static com.aaronfogarty.huh.HuhConnectionService.UNAVAILABLE_MESSAGE_ARRAYLIST;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";


    private String contactJid, displayJid;
    private ChatView mChatView;
    private SendButton mSendButton;
    private BroadcastReceiver mBroadcastReceiver;
    private BroadcastReceiver unavailableBroadcastReceiver;
    private BroadcastReceiver offlineBroadcastReceiver;
    private String messageLog;
    private String userJid;
    private String FILENAME;
    //HashSet<HuhMessage> ChatHistoryList;
    List<HuhMessage> ChatHistoryList;
    List<HuhMessage> cachedEntries;
    List<String> unavailableMessages;
    List<String> offlineMessages;
    private String translatedText;

    public String getTranslatedText(){
        return translatedText;
    }
    public void setTranslatedText(String inputText){
        translatedText = inputText;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() ");

        setContentView(R.layout.activity_chat);

        Intent i = new Intent(HuhConnectionService.UI_UNAVAILABLE);
        i.setPackage(getApplication().getPackageName());
        getApplication().sendBroadcast(i);
        Log.d(TAG, "BROADCAST: (onCreate()Sent the broadcast that we are Unvailable to HuhConnection broadCastAvailabilityReceiver()");

        // Reading from SharedPreferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        userJid = prefs.getString("xmpp_jid", "");
        // The list that should be saved to internal storage.
        mChatView = (ChatView) findViewById(R.id.huh_chat_view);
        mChatView.setEventListener(new ChatViewEventListener() {
            @Override
            public void userIsTyping() {
                //Here you know that the user is typing
                //Log.d(TAG, "User is typing");

            }

            @Override
            public void userHasStoppedTyping() {
                //Here you know that the user has stopped typing.
                //Log.d(TAG, "User is typing has stopped typing");
            }
        });



        mSendButton = mChatView.getSendButton();
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Only send the message if the client is connected
                //to the server.
                if (HuhConnectionService.getState().equals(HuhConnection.ConnectionState.CONNECTED)) {
                    Log.d(TAG, "The client is connected to the server,Sending Message");
                    //Send the message to the server

                    //BOROADCAST
                    Intent intent = new Intent(HuhConnectionService.SEND_MESSAGE);
                    intent.putExtra(HuhConnectionService.BUNDLE_MESSAGE_BODY,
                            mChatView.getTypedString());
                    intent.putExtra(HuhConnectionService.BUNDLE_TO, contactJid);
                    //Broadcasting to setupUiThreadBroadCastMessageReceiver listening for type HuhConnectionService.SEND_MESSAGE in the HuhConnection class
                    sendBroadcast(intent);

                    //Saves message to internal storage
                    writeToChatHistoryList(userJid,contactJid, mChatView.getTypedString(), true);
                    //Update the chat view.
                    mChatView.sendMessage();


                } else {
                    Log.d(TAG, "Client not connected to server ,Message not sent!");
                    Toast.makeText(getApplicationContext(), "Client not connected to server ,Message not sent!", Toast.LENGTH_LONG).show();
                }
            }
        });

        Intent intent = getIntent();
        //initialise contactJid
        contactJid = intent.getStringExtra("EXTRA_CONTACT_JID");
        if( contactJid != null)
        {
            displayJid = contactJid.split("@")[0];
        }
        FILENAME = "Chat.History" + userJid + contactJid;
        Log.d(TAG, "(onCreate) Creating a file name for chatHistory. FILENAME: " + FILENAME);
        setTitle(displayJid);

       // processUnavailableMessages();
        processOfflineMessages();
        unregisterReceiver(offlineBroadcastReceiver);

        chatHistory();

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
        unregisterReceiver(unavailableBroadcastReceiver);
        unregisterReceiver(offlineBroadcastReceiver);

        Intent i = new Intent(HuhConnectionService.UI_UNAVAILABLE);
        i.setPackage(getApplication().getPackageName());
        getApplication().sendBroadcast(i);
        Log.d(TAG, "BROADCAST: (onPause)Sent the broadcast that we are Unvailable to HuhConnection broadCastAvailabilityReceiver()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent i = new Intent(HuhConnectionService.UI_AVAILABLE);
        i.setPackage(getApplication().getPackageName());
        getApplication().sendBroadcast(i);
        Log.d(TAG, "BROADCAST: (onResume)Sent the broadcast that we are Available to HuhConnection broadCastAvailabilityReceiver()");

       processUnavailableMessages();
       processOfflineMessages();
///START
        Runnable r = new Runnable() {
            @Override
            public void run() {
                //Initialise broadcast receiver that will listen for  broadcasts from messageListener(ChatMessageListener) in HuhConnection class
                mBroadcastReceiver = new BroadcastReceiver() {
                    //String body ="";

                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Log.d(TAG, "BROADCAST RECEIVED:(onResume()) recieving messages from huhConnection processMessage() " );
                        String action = intent.getAction();
                        switch (action) {
                            case HuhConnectionService.NEW_MESSAGE:
                                String from = intent.getStringExtra(HuhConnectionService.BUNDLE_FROM_JID);
                                //body = translateMessageText(intent.getStringExtra(HuhConnectionService.BUNDLE_MESSAGE_BODY),"fr");
                                String body = intent.getStringExtra(HuhConnectionService.BUNDLE_MESSAGE_BODY);
                                Log.d(TAG, "BROADCAST RECEIVED:(onResume()) recieving messages from huhConnection processMessage() MESSAGE: " + body);
                                messageLog = body;

                                // String transtext = translateMessageText(body,"ja");
                                //   Log.d(TAG, "_____________________________---------------------______________________transtext :" + transtext);

                                if (from.equals(contactJid)) {
                                    //String transtext = translateMessageText(body,"fr");

                                    //writeMessageToChatHistory(body);
                                    //Saves message to internal storage
                                    writeToChatHistoryList(contactJid, userJid, body, false);
                                    mChatView.receiveMessage(body);
                                } else {
                                    Log.d(TAG, "Got a message from jidPhoneNumber :" + from);
                                }

                                return;
                        }

                    }
                };

                IntentFilter filter = new IntentFilter(HuhConnectionService.NEW_MESSAGE);
                registerReceiver(mBroadcastReceiver, filter);
            }
        };

        Thread t = new Thread(r);
        t.start();


///END
    }

//    //writes messages as strings to file, NOT USED
//    public void writeMessageToChatHistory(String msg) {
//        messageLog = msg + "\n";
//        try {
//            if (messageLog != null)
//
//            {
//                FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_APPEND);
//                fos.write(messageLog.getBytes());
//                fos.close();
//                Log.d(TAG, "****WRITE CHATHISTORY*** :" + messageLog);
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    //reads messages as strings from file and displays, NOT USED
//    public void readMessageFromChatHistory(String file) {
//
//
//        try {
//            String line;
//            FileInputStream in = openFileInput(file);
//
//            InputStreamReader inputStreamReader = new InputStreamReader(in);
//            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//            StringBuilder sb = new StringBuilder();
//            while ((line = bufferedReader.readLine()) != null) {
//                sb.append(line + "\n");
//
//
//                //Log.d(TAG, "****READ CHATHISTORY*** :" + line);
//                mChatView.receiveMessage("recall " + line);
//
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
    public static void writeObject(Context context, String file, Object object) throws IOException {
        FileOutputStream fos = context.openFileOutput(file, Context.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(object);
        oos.close();
        fos.close();
    }

    public static Object readObject(Context context, String file) throws IOException,
            ClassNotFoundException {
        FileInputStream fis = context.openFileInput(file);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object object = ois.readObject();
        return object;
    }

    //Saves message to internal storage
    public void writeToChatHistoryList(String sender, String receiver, String msg, boolean isMine) {

        //Log.d(TAG, "INSIDES WRITELIST line 244, Body: " + msg);
        // HashSet<HuhMessage> ChatHistoryList = new HashSet<HuhMessage>();
//        if(userJid == receiver){
//            isMine = true;
//        }

        HuhMessage m = new HuhMessage(sender,receiver, msg, isMine);
        ChatHistoryList.add(m);
        try {
            // Save the list of entries to internal storage
            ChatActivity.writeObject(this, FILENAME, ChatHistoryList);

        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

    }

    //Reads message from internal storage
    public void readFromChatHistoryList() {
        try {
            // Retrieve the list from internal storage
            cachedEntries = (List<HuhMessage>) readObject(this, FILENAME);
            // Display the items from the list retrieved.
            for (HuhMessage entry : cachedEntries) {
                //Log.d(TAG, "line 231" + entry.sender);
               // Log.d(TAG, "INSIDES ReadList line 233" + entry.sender);
                if(entry.isMine == true){
                    ChatMessage cm = new ChatMessage(entry.body, 12, ChatMessage.Status.SENT);
                    mChatView.sendMessage(cm);
                }
                if (entry.isMine == false){
                    mChatView.receiveMessage("Sender: " + entry.sender + "\nReceiver: " + entry.receiver + "\nbody: " + entry.body + "\nTime: " + System.currentTimeMillis());
                }

            }

        } catch (ClassNotFoundException | IOException e) {
            Log.e(TAG, e.getMessage());
        }

    }

    //Retrieves and displays Chat History
    public void chatHistory() {
        try {
            if (!fileExistance(FILENAME)) {
                ChatHistoryList = new ArrayList<HuhMessage>();
                Log.d(TAG, "Creating NEW ChatHistory " + FILENAME);

            } else {
                // Retrieve the list from internal storage
                ChatHistoryList = (List<HuhMessage>) readObject(this, FILENAME);
                Log.d(TAG, "Initialising ChatHistory with file " + FILENAME);
            }

        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }

        readFromChatHistoryList();
    }

    public boolean fileExistance(String fname) {
        File file = getBaseContext().getFileStreamPath(fname);
        return file.exists();
    }

    public void processUnavailableMessages(){
        Log.d(TAG, "processUnavailableMessages()");

        //Initialise broadcast receiver that will listen for  broadcasts from messageListener(ChatMessageListener) in HuhConnection class
       // unavailableMessages = new ArrayList<String>();

        unavailableBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d(TAG, "processUnavailableMessages()++++++++++++++++++++++++++++++working");

                switch (action) {
                    case HuhConnectionService.UNAVAILABLE_MESSAGE:
                        unavailableMessages = intent.getStringArrayListExtra(UNAVAILABLE_MESSAGE_ARRAYLIST);

                        //if (from.equals(contactJid)) {
                            for (String body : unavailableMessages) {

                                Log.d(TAG, "processUnavailableMessages() message: " + body);

                                //writeMessageToChatHistory(body);
                                //Saves message to internal storage
                                writeToChatHistoryList(contactJid, userJid, body, false);
                                //mChatView.receiveMessage("processUnavailableMessages: " + body);
                            }

//                        } else {
//                            Log.d(TAG, "Got a message from jidPhoneNumber :" + from);
//                        }

                }

            }
        };

        IntentFilter filter = new IntentFilter(HuhConnectionService.UNAVAILABLE_MESSAGE);
        registerReceiver(unavailableBroadcastReceiver, filter);
    }

    public void processOfflineMessages(){
        Log.d(TAG, "processOfflineMessages()");

        //Initialise broadcast receiver that will listen for  broadcasts from messageListener(ChatMessageListener) in HuhConnection class
        offlineMessages = new ArrayList<String>();

        offlineBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d(TAG, "processOfflineMessages()++++++++++++++++++++++++++++++working");
if(intent.hasExtra(UNAVAILABLE_MESSAGE_ARRAYLIST)){
    Log.d(TAG, "processOfflineMessages()++++++++++++++YESSSSSS");

}
                switch (action) {
                    case HuhConnectionService.OFFLINE_MESSAGE:
                        offlineMessages = intent.getStringArrayListExtra(OFFLINE_MESSAGE_ARRAYLIST);

                            if (!offlineMessages.isEmpty()) {
                                for (String body : offlineMessages) {

                                    Log.d(TAG, "processOfflineMessages() message: " + body);

                                    //writeMessageToChatHistory(body);
                                    //Saves message to internal storage
                                    writeToChatHistoryList(contactJid, userJid, body, false);
                                    mChatView.receiveMessage("processOfflineMessages: " + body);
                                }
                            }

                }

            }
        };

        IntentFilter filter = new IntentFilter(HuhConnectionService.OFFLINE_MESSAGE);
        registerReceiver(offlineBroadcastReceiver, filter);
    }

    public String translateMessageText(String text, String toLanguage) {

        String output ="";
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = "https://iueh1tvfn3.execute-api.us-west-2.amazonaws.com/tranlateenpointbeta/";
            Log.d("translateText()", "");

            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("translatedText", text);
                jsonBody.put("targetLanguage", toLanguage);
                jsonBody.put("sourceLanguage", toLanguage);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            String requestBody = jsonBody.toString();
            Log.d("RequestBody", requestBody);
            //

            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Log.d("translateText", "response: " + response.toString());
                                JSONObject jsonObject = new JSONObject(response.toString());

                                String translatedText = response.getString("translatedText");
                                // String tolanguage = response.get("targetLanguage").toString();
                                String sourceLanguage = response.getString("detectedSourceLanguage");
                                setTranslatedText(translatedText);
                                Log.d("translateText()", "get response: " + translatedText + " detectedSourceLanguage: " + sourceLanguage);

                            } catch (JSONException e) {
                                Log.d("Trans JSON ex: ", e.getMessage());
                            } catch (Exception e) {
                                Log.d("Translation Exception ", e.getMessage());
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.i("Trans Exception ", error.getMessage());

                        }
                    });
            //

            queue.add(jsObjRequest);

            output = getTranslatedText();
            translatedText = "";


        return output;
    }
}
