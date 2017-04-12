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
    private List<HuhMessage> cachedEntries;
    private String chatHistory;
    private String FILENAME;
    private String TAG = "PersonalityInsights ";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personality_insights);

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
            cachedEntries = (List<HuhMessage>) readObject(this, FILENAME);
            // Display the items from the list retrieved.
            for (HuhMessage entry : cachedEntries) {
                //Log.d(TAG, "line 231" + entry.sender);
                // Log.d(TAG, "INSIDES ReadList line 233" + entry.sender);
                if(entry.isMine == true){
                    chatHistory = chatHistory+ ". "+ entry.body;
                }
                if (entry.isMine == false){
                    //if message not the users do something
                }

            }

        } catch (ClassNotFoundException | IOException e) {
            Log.e(TAG, e.getMessage());
        }

    }

    public void getUserHistoryFileName(){
        mApplicationContext =  getApplicationContext();
        String jid = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
                .getString("xmpp_jid", null);
    }

}
