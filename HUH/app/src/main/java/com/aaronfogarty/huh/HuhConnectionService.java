package com.aaronfogarty.huh;

/**
 * Created by aaronfogartyfogarty on 16/02/2017.
 */

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;


import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.IOException;


public class HuhConnectionService extends Service {


    private static final String TAG = "HuhConnectionService";
    public static final String UI_AUTHENTICATED = "com.aaronfogarty.huh.uiauthenticated";
    public static final String SEND_MESSAGE = "com.aaronfogarty.huh.sendmessage";
    public static final String BUNDLE_MESSAGE_BODY = "b_body";
    public static final String BUNDLE_TO = "b_to";
    public static final String NEW_MESSAGE = "com.aaronfogarty.huh.newmessage";
    public static final String BUNDLE_FROM_JID = "b_from";
    public static final String UI_UNAVAILABLE = "com.aaronfogarty.huh.unavailable";
    public static final String UI_AVAILABLE = "com.aaronfogarty.huh.available";
    public static final String UNAVAILABLE_MESSAGE = "com.aaronfogarty.huh.unavailablemessage";
    public static final String UNAVAILABLE_MESSAGE_ARRAYLIST = "com.aaronfogarty.huh.unavailablemessage";
    public static final String OFFLINE_MESSAGE = "com.aaronfogarty.huh.offlineemessage";
    public static final String OFFLINE_MESSAGE_ARRAYLIST = "com.aaronfogarty.huh.offlineemessage";


    public static HuhConnection.ConnectionState sConnectionState;
    public static HuhConnection.LoggedInState sLoggedInState;
    private boolean mActive;//Stores whether or not the thread is active
    private Thread mThread;
    private Handler mTHandler;//We use this handler to post messages to
    //the background thread.
    private HuhConnection mConnection;

    // must be implemented for service, not used
    public HuhConnectionService() {
    }

    public static HuhConnection.ConnectionState getState() {
        if (sConnectionState == null) {
            return HuhConnection.ConnectionState.DISCONNECTED;
        }
        return sConnectionState;
    }

    public static HuhConnection.LoggedInState getLoggedInState() {
        if (sLoggedInState == null) {
            return HuhConnection.LoggedInState.LOGGED_OUT;
        }
        return sLoggedInState;
    }

    @Nullable
    @Override // method must be implemented by a service (for a bound service), not used
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");

    }

    private void initConnection() {
        Log.d(TAG, "initConnection()");

        if (mConnection == null) {
            mConnection = new HuhConnection(this);
        }
        try {
            mConnection.connect();

        } catch (IOException | SmackException | XMPPException e) {
            Log.d(TAG, "Something went wrong while connecting ,make sure the credentials are right and try again");
            //Toast.makeText(getApplicationContext(), TAG + ": Something went wrong while connecting ,make sure the credentials are right and try again", Toast.LENGTH_LONG).show();

            e.printStackTrace();
            //Stops the service completely
            stopSelf();
        }

    }

    //Starts Service thread
    public void start() {
        Log.d(TAG, " Service Start()");
        //Toast.makeText(getApplicationContext(), TAG + ": Service Start()", Toast.LENGTH_LONG).show();

        if (!mActive) {
            mActive = true;
            if (mThread == null || !mThread.isAlive()) {
                mThread = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Looper.prepare();
                        mTHandler = new Handler();

                        initConnection();
                        //this is the code that runs in background
                        Looper.loop();

                    }
                });
                mThread.start();
            }


        }

    }

    public void stop() {
        Log.d(TAG, "stop()");
        mActive = false;
        mTHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mConnection != null) {
                    mConnection.disconnect();
                }
            }
        });

    }


    @Override//Service Starts here
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()" + mActive);
        //Toast.makeText(getApplicationContext(), TAG + ": onStartCommand()", Toast.LENGTH_LONG).show();

        //starts service thread
        start();
        return Service.START_STICKY;
        //RETURNING START_STICKY CAUSES CODE TO STICK AROUND WHEN THE APP ACTIVITY HAS DIED. Restarts this service if app fails.
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        //Toast.makeText(getApplicationContext(), TAG + ": onDestroy()", Toast.LENGTH_LONG).show();

        super.onDestroy();
        stop();
    }
}