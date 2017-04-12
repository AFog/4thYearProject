package com.aaronfogarty.huh;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;


public class PersonalityInsightsActivity extends AppCompatActivity {

    Thread personalityInsightThread;
//    JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
//    ObjectNode objectNode = nodeFactory.objectNode();
//    ArrayNode arrayNode = nodeFactory.arrayNode();
//    ObjectNode outterNode = nodeFactory.objectNode();

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


}
