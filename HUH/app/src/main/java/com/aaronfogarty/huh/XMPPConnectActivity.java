package com.aaronfogarty.huh;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatMessageListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;

import static com.aaronfogarty.huh.R.id.login;
import static org.jivesoftware.smackx.filetransfer.FileTransfer.Error.connection;

public class XMPPConnectActivity extends AppCompatActivity {

    private static final String TAG = "XMPPConnectActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_xmppconnect);
        TextView textview = (TextView) findViewById(R.id.XMPPConnectTestView);
        textview.setText("Before Connect");
        Log.d(TAG, "Connecting with server *****************************************************");


//        new Thread() {
//            public void run() {
//                try {
//
//// Create a connection to the Openfire server.
//                    AbstractXMPPConnection conn1 = null;
//
//                    conn1 = new XMPPTCPConnection("aaron", "aaron", "ec2-35-162-128-9.us-west-2.compute.amazonaws.com");
//                    conn1.connect();
//
////            textview = (TextView) findViewById(R.id.XMPPConnectTestView);
////            textview.setText("Connected");
////            Log.d(TAG,"Connected with server ***************************************************");
//
//                    conn1.login();
//                   // conn1.disconnect();
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }.start();


        new Thread(){
            public void run(){
                try{

                    XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                            .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                            .setSendPresence(true)
                            .setUsernameAndPassword("aaron", "aaron")
                            .setServiceName("ec2-35-162-128-9.us-west-2.compute.amazonaws.com")
                            .setHost("ec2-35-162-128-9.us-west-2.compute.amazonaws.com") //WIN-H6G4CDQOT7E
                            .setPort(5222)
                            .setDebuggerEnabled(true)
                            .build();

                    AbstractXMPPConnection conn2 = new XMPPTCPConnection(config);

                    conn2.connect();
                    conn2.login();

//                    ChatManager chatManager = ChatManager.getInstanceFor(conn2);
//                    Chat chat = chatManager.createChat("shay@win-h6g4cdgot7e.com/Spark", new ChatMessageListener() {
//                        @Override
//                        public void processMessage(Chat chat, Message message) {
//
//                        }
//                    });
//                        chat.sendMessage("hello!!");

                    ChatManager chatManager = ChatManager.getInstanceFor(conn2);
                    Chat chat = chatManager.createChat("shay", new ChatMessageListener(){

                        public void processMessage(Chat chat, Message msg){
                            String output = chat.getParticipant() + "Message recieved: " + msg.getBody();
                            Log.d(TAG, "inside chat");
                        }
                    });

                        Log.d(TAG,"Message sent ***************************************************");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

        TextView textview2 = (TextView) findViewById(R.id.XMPPConnectTestView);
        textview2.setText("Connected!!");
        Log.d(TAG,"Connected with server ***************************************************");

    }

}
