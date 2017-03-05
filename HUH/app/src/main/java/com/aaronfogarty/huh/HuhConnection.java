/**
 * Created by aaronfogartyfogarty on 16/02/2017.
 */
package com.aaronfogarty.huh;


        import org.jivesoftware.smack.ChatManagerListener;
        import org.jivesoftware.smack.ConnectionConfiguration;
        import org.jivesoftware.smack.XMPPConnection;
        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;
        import android.content.IntentFilter;
        import android.preference.PreferenceManager;
        import android.telecom.ConnectionService;
        import android.util.Log;

        import org.jivesoftware.smack.Chat;
        import org.jivesoftware.smack.ChatManager;
        import org.jivesoftware.smack.ChatMessageListener;
        import org.jivesoftware.smack.ConnectionListener;
        import org.jivesoftware.smack.ReconnectionManager;
        import org.jivesoftware.smack.SmackException;
        import org.jivesoftware.smack.XMPPException;
        import org.jivesoftware.smack.packet.Message;
        import org.jivesoftware.smack.tcp.XMPPTCPConnection;
        import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
        import android.widget.Toast;

        import java.io.IOException;

        import static org.jivesoftware.smackx.privacy.packet.PrivacyItem.Type.jid;


public class HuhConnection implements ConnectionListener {

    private static final String TAG = "HuhConnection";

    private  final Context mApplicationContext;
    private  final String mUsername;
    private  final String mPassword;
    private  final String mServiceName;
    private XMPPTCPConnection mConnection;
    private BroadcastReceiver uiThreadMessageReceiver;//Receives messages from the ui thread.
    private  ChatMessageListener messageListener;

    public static enum ConnectionState
    {
        CONNECTED ,AUTHENTICATED, CONNECTING ,DISCONNECTING ,DISCONNECTED;
    }

    public static enum LoggedInState
    {
        LOGGED_IN , LOGGED_OUT;
    }


    public HuhConnection( Context context)
    {
        Log.d(TAG,"HuhConnection Constructor called.");
        //Toast.makeText(context, TAG+"HuhConnection Constructor called", Toast.LENGTH_LONG).show();
        mApplicationContext = context.getApplicationContext();
        String jid = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
                .getString("xmpp_jid",null);
        mPassword = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
                .getString("xmpp_password",null);

        if( jid != null)
        {
            mUsername = jid.split("@")[0];
            mServiceName = "ec2-35-162-128-9.us-west-2.compute.amazonaws.com";
            //mServiceName = jid.split("@")[1];
        }else
        {
            mUsername ="";
            mServiceName="";
        }
    }


    public void connect() throws IOException,XMPPException,SmackException
    {
        Log.d(TAG, "Connecting to server " + mServiceName);
        //Toast.makeText(mApplicationContext,TAG + "Connecting to server " + mServiceName, Toast.LENGTH_LONG).show();

        XMPPTCPConnectionConfiguration.XMPPTCPConnectionConfigurationBuilder builder=
                XMPPTCPConnectionConfiguration.builder();
        builder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        builder.setServiceName(mServiceName);
        builder.setUsernameAndPassword(mUsername, mPassword);
        builder.setRosterLoadedAtLogin(true);
        builder.setResource("Huh");

        //Set up the ui thread broadcast message receiver.
        setupUiThreadBroadCastMessageReceiver();

        mConnection = new XMPPTCPConnection(builder.build());
        mConnection.addConnectionListener(this);
        mConnection.connect();
        //if authentication successful authenticated(XMPPConnection connection) called
        mConnection.login();

        messageListener = new ChatMessageListener() {
            @Override
            public void processMessage(Chat chat, Message message) {
                ///ADDED
                Log.d(TAG,"message.getBody() :"+message.getBody());
                Log.d(TAG,"message.getFrom() :"+message.getFrom());

                String from = message.getFrom();
                String contactJid="";
                if ( from.contains("/"))
                {
                    contactJid = from.split("/")[0];
                    Log.d(TAG,"The real jid is :" +contactJid);
                }else
                {
                    contactJid=from;
                }

                //BOROADCAST
                //Bundle up the intent and send the broadcast.
                //  Intent intent = new Intent(HuhConnectionService.NEW_MESSAGE);
                Intent intent = new Intent();
                // sets keyword to listen out for for this broadcast
                intent.setAction(HuhConnectionService.NEW_MESSAGE);
                intent.setPackage(mApplicationContext.getPackageName());
                intent.putExtra(HuhConnectionService.BUNDLE_FROM_JID,contactJid);
                intent.putExtra(HuhConnectionService.BUNDLE_MESSAGE_BODY,message.getBody());
                //Sends out broadcast
                mApplicationContext.sendBroadcast(intent);
                Log.d(TAG,"Received message from :"+contactJid+" broadcast sent.");

                ///ADDED

            }
        };

        //The snippet below is necessary for the message listener to be attached to our connection.
        ChatManager.getInstanceFor(mConnection).addChatListener(new ChatManagerListener() {
            @Override
            public void chatCreated(Chat chat, boolean createdLocally) {

                //If the line below is missing ,processMessage won't be triggered and you don't receive messages.
                chat.addMessageListener(messageListener);

            }
        });

        ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(mConnection);
        reconnectionManager.setEnabledPerDefault(true);
        reconnectionManager.enableAutomaticReconnection();

    }

    private void setupUiThreadBroadCastMessageReceiver()
    {
        // inialise BroadcastReceiver to receive messages from the ui thread.
        uiThreadMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Check if the Intents purpose is to send the message.
                String action = intent.getAction();
                if( action.equals(HuhConnectionService.SEND_MESSAGE))
                {
                    //Send the message.
                    //retrieves the message string from the broadcast sent by ChatActivity
                    sendMessage(intent.getStringExtra(HuhConnectionService.BUNDLE_MESSAGE_BODY),
                            intent.getStringExtra(HuhConnectionService.BUNDLE_TO));
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(HuhConnectionService.SEND_MESSAGE);
        mApplicationContext.registerReceiver(uiThreadMessageReceiver,filter);

    }

    private void sendMessage ( String body ,String toJid)
    {
        Log.d(TAG,"Sending message to :"+ toJid);
        Chat chat = ChatManager.getInstanceFor(mConnection)
                .createChat(toJid,messageListener);
        try
        {
            chat.sendMessage(body);
        }catch (SmackException.NotConnectedException | XMPPException e)
        {
            e.printStackTrace();
        }


    }



    public void disconnect()
    {
        Log.d(TAG,"Disconnecting from serser "+ mServiceName);
        //Toast.makeText(mApplicationContext, TAG +"Disconnecting from serser "+ mServiceName, Toast.LENGTH_LONG).show();

        try
        {
            if (mConnection != null)
            {
                mConnection.disconnect();
            }

        }catch (SmackException.NotConnectedException e)
        {
            HuhConnectionService.sConnectionState=ConnectionState.DISCONNECTED;
            e.printStackTrace();

        }
        mConnection = null;
        // Unregister the message broadcast receiver.
        if( uiThreadMessageReceiver != null)
        {
            mApplicationContext.unregisterReceiver(uiThreadMessageReceiver);
            uiThreadMessageReceiver = null;
        }

    }

    private void showContactListActivityWhenAuthenticated()
    {
        Intent i = new Intent(HuhConnectionService.UI_AUTHENTICATED);
        i.setPackage(mApplicationContext.getPackageName());
        mApplicationContext.sendBroadcast(i);
        Log.d(TAG,"Sent the broadcast that we are authenticated");
        //Toast.makeText(mApplicationContext,TAG + ": Sent the broadcast that we are authenticated ", Toast.LENGTH_LONG).show();

    }

    @Override
    public void connected(XMPPConnection connection) {
        //setsm
        HuhConnectionService.sConnectionState=ConnectionState.CONNECTED;
        Log.d(TAG,"Connected Successfully Aaron");
        //Toast.makeText(mApplicationContext,TAG + ": Connected Successfully ", Toast.LENGTH_LONG).show();

    }

    @Override
    public void authenticated(XMPPConnection connection) {
        HuhConnectionService.sConnectionState=ConnectionState.CONNECTED;
        Log.d(TAG,"Authenticated Successfully");
        //Toast.makeText(mApplicationContext,TAG + ": Authenticated Successfully ", Toast.LENGTH_LONG).show();
        showContactListActivityWhenAuthenticated();

    }

    @Override
    public void connectionClosed() {
        HuhConnectionService.sConnectionState=ConnectionState.DISCONNECTED;
        Log.d(TAG,"Connectionclosed()");
        //Toast.makeText(mApplicationContext,TAG + ": Connectionclosed ", Toast.LENGTH_LONG).show();


    }

    @Override
    public void connectionClosedOnError(Exception e) {
        HuhConnectionService.sConnectionState=ConnectionState.DISCONNECTED;
        Log.d(TAG,"ConnectionClosedOnError, error "+ e.toString());


    }

    @Override
    public void reconnectingIn(int seconds) {
        HuhConnectionService.sConnectionState = ConnectionState.CONNECTING;
        Log.d(TAG,"ReconnectingIn()");

    }

    @Override
    public void reconnectionSuccessful() {
        HuhConnectionService.sConnectionState = ConnectionState.CONNECTED;
        Log.d(TAG,"ReconnectionSuccessful()");


    }

    @Override
    public void reconnectionFailed(Exception e) {
        HuhConnectionService.sConnectionState = ConnectionState.DISCONNECTED;
        Log.d(TAG,"ReconnectionFailed()");


    }


}
