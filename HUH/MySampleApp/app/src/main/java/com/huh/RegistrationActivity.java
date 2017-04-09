package com.huh;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mysampleapp.R;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.xdata.Form;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class RegistrationActivity extends AppCompatActivity implements ConnectionListener {
    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;
    private static final String TAG = "RegistrationActivity";
    String jidPhoneNumber;
    String password;
    boolean isHuhUser;

    // UI references.
    private AutoCompleteTextView mJidView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private Context mApplicationContext;
    private String mUsername = "admin";
    private String mPassword = "admin";
    private String mServiceName = "ec2-35-162-128-9.us-west-2.compute.amazonaws.com";
    Roster roster;
    private XMPPTCPConnection mConnection;
    private Map<String, String> attributes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jidlogin);
        // Set up the login form.
        mJidView = (AutoCompleteTextView) findViewById(R.id.registerJid);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.registerPassword);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attempRegister();
                    return true;
                }
                return false;
            }
        });

        Button mJidSignInButton = (Button) findViewById(R.id.jid_register_in_button);
        mJidSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Button attempt register ");

                attempRegister();
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
    private void attempRegister() {
        // Reset errors.
        mJidView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        jidPhoneNumber = mJidView.getText().toString();
        password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        //Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid jidPhoneNumber.
        if (TextUtils.isEmpty(jidPhoneNumber)) {
            mJidView.setError(getString(R.string.error_field_required));
            focusView = mJidView;
            cancel = true;
        } else if (!isEmailValid(jidPhoneNumber)) {
            mJidView.setError(getString(R.string.error_invalid_jid));
            focusView = mJidView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            //This is where the login login is fired up.
            try {
                registerNewUser();
                Log.d(TAG, "user was created " + cancel);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (XMPPException e) {
                e.printStackTrace();
            } catch (SmackException e) {
                e.printStackTrace();
            }
          //  connect();

            Log.d(TAG, "Jid and password are valid, proceed with login");
            //Toast.makeText(getApplicationContext(), TAG + ": Jid and password are valid, proceed with login", Toast.LENGTH_LONG).show();

        }

    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        // return email.contains("@");
        return jidPhoneNumber.length() == 10;

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

    public void goToXMPPConnect(View view) {

        Intent intent = new Intent(this, SendMessageActivity.class);
        startActivity(intent);
    }

    public boolean registerNewUser() throws IOException, XMPPException, SmackException {

        new Thread() {
            public void run() {
                try {

                    Log.d(TAG, "Connecting to server " + mServiceName);
                    XMPPTCPConnectionConfiguration.XMPPTCPConnectionConfigurationBuilder builder =
                            XMPPTCPConnectionConfiguration.builder();
                    builder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
                    builder.setServiceName(mServiceName);
                    builder.setUsernameAndPassword(mUsername, mPassword);
                    builder.setRosterLoadedAtLogin(true);
                    builder.setResource("Huh");

                    mConnection = new XMPPTCPConnection(builder.build());
                    //Listens fo connection events and calls connevction listener method based on result
                    mConnection.addConnectionListener((ConnectionListener) mApplicationContext);
                    mConnection.connect();
                    //if authentication successful authenticated(XMPPConnection connection) method called

                    mConnection.login();

                    AccountManager accountManager = AccountManager.getInstance(mConnection);
                    Log.d(TAG, "CREATE ACCOUNT: supports creation : - " + accountManager.supportsAccountCreation());

                    try {
                        accountManager.createAccount(jidPhoneNumber, password);
                    } catch (SmackException.NoResponseException e) {
                        Log.d(TAG, "CREATE ACCOUNT: NoResponseException " + e.getMessage());
                        e.printStackTrace();
                        e.getMessage();
                    } catch (XMPPException.XMPPErrorException e) {
                        Log.d(TAG, "CREATE ACCOUNT: XMPPErrorException " + e.getMessage());
                        e.printStackTrace();
                        e.getMessage();
                    } catch (SmackException.NotConnectedException e) {
                        Log.d(TAG, "CREATE ACCOUNT: NotConnectedException " + e.getMessage());
                        e.printStackTrace();
                        e.getMessage();
                    }

//                    Log.d(TAG,"Connected with server ***************************************************");
//                    Intent intent = new Intent(getApplicationContext(), ConnectedActivity.class);
//                    startActivity(intent);
//                    while(!isHuhUser){
//                        Thread.sleep(2000);
//                       isHuhUser = checkIfUserExists(jidPhoneNumber);
//                         Log.d(TAG, "User exists ***************************************************" + isHuhUser);
//
//                     }

                } catch (Exception e) {
                    Log.d(TAG, "Not Connected with server ***************************************************");
                    e.printStackTrace();
                }
            }
        }.start();
        return true;
    }

    public ArrayList<RosterContact> getRosterContacts() {
        Log.d(TAG, "getRosterContacs()");

        mConnection.getRoster();
        Roster roster = mConnection.getRoster();
        Collection<RosterEntry> entries = roster.getEntries();
        ArrayList<RosterContact> rosterEntries = new ArrayList<>();
        for (RosterEntry entry : entries) {
            Log.d(TAG, "Here: " + entry + " \nUser: " + entry.getUser() + "Name" + entry.getName() + "\nStatus " + entry.getStatus() + "\nType " + entry.getType() + "\nGroup " + entry.getGroups());
            RosterContact tempContact = new RosterContact(entry.getName(), entry.getUser());
            rosterEntries.add(tempContact);
        }
        return rosterEntries;
    }

    public void connect() {

        new Thread() {
            public void run() {

                Log.d(TAG, "Connecting to server " + mServiceName);
                XMPPTCPConnectionConfiguration.XMPPTCPConnectionConfigurationBuilder builder =
                        XMPPTCPConnectionConfiguration.builder();
                builder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
                builder.setServiceName(mServiceName);
                builder.setUsernameAndPassword(mUsername, mPassword);
                builder.setRosterLoadedAtLogin(true);
                builder.setResource("Huh");

                mConnection = new XMPPTCPConnection(builder.build());
                //Listens fo connection events and calls connevction listener method based on result
                mConnection.addConnectionListener((ConnectionListener) mApplicationContext);
                try {
                    mConnection.connect();
                } catch (SmackException e1) {
                    e1.printStackTrace();

                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (XMPPException e1) {
                    e1.printStackTrace();
                }
                //if authentication successful authenticated(XMPPConnection connection) method called

//                isHuhUser = checkIfUserExists(jidPhoneNumber);
//
//                if(isHuhUser){
//                    ArrayList<RosterContact> rosterEntries = getRosterContacts();
//                }
            }

        }.start();
    }

    public Boolean checkIfUserExists(String user) {
        Log.d(TAG, "CHECKING IF USER EXISTS: " + user + "@win-h6g4cdqot7e");
        try {

            UserSearchManager search = new UserSearchManager(mConnection);
            Form searchForm = null;
            searchForm = search
                    .getSearchForm("search." + mConnection.getServiceName());
            Form answerForm = searchForm.createAnswerForm();
            answerForm.setAnswer("Username", true);
            answerForm.setAnswer("search", user);
            ReportedData data = search
                    .getSearchResults(answerForm, "search." + mConnection.getServiceName());

            if (data.getRows() != null) {
                for (ReportedData.Row row : data.getRows()) {
                    for (String value : row.getValues("jidPhoneNumber")) {
                        Log.i("USER EXISTS", " " + value);
                        return true;

                    }
                }
                Toast.makeText(mApplicationContext, "Username Exists", Toast.LENGTH_SHORT).show();
            }

        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "USER DOES NOT EXIST");

        return false;

    }


    @Override
    public void connected(XMPPConnection connection) {
        Toast.makeText(mApplicationContext, TAG + "Connecting to server " + mServiceName, Toast.LENGTH_LONG).show();
        Log.d(TAG, "Connecting to server " + mServiceName);
    }

    @Override
    public void authenticated(XMPPConnection connection) {

    }

    @Override
    public void connectionClosed() {

    }

    @Override
    public void connectionClosedOnError(Exception e) {

    }

    @Override
    public void reconnectingIn(int seconds) {

    }

    @Override
    public void reconnectionSuccessful() {

    }

    @Override
    public void reconnectionFailed(Exception e) {

    }
}