package com.aaronfogarty.huh;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.ConnectionService;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Message;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import co.devcenter.androiduilibrary.ChatView;
import co.devcenter.androiduilibrary.ChatViewEventListener;
import co.devcenter.androiduilibrary.SendButton;

import static android.R.attr.entries;
import static android.R.id.list;
import static org.jivesoftware.smack.packet.RosterPacket.ItemType.from;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";

    private String contactJid;
    private ChatView mChatView;
    private SendButton mSendButton;
    private BroadcastReceiver mBroadcastReceiver;
    private String messageLog;
    private boolean isMine;
    private String userJid;
    private String FILENAME = "Chat.History" + userJid + contactJid;
    //HashSet<HuhMessage> ChatHistoryList;
    List<HuhMessage> ChatHistoryList;
    List<HuhMessage> cachedEntries;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() " + contactJid);

        setContentView(R.layout.activity_chat);


        // Reading from SharedPreferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        userJid = prefs.getString("xmpp_jid", "");
        Log.d(TAG, userJid);
        // The list that should be saved to internal storage.
        mChatView = (ChatView) findViewById(R.id.huh_chat_view);
        mChatView.setEventListener(new ChatViewEventListener() {
            @Override
            public void userIsTyping() {
                //Here you know that the user is typing
            }

            @Override
            public void userHasStoppedTyping() {
                //Here you know that the user has stopped typing.
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
                    //Broadcasting to setupUiThreadBroadCastMessageReceiver listening for type HuhConnectionService.SEND_MESSAGE
                    sendBroadcast(intent);

                    //writeMessageToChatHistory(mChatView.getTypedString());

                    //Update the chat view.
                    mChatView.sendMessage();
                } else {
                    Log.d(TAG, "Client not connected to server ,Message not sent!");
                    //  Toast.makeText(getApplicationContext(), "Client not connected to server ,Message not sent!", Toast.LENGTH_LONG).show();
                }
            }
        });

        Intent intent = getIntent();
        contactJid = intent.getStringExtra("EXTRA_CONTACT_JID");
        setTitle(contactJid);
        chatHistory();

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Initialise broadcast receiver that will listen for  broadcasts from messageListener(ChatMessageListener) in HuhConnection class
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case HuhConnectionService.NEW_MESSAGE:
                        String from = intent.getStringExtra(HuhConnectionService.BUNDLE_FROM_JID);
                        String body = intent.getStringExtra(HuhConnectionService.BUNDLE_MESSAGE_BODY);
                        messageLog = body;
                        if (from.equals(contactJid)) {

                            //writeMessageToChatHistory(body);
                            //Saves message to internal storage
                            writeList(contactJid, body);
                            mChatView.receiveMessage(body);

                        } else {
                            Log.d(TAG, "Got a message from jid :" + from);
                        }

                        return;
                }

            }
        };

        IntentFilter filter = new IntentFilter(HuhConnectionService.NEW_MESSAGE);
        registerReceiver(mBroadcastReceiver, filter);


    }

    //writes messages as strings to file, not used
    public void writeMessageToChatHistory(String msg) {
        messageLog = msg + "\n";
        try {
            if (messageLog != null)

            {
                FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_APPEND);
                fos.write(messageLog.getBytes());
                fos.close();
                Log.d(TAG, "****WRITE CHATHISTORY*** :" + messageLog);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //reads messages as strings from file and displays, not used
    public void readMessageFromChatHistory(String file) {


        try {
            String line;
            FileInputStream in = openFileInput(file);

            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder sb = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line + "\n");


                //Log.d(TAG, "****READ CHATHISTORY*** :" + line);
                mChatView.receiveMessage("recall " + line);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

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
    public void writeList(String receiver, String msg) {

        Log.d(TAG, "INSIDES WRITELIST line 244, Body: " + msg);
        // HashSet<HuhMessage> ChatHistoryList = new HashSet<HuhMessage>();
//        if(userJid == receiver){
//            isMine = true;
//        }

        HuhMessage m = new HuhMessage(userJid, receiver, msg, isMine);
        ChatHistoryList.add(m);
        try {
            // Save the list of entries to internal storage
            ChatActivity.writeObject(this, FILENAME, ChatHistoryList);

        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

    }

    //Reads message from internal storage
    public void readList() {
        try {
            // Retrieve the list from internal storage
            cachedEntries = (List<HuhMessage>) readObject(this, FILENAME);
            // Display the items from the list retrieved.
            for (HuhMessage entry : cachedEntries) {
                Log.d(TAG, "line 231" + entry.sender);
                Log.d(TAG, "INSIDES ReadList line 233" + entry.sender);
                mChatView.receiveMessage("Sender: " + entry.sender + "\nReceiver: " + entry.receiver + "\nbody: " + entry.body + "\nTime: " + System.currentTimeMillis());
            }

        } catch (ClassNotFoundException | IOException e) {
            Log.e(TAG, e.getMessage());
        }

    }

    //Retrieves and displays Chat History
    public void chatHistory() {
        // readMessageFromChatHistory(FILENAME);
        try {
            if (!fileExistance(FILENAME)) {
                ChatHistoryList = new ArrayList<HuhMessage>();
                Log.d(TAG, "Initialising NEW ChatHistory");

            } else {
                // Retrieve the list from internal storage
                ChatHistoryList = (List<HuhMessage>) readObject(this, FILENAME);
                Log.d(TAG, "Initialising ChatHistory with file");
            }

        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }

        readList();
    }

    public boolean fileExistance(String fname) {
        File file = getBaseContext().getFileStreamPath(fname);
        return file.exists();
    }

}