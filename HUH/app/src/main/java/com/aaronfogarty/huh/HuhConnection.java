/**
 * Created by aaronfogartyfogarty on 16/02/2017.
 */
package com.aaronfogarty.huh;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ChatMessageListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.sasl.SASLMechanism;
import org.jivesoftware.smack.sasl.provided.SASLDigestMD5Mechanism;
import org.jivesoftware.smack.sasl.provided.SASLExternalMechanism;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.UserSearch;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.xdata.Form;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.jivesoftware.smack.packet.Presence.Type.unavailable;


public class HuhConnection implements ConnectionListener {

    private static final String TAG = "HuhConnection";
    private Map<String, String> attributes;

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
    private static List<String> unavailableMessages;
    private static List<String> offlineMessages;
    private String translatedText;
    private String baseLanguage;
    private static List<RosterContact> huhContacts;
    boolean isHuhUser;

    public String getTranslatedText(){
        return translatedText;
    }
    public void setTranslatedText(String inputText){
        translatedText = inputText;
    }


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
        baseLanguage = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
                .getString("language", "en");

        if (jid != null) {
            mUsername = jid.split("@")[0];
            mServiceName = "ec2-35-162-128-9.us-west-2.compute.amazonaws.com";
            //mServiceName = jidPhoneNumber.split("@")[1];
        } else {
            mUsername = "";
            mServiceName = "";
        }

        Log.d(TAG, "Getting from prefs Username: " + mUsername + "password " + mPassword);

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

        //unavailableMessages = new ArrayList<String>();
        offlineMessages = new ArrayList<String>();

        //Set up the ui thread broadcast message receiver.
        broadCastMessageReceiver();
        broadCastAvailabilityReceiver();

        SASLMechanism mechanism = new SASLDigestMD5Mechanism();
        SASLAuthentication.registerSASLMechanism(mechanism);
        SASLAuthentication.blacklistSASLMechanism("SCRAM-SHA-1");
        SASLAuthentication.blacklistSASLMechanism("DIGEST-MD5");

        mConnection = new XMPPTCPConnection(builder.build());
//        SASLMechanism sm = new SASLExternalMechanism();
//        SASLAuthentication.registerSASLMechanism(sm.instanceForAuthentication());
        //Listens fo connection events and calls connevction listener method based on result
        mConnection.addConnectionListener(this);



        mConnection.connect();
        //if authentication successful authenticated(XMPPConnection connection) method called
        Log.d(TAG,"Is user authenticated: " + mConnection.isAuthenticated());
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

        messageListener = new ChatMessageListener() {
            @Override
            public void processMessage(Chat chat, Message message) {

            //    if(message.getBody() != null){
                String incomingMessage = message.getBody();
                Log.d(TAG, "message.getBody() :" + message.getBody());
                Log.d(TAG, "message.getFrom() :" + message.getFrom());
               // Log.d(TAG, "message.getExtension(\"x\",\"jabber:x:delay\") :" + message.getExtension("x", "jabber:x:delay"));
                Log.d(TAG, "message.getBody() incomingMessage before :" + incomingMessage);

                incomingMessage = translateMessageText2(incomingMessage, baseLanguage);

                if (hasJustLoggedIn) {
                    //Handles offline messages when reconnected
                    offlineMessages(message);
                }

                //handles messages when user is unavailable
                    unavailableMessages(message.getBody());

                String from = message.getFrom();
                String contactJid = "";
                if (from.contains("/")) {
                    contactJid = from.split("/")[0];
                    Log.d(TAG, "The real jidPhoneNumber is :" + contactJid);
                } else {
                    contactJid = from;
                }

                mConnection.setPacketReplyTimeout(0);

                Log.d(TAG, "^^^^^^^^^^^^^**************** iaAvailable before broadcast: " + isAvailable );
                if (isAvailable) {
                    broadcastunAvailableArray();
                    unavailableMessages.clear();
                }

                //String body = translateMessageText(message.getBody(), baseLanguage);
                //BOROADCAST
                //Data within intent to send in a broadcast.
                Intent intent = new Intent();
                // sets keyword to listen out for for this broadcast
                intent.setAction(HuhConnectionService.NEW_MESSAGE);
                intent.setPackage(mApplicationContext.getPackageName());
                intent.putExtra(HuhConnectionService.BUNDLE_FROM_JID, contactJid);
                intent.putExtra(HuhConnectionService.BUNDLE_MESSAGE_BODY, incomingMessage);

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
    public void unavailableMessages(String message){
        Log.d(TAG, "unavailableMessages()");
        unavailableMessages = new ArrayList<String>();
        unavailableMessages.add(message);
       // unavailableMessages.add(message.getBody());
        for (String s:unavailableMessages) {
            Log.d(TAG, "unavailableMessages ARRAYLIST: " + s);
        }
    }

    //handles messages when user unavailable
    public void offlineMessages(Message message){
        Log.d(TAG, "offlineMessages()");
        offlineMessages.add(message.getBody());
        //offlineMessages.add(message.getBody());
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

    //Translates Messages to chosen base language
    public String translateMessageText(String text, String toLanguage) {

        String output = "";

        RequestQueue queue = Volley.newRequestQueue(mApplicationContext);
        String url = "https://iueh1tvfn3.execute-api.us-west-2.amazonaws.com/tranlateenpointbeta/";
        Log.d("translateText()", "");

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("translatedText", text);
            jsonBody.put("targetLanguage", toLanguage);
            jsonBody.put("sourceLanguage", toLanguage);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        String requestBody = jsonBody.toString();
        Log.d("RequestBody", requestBody);
        //

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("translateText", "response: " + response.toString());
                            JSONObject jsonObject = new JSONObject(response.toString());

                            String translatedText = response.getString("translatedText");
                            // String tolanguage = response.get("targetLanguage").toString();
                            String sourceLanguage = response.getString("detectedSourceLanguage");
                            setTranslatedText(translatedText);
                            Log.d("translateText()", "get response: " + translatedText + " detectedSourceLanguage: " + sourceLanguage);

                        } catch (JSONException e) {
                            Log.d("Trans JSON ex: ", e.getMessage());
                        } catch (Exception e) {
                            Log.d("Translation Exception ", e.getMessage());
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("Trans Exception ", error.getMessage());

                    }
                });
        //

        queue.add(jsObjRequest);

        output = getTranslatedText();
        Log.d(TAG,"INSIDE TRANSLATE output: "+ output);
        Log.d(TAG,"INSIDE TRANSLATE translatedText: "+translatedText);
        Log.d(TAG,"INSIDE TRANSLATE getTranlayedText: "+ getTranslatedText());

        translatedText = "";
        return output;
    }

    //Retrieves Users Roster contacts from Openfire server
    public ArrayList <RosterContact>  getRosterContacts(){
        Log.d(TAG, "getRosterContacs()");

        mConnection.getRoster();
        Roster roster =  mConnection.getRoster();
        Collection <RosterEntry> entries = roster.getEntries();
        ArrayList<RosterContact> rosterEntries = new ArrayList<>();
        for (RosterEntry entry : entries) {
            Log.d(TAG, "Here: " + entry + " \nUser: " + entry.getUser() + "Name" + entry.getName() + "\nStatus " + entry.getStatus() + "\nType " + entry.getType() + "\nGroup " + entry.getGroups() );
            RosterContact tempContact = new RosterContact(entry.getName(), entry.getUser());
            rosterEntries.add(tempContact);
        }
        return rosterEntries;
    }

    public void getPhoneContacts(){
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
        for (RosterContact c:phoneContacts) {
        Log.d(TAG, "returned roster contacts: \nUser: " + c.getJid() + "\nPhoneNumber: " + c.getphoneNumber());

        }
    }

    public void createHuhContacts(){
        Log.d(TAG, "createHuhContacts()");

        huhContacts = new ArrayList<>();

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

        //check if phone contact exists on the openfire server and adds it to huhContacts ArrayList if it does
        for (RosterContact c: phoneContacts) {
            //isHuhUser = checkIfUserExists(c.getphoneNumber());
            checkIfUserExists2(c.getphoneNumber());
            Log.d(TAG, "Is huh user: " + isHuhUser);

            if(isHuhUser == true) {
                huhContacts.add(c);
                Log.d(TAG, "Adding to huhContacts: Phone" + c.getphoneNumber() + " Name: " + c.getJid());

                //subscribes to that user allowing communication
//                Presence subscribe = new Presence(Presence.Type.subscribe);
//                subscribe.setTo(c.getphoneNumber() + "@win-h6g4cdqot7e");
//                try {
//                    mConnection.sendPacket(subscribe);
//                } catch (SmackException.NotConnectedException e) {
//                    e.printStackTrace();
//                }
                isHuhUser = false;
            }

        }

        // saves huHContacts arraylist to preference to be used in ContactModel
       // SharedPreferences prefs1 = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            prefs.edit()
                    .putString("huhContacts", ObjectSerializer.serialize((Serializable)huhContacts))
                    .commit();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        for (RosterContact c: huhContacts) {
                Log.d(TAG, "huhContacts: Phone" + c.getphoneNumber() + " Name: " + c.getJid());
        }
    }

    public ArrayList<RosterContact> gethuhContacts(){

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

    public void displayhuhContacts(){
        Log.d(TAG, "displayHuhContacts()");
        for (RosterContact rc : huhContacts) {
            Log.d(TAG, "creathuhContacts() huhcontacts: \nUser: " + rc.getJid() + "\nPhoneNumber: " + rc.getphoneNumber());

        }
    }

    public Boolean checkIfUserExists(String user){
        Log.d(TAG, "CHECKING IF USER EXISTS: "  + user + "@win-h6g4cdqot7e");
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
            for (ReportedData.Row row: data.getRows()) {
                for (String value: row.getValues("jidPhoneNumber")) {
                    Log.i("USER EXISTS", " " + value);
                    return true;

                }
            }
           // Toast.makeText( mApplicationContext,"Username Exists", Toast.LENGTH_SHORT).show();
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

    public void checkIfUserExists2(String user){
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
            for (ReportedData.Row row: data.getRows()) {
                for (String value: row.getValues("jid")) {
                    Log.i("Iteartor values......", " " + value);
                    isHuhUser = true;

                }
            }
          //  Toast.makeText(mApplicationContext, "Username Exists", Toast.LENGTH_SHORT).show();
        }
    }

    public void getAllXmppUsers()
    {
        try {
            UserSearchManager manager = new UserSearchManager(mConnection);
            String searchFormString = "search." + mConnection.getServiceName();
            Log.d("***", "SearchForm: " + searchFormString);
            Form searchForm = null;

            searchForm = manager.getSearchForm(searchFormString);

            Form answerForm = searchForm.createAnswerForm();

            UserSearch userSearch = new UserSearch();
            answerForm.setAnswer("Username", true);
            answerForm.setAnswer("search", "*");

            ReportedData results = userSearch.sendSearchForm(mConnection, answerForm, searchFormString);
            if (results != null) {
                List<ReportedData.Row> rows = results.getRows();
                for (ReportedData.Row row : rows) {
                    Log.d("***", "row: " + row.getValues("Username").toString());
                }
            } else {
                Log.d("***", "No result found");
            }

        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    public String translateMessageText2(String text, String toLanguage) {

        baseLanguage = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
                .getString("language", "en");

        String output = "";

        RequestQueue queue = Volley.newRequestQueue(mApplicationContext);
        String url = "https://iueh1tvfn3.execute-api.us-west-2.amazonaws.com/tranlateenpointbeta/";
        Log.d("translateText()", "");

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("translatedText", text);
            jsonBody.put("targetLanguage", toLanguage);
            jsonBody.put("sourceLanguage", toLanguage);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        String requestBody = jsonBody.toString();
        Log.d("RequestBody", requestBody);
        //

        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody, future, future);
        queue.add(request);

        try {
            JSONObject response = null;
            while (response == null) {
                try {
                    response = future.get(30, TimeUnit.SECONDS); // Block thread, waiting for response, timeout after 30 seconds
                } catch (InterruptedException e) {
                    // Received interrupt signal, but still don't have response
                    // Restore thread's interrupted status to use higher up on the call stack
                    Thread.currentThread().interrupt();
                    // Continue waiting for response (unless you specifically intend to use the interrupt to cancel your request)
                }
            }
            // Do something with response, i.e.
            Log.d("translateText", "response: " + response.toString());
            JSONObject jsonObject = new JSONObject(response.toString());

            String translatedText = response.getString("translatedText");
            // String tolanguage = response.get("targetLanguage").toString();
            String sourceLanguage = response.getString("detectedSourceLanguage");
            setTranslatedText(translatedText);
            Log.d("translateText()", "get response: " + translatedText + " detectedSourceLanguage: " + sourceLanguage);

        } catch (ExecutionException e) {
            // Do something with error, i.e.
        } catch (TimeoutException e) {
            // Do something with timeout, i.e.
        } catch (JSONException e) {
            e.printStackTrace();
        }

        output = getTranslatedText();
        Log.d(TAG,"INSIDE TRANSLATE output: "+ output);
        Log.d(TAG,"INSIDE TRANSLATE translatedText: "+translatedText);
        Log.d(TAG,"INSIDE TRANSLATE getTranlayedText: "+ getTranslatedText());

        translatedText = "";
        return output;
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
        HuhConnectionService.sConnectionState = ConnectionState.CONNECTED;
        Log.d(TAG, "Connected Successfully");
        //Toast.makeText(mApplicationContext,TAG + ": Connected Successfully ", Toast.LENGTH_LONG).show();
        //showContactListActivityWhenAuthenticated();

    }

    @Override
    public void authenticated(XMPPConnection connection) {
        HuhConnectionService.sConnectionState = ConnectionState.CONNECTED;
        Log.d(TAG, "Authenticated Successfully");
        //Toast.makeText(mApplicationContext,TAG + ": Authenticated Successfully ", Toast.LENGTH_LONG).show();

    }

    @Override
    public void connectionClosed() {
        HuhConnectionService.sConnectionState = ConnectionState.DISCONNECTED;
        Log.d(TAG, "Connectionclosed()");
        //Toast.makeText(mApplicationContext,TAG + ": Connectionclosed ", Toast.LENGTH_LONG).show();
//        try {
//            if (mConnection != null) {
//                mConnection.disconnect();
//            }
//            // mConnection = null;
//            // Unregister the message broadcast receiver.
//            if (uiThreadMessageReceiver != null) {
//                mApplicationContext.unregisterReceiver(uiThreadMessageReceiver);
//                uiThreadMessageReceiver = null;
//            }
//
//            connect();
//        } catch (IOException e1) {
//            e1.printStackTrace();
//        } catch (XMPPException e1) {
//            e1.printStackTrace();
//        } catch (SmackException e1) {
//            e1.printStackTrace();
//        }

    }

    @Override
    public void connectionClosedOnError(Exception e) {
        HuhConnectionService.sConnectionState = ConnectionState.DISCONNECTED;
        Log.d(TAG, "ConnectionClosedOnError, error " + e.toString());

//        try {
//            if (mConnection != null) {
//                mConnection.disconnect();
//            }
//
//
//       // mConnection = null;
//        // Unregister the message broadcast receiver.
//        if (uiThreadMessageReceiver != null) {
//            mApplicationContext.unregisterReceiver(uiThreadMessageReceiver);
//            uiThreadMessageReceiver = null;
//        }
//
//            connect();
//        } catch (IOException e1) {
//            e1.printStackTrace();
//        } catch (XMPPException e1) {
//            e1.printStackTrace();
//        } catch (SmackException e1) {
//            e1.printStackTrace();
//        }
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