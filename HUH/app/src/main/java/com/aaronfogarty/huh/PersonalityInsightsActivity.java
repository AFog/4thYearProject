package com.aaronfogarty.huh;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    private String token;
    private String TAG = "PersonalityInsights ";
    private String PERSONHISTORYFILE;
    private String personalityRespone;
    private Handler personInsightHandler;
    private JSONObject personalityResponeJson;
    private JSONObject temp ;
    private TextView profileNotReady;
    private TextView profileResults;
    private TextView profileResultsPrcnt;
    private TextView wordCountText;
    private ProgressBar progressBar;
    private ImageView insightImage;
    private String word_count;
    private int word_countInt;
    private String personalityInsights;
    private String insightsPrcnt;
    private String personalityInsightsCnsmpt;
    private String insightsScore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personality_insights);
        FILENAME = getUserHistoryFileName();
        token = this.getString(R.string.pi);
        chatHistory = "";
        progressBar = (ProgressBar) findViewById(R.id.progressBarInsights);
        progressBar.setVisibility(View.VISIBLE);
        profileNotReady = (TextView) findViewById(R.id.noHistoryText);
        profileResults = (TextView) findViewById(R.id.personInsightsText);
        //profileResults.setMovementMethod(new ScrollingMovementMethod());
        profileResultsPrcnt = (TextView) findViewById(R.id.personInsightsPrcnt);
        wordCountText = (TextView) findViewById(R.id.wordCountText);
        insightImage = (ImageView) findViewById(R.id.insightimageView);

        progressBar = (ProgressBar) findViewById(R.id.progressBarInsights);
        readFromChatHistoryList();

        //word_countInt = Integer.parseInt(word_count);
        word_countInt = count(chatHistory);
        Log.d(TAG, "Word count is " + word_count);


        if(word_countInt < 500){
            Log.d(TAG, "less than 1000 " + chatHistory.length());
            progressBar.setVisibility(View.INVISIBLE);
            profileNotReady.setVisibility(View.VISIBLE);
            insightImage.setVisibility(View.VISIBLE);
            wordCountText.setText(getString(R.string.current_word_count) + word_count);
            wordCountText.setVisibility(View.VISIBLE);
        }
        else {
            getPersonalityInsight();
        }

        //when personality Insights built extract preferred traits
        personInsightHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Log.d(TAG, "personInsightHandler Handler FINISHED THREAD");
                getPersonalityTraits();

            }
        };
    }

    public static int count(String words){
        int wordcount = 1;
        char[] charArray = words.trim().toCharArray();
        for (char c: charArray) {
            if(c == ' '){
                wordcount++;
            }
        }
        return  wordcount;
    }

    public void getPersonalityTraits(){
        Log.d(TAG, "getPersonalityTraits()");
        word_count ="";
        try {

            insightsPrcnt ="";
            personalityInsights = "";
            personalityInsightsCnsmpt="";
            //word_count = getPersonalityResponseJson().getString("word_count");
            Log.d(TAG, "Word count select " + word_count);
            //word_countInt = Integer.parseInt(word_count);
            word_countInt = count(chatHistory);

            JSONArray personality = getPersonalityResponseJson().getJSONArray("personality");

            JSONObject open = personality.getJSONObject(0);
            String opennessPrcnt1 = open.getString("percentile");



                //Openness
                for(int j =0; j < 4 ; j++) {

                    for (int i = 0; i < 4; i++) {
                        String name = personality.getJSONObject(j).getJSONArray("children").getJSONObject(i).getString("name");
                        String opennessPrcnt = personality.getJSONObject(0).getJSONArray("children").getJSONObject(i).getString("percentile");
                        opennessPrcnt = getInsightBracket(Double.parseDouble(opennessPrcnt));
                        Log.d(TAG, "\n\n******Percentile " + opennessPrcnt + " Openness " + name);

                        personalityInsights = personalityInsights + "\n" + name;
                        insightsPrcnt = insightsPrcnt + "\nScore:\t  " + opennessPrcnt;
                    }
                }

                for(int i = 1; i < 5 ; i++){
                    //String name = getPersonalityResponseJson().getJSONArray("consumption_preferences").toString();
                    String name = getPersonalityResponseJson().getJSONArray("consumption_preferences").getJSONObject(0).getJSONArray("consumption_preferences").getJSONObject(i).getString("name");
                    String score = getPersonalityResponseJson().getJSONArray("consumption_preferences").getJSONObject(0).getJSONArray("consumption_preferences").getJSONObject(i).getString("score");
                    score = getInsightBracketscore(Double.parseDouble(score));

                    Log.d(TAG, "\n\n******name " + name + " score: " + score);
                    name = name.replace("Likely to prefer ", "");
                    // personalityInsightsCnsmpt = personalityInsightsCnsmpt + "\n" + name + "\n" + "Score:" + score;
                    personalityInsightsCnsmpt = personalityInsightsCnsmpt + score + "\n" + name + "\n\n" ;

                    insightsScore = insightsScore + "\nScore:\t  " + score;
                }
                profileResults.setVisibility(View.VISIBLE);
                profileResults.setText(personalityInsights + "\n\n" + personalityInsightsCnsmpt);

                profileResultsPrcnt.setVisibility(View.VISIBLE);
                profileResultsPrcnt.setText(insightsPrcnt);
                insightImage.setVisibility(View.INVISIBLE);
                wordCountText.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.INVISIBLE);


            Log.d(TAG, "\n\n******personalityInsights " + personalityInsights + " insightsPrcnt " + insightsPrcnt);

//            String openness = jArray.getJSONObject(0).toString();
//            String opennessPrcnt = jArray.getJSONObject(1).toString();


            //Log.d(TAG, "\n\n******Openness " + opennessPrcnt + "Adventure " +adventure);



        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String myFormat(double number) {
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(number);
    }

    public String getInsightBracket(double percentile){
        String level = "";
        if(percentile <= 0.33){
            level = "Low";
        }
        else if(percentile > 0.33 && percentile <= 0.66){
            level = "Medium";
        }
        else if(percentile > 0.66) {
            level = "High";
        }
        return level;
    }

    public String getInsightBracketscore(double score){
        String level = "";
        if(score == 0.0){
            level = "You are not likely to prefer:";
        }
        else if(score == 0.5){
            level = "You are likely to prefer:";
        }
        else if(score == 1.0) {
            level = "You are very likely to prefer:";
        }
        return level;
    }

    public void getPersonalityInsight() {

        Runnable personInsightRun = new Runnable() {
            @Override
            public void run() {

                RequestQueue queue = Volley.newRequestQueue(mApplicationContext);

                JSONArray jsonArray = new JSONArray();
                JSONObject jsonInnerBody = new JSONObject();
                JSONObject jsonBody = new JSONObject();

                String url = "https://gateway.watsonplatform.net/personality-insights/api/v3/profile?version=2016-10-20&consumption_preferences=true&raw_scores=true";
                Log.d(TAG, "Getting Personality insights:");

                try {
                    //chatHistory = "Things will work out fine between the U.S.A. and Russia. At the right time everyone will come to their senses &amp; there will be lasting peace! I have great confidence that China will properly deal with North Korea. If they are unable to do so, the U.S., with its allies, will! U.S.A. Jobs are returning, illegal immigration is plummeting, law, order and justice are being restored. We are truly making America great again! One by one we are keeping our promises - on the border, on energy, on jobs, on regulations. Big changes are happening! Economic confidence is soaring as we unleash the power of private sector job creation and stand up for the American Workers. #AmericaFirst Great meeting w/ NATO Sec. Gen. We agreed on the importance of getting countries to pay their fair share &amp; focus on… https://t.co/G9TdjWezmH Great win in Kansas last night for Ron Estes, easily winning the Congressional race against the Dems, who spent heavily &amp; predicted victory! Had a very good call last night with the President of China concerning the menace of North Korea. I will be interviewed by @MariaBartiromo at 6:00 A.M. @FoxBusiness. Enjoy! Great Strategic &amp; Policy CEO Forum today with my Cabinet Secretaries and top CEO's from around the United States.… https://t.co/pmGIL08OpP Ron Estes is running TODAY for Congress in the Great State of Kansas. A wonderful guy, I need his help on Healthcare &amp; Tax Cuts (Reform). North Korea is looking for trouble. If China decides to help, that would be great. If not, we will solve the problem without them! U.S.A. I explained to the President of China that a trade deal with the U.S. will be far better for them if they solve the North Korean problem! RT @foxnation: Grateful Syrians React To @realDonaldTrump Strike: 'I'll Name My Son Donald' https://t.co/7KexXsXLUx #SyrianStrikes Happy Passover to everyone celebrating in the United States of America, Israel, and around the world. #ChagSameach Congratulations to Justice Neil Gorsuch on his elevation to the United States Supreme Court. A great day for Americ… https://t.co/RM9lfTaePS Thank you @USNavy! #USA https://t.co/oD7L8vPgjq ...confidence that President Al Sisi will handle situation properly. So sad to hear of the terrorist attack in Egypt. U.S. strongly condemns. I have great... Judge Gorsuch will be sworn in at the Rose Garden of the White House on Monday at 11:00 A.M. He will be a great Justice. Very proud of him! The reason you don't generally hit runways is that they are easy and inexpensive to quickly fix (fill in and top)! Congratulations to our great military men and women for representing the United States, and the world, so well in the Syria attack. ...goodwill and friendship was formed, but only time will tell on trade. It was a great honor to have President Xi Jinping and Madame Peng Liyuan of China as our guests in the United States. Tremendous... RT @IvankaTrump: Very proud of Arabella and Joseph for their performance in honor of President Xi Jinping and Madame Peng Liyuan's official… It was an honor to host our American heroes from the @WWP #SoldierRideDC at the @WhiteHouse today with @FLOTUS, @VP… https://t.co/u5AI1pupVV JOBS, JOBS, JOBS!\\nhttps://t.co/XGOQPHywrt https://t.co/B5Qbn6llzE I am deeply committed to preserving our strong relationship &amp; to strengthening America's long-standing support for… https://t.co/GxrOR3ALCE Great to talk jobs with #NABTU2017. Tremendous spirit &amp; optimism - we will deliver! https://t.co/6lRuQZZHrc Thank you Sean McGarvey &amp; the entire Governing Board of Presidents for honoring me w/an invite to speak. #NABTU2017… https://t.co/dJlZvlq6Tj .@WhiteHouse #CEOTownHall\\n https://t.co/ADSKuUXf1b https://t.co/XHfQ6zmF2H RT @DRUDGEREPORT: RICE ORDERED SPY DOCS ON TRUMP? https://t.co/bL2nZRFxk9";
                    jsonInnerBody.put("content", chatHistory);
                    jsonArray.put(jsonInnerBody);
                    jsonBody.put("contentItems", jsonArray);
                    Log.d(TAG, "JSONbody POST: " + jsonBody.toString());

                    JsonObjectRequest request = new JsonObjectRequest(
                            Request.Method.POST,
                            url,
                            jsonBody,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    //Log.d(TAG, "Response:" + response.toString());
                                    //Log.d(TAG,"Setting Response to string:\n" + response.toString());
                                        Log.d(TAG, "Setting Response to string:");
                                        setPersonalityResponseString(response.toString());
                                        Log.d(TAG, "Getting Response to string:" + getPersonalityResponseString());

                                  //  temp = response;
                                        setPersonalityResponseJson(response);

                                    personInsightHandler.sendEmptyMessage(0);

                                }
                            },
                            null) {

                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            HashMap<String, String> headers = new HashMap<String, String>();
                            headers.put("Authorization", token);
                            headers.put("Content-Type", "application/json");
                            return headers;
                        }
                    };

                    queue.add(request);
                } catch (Exception e) {
                    e.printStackTrace();
                }

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
               // Log.d(TAG, "PersonHistory entry: " + entry);
                chatHistory = chatHistory + "." + entry;
            }
            Log.d(TAG, "PersonHistory file: " + chatHistory);

        } catch (ClassNotFoundException | IOException e) {
            Log.e(TAG, e.getMessage());
        }

    }

    public String getUserHistoryFileName() {
        mApplicationContext = getApplicationContext();
        String jid = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
                .getString("xmpp_jid", null);
        PERSONHISTORYFILE = "Chat.History" + jid;

        return PERSONHISTORYFILE;
    }

    public void setPersonalityResponseString(String setPersonalityRespone){
        personalityRespone = setPersonalityRespone;
      //  Log.d(TAG,"\nsetPersonalityResponseString() " + personalityRespone);
    }

    public String getPersonalityResponseString(){
        return personalityRespone;
    }

    public void setPersonalityResponseJson(JSONObject setPersonalityRespone){
        personalityResponeJson = setPersonalityRespone;
    }

    public JSONObject getPersonalityResponseJson(){
        return personalityResponeJson;
    }

}
