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
import java.lang.reflect.Array;
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


    private String contactJid, contactJidDis, displayJid;
    private ChatView mChatView;
    private SendButton mSendButton;
    private BroadcastReceiver mBroadcastReceiver;
    private BroadcastReceiver unavailableBroadcastReceiver;
    private BroadcastReceiver offlineBroadcastReceiver;
    private String messageLog;
    private String userJid;
    private String FILENAME;
    private String PERSONHISTORYFILE;
    //HashSet<HuhMessage> ChatHistoryList;
    List<HuhMessage> ChatHistoryList;
    List<String> personChatHistoryList;
    List<HuhMessage> cachedEntries;
    List<String> unavailableMessages;
    List<String> offlineMessages;
    private String translatedText;
    private String unavailablefromJid;

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String inputText) {
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
                    writeToChatHistoryList(userJid, contactJid, mChatView.getTypedString(), true);
                    writeToPersonHistory(mChatView.getTypedString());
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
        Log.d(TAG, "broadcast contact from listview: " + contactJid);
        contactJidDis = intent.getStringExtra("EXTRA_CONTACT_DISPLAY");
        Log.d(TAG, "broadcast contact from listview: " + contactJidDis);

        if (contactJidDis != null) {
            displayJid = contactJidDis.split("@")[0];
        }

        FILENAME = "Chat.History" + userJid + contactJid;
        PERSONHISTORYFILE = "Chat.History" + userJid;
        Log.d(TAG, "(onCreate) Creating a file name for chatHistory. FILENAME: " + FILENAME);
        setTitle(displayJid);

        // processUnavailableMessages();
        processOfflineMessages();
        unregisterReceiver(offlineBroadcastReceiver);

        chatHistory();
        personChatHistory();

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
                        Log.d(TAG, "BROADCAST RECEIVED:(onResume()) recieving messages from huhConnection processMessage() ");
                        String action = intent.getAction();
                        switch (action) {
                            case HuhConnectionService.NEW_MESSAGE:
                                String from = intent.getStringExtra(HuhConnectionService.BUNDLE_FROM_JID);
                                //body = translateMessageText(intent.getStringExtra(HuhConnectionService.BUNDLE_MESSAGE_BODY),"fr");
                                String body = intent.getStringExtra(HuhConnectionService.BUNDLE_MESSAGE_BODY);
                                Log.d(TAG, "BROADCAST RECEIVED:(onResume()) recieving messages from huhConnection processMessage() MESSAGE: " + from + "  ContactJid is " + contactJid);
                                Log.d(TAG, "BROADCAST RECEIVED:(onResume()) recieving messages from huhConnection processMessage() MESSAGE: " + body);
                                messageLog = body;

                                Log.d(TAG, "TEST Got a message from jidPhoneNumber :" + from + "FILENAME: " + FILENAME);
                                //TODO: Replace this with your own logic
                                //add this check to unavailable and offline
                                if (from.equals(contactJid)) {
                                    //String transtext = translateMessageText(body,"fr");

                                    //writeMessageToChatHistory(body);
                                    //Saves message to internal storage
                                    writeToChatHistoryList(contactJid, userJid, body, false);
                                    mChatView.receiveMessage(body);
                                } else {
                                    Log.d(TAG, "NOT THE CURRENT USER Got a message from jidPhoneNumber :" + from);
                                    writeToOtherContactChatHistoryList(userJid, from, body, false);
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

        HuhMessage m = new HuhMessage(sender, receiver, "CHATHISTORY "+msg, isMine);
        ChatHistoryList.add(m);
        try {
            // Save the list of entries to internal storage
            ChatActivity.writeObject(this, FILENAME, ChatHistoryList);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

    }

    public void writeToPersonHistory(String msg) {
        Log.d(TAG, "saving message to personal history");

        personChatHistoryList.add(msg);
        try {
            // Save the list of entries to internal storage
            ChatActivity.writeObject(this, PERSONHISTORYFILE, personChatHistoryList);
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
                if (entry.isMine == true) {
                    ChatMessage cm = new ChatMessage(entry.body, 12, ChatMessage.Status.SENT);
                    mChatView.sendMessage(cm);
                }
                if (entry.isMine == false) {
                    // mChatView.receiveMessage("Sender: " +entry.sender + "\nReceiver: " + entry.receiver + "\nbody: " + entry.body + "\nTime: " + System.currentTimeMillis());
                    mChatView.receiveMessage(entry.body);
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

    public void personChatHistory() {
        try {
            if (!fileExistance(PERSONHISTORYFILE)) {
                personChatHistoryList = new ArrayList<String>();
                Log.d(TAG, "Creating NEW PersonChatHistory " + PERSONHISTORYFILE);

            } else {
                // Retrieve the list from internal storage
                personChatHistoryList = (List<String>) readObject(this, PERSONHISTORYFILE);
                Log.d(TAG, "Initialising PersonChatHistory with file " + PERSONHISTORYFILE);
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

    public void processUnavailableMessages() {
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
                        unavailablefromJid = intent.getStringArrayListExtra(UNAVAILABLE_MESSAGE_ARRAYLIST).get(0);
                        if (!unavailableMessages.isEmpty()) {
                            //String fromJid = intent.getStringExtra("FROM_JID");
                            //Log.d(TAG, "processUnavailableMessages() fromJid: " + fromJid);
                            Log.d(TAG, "processUnavailableMessages() from: " + unavailablefromJid);

                            unavailablefromJid = unavailablefromJid.split("-*_-")[0];
                            unavailablefromJid = unavailablefromJid.split("/")[0];

                            Log.d(TAG, "processUnavailableMessages() AFTER SPLIT from: " + unavailablefromJid + " CONTACT JID " + contactJid);

                            if (unavailablefromJid.equals(contactJid)) {
                                for (String body : unavailableMessages) {
                                    body = body.split("-*_-")[1];
                                    Log.d(TAG, "processUnavailableMessages() from: " + unavailablefromJid);
                                    Log.d(TAG, "processUnavailableMessages() message: " + body);

                                    //writeMessageToChatHistory(body);
                                    //Saves message to internal storage
                                    writeToChatHistoryList(contactJid, userJid, "History unavailable: " + body, false);
                                    mChatView.receiveMessage("processUnavailableMessages: " + body);
                                }

                            } else {
                                if (!unavailableMessages.isEmpty()) {
                                    for (String body : unavailableMessages) {
                                        body = body.split("-*_-")[1];
                                        Log.d(TAG, "processUnavailableMessages() from: " + unavailablefromJid);
                                        Log.d(TAG, "processUnavailableMessages() message: " + body);
                                        //Saves message to internal storage for other contact
                                        writeToOtherContactChatHistoryList(userJid, unavailablefromJid, body, false);
                                    }
                                    Log.d(TAG, "processOfflineMessages() OTHER CONTACT Got a message from jidPhoneNumber :" + unavailablefromJid);
                                }
                                Log.d(TAG, "processUnavailableMessages() AFTER NOT THE SAME Got a message from jidPhoneNumber :" + unavailablefromJid);
                            }
                        }
                }

            }
        };

        IntentFilter filter = new IntentFilter(HuhConnectionService.UNAVAILABLE_MESSAGE);
        registerReceiver(unavailableBroadcastReceiver, filter);
    }

    public void processOfflineMessages() {
        Log.d(TAG, "processOfflineMessages()");

        //Initialise broadcast receiver that will listen for  broadcasts from messageListener(ChatMessageListener) in HuhConnection class
        offlineMessages = new ArrayList<String>();

        offlineBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d(TAG, "processOfflineMessages()++++++++++++++++++++++++++++++working");

                switch (action) {
                    case HuhConnectionService.OFFLINE_MESSAGE:
                        offlineMessages = intent.getStringArrayListExtra(OFFLINE_MESSAGE_ARRAYLIST);
                        if (!offlineMessages.isEmpty()) {

                            for(int i = 0; i < offlineMessages.size(); i ++){

                                Log.d(TAG, "processOfflineMessages() int i = : " + i);
                                unavailablefromJid = intent.getStringArrayListExtra(OFFLINE_MESSAGE_ARRAYLIST).get(i);
                                Log.d(TAG, "processOfflineMessages() from: " + unavailablefromJid);

                                unavailablefromJid = unavailablefromJid.split("-*_-")[0];
                                unavailablefromJid = unavailablefromJid.split("/")[0];
                                Log.d(TAG, "processOfflineMessages() AFTER SPLIT from: " + unavailablefromJid + " CURRENT CONTACT JID " + contactJid);
                                String body = offlineMessages.get(i);
                                if (unavailablefromJid.equals(contactJid)) {

                                    if (!offlineMessages.isEmpty()) {
                                            body = body.split("-*_-")[1];
                                            Log.d(TAG, "processOfflineMessages() from: " + unavailablefromJid);
                                            Log.d(TAG, "processOfflineMessages() message: " + body);

                                            //writeMessageToChatHistory(body);
                                            //Saves message to internal storage
                                            writeToChatHistoryList(contactJid, userJid, body, false);
                                            mChatView.receiveMessage("processOfflineMessages: " + body);

                                    }
                                } else {

                                if (!offlineMessages.isEmpty()) {
                                        body = body.split("-*_-")[1];
                                        Log.d(TAG, "processOfflineMessages()OTHER from: " + unavailablefromJid);
                                        Log.d(TAG, "processOfflineMessages()OTHER message: " + body);
                                        //Saves message to internal storage for other contact
                                        writeToOtherContactChatHistoryList(userJid, unavailablefromJid, body, false);

                                    Log.d(TAG, "processOfflineMessages() OTHER CONTACT Got a message from jidPhoneNumber :" + unavailablefromJid);
                                }
                                    Log.d(TAG, "processOfflineMessages() OTHER CONTACT Got a message from jidPhoneNumber :" + unavailablefromJid);

                                }

                            }
                        }
                }


            }
        };

        IntentFilter filter = new IntentFilter(HuhConnectionService.OFFLINE_MESSAGE);
        registerReceiver(offlineBroadcastReceiver, filter);
    }

    public void processOfflineMessagesoriginal() {
        Log.d(TAG, "processOfflineMessages()");

        //Initialise broadcast receiver that will listen for  broadcasts from messageListener(ChatMessageListener) in HuhConnection class
        offlineMessages = new ArrayList<String>();

        offlineBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d(TAG, "processOfflineMessages()++++++++++++++++++++++++++++++working");

                switch (action) {
                    case HuhConnectionService.OFFLINE_MESSAGE:
                        offlineMessages = intent.getStringArrayListExtra(OFFLINE_MESSAGE_ARRAYLIST);
                        if (!offlineMessages.isEmpty()) {
                            unavailablefromJid = intent.getStringArrayListExtra(OFFLINE_MESSAGE_ARRAYLIST).get(0);

                            //String fromJid = intent.getStringExtra("FROM_JID");
                            Log.d(TAG, "processOfflineMessages() from: " + unavailablefromJid);

                            unavailablefromJid = unavailablefromJid.split("-*_-")[0];
                            unavailablefromJid = unavailablefromJid.split("/")[0];

                            Log.d(TAG, "processOfflineMessages() AFTER SPLIT from: " + unavailablefromJid + " CONTACT JID " + contactJid);

                            if (unavailablefromJid.equals(contactJid)) {

                                if (!offlineMessages.isEmpty()) {
                                    for (String body : offlineMessages) {
                                        body = body.split("-*_-")[1];
                                        Log.d(TAG, "processOfflineMessages() from: " + unavailablefromJid);
                                        Log.d(TAG, "processOfflineMessages() message: " + body);

                                        //writeMessageToChatHistory(body);
                                        //Saves message to internal storage
                                        writeToChatHistoryList(contactJid, userJid, body, false);
                                        mChatView.receiveMessage("processOfflineMessages: " + body);
                                    }
                                }
                            } else {

//                                if (!offlineMessages.isEmpty()) {
//                                    for (String body : offlineMessages) {
//                                        body = body.split("-*_-")[1];
//                                        Log.d(TAG, "processOfflineMessages() from: " + unavailablefromJid);
//                                        Log.d(TAG, "processOfflineMessages() message: " + body);
//                                        //Saves message to internal storage for other contact
//                                        writeToOtherContactChatHistoryList(userJid, unavailablefromJid, body, false);
//                                    }
//                                    Log.d(TAG, "processOfflineMessages() OTHER CONTACT Got a message from jidPhoneNumber :" + unavailablefromJid);
//                                }
                                Log.d(TAG, "processOfflineMessages() OTHER CONTACT Got a message from jidPhoneNumber :" + unavailablefromJid);

                            }
                        }
                }


            }
        };

        IntentFilter filter = new IntentFilter(HuhConnectionService.OFFLINE_MESSAGE);
        registerReceiver(offlineBroadcastReceiver, filter);
    }

    public void writeToOtherContactChatHistoryList(String userJid, String otherContactJid, String msg, boolean isMine) {
//       Runnable r = new Runnable() {
//           @Override
//           public void run() {
//
//           }
//       };
//       Thread t = new Thread(r);
//        t.start();

        Log.d(TAG, "writeToOtherContactChatHistoryList()");
        String fileToretrieve = "Chat.History" + userJid + otherContactJid;
        Log.d(TAG, "FILE NAME " + fileToretrieve);

        List<HuhMessage> otherContactHistory = new ArrayList<>();

        try {
            // Retrieve the list from internal storage
            otherContactHistory = (List<HuhMessage>) readObject(this, fileToretrieve);
        } catch (ClassNotFoundException | IOException e) {
            Log.e(TAG, e.getMessage());
        }

        // Display the items from the list retrieved.
        for (HuhMessage entry : otherContactHistory) {
            //Log.d(TAG, "line 231" + entry.sender);
            // Log.d(TAG, "INSIDES ReadList line 233" + entry.sender);
            if (entry.isMine == true) {
                Log.d(TAG, "Sender: " + entry.sender + "\nReceiver: " + entry.receiver + "\nbody: " + entry.body + "\nTime: " + System.currentTimeMillis());
            }
            if (entry.isMine == false) {
                Log.d(TAG, "Sender: " + entry.sender + "\nReceiver: " + entry.receiver + "\nbody: " + entry.body + "\nTime: " + System.currentTimeMillis());
            }

        }

        HuhMessage m = new HuhMessage(userJid, otherContactJid, "OTHERHISTORY "+msg, isMine);
        otherContactHistory.add(m);

        try {
            // Save the list of entries to internal storage
            ChatActivity.writeObject(this, fileToretrieve, otherContactHistory);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

    }

    public String translateMessageText(String text, String toLanguage) {

        String output = "";
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
