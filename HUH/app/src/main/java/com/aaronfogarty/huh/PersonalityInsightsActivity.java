package com.aaronfogarty.huh;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import co.devcenter.androiduilibrary.models.ChatMessage;


public class PersonalityInsightsActivity extends AppCompatActivity {

    Thread personalityInsightThread;
//    JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
//    ObjectNode objectNode = nodeFactory.objectNode();
//    ArrayNode arrayNode = nodeFactory.arrayNode();
//    ObjectNode outterNode = nodeFactory.objectNode();

    private Context mApplicationContext;
    private List<String> personChatHistoryList;
    private String chatHistory;
    private String FILENAME;
    private String TAG = "PersonalityInsights ";
    private String PERSONHISTORYFILE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personality_insights);
        FILENAME = getUserHistoryFileName();


        readFromChatHistoryList();


//        objectNode.put("content", "test values");
//        arrayNode.add(objectNode);
//        outterNode.put("contentItems",arrayNode);
//
//        Log.d("testin json ",outterNode.asText());


    }

    public void getPersonalityInsight(){

        Runnable personInsightRun = new Runnable() {
            @Override
            public void run() {



            }
        };

        Thread personalityInsightThread = new Thread(personInsightRun);
        personalityInsightThread.start();

    }
    public static Object readObject(Context context, String file) throws IOException,
            ClassNotFoundException {
        FileInputStream fis = context.openFileInput(file);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object object = ois.readObject();
        return object;
    }

    public void readFromChatHistoryList() {
        try {
            // Retrieve the list from internal storage
            personChatHistoryList = (List<String>) readObject(this, FILENAME);
            // Display the items from the list retrieved.
            for (String entry : personChatHistoryList) {
                Log.d(TAG,"PersonHistory entry: " + entry);
                chatHistory = chatHistory + "." + entry;
            }
            Log.d(TAG,"PersonHistory file: " + chatHistory);

        } catch (ClassNotFoundException | IOException e) {
            Log.e(TAG, e.getMessage());
        }

    }

    public String getUserHistoryFileName(){
        mApplicationContext = getApplicationContext();
        String jid = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
                .getString("xmpp_jid", null);
        PERSONHISTORYFILE = "Chat.History" + jid;

        return PERSONHISTORYFILE;
    }

}
