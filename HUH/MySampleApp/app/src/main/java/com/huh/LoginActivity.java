package com.huh;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.mysampleapp.R;

import java.io.IOException;
import java.util.ArrayList;

import static android.Manifest.permission.READ_CONTACTS;

//import org.apache.pig.impl.util.ObjectSerializer;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final String TAG = "! LoginActivity";
    private static final int REQUEST_READ_CONTACTS = 0;

    // UI references.
    private AutoCompleteTextView mJidView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private BroadcastReceiver mBroadcastReceiver;
    private Context mContext;
    private ArrayList<RosterContact> phoneNumbers;


    @Override
    protected void onPause() {
        super.onPause();
        //this.unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Log.d(TAG,"1Got a broadcast to show the main app window");
//
//        mBroadcastReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//
//                String action = intent.getAction();
//                switch (action)
//                {
//                    case HuhConnectionService.UI_AUTHENTICATED:
//                        Log.d(TAG,"Got a broadcast to show the main app window");
//                        Toast.makeText(getApplicationContext(), TAG + ": Got a broadcast to show the main app window", Toast.LENGTH_LONG).show();
//                        //Show the main app window
//                        showProgress(false);
//                        Intent i2 = new Intent(mContext,ContactListActivity.class);
//                        startActivity(i2);
//                        break;
//                }
//
//            }
//        };
//        IntentFilter filter = new IntentFilter(HuhConnectionService.UI_AUTHENTICATED);
//        this.registerReceiver(mBroadcastReceiver, filter);
//        Log.d(TAG,"2Got a broadcast to show the main app window");

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mJidView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();


        mPasswordView = (EditText) findViewById(R.id.registerPassword);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mJidSignInButton = (Button) findViewById(R.id.jid1_sign_in_button);
        mJidSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        //getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mJidView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mJidView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String jid = mJidView.getText().toString();
        jid = jid +"@win-h6g4cdqot7e";
        String password = mPasswordView.getText().toString();

        Log.d(TAG,"Logging in with Jid: " + jid);
        //Toast.makeText(getApplicationContext(), TAG + ": Logging in with Jid: " + jidPhoneNumber, Toast.LENGTH_LONG).show();
        Log.d(TAG,"Logging in with Name:" + mJidView.getText() + "Logging in with Password: " + password);

        boolean cancel = false;
        View focusView = null;

//        // Check for a valid password, if the user entered one.
//        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
//            mPasswordView.setError(getString(R.string.error_invalid_password));
//            focusView = mPasswordView;
//            cancel = true;
//        }
//
//        // Check for a valid email address.
//        if (TextUtils.isEmpty(email)) {
//            mJidView.setError(getString(R.string.error_field_required));
//            focusView = mJidView;
//            cancel = true;
//        } else if (!isEmailValid(email)) {
//            mJidView.setError(getString(R.string.error_invalid_jid));
//            focusView = mJidView;
//            cancel = true;
//        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            //showProgress(true);<<---FOR NOW WE DON'T WANT TO SEE THIS PROGRESS THING.
            //This is where the login login is fired up.
            Log.d(TAG,"Jid and password are valid ,proceeding with login.");
            //Toast.makeText(getApplicationContext(), TAG + ": Jid and password are valid ,proceeding with login. ", Toast.LENGTH_LONG).show();

            Log.d(TAG,"ContactListActivity started");
            //Toast.makeText(getApplicationContext(), TAG + ": ContactListActivity started", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this,ContactListActivity.class));

            //Save the credentials and login
            saveCredentialsAndLogin();

        }
    }

    private void saveCredentialsAndLogin()
    {
        //retrieves contacts from phone and save to shared prefs
        phoneContacts();
        /////
        Log.d(TAG,"saveCredentialsAndLogin() called.");
        //Toast.makeText(getApplicationContext(), TAG + ": saveCredentialsAndLogin() called.", Toast.LENGTH_LONG).show();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit()
                .putString("xmpp_jid", mJidView.getText().toString())
                .putString("xmpp_password", mPasswordView.getText().toString())
                .putBoolean("xmpp_logged_in",true)
                .commit();

        //Start the service
        Log.d(TAG,"StartService called from Login.");
        //Toast.makeText(getApplicationContext(), TAG + ": StartService called from Login.", Toast.LENGTH_LONG).show();
        Intent i1 = new Intent(this,HuhConnectionService.class);
        startService(i1);
        finish();

    }

    public void phoneContacts(){
        Log.d(TAG,"Getting phone Contacts" );

        phoneNumbers = new ArrayList<RosterContact>();
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));

                        if (phoneNo.length() > 10) {

                            //Log.d(TAG, "Fixing length Name: " + name + ", Phone No: " + phoneNo);
                            phoneNo = phoneNo.replace("+353", "0");
                            phoneNo = phoneNo.replace(" ", "");
                            phoneNo = phoneNo.replaceAll("\\s+", "");
                           // Log.d(TAG, "Fixed length Name: " + name + ", Phone No: " + phoneNo);

                        }
                        RosterContact tempContact = new RosterContact(name, phoneNo);
                        if (!phoneNumbers.contains(tempContact)) {
                            phoneNumbers.add(tempContact);
                            Log.d(TAG, "Name: " + name + ", Phone No: " + phoneNo);
                        }
                    }
                    pCur.close();
                }
            }
        }
        savePhnoneNumbersList(phoneNumbers);
    }

    public void savePhnoneNumbersList(ArrayList<RosterContact> t) {
        Log.d(TAG,"Saving phonenumbers to arraylist to preference" );

        // saves phonenumbers arraylist to preference
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            prefs.edit()
            .putString("PhoneContacts", ObjectSerializer.serialize(phoneNumbers))
            .commit();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}