package com.aaronfogarty.huh;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;

public class MainActivity extends AppCompatActivity {

    private EditText translateEditText;
    private TextView textview;
    private RequestQueue requestQueue;
    private Button registrationbutton;

    private static final String TAG = "MainActivity";

//    public void onWindowFocusChanged (boolean hasFocus) {
//        Log.d(TAG, "onWindowFocusChanged()");
//        super.onWindowFocusChanged(hasFocus);
//        setContentView(R.layout.activity_main);
//
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        registrationbutton = (Button) findViewById(R.id.main_register_in_button);
//        setSupportActionBar(toolbar);
//        Log.d(TAG, "onCreate()");
//        Log.d(TAG, "Is the huh service running? " + isMyServiceRunning(HuhConnectionService.class));
//
//        Boolean isRegistered = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("xmpp_user_registered", false);
//
//        if(isRegistered) {
//            registrationbutton.setVisibility(View.INVISIBLE);
//            Log.d(TAG, "user is registered ");
//            if (!isMyServiceRunning(HuhConnectionService.class)) {
//                Log.d(TAG, " Service not running");
//                //this.stopService(new Intent(this, HuhConnectionService.class));
//                Log.d(TAG, "StartService called from Main.");
//                //Toast.makeText(getApplicationContext(), TAG + ": StartService called from Login.", Toast.LENGTH_LONG).show();
//                Intent i1 = new Intent(this, HuhConnectionService.class);
//                startService(i1);
//            }
//            Log.d(TAG, " Service is running");
//            Log.d(TAG, "HuConnectionService state:" + HuhConnectionService.getState());
//        }
//////        else{
//////            Log.d(TAG," Service is running");
//////            Log.d(TAG, "HuConnectionService state:" + HuhConnectionService.getState());
//////
//////            if (HuhConnectionService.getState().equals(HuhConnection.ConnectionState.DISCONNECTED)) {
//////                Log.d(TAG,"Server Disconnected");
//////                this.stopService(new Intent(this, HuhConnectionService.class));
//////                Log.d(TAG,"StartService called from Main.");
//////                //Toast.makeText(getApplicationContext(), TAG + ": StartService called from Login.", Toast.LENGTH_LONG).show();
//////                Intent i1 = new Intent(this,HuhConnectionService.class);
//////                startService(i1);
////////            finish();
//////            }
//////        }
////        }
////        else{
////            Log.d(TAG, "user is not registered ");
////
////        }
//       /* FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });*/
//
//        //Your code to be executed here
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        registrationbutton = (Button) findViewById(R.id.main_register_in_button);
        setSupportActionBar(toolbar);
        Log.d(TAG, "onCreate()");
        Log.d(TAG, "Is the huh service running? " + isMyServiceRunning(HuhConnectionService.class));

        Boolean isRegistered = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("xmpp_user_registered", false);

        if (isRegistered) {
            registrationbutton.setVisibility(View.INVISIBLE);
            Log.d(TAG, "user is registered ");
            if (!isMyServiceRunning(HuhConnectionService.class)) {
                Log.d(TAG, " Service not running");
                //this.stopService(new Intent(this, HuhConnectionService.class));
                Log.d(TAG, "StartService called from Main.");
                //Toast.makeText(getApplicationContext(), TAG + ": StartService called from Login.", Toast.LENGTH_LONG).show();
                Intent i1 = new Intent(this, HuhConnectionService.class);
                startService(i1);
                //finish();
            }
//        Log.d(TAG," Service is running");
//        Log.d(TAG, "HuConnectionService state:" + HuhConnectionService.getState());
//
////        else{
////            Log.d(TAG," Service is running");
////            Log.d(TAG, "HuConnectionService state:" + HuhConnectionService.getState());
////
////            if (HuhConnectionService.getState().equals(HuhConnection.ConnectionState.DISCONNECTED)) {
////                Log.d(TAG,"Server Disconnected");
////                this.stopService(new Intent(this, HuhConnectionService.class));
////                Log.d(TAG,"StartService called from Main.");
////                //Toast.makeText(getApplicationContext(), TAG + ": StartService called from Login.", Toast.LENGTH_LONG).show();
////                Intent i1 = new Intent(this,HuhConnectionService.class);
////                startService(i1);
//////            finish();
////            }
////        }
//    }
//    else{
//        Log.d(TAG, "user is not registered ");
//
//    }
//       /* FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });*/
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume(");
        Log.d(TAG, "user is registered ");
        Boolean isRegistered = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("xmpp_user_registered", false);
        if(isRegistered){
            registrationbutton.setVisibility(View.INVISIBLE);
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void goToTranslate(View view){

        Intent intent = new Intent(this, TranslationSpinner.class);
        startActivity(intent);

    }

    public void goToRegistration(View view){

        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
    }

    public void goToSendMessage(View view){

        Intent intent = new Intent(this, SendMessageActivity.class);
        startActivity(intent);
    }

    public void goToLogin(View view){

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        //finish();
    }

    public void goToContacts(View view){

        Intent intent = new Intent(this, ContactListActivity.class);
        startActivity(intent);

    }

    public void goToContactsListView(View view){

        Intent intent = new Intent(this, ContactsListActivity.class);
        startActivity(intent);


    }
    public void goToRegisterView(View view){

        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    public void goToSendReceiveLanguages(View view){

        Intent intent = new Intent(this, SendRecieveLanguageActivity.class);
        startActivity(intent);

    }

//    public void goToSourceLanguages(View view){
//
//        Intent intent = new Intent(this, SourceLanguageSelectActivity.class);
//        startActivity(intent);
//    }
    public void goToPersonView(View view){

        Intent intent = new Intent(this, PersonalityInsightsActivity.class);
        startActivity(intent);

    }

}