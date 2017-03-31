/**
 * Created by aaronfogartyfogarty on 16/02/2017.
 */
package com.aaronfogarty.huh;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.preference.PreferenceManager;
import android.util.Log;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ChatMessageListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.jivesoftware.smack.packet.Presence.Type.unavailable;


public class HuhConnection implements ConnectionListener {

    private static final String TAG = "HuhConnection";

    private final Context mApplicationContext;
    private final String mUsername;
    private final String mPassword;
    private final String mServiceName;
    private XMPPTCPConnection mConnection;
    private BroadcastReceiver uiThreadMessageReceiver;//Receives messages from the ui thread.
    private BroadcastReceiver uiAvailabilityReciever;
    private ChatMessageListener messageListener;
    boolean isFlexRetrievalSuppoart;
    private boolean isAvailable;
    private boolean hasJustLoggedIn;
    //List<Message> unavailableMessages;
    private static List<String> unavailableMessages;
    private static List<String> offlineMessages;


    public static enum ConnectionState {
        CONNECTED, AUTHENTICATED, CONNECTING, DISCONNECTING, DISCONNECTED;
    }

    public static enum LoggedInState {
        LOGGED_IN, LOGGED_OUT;
    }

    public HuhConnection(Context context) {
        Log.d(TAG, "HuhConnection Constructor called.");
        //Toast.makeText(context, TAG+"HuhConnection Constructor called", Toast.LENGTH_LONG).show();
        mApplicationContext = context.getApplicationContext();
        String jid = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
                .getString("xmpp_jid", null);
        mPassword = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
                .getString("xmpp_password", null);

        if (jid != null) {
            mUsername = jid.split("@")[0];
            mServiceName = "ec2-35-162-128-9.us-west-2.compute.amazonaws.com";
            //mServiceName = jid.split("@")[1];
        } else {
            mUsername = "";
            mServiceName = "";
        }
    }

    public void connect() throws IOException, XMPPException, SmackException {
        Log.d(TAG, "Connecting to server " + mServiceName);
        SmackConfiguration.DEBUG_ENABLED = true;

        //Toast.makeText(mApplicationContext,TAG + "Connecting to server " + mServiceName, Toast.LENGTH_LONG).show();

        XMPPTCPConnectionConfiguration.XMPPTCPConnectionConfigurationBuilder builder =
                XMPPTCPConnectionConfiguration.builder();
        builder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        builder.setServiceName(mServiceName);
        builder.setUsernameAndPassword(mUsername, mPassword);
        builder.setRosterLoadedAtLogin(true);
        builder.setResource("Huh");



        //unavailableMessages = new ArrayList<String>();
        offlineMessages = new ArrayList<String>();

        //Set up the ui thread broadcast message receiver.
        broadCastMessageReceiver();
        broadCastAvailabilityReceiver();

        mConnection = new XMPPTCPConnection(builder.build());
        //Listens fo connection events and calls connevction listener method based on result
        mConnection.addConnectionListener(this);
        mConnection.connect();
        //if authentication successful authenticated(XMPPConnection connection) method called
        mConnection.login();
        hasJustLoggedIn = true;
      //  Log.d(TAG, "**************** hasJustLoggedIn @login: " + hasJustLoggedIn);

        Presence presence = new Presence(unavailable);
        try {
            mConnection.sendPacket(presence);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "%%%%%%%%%%%%%%Initial Login Presence set to Unavailable. User is available: "  + isAvailable);

//        AccountManager accountManager = AccountManager.getInstance(mConnection);
//        accountManager.createAccount("Tom", "Tom");


        messageListener = new ChatMessageListener() {
            @Override
            public void processMessage(Chat chat, Message message) {


                Log.d(TAG, "message.getBody() :" + message.getBody());
                Log.d(TAG, "message.getFrom() :" + message.getFrom());
                //Log.d(TAG, "message.getExtension(\"x\",\"jabber:x:delay\") :" + message.getExtension("x", "jabber:x:delay"));

                Log.d(TAG, "**************** hasJustLoggedIn before if: " + hasJustLoggedIn);
                if(hasJustLoggedIn) {
                //Handles offline messages when reconnected
                    offlineMessages(message);
                }
                Log.d(TAG, "**************** hasJustLoggedIn after if: " + hasJustLoggedIn);

                //handles messages whn user is unavailable
                unavailableMessages(message);


                String from = message.getFrom();
                    String contactJid = "";
                    if (from.contains("/")) {
                        contactJid = from.split("/")[0];
                        Log.d(TAG, "The real jid is :" + contactJid);
                    } else {
                        contactJid = from;
                    }

                    mConnection.setPacketReplyTimeout(0);
                     Log.d(TAG, "^^^^^^^^^^^^^**************** iaAvailable before broadcast: " + isAvailable);



                if(isAvailable) {

                    broadcastunAvailableArray();

                }
                    //BOROADCAST
                    //Data within intent to send in a broadcast.
                    //  Intent intent = new Intent(HuhConnectionService.NEW_MESSAGE);
                    Intent intent = new Intent();
                    // sets keyword to listen out for for this broadcast
                    intent.setAction(HuhConnectionService.NEW_MESSAGE);
                    intent.setPackage(mApplicationContext.getPackageName());
                    intent.putExtra(HuhConnectionService.BUNDLE_FROM_JID, contactJid);
                    intent.putExtra(HuhConnectionService.BUNDLE_MESSAGE_BODY, message.getBody());

                    //Sends out broadcast
                    mApplicationContext.sendBroadcast(intent);
                    Log.d(TAG, "BROADCAST:(connect()) message from :" + contactJid + " broadcast sent to ChatActivity onResume()");

                }
        };
        //This allows for the message listener to be attached to the connection.
        ChatManager.getInstanceFor(mConnection).addChatListener(new ChatManagerListener() {
            @Override
            public void chatCreated(Chat chat, boolean createdLocally) {

                //If missing ,processMessage won't be triggered and messages are not received.
                chat.addMessageListener(messageListener);

            }
        });




    ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(mConnection);
        reconnectionManager.setEnabledPerDefault(true);
        reconnectionManager.enableAutomaticReconnection();

    }

    private void broadCastMessageReceiver() {
        Log.d(TAG, "line 107 & 228 setupUiThreadBroadCastMessageReceiver() ");

        // inialise BroadcastReceiver to receive messages from the ui thread.
        uiThreadMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "setupUiThreadBroadCastMessageReceiver() MESSAGE RECIEVED");
                //Check if the Intents purpose is to send the message.
                String action = intent.getAction();
                if (action.equals(HuhConnectionService.SEND_MESSAGE)) {
                    //Send the message.
                    //retrieves the message string from the broadcast sent by ChatActivity
                    sendMessage(intent.getStringExtra(HuhConnectionService.BUNDLE_MESSAGE_BODY),
                            intent.getStringExtra(HuhConnectionService.BUNDLE_TO));
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(HuhConnectionService.SEND_MESSAGE);
        mApplicationContext.registerReceiver(uiThreadMessageReceiver, filter);

    }

    private void broadCastAvailabilityReceiver(){

        Log.d(TAG, "setupUiThreadtAvailabilityReceiver() ");
        // inialise BroadcastReceiver to receive messages from the ui thread ChatActivity to check availability.
         uiAvailabilityReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Check if the Intents purpose is to send the message.
                String action = intent.getAction();
                if (action.equals(HuhConnectionService.UI_AVAILABLE)) {
                    isAvailable = true;
                    Log.d(TAG, "Presence set to Available. User is available: "  + isAvailable);
                    Presence presence = new Presence(Presence.Type.available);
                    try {
                        mConnection.sendPacket(presence);
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }

                    if(isAvailable && hasJustLoggedIn){
                        broadcastOfflineArray();
                        offlineMessages.clear();
                        hasJustLoggedIn = false;
                        Log.d(TAG, "^^^^^^^^^^^^^ broadcast: " + isAvailable  + " hasJustLoggedIn: " + hasJustLoggedIn);

                    }
                }
                if(action.equals(HuhConnectionService.UI_UNAVAILABLE)){
                    isAvailable = false;
                    Log.d(TAG, "Presence set to Unavailable. User is available: "  + isAvailable);
                    Presence presence = new Presence(unavailable);
                    try {
                        mConnection.sendPacket(presence);
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }

                }
            }
        };

        IntentFilter filter = new IntentFilter(HuhConnectionService.UI_AVAILABLE);
        filter.addAction(HuhConnectionService.UI_UNAVAILABLE);
        mApplicationContext.registerReceiver(uiAvailabilityReciever, filter);
    }

    private void sendMessage(String body, String toJid) {
        Log.d(TAG, "Sending message to :" + toJid);
        Chat chat = ChatManager.getInstanceFor(mConnection)
                .createChat(toJid, messageListener);
        try {
            chat.sendMessage(body);
        } catch (SmackException.NotConnectedException | XMPPException e) {
            e.printStackTrace();
        }


    }

//Not in Use, Old offline messages method
//    public void offlineMessages(Message message){
//        Log.d(TAG, "*******************offLineMessages()");
//
//        try {
//            OfflineMessageManager offlineManager = new OfflineMessageManager(mConnection);
//            if (0 == offlineManager.getMessageCount()) {
//                Log.d(TAG, "No offline messages found on server");
//            }
//            else {
//
//            Iterator<Message> it = (Iterator<Message>) offlineManager.getMessages();
//            Log.d(TAG, String.valueOf(offlineManager.supportsFlexibleRetrieval()));
//            Log.d(TAG, "Number of offline messages:: " + offlineManager.getMessageCount());
//            Map<String, ArrayList<Message>> offlineMsgs = new HashMap<String, ArrayList<Message>>();
//            while (it.hasNext()) {
//                org.jivesoftware.smack.packet.Message message1 = it.next();
//                Log.d(TAG, "receive offline messages, the Received from [" + message1.getFrom() + "] the message:" + message1.getBody());
//                String fromUser = message1.getFrom().split("/")[0];
//
//                if (offlineMsgs.containsKey(fromUser)) {
//                    offlineMsgs.get(fromUser).add(message);
//                } else {
//                    ArrayList<Message> temp = new ArrayList<Message>();
//                    temp.add(message);
//                    offlineMsgs.put(fromUser, temp);
//                }
//            }
//            // Deal with a collection of offline messages ...
//            offlineManager.deleteMessages();
////            Presence presence = new Presence(Presence.Type.available);
////            mConnection.sendPacket(presence);
//        }
//        } catch (Exception e) {
//            Log.e("CATCH", "OFFLINE");
//            e.printStackTrace();
//        }
////        try {
////            Presence presence = new Presence(Presence.Type.available);
////
////            mConnection.sendPacket(presence);
////
////            //setConnection(mConnection);//Packet Listener
////            // Set the status to available
//////            } catch (XMPPException ex) {
//////
//////                //setConnection(null);
////        } catch (SmackException.NotConnectedException e) {
////            e.printStackTrace();
////        }
//    }

    //handles messages when user unavailable
    public void unavailableMessages(Message message){
        Log.d(TAG, "unavailableMessages()");
        unavailableMessages = new ArrayList<String>();
        unavailableMessages.add(message.getBody());
        for (String s:unavailableMessages) {
            Log.d(TAG, "unavailableMessages ARRAYLIST: " + s);
        }
    }

    //handles messages when user unavailable
    public void offlineMessages(Message message){
        Log.d(TAG, "offlineMessages()");
        offlineMessages.add(message.getBody());
        for (String s:offlineMessages) {
            Log.d(TAG, "offlineMessages ARRAYLIST: " + s);
        }
    }

    public void broadcastunAvailableArray(){

        ///ADDED
        //BOROADCAST
        //Data within intent to send in a broadcast.
        //  Intent intent = new Intent(HuhConnectionService.NEW_MESSAGE);
        Intent ArraySendintent = new Intent();
        // sets keyword to listen out for for this broadcast
        ArraySendintent.setAction(HuhConnectionService.UNAVAILABLE_MESSAGE);
        ArraySendintent.setPackage(mApplicationContext.getPackageName());
        ArraySendintent.putStringArrayListExtra(HuhConnectionService.UNAVAILABLE_MESSAGE_ARRAYLIST, (ArrayList<String>) unavailableMessages);

        //Sends out broadcast
        mApplicationContext.sendBroadcast(ArraySendintent);
        Log.d(TAG, "BROADCAST: Unavailable Message ArrayList broadcast sent.");
        /////
    }

    public void broadcastOfflineArray(){

        ///ADDED
        //BOROADCAST
        //Data within intent to send in a broadcast.
        //  Intent intent = new Intent(HuhConnectionService.NEW_MESSAGE);
        Intent ArraySendintent = new Intent();
        // sets keyword to listen out for for this broadcast
        ArraySendintent.setAction(HuhConnectionService.OFFLINE_MESSAGE);
        ArraySendintent.setPackage(mApplicationContext.getPackageName());
        ArraySendintent.putStringArrayListExtra(HuhConnectionService.OFFLINE_MESSAGE_ARRAYLIST, (ArrayList<String>) offlineMessages);

        //Sends out broadcast
        mApplicationContext.sendBroadcast(ArraySendintent);
        Log.d(TAG, "BROADCAST: Offline Message ArrayList broadcast sent.");
        /////
    }

    public void disconnect() {
        Log.d(TAG, "Disconnecting from serser " + mServiceName);
        //Toast.makeText(mApplicationContext, TAG +"Disconnecting from serser "+ mServiceName, Toast.LENGTH_LONG).show();

        try {
            if (mConnection != null) {
                mConnection.disconnect();
            }

        } catch (SmackException.NotConnectedException e) {
            HuhConnectionService.sConnectionState = ConnectionState.DISCONNECTED;
            e.printStackTrace();

        }
        mConnection = null;
        // Unregister the message broadcast receiver.
        if (uiThreadMessageReceiver != null) {
            mApplicationContext.unregisterReceiver(uiThreadMessageReceiver);
            uiThreadMessageReceiver = null;
        }

    }

    private void showContactListActivityWhenAuthenticated() {
        Intent i = new Intent(HuhConnectionService.UI_AUTHENTICATED);
        i.setPackage(mApplicationContext.getPackageName());
        mApplicationContext.sendBroadcast(i);
        Log.d(TAG, "BROADCAST:(showContactListActivityWhenAuthenticated())Sent the broadcast that we are authenticated to ");
        //Toast.makeText(mApplicationContext,TAG + ": Sent the broadcast that we are authenticated ", Toast.LENGTH_LONG).show();

    }

    @Override
    public void connected(XMPPConnection connection) {
        //setsm
        HuhConnectionService.sConnectionState = ConnectionState.CONNECTED;
        Log.d(TAG, "Connected Successfully Aaron");
        //Toast.makeText(mApplicationContext,TAG + ": Connected Successfully ", Toast.LENGTH_LONG).show();

    }

    @Override
    public void authenticated(XMPPConnection connection) {
        HuhConnectionService.sConnectionState = ConnectionState.CONNECTED;
        Log.d(TAG, "Authenticated Successfully");
        //Toast.makeText(mApplicationContext,TAG + ": Authenticated Successfully ", Toast.LENGTH_LONG).show();
        //showContactListActivityWhenAuthenticated();

    }

    @Override
    public void connectionClosed() {
        HuhConnectionService.sConnectionState = ConnectionState.DISCONNECTED;
        Log.d(TAG, "Connectionclosed()");
        //Toast.makeText(mApplicationContext,TAG + ": Connectionclosed ", Toast.LENGTH_LONG).show();


    }

    @Override
    public void connectionClosedOnError(Exception e) {
        HuhConnectionService.sConnectionState = ConnectionState.DISCONNECTED;
        Log.d(TAG, "ConnectionClosedOnError, error " + e.toString());


    }

    @Override
    public void reconnectingIn(int seconds) {
        HuhConnectionService.sConnectionState = ConnectionState.CONNECTING;
        Log.d(TAG, "ReconnectingIn()");

    }

    @Override
    public void reconnectionSuccessful() {
        HuhConnectionService.sConnectionState = ConnectionState.CONNECTED;
        Log.d(TAG, "ReconnectionSuccessful()");


    }

    @Override
    public void reconnectionFailed(Exception e) {
        HuhConnectionService.sConnectionState = ConnectionState.DISCONNECTED;
        Log.d(TAG, "ReconnectionFailed()");


    }

}