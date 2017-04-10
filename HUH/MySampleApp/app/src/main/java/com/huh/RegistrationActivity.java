package com.huh;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mysampleapp.MainActivity;
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
    String jidPhoneNumber;
    String password;
    boolean isHuhUser;
    boolean isNewHuhUser;
    private boolean hasContacts;
    private boolean isAllowedContacts;
    private List<RosterContact> huhContacts;
    private ArrayList<RosterContact> phoneNumbers;
    private Handler handler;
    private Handler registerNewUserhandler;
    private Handler createHuhContactshandler;
    private SharedPreferences prefs;

    // UI references.
    private AutoCompleteTextView mJidView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private Context mApplicationContext;
    private String mUsername = "admin";
    private String mPassword = "admin";
    private String mNewUsername;
    private String mNewUserPassword;
    private String mServiceName = "ec2-35-162-128-9.us-west-2.compute.amazonaws.com";
    Roster roster;
    private XMPPTCPConnection mConnection;
    private Map<String, String> attributes;

//    public MainActivity activity;
//
//    @Override
//    public void onAttach(Activity activity){
//        this.activity = (MainActivity) activity;
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        ProgressBar p = (ProgressBar) findViewById(R.id.registerprogressBar);
        p.setVisibility(View.VISIBLE);

        //get phone contactsa nd put them in an array for later comparision
        phoneContacts();

        //initialise huhContacts
        huhContacts = new ArrayList<>();

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // mApplicationContext = context.getApplicationContext();
        jidPhoneNumber = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getString("xmpp_jid", null);
        jidPhoneNumber = jidPhoneNumber.replace("+353", "0");
        jidPhoneNumber = jidPhoneNumber.replace(" ", "");
        jidPhoneNumber = jidPhoneNumber.replaceAll("\\s+", "");
        password = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getString("xmpp_password", null);

        Log.d(TAG, "Registration credentials " + jidPhoneNumber + "  " + password);
        populateAutoComplete();
        //attempRegister();

        // Register new user
        try {
            registerNewUser();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (SmackException e) {
            e.printStackTrace();
        }

        //when registration completes create contacts
        registerNewUserhandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Log.d(TAG, "RegisterNew User Handler ");
                createHuhContacts();
            }
        };

        //when create huh contacts completes go to login
        createHuhContactshandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Log.d(TAG, "Create Huh Contacts  Handler ");
                Log.d(TAG, "Create Huh Contacts  Handler Saving huhContsct to shared prefs ");

//                for (RosterContact c : huhContacts) {
//                    Log.d(TAG, "huhContacts: Phone" + c.getphoneNumber() + " Name: " + c.getJid());
//                }
//                try {
//                    prefs.edit().putString("huhContacts", ObjectSerializer.serialize((Serializable)huhContacts)).commit();
//                } catch (IOException e1) {
//                    e1.printStackTrace();
//

            }
        };


        //goToLogin();
        // saveCredentialsAndLogin();
    }

    private void saveCredentialsAndLogin() {
        Log.d(TAG, "saveCredentialsAndLogin() called.");
        //Toast.makeText(getApplicationContext(), TAG + ": saveCredentialsAndLogin() called.", Toast.LENGTH_LONG).show();
        //Start the service
        Log.d(TAG, "StartService called from Registration.");
        //Toast.makeText(getApplicationContext(), TAG + ": StartService called from Login.", Toast.LENGTH_LONG).show();
        Intent i1 = new Intent(this, HuhConnectionService.class);
        startService(i1);
        finish();

    }

    public void contactsPermission() {

        Runnable r = new Runnable() {
            @Override
            public void run() {
                populateAutoComplete();

            }
        };
        Thread t = new Thread(r);
        t.start();
    }

    public void goToLogin() {

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        //finish();
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }
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
     * Attempts to sign in or register the account specified by the login for m.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attempRegister() {

//Sets progress bar to visible
        ProgressBar p = (ProgressBar) findViewById(R.id.registerprogressBar);
        p.setVisibility(View.VISIBLE);

        try {
            registerNewUser();
            Log.d(TAG, "user was created ");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (SmackException e) {
            e.printStackTrace();
        }

        //  connect();
        //createHuhContacts();

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
        savePhoneNumbersList(phoneNumbers);
        hasContacts = true;
    }

    public void savePhoneNumbersList(ArrayList<RosterContact> t) {
        Log.d(TAG, "Saving phonenumbers to arraylist to preference");

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

    public void getPhoneContacts() {
        ArrayList<RosterContact> phoneContacts = new ArrayList<RosterContact>();
        // load Phone Contacts from preference
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mApplicationContext);
        try {
            phoneContacts = (ArrayList<RosterContact>) ObjectSerializer.deserialize(prefs.getString("PhoneContacts", ObjectSerializer.serialize(new ArrayList<RosterContact>())));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        for (RosterContact c : phoneContacts) {
            Log.d(TAG, "returned roster contacts: \nUser: " + c.getJid() + "\nPhoneNumber: " + c.getphoneNumber());

        }
    }

    public boolean createHuhContactsforRegistration() {
        Log.d(TAG, "createHuhContacts()");

        huhContacts = new ArrayList<>();

//        ArrayList<RosterContact> phoneContacts = new ArrayList<RosterContact>();
//        // load Phone Contacts from shared preference
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mApplicationContext);
//        try {
//            phoneContacts = (ArrayList<RosterContact>) ObjectSerializer.deserialize(prefs.getString("PhoneContacts", ObjectSerializer.serialize(new ArrayList<RosterContact>())));
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }

        //check if phone contact exists on the openfire server and adds it to huhContacts ArrayList if it does
        for (RosterContact c : phoneNumbers) {
            //isHuhUser = checkIfUserExists(c.getphoneNumber());
            checkIfUserExists2(c.getphoneNumber());
            Log.d(TAG, "Is huh user: " + isHuhUser);

            if (isHuhUser == true) {
                huhContacts.add(c);
                Log.d(TAG, "Adding to huhContacts: Phone" + c.getphoneNumber() + " Name: " + c.getJid());
                isHuhUser = false;
            }

        }

        // saves huHContacts arraylist to preference to be used in ContactModel
        // SharedPreferences prefs1 = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            prefs.edit()
                    .putString("huhContacts", ObjectSerializer.serialize((Serializable) huhContacts))
                    .commit();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        for (RosterContact c : huhContacts) {
            Log.d(TAG, "huhContacts: Phone" + c.getphoneNumber() + " Name: " + c.getJid());
        }
        return true;
    }

    public ArrayList<RosterContact> gethuhContacts() {

        ArrayList<RosterContact> phoneContacts = new ArrayList<RosterContact>();
        // load Phone Contacts from preference
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mApplicationContext);
        try {
            phoneContacts = (ArrayList<RosterContact>) ObjectSerializer.deserialize(prefs.getString("huhContacts", ObjectSerializer.serialize(new ArrayList<RosterContact>())));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return phoneContacts;

    }

    public void displayhuhContacts() {
        Log.d(TAG, "displayHuhContacts()");
        for (RosterContact rc : huhContacts) {
            Log.d(TAG, "creathuhContacts() huhcontacts: \nUser: " + rc.getJid() + "\nPhoneNumber: " + rc.getphoneNumber());

        }
    }

    public boolean registerNewUser() throws IOException, XMPPException, SmackException {



        Runnable registerRun = new Runnable() {
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

                    Log.d(TAG, "Checking user was created");
                    while (!isNewHuhUser) {
                        Thread.sleep(2000);
                        checkIfNewUserExists(jidPhoneNumber);
                        Log.d(TAG, "User exists *************************************************** " + jidPhoneNumber );
                    }
                    mConnection.disconnect();
                    isNewHuhUser = false;

                } catch (Exception e) {
                    Log.d(TAG, "Not Connected with server ***************************************************");
                    e.printStackTrace();
                }
                registerNewUserhandler.sendEmptyMessage(0);

            }

        };

        Thread registerThread = new Thread(registerRun);
        registerThread.start();

//        new Thread() {
//            public void run() {
//                try {
//
//                    createHuhContacts();
//
//                    Log.d(TAG, "Connecting to server " + mServiceName);
//                    XMPPTCPConnectionConfiguration.XMPPTCPConnectionConfigurationBuilder builder =
//                            XMPPTCPConnectionConfiguration.builder();
//                    builder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
//                    builder.setServiceName(mServiceName);
//                    builder.setUsernameAndPassword(mUsername, mPassword);
//                    builder.setRosterLoadedAtLogin(true);
//                    builder.setResource("Huh");
//
//                    mConnection = new XMPPTCPConnection(builder.build());
//                    //Listens fo connection events and calls connevction listener method based on result
//                    mConnection.addConnectionListener((ConnectionListener) mApplicationContext);
//                    mConnection.connect();
//                    //if authentication successful authenticated(XMPPConnection connection) method called
//
//                    mConnection.login();
//
//                    AccountManager accountManager = AccountManager.getInstance(mConnection);
//                    Log.d(TAG, "CREATE ACCOUNT: supports creation : - " + accountManager.supportsAccountCreation());
//
//                    try {
//                        accountManager.createAccount(jidPhoneNumber, password);
//                    } catch (SmackException.NoResponseException e) {
//                        Log.d(TAG, "CREATE ACCOUNT: NoResponseException " + e.getMessage());
//                        e.printStackTrace();
//                        e.getMessage();
//                    } catch (XMPPException.XMPPErrorException e) {
//                        Log.d(TAG, "CREATE ACCOUNT: XMPPErrorException " + e.getMessage());
//                        e.printStackTrace();
//                        e.getMessage();
//                    } catch (SmackException.NotConnectedException e) {
//                        Log.d(TAG, "CREATE ACCOUNT: NotConnectedException " + e.getMessage());
//                        e.printStackTrace();
//                        e.getMessage();
//                    }
//
//
//                    Log.d(TAG,"Connected with server ***************************************************");
//                    while(!isNewHuhUser){
//                        Thread.sleep(2000);
//                        checkIfNewUserExists(jidPhoneNumber);
//                         Log.d(TAG, "User exists *************************************************** " + jidPhoneNumber + " "+ isHuhUser);
//                     }
//
//                    isNewHuhUser = false;
//                     mConnection.disconnect();
//
//                } catch (Exception e) {
//                    Log.d(TAG, "Not Connected with server ***************************************************");
//                    e.printStackTrace();
//                }
//            }
//        }.start();

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

                mConnection = new XMPPTCPConnection(builder.build());
                //Listens fo connection events and calls connevction listener method based on result
                mConnection.addConnectionListener((ConnectionListener) mApplicationContext);
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
               // huhContacts = new ArrayList<>();
//        ArrayList<RosterContact> phoneContacts = new ArrayList<RosterContact>();
//        // load Phone Contacts from shared preference
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mApplicationContext);
//        try {
//            phoneContacts = (ArrayList<RosterContact>) ObjectSerializer.deserialize(prefs.getString("PhoneContacts", ObjectSerializer.serialize(new ArrayList<RosterContact>())));
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }

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
                        isHuhUser = false;
                    }

                }
                for (RosterContact c : huhContacts) {
                    Log.d(TAG, "Checking huhContacts is populated: Phone" + c.getphoneNumber() + " Name: " + c.getJid());

                }


                Log.d(TAG, "Saving huhContsct to shared prefs ");
                for (RosterContact c : huhContacts) {
                    Log.d(TAG, "huhContacts: Phone" + c.getphoneNumber() + " Name: " + c.getJid());
                }
                try {
                    prefs.edit().putString("huhContacts", ObjectSerializer.serialize((Serializable)huhContacts)).commit();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

//                // saves huHContacts arraylist to preference to be used in ContactModel
//                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mApplicationContext);
//                try {
//                    prefs.edit().putString("huhContacts", ObjectSerializer.serialize((Serializable) huhContacts)).commit();
//                } catch (IOException e1) {
//                    e1.printStackTrace();
//                }
//
//                for (RosterContact c : huhContacts) {
//                    Log.d(TAG, "CreatehuhContacts: Phone" + c.getphoneNumber() + " Name: " + c.getJid());
//                }

                try {
                    mConnection.disconnect();
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }

                createHuhContactshandler.sendEmptyMessage(0);
            }

        };

        Thread createHuhContactsThread = new Thread(createHuhContactsRun);
        createHuhContactsThread.start();
//        Log.d(TAG, "createHuhContacts()");
//        huhContacts = new ArrayList<>();
////        ArrayList<RosterContact> phoneContacts = new ArrayList<RosterContact>();
////        // load Phone Contacts from shared preference
////        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mApplicationContext);
////        try {
////            phoneContacts = (ArrayList<RosterContact>) ObjectSerializer.deserialize(prefs.getString("PhoneContacts", ObjectSerializer.serialize(new ArrayList<RosterContact>())));
////        } catch (IOException e) {
////            e.printStackTrace();
////        } catch (ClassNotFoundException e) {
////            e.printStackTrace();
////        }
//
//        //check if phone contact exists on the openfire server and adds it to huhContacts ArrayList if it does
//        for (RosterContact c : phoneNumbers) {
//            //isHuhUser = checkIfUserExists(c.getphoneNumber());
//            checkIfUserExists2(c.getphoneNumber());
//            Log.d(TAG, "Is huh user: " + isHuhUser);
//
//            if (isHuhUser == true) {
//                huhContacts.add(c);
//                Log.d(TAG, "Adding to huhContacts: Phone" + c.getphoneNumber() + " Name: " + c.getJid());
//                isHuhUser = false;
//            }
//
//        }
//
//        // saves huHContacts arraylist to preference to be used in ContactModel
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mApplicationContext);
//        try {
//            prefs.edit()
//                    .putString("huhContacts", ObjectSerializer.serialize((Serializable) huhContacts))
//                    .commit();
//        } catch (IOException e1) {
//            e1.printStackTrace();
//        }
//
//        for (RosterContact c : huhContacts) {
//            Log.d(TAG, "huhContacts: Phone" + c.getphoneNumber() + " Name: " + c.getJid());
//        }
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

    public void checkIfNewUserExists(String user) {
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
                    isNewHuhUser = true;

                }
            }
            //  Toast.makeText(mApplicationContext, "Username Exists", Toast.LENGTH_SHORT).show();
        }
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
                builder.setUsernameAndPassword(jidPhoneNumber, password);
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

                isHuhUser = checkIfUserExists(jidPhoneNumber);

                if (isHuhUser) {
                    ArrayList<RosterContact> rosterEntries = getRosterContacts();
                }
            }

        }.start();
    }

    public Boolean checkIfUserExists(String user) {

        Log.d(TAG, "CHECKING IF USER EXISTS: " + user + "@win-h6g4cdqot7e");
        try {

            Log.i("CONNECTION LOGGED IN? ", String.valueOf(mConnection.isConnected()));


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