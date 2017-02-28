package com.aaronfogarty.huh;

import android.net.Uri;
import android.os.Handler;
//import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ChatMessageListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

public class SendMessageActivity extends AppCompatActivity {

    private static final String TAG = "SendMessageActivity";
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    ChatMessageListener messageListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_send_message);
        TextView textview = (TextView) findViewById(R.id.XMPPConnectTestView);
        textview.setText("Before Connect");
        Log.d(TAG, "Connecting with server *****************************************************");

        Thread connectedThread;
        final Handler connectHanlder = new Handler() {
            @Override
            public void handleMessage(android.os.Message msg) {
                TextView textview2 = (TextView) findViewById(R.id.XMPPConnectTestView);
                textview2.setText("Connected!!");
                Log.d(TAG, "Connected with server ***************************************************");
            }
        };

        Runnable rconnect = new Runnable() {
            @Override
            public void run() {
                XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                        .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                        .setSendPresence(true)
                        .setUsernameAndPassword("aaron", "aaron")
                        .setServiceName("ec2-35-162-128-9.us-west-2.compute.amazonaws.com")
                        .setHost("ec2-35-162-128-9.us-west-2.compute.amazonaws.com") //WIN-H6G4CDQOT7E
                        .setPort(5222)
                        .setDebuggerEnabled(true)
                        .build();

                //AbstractXMPPConnection conn2 = new XMPPTCPConnection(config);
                XMPPTCPConnection conn = new XMPPTCPConnection(config);

                try {
                    conn.connect();
                    conn.login();
                    String p = "";

                    ChatManager chatManager = ChatManager.getInstanceFor(conn);
                    Chat chat = chatManager.createChat("shay@win-h6g4cdqot7e", new ChatMessageListener() {
                        @Override
                        public void processMessage(Chat chat, Message message) {
                            String output = chat.getParticipant() + "Message recieved: " + message.getBody();
                          //  p = chat.getParticipant() + "Message recieved: " + message.getBody();
                            chat.addMessageListener(this);
                            Log.d(TAG, "!!!!!!! inside chat" + output);
                        }
                    });

                    ChatManager.getInstanceFor(conn).addChatListener(new ChatManagerListener() {
                        @Override
                        public void chatCreated(Chat chat, boolean createdLocally) {

                            //If the line below is missing ,processMessage won't be triggered and you won't receive messages.
                            chat.addMessageListener(messageListener);

                        }
                    });

                    chat.sendMessage("hello to Shaylor " + chat.getParticipant());
                } catch (Exception e) {

                }
                connectHanlder.sendEmptyMessage(0);
            }
        };

        connectedThread = new Thread(rconnect);
        connectedThread.start();

//        new Thread() {
//            public void run() {
//                try {
//
//                    XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
//                            .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
//                            .setSendPresence(true)
//                            .setUsernameAndPassword("aaron", "aaron")
//                            .setServiceName("ec2-35-162-128-9.us-west-2.compute.amazonaws.com")
//                            .setHost("ec2-35-162-128-9.us-west-2.compute.amazonaws.com") //WIN-H6G4CDQOT7E
//                            .setPort(5222)
//                            .setDebuggerEnabled(true)
//                            .build();
//
//                    //AbstractXMPPConnection conn2 = new XMPPTCPConnection(config);
//                    XMPPTCPConnection conn2 = new XMPPTCPConnection(config);
//
//
//                    conn2.connect();
//                    conn2.login();
//
//                    ChatManager chatManager = ChatManager.getInstanceFor(conn2);
//
//                    Chat chat = chatManager.createChat("shay@win-h6g4cdqot7e/Spark", new ChatMessageListener() {
//                        @Override
//                        public void processMessage(Chat chat, Message message) {
//                            String output = chat.getParticipant() + "Message recieved: " + message.getBody();
//                            Log.d(TAG, "!!!!!!! inside chat" + output);
//                        }
//                    });
//
//
//                    chat.sendMessage("hello to Shaylor " + chat.getParticipant());
//
////                    while(conn2.isConnected()){
////                        for(int i =0;i<=5;i++){
////                            chat.sendMessage( "Hi " + i);
////                            if(i == 5){
////                                conn2.disconnect();
////                            }
////                        }
////                    }
//
//                    try {
//
//                        chat.sendMessage("Howdy!");
//                    } catch (XMPPException e) {
//                        Log.d(TAG, "Message Error Delivering block ***************************************************");
//                    }
//
//                    Log.d(TAG, "Message sent ***************************************************");
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }.start();
//
//
//        TextView textview2 = (TextView) findViewById(R.id.XMPPConnectTestView);
//        textview2.setText("Connected!!");
//        Log.d(TAG, "Connected with server ***************************************************");

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("SendMessage Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}