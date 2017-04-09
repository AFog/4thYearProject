package com.aaronfogarty.huh;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ChatMessageListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.jivesoftware.smack.packet.Presence.Type.unavailable;
import static org.jivesoftware.smackx.filetransfer.FileTransfer.Error.connection;
import static org.jivesoftware.smackx.pubsub.AccessModel.roster;

public class RegisterActivity extends AppCompatActivity {

    private Context mApplicationContext;
    private String mUsername;
    private String mPassword;
    private String mServiceName;
    private XMPPTCPConnection mConnection;
    private static final String TAG = "RegisterActivity";
    private Map<String, String> attributes;
    //String mServiceName = "ec2-35-162-128-9.us-west-2.compute.amazonaws.com";

    public Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        Log.d(TAG, "Connecting to server " + mServiceName);
        //Toast.makeText(mApplicationContext,TAG + "Connecting to server " + mServiceName, Toast.LENGTH_LONG).show();
        try {
            XMPPTCPConnectionConfiguration.XMPPTCPConnectionConfigurationBuilder builder =
                    XMPPTCPConnectionConfiguration.builder();
            builder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
            builder.setUsernameAndPassword("admin", "admin");
            builder.setServiceName(mServiceName);
            builder.setRosterLoadedAtLogin(true);
            builder.setResource("Huh");

            mConnection = new XMPPTCPConnection(builder.build());
           // mConnection.addConnectionListener(this);

            mConnection.connect();

            AccountManager accountManager = AccountManager.getInstance(mConnection);
            accountManager.createAccount("Tom", "Tom");

            //  conn2.login();
        } catch (IOException | XMPPException | SmackException e) {
            e.printStackTrace();
            Log.e("TAG", e.getMessage());

        }
    }
        RegisterActivity(Context context) {
            Log.d(TAG, "RegisterActivity Constructor called.");
            //Toast.makeText(context, TAG+"HuhConnection Constructor called", Toast.LENGTH_LONG).show();
            mApplicationContext = context.getApplicationContext();
            String jid = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
                    .getString("xmpp_jid", null);
            mPassword = "admin";

            if (jid != null) {
                mUsername = "admin";
                mServiceName = "ec2-35-162-128-9.us-west-2.compute.amazonaws.com";
                //mServiceName = jidPhoneNumber.split("@")[1];
            } else {
                mUsername = "";
                mServiceName = "";
            }
        }

    public void connect() throws IOException, XMPPException, SmackException {
        Log.d(TAG, "Connecting to server " + mServiceName);

        //Toast.makeText(mApplicationContext,TAG + "Connecting to server " + mServiceName, Toast.LENGTH_LONG).show();

        XMPPTCPConnectionConfiguration.XMPPTCPConnectionConfigurationBuilder builder =
                XMPPTCPConnectionConfiguration.builder();
        builder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        builder.setServiceName(mServiceName);
        builder.setUsernameAndPassword(mUsername, mPassword);
        builder.setRosterLoadedAtLogin(true);
        builder.setResource("Huh");

        mConnection = new XMPPTCPConnection(builder.build());
        //Listens fo connection events and calls connection listener method based on result
        mConnection.addConnectionListener((ConnectionListener) this);
        mConnection.connect();
        //if authentication successful authenticated(XMPPConnection connection) method called
        mConnection.login();
        mConnection.getRoster();

      //  mConnection.getRoster();
        Roster roster =  mConnection.getRoster();
        //roster.setSubscriptionMode(r.SubscriptionMode);
        roster.reload();
//        try {
//            roster.wait();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        Collection <RosterEntry> entries = roster.getEntries();
        for (RosterEntry entry : entries) {
            Log.d(TAG, "Here: " + entry);
        }

        Presence presence = new Presence(unavailable);
        try {
            mConnection.sendPacket(presence);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "CREATE ACCOUNT: "  );
        attributes = new HashMap<String, String>();

        attributes.put("Name","Tom");
        attributes.put("Email","Tom");
        attributes.put("Name","Tom");
        AccountManager accountManager = AccountManager.getInstance(mConnection);

        try {
            accountManager.createAccount("123", "tom");
        } catch (SmackException.NoResponseException e) {
            Log.d(TAG, "CREATE ACCOUNT: NoResponseException "+ e.getMessage() );
            e.printStackTrace();
            e.getMessage();
        } catch (XMPPException.XMPPErrorException e) {
            Log.d(TAG, "CREATE ACCOUNT: XMPPErrorException " + e.getMessage() );
            e.printStackTrace();
            e.getMessage();
        } catch (SmackException.NotConnectedException e) {
            Log.d(TAG, "CREATE ACCOUNT: NotConnectedException "+ e.getMessage()  );
            e.printStackTrace();
            e.getMessage();
        }

//        ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(mConnection);
//        reconnectionManager.setEnabledPerDefault(true);
//        reconnectionManager.enableAutomaticReconnection();

    }

    public void registerUser(XMPPTCPConnection mConnection, String number, String name, String password){


        attributes = new HashMap<String, String>();

        attributes.put("Name",name);
        AccountManager accountManager = AccountManager.getInstance(mConnection);
        try {
            accountManager.createAccount(number, password, attributes);
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }

    }

}