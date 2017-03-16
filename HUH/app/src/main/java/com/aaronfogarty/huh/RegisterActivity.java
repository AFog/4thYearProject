package com.aaronfogarty.huh;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;

import java.io.IOException;

import static org.jivesoftware.smackx.filetransfer.FileTransfer.Error.connection;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    //private HuhConnection mConnection;
    private XMPPTCPConnection mConnection;
    String mServiceName = "ec2-35-162-128-9.us-west-2.compute.amazonaws.com";



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
            //mConnection.addConnectionListener(this);

            mConnection.connect();

            AccountManager accountManager = AccountManager.getInstance(mConnection);
            accountManager.createAccount("Tom", "Tom");

            //  conn2.login();
        } catch (IOException | XMPPException | SmackException e) {
            e.printStackTrace();
            Log.e("TAG", e.getMessage());

        }

        //if authentication successful authenticated(XMPPConnection connection) called

//        try {
//        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
//                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
//                .setSendPresence(true)
//                .setUsernameAndPassword("admin", "admin" )
//                .setServiceName("ec2-35-162-128-9.us-west-2.compute.amazonaws.com")
//                .setPort(5222)
//                .build();
//
//
//        AbstractXMPPConnection conn2 = new XMPPTCPConnection(config);
//
//            conn2.connect();
//
//            AccountManager accountManager = AccountManager.getInstance(conn2);
//            accountManager.createAccount("Tom", "Tom");
//
//            //  conn2.login();
//        } catch (IOException|XMPPException|SmackException e) {
//            e.printStackTrace();
//            Log.e("TAG", e.getMessage());
//
//        }

//        try {
//            mConnection.connect();
//        } catch (SmackException|XMPPException|IOException e) {
//        }
//


//        //Start the service
//        Log.d(TAG,"StartService called from Register.");
//        //Toast.makeText(getApplicationContext(), TAG + ": StartService called from Login.", Toast.LENGTH_LONG).show();
//        Intent i1 = new Intent(this,HuhConnectionService.class);
//        startService(i1);
    }
}