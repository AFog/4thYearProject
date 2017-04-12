package com.aaronfogarty.huh;

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
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.sasl.SASLMechanism;
import org.jivesoftware.smack.sasl.provided.SASLDigestMD5Mechanism;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.xdata.Form;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
    boolean isHuhUser;
    Roster roster;
    UserSearchManager search;
    private String jidPhoneNumber;
    private String password;
    private Thread registerUserTread;
    private Thread createHuhContactsThread;
    private Thread checkUserNameAvailableThread;
    private BroadcastReceiver mBroadcastReceiver;
    private String mUsername = "admin";
    private String mPassword = "admin";
    private String mServiceName = "ec2-35-162-128-9.us-west-2.compute.amazonaws.com";
    private XMPPTCPConnection mConnection;
    private Map<String, String> attributes;
    private ArrayList<RosterContact> phoneNumbers;
    private List<RosterContact> huhContacts;
    private Handler registerUserHandler;
    private Handler createHuhContactshandler;
    private Handler checkUserNameAvailableHandler;
    private Handler checkUserNameAvailableHandler2;
    private SharedPreferences prefs;
    private Context mApplicationContext;
    private Boolean isUserNameAvailable;
    // UI references.
    private TextView reisterTextView;
    private TextView userNameAvailTextView;
    private ProgressBar progressBar;
    private AutoCompleteTextView mJidView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        //mApplicationContext = getApplicationContext();
        //initialise huhContacts
        huhContacts = new ArrayList<>();
        isUserNameAvailable = true;
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // Set up the register form.

        progressBar = (ProgressBar) findViewById(R.id.registration_progress);
        reisterTextView = (TextView) findViewById(R.id.registrationoutput);
        userNameAvailTextView = (TextView) findViewById(R.id.userNotAvailableText);

        mJidView = (AutoCompleteTextView) findViewById(R.id.registerJid);
        //get permisssion to use phone contacts
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

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);


        Button mJidSignInButton = (Button) findViewById(R.id.jid_register_in_button);
        mJidSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(TAG, "Button attempt register ");
//                isUserNameAvailable = true;
//                mLoginFormView.setVisibility(View.GONE);
//                progressBar.setVisibility(View.VISIBLE);
//                reisterTextView.setVisibility(View.VISIBLE);
//                userNameAvailTextView.setVisibility(View.GONE);
//
                checkUserNameAvailableHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        progressBar.setVisibility(View.GONE);
                        reisterTextView.setVisibility(View.GONE);
                        mLoginFormView.setVisibility(View.VISIBLE);
                        userNameAvailTextView.setVisibility(View.VISIBLE);

                        if (isUserNameAvailable) {
                            Log.d(TAG, "RegisterNew User Handler ");
                            attempRegister();
                            mLoginFormView.setVisibility(View.GONE);
                            progressBar.setVisibility(View.VISIBLE);
                            reisterTextView.setVisibility(View.VISIBLE);
                        }
                        else{

                            Toast.makeText(getApplication(), "Number is already Registered", Toast.LENGTH_LONG).show();
                        }
                    }
                };

                attempRegister();

            }
        });


        //when registration completes create contacts
        registerUserHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Log.d(TAG, "RegisterNew User Handler ");
                reisterTextView.setText("Huh Account Created list list built... Builing contact list, please wait");

                createHuhContacts();
            }
        };

        //when create huh contacts completes go to login
        createHuhContactshandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Log.d(TAG, "Create Huh Contacts  Handler Saving huhContsct to shared prefs ");
                reisterTextView.setText("Contact list built... logging in");
                saveCredentialsAndGoToLogin();
            }
        };

    }

    public void goMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void startServiceAndLogin() {
        //Start the service
        Log.d(TAG, "StartService called from Registration.");
        //Toast.makeText(getApplicationContext(), TAG + ": StartService called from Login.", Toast.LENGTH_LONG).show();
        Intent i1 = new Intent(this, HuhConnectionService.class);
        startService(i1);
        finish();

    }

    public void phoneContacts() {
        Log.d(TAG, "Getting phone Contacts");

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
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
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
        Log.d(TAG, "Saveing phonenumbers to arraylist to preference");

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
            Log.d(TAG, "password; " + password);
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
            //showProgress(true);
            //This is where the login login is fired up.

           // checkUserNameAvailable();
            Log.d(TAG, " AFTER CHECK username available " + isUserNameAvailable);

            mLoginFormView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            reisterTextView.setVisibility(View.VISIBLE);

            if(isUserNameAvailable) {
                Log.d(TAG, "HERE WE BEGIN REGISTRATION");

            try {
                registerNewUser();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XMPPException e) {
                e.printStackTrace();
            } catch (SmackException e) {
                e.printStackTrace();
            }
            }
            Log.d(TAG, "Jid and password are valid, proceed with login");
            //Toast.makeText(getApplicationContext(), TAG + ": Jid and password are valid, proceed with login", Toast.LENGTH_LONG).show();

        }

    }

    public Boolean checkUserNameAvailable() {

        Runnable checkUserNameAvailabe = new Runnable() {
            @Override
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

                    SASLMechanism mechanism = new SASLDigestMD5Mechanism();
                    SASLAuthentication.registerSASLMechanism(mechanism);
                    SASLAuthentication.blacklistSASLMechanism("SCRAM-SHA-1");
                    SASLAuthentication.blacklistSASLMechanism("DIGEST-MD5");

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
                        //mConnection.login();
                    } catch (SmackException.NoResponseException e) {
                        Log.d(TAG, "CREATE ACCOUNT: NoResponseException " + e.getMessage());
                        e.printStackTrace();
                        e.getMessage();
                    } catch (XMPPException.XMPPErrorException e) {
                        Log.d(TAG, "CREATE ACCOUNT: XMPPErrorException " + e.getMessage());
                        e.printStackTrace();
                        e.getMessage();
                        isUserNameAvailable = false;
                        Log.d(TAG, "User already exists, Is user name available " + isUserNameAvailable);
                    } catch (SmackException.NotConnectedException e) {
                        Log.d(TAG, "CREATE ACCOUNT: NotConnectedException " + e.getMessage());
                        e.printStackTrace();
                        e.getMessage();
                    }

                    mConnection.disconnect();

                } catch (Exception e) {
                    Log.d(TAG, "Not Connected with server ***************************************************");
                    e.printStackTrace();
                }
                checkUserNameAvailableHandler.sendEmptyMessage(0);
            }
        };

        checkUserNameAvailableThread = new Thread(checkUserNameAvailabe);
        checkUserNameAvailableThread.start();

        return isUserNameAvailable;
    }

    public boolean registerNewUser() throws IOException, XMPPException, SmackException {

        Runnable registerUserRun = new Runnable() {
            @Override
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

                    SASLMechanism mechanism = new SASLDigestMD5Mechanism();
                    SASLAuthentication.registerSASLMechanism(mechanism);
                    SASLAuthentication.blacklistSASLMechanism("SCRAM-SHA-1");
                    SASLAuthentication.blacklistSASLMechanism("DIGEST-MD5");

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
                        //mConnection.login();
                    } catch (SmackException.NoResponseException e) {
                        Log.d(TAG, "CREATE ACCOUNT: NoResponseException " + e.getMessage());
                        e.printStackTrace();
                        e.getMessage();
                    } catch (XMPPException.XMPPErrorException e) {
                        Log.d(TAG, "CREATE ACCOUNT: XMPPErrorException " + e.getMessage());
                        e.printStackTrace();
                        e.getMessage();
                        Log.d(TAG, "User already exists");
                    } catch (SmackException.NotConnectedException e) {
                        Log.d(TAG, "CREATE ACCOUNT: NotConnectedException " + e.getMessage());
                        e.printStackTrace();
                        e.getMessage();
                    }

                    mConnection.disconnect();

                } catch (Exception e) {
                    Log.d(TAG, "Not Connected with server ***************************************************");
                    e.printStackTrace();
                }
                registerUserHandler.sendEmptyMessage(0);

            }
        };

        registerUserTread = new Thread(registerUserRun);
        registerUserTread.start();

        return true;
    }

    public void createHuhContacts() {

        Runnable createHuhContactsRun = new Runnable() {
            @Override
            public void run() {

                Log.d(TAG, "Connecting to server " + mServiceName);
                XMPPTCPConnectionConfiguration.XMPPTCPConnectionConfigurationBuilder builder =
                        XMPPTCPConnectionConfiguration.builder();
                builder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
                builder.setServiceName(mServiceName);
                builder.setUsernameAndPassword(jidPhoneNumber, password);
                builder.setRosterLoadedAtLogin(true);
                builder.setResource("Huh");

                SASLMechanism mechanism = new SASLDigestMD5Mechanism();
                SASLAuthentication.registerSASLMechanism(mechanism);
                SASLAuthentication.blacklistSASLMechanism("SCRAM-SHA-1");
                SASLAuthentication.blacklistSASLMechanism("DIGEST-MD5");

                mConnection = new XMPPTCPConnection(builder.build());
                //Listens fo connection events and calls connevction listener method based on result
                mConnection.addConnectionListener((ConnectionListener) mApplicationContext);
                search = new UserSearchManager(mConnection);

                try {
                    mConnection.connect();
                } catch (SmackException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XMPPException e) {
                    e.printStackTrace();
                }
                //if authentication successful authenticated(XMPPConnection connection) method called

                try {
                    mConnection.login();
                } catch (XMPPException e) {
                    e.printStackTrace();
                } catch (SmackException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "createHuhContacts()");


                //check if phone contact exists on the openfire server and adds it to huhContacts ArrayList if it does
                for (RosterContact c : phoneNumbers) {
                    Log.d(TAG, "Checking phone Numbers exist " + c.getphoneNumber() + " Name: " + c.getJid());

                }
                for (RosterContact c : phoneNumbers) {
                    //isHuhUser = checkIfUserExists(c.getphoneNumber());
                    checkIfUserExists2(c.getphoneNumber());
                    Log.d(TAG, "Is huh user: " + isHuhUser + "PhoneNumber" + c.getphoneNumber());

                    if (isHuhUser == true) {
                        huhContacts.add(c);
                        Log.d(TAG, "Adding to huhContacts: Phone" + c.getphoneNumber() + " Name: " + c.getJid());
                        Presence subscribe = new Presence(Presence.Type.subscribe);
                        subscribe.setTo(c.getphoneNumber() + "@win-h6g4cdqot7e");
                        try {
                            mConnection.sendPacket(subscribe);
                        } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                        }
                        isHuhUser = false;
                    }

                }
                for (RosterContact c : huhContacts) {
                    Log.d(TAG, "Checking huhContacts is populated: Phone" + c.getphoneNumber() + " Name: " + c.getJid());

                }

                //Saving huhContacts to shared prefs
                Log.d(TAG, "Saving huhContsct to shared prefs ");
                for (RosterContact c : huhContacts) {
                    Log.d(TAG, "huhContacts: Phone" + c.getphoneNumber() + " Name: " + c.getJid());
                }
                try {
                    prefs.edit().putString("huhContacts", ObjectSerializer.serialize((Serializable) huhContacts)).commit();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                try {
                    mConnection.disconnect();
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }

                createHuhContactshandler.sendEmptyMessage(0);
            }

        };

        createHuhContactsThread = new Thread(createHuhContactsRun);
        createHuhContactsThread.start();

    }

    public void checkIfUserExists2(String user) {

        UserSearchManager search = new UserSearchManager(mConnection);
        Form searchForm = null;
        try {
            searchForm = search
                    .getSearchForm("search." + mConnection.getServiceName());
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }

        Form answerForm = searchForm.createAnswerForm();
        answerForm.setAnswer("Username", true);
        answerForm.setAnswer("search", user);
        ReportedData data = null;
        try {
            data = search
                    .getSearchResults(answerForm, "search." + mConnection.getServiceName());
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }

        if (data.getRows() != null) {
            for (ReportedData.Row row : data.getRows()) {
                for (String value : row.getValues("jid")) {
                    Log.i("Iteartor values......", " " + value);
                    isHuhUser = true;

                }
            }
            //  Toast.makeText(mApplicationContext, "Username Exists", Toast.LENGTH_SHORT).show();
        }

    }

    private void saveCredentialsAndGoToLogin() {

        Log.d(TAG, "saveCredentialsAndLogin() called.");
        //Toast.makeText(getApplicationContext(), TAG + ": saveCredentialsAndLogin() called.", Toast.LENGTH_LONG).show();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit()
                .putString("xmpp_jid", mJidView.getText().toString())
                .putString("xmpp_password", mPasswordView.getText().toString())
                .putString("language", "en")
                .putBoolean("xmpp_logged_in", true)
                .putBoolean("xmpp_user_registered", true)
                .commit();

//        Intent intent = new Intent(this, LoginActivity.class);
//        startActivity(intent);
//        finish();

        //Start the service
        Log.d(TAG, "StartService called from Registration.");
        //Toast.makeText(getApplicationContext(), TAG + ": StartService called from Login.", Toast.LENGTH_LONG).show();
        Intent i1 = new Intent(this, HuhConnectionService.class);
        startService(i1);
        finish();
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }
        //get phone contacts from users phone
        phoneContacts();

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

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        // return email.contains("@");
        return jidPhoneNumber.length() == 10;

    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        Boolean isValid = true;
        if(password.isEmpty()){
            isValid = false;
        }
        if(password.length() < 4){
            return false;
        }
        if(password.length() == 0){
            isValid = false;
        }
        if(password == null){
            isValid = false;
        }
        if(password.equals("null")){
            isValid = false;
        }
        return isValid ;
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