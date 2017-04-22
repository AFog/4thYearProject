package com.aaronfogarty.huh;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

//import com.google.gson.Gson;

public class TranslationSpinner extends AppCompatActivity {


    private Context context;
    private String url;
    private String translatedText;
    private ArrayList<String> languageName = null;
    private ArrayList<String>  languageIso = null;
    private String[] languages;
    private String[] displayLanguages;
    private String sourceLanguage;
    private String langselected;
    private String output;
    private Context mContext;
    private Handler translateHandler;
    private static final String TAG = "TranslationSpinner";


    public String getTranslatedText(){
        return translatedText;
    }
    public void setTranslatedText(String inputText){
        translatedText = inputText;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translation_spinner);

        mContext = getApplicationContext();
        tranlateSpinner();
    }

public void tranlateSpinner(){

    final EditText inputTextEditText = (EditText) findViewById(R.id.translationInputText);
    final TextView outputTexttextview = (TextView) findViewById(R.id.translationTextView);
    outputTexttextview.setMovementMethod(new ScrollingMovementMethod());


    String inputText ="";
    output ="";


    ///START
    sourceLanguage = PreferenceManager.getDefaultSharedPreferences(this)
            .getString("sourcelanguage", "en");

    languages = getResources().getStringArray(R.array.Languages);
    displayLanguages = getResources().getStringArray(R.array.displayLanguages);
    languageName = new ArrayList<>();
    languageIso = new ArrayList<>();

    for (String s: languages) {
        String temp[];
        temp = s.split(" ");
        languageName.add(temp[0]);
        languageIso.add(temp[1]);
    }
    displayLanguages = getResources().getStringArray(R.array.displayLanguages);


    Spinner spinner  = (Spinner) findViewById(R.id.spinner2);
    //spinner.setBackgroundColor(Color.WHITE);
    // Create an ArrayAdapter using the string array and a default spinner layout
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.displayLanguages, android.R.layout.simple_spinner_item);
    // Specify the layout to use when the list of choices appears
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // Apply the adapter to the spinner
    spinner.setAdapter(adapter);

    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            Toast.makeText(TranslationSpinner.this,parentView.getItemAtPosition(position).toString() ,Toast.LENGTH_SHORT).show();
            Log.d("names ", languageName.get(position));
            Log.d("iso ", languageIso.get(position));
            langselected =languageIso.get(position);
//            String text = inputTextEditText.getText().toString();
//            output = translateMessageText2(text,langselected, sourceLanguage);
//            Log.d(TAG, "output " + getTranslatedText());
//            outputTexttextview.setText(getTranslatedText());
//
//
//            //when translation completes display
//            translateHandler = new Handler() {
//                @Override
//                public void handleMessage(Message msg) {
//                    Log.d(TAG, "Translate  Handler ");
//                    Log.d("output ", getTranslatedText());
//                    outputTexttextview.setText(getTranslatedText());
//
//                }
//            };
            //START

//                if(langselected.equals(sourceLanguage)){
//                    textview.setText(translatedText);
//                }
//
//                RequestQueue queue = Volley.newRequestQueue(mContext);
//                String url = "https://iueh1tvfn3.execute-api.us-west-2.amazonaws.com/tranlateenpointbeta/";
//                Log.d("translateText()", "");
//
//                JSONObject jsonBody = new JSONObject();
//                try {
//                    jsonBody.put("translatedText", text);
//                    jsonBody.put("targetLanguage", langselected);
//                    jsonBody.put("sourceLanguage", sourceLanguage);
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                String requestBody = jsonBody.toString();
//                Log.d("RequestBody", requestBody);
//                //
//                JsonObjectRequest request = new JsonObjectRequest(
//                        Request.Method.POST,
//                        url,
//                        jsonBody,
//                        new Response.Listener<JSONObject>() {
//                            @Override
//                            public void onResponse(JSONObject response) {
//                                // Do something with response, i.e.
//                                Log.d("translateText", "response: " + response.toString());
//
//                                try {
//                                    Log.d("translateText", "response: " + response.toString());
//                                    JSONObject jsonObject = new JSONObject(response.toString());
//                                    String translatedText = response.getString("translatedText");
//                                    textview.setText(translatedText);
//
//                                    ///String sourceLanguage = response.getString("detectedSourceLanguage");
//                                    Log.d("translateText()", "get response: " + translatedText + " detectedSourceLanguage: " + sourceLanguage);
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        },
//                        null) {
//
//                    @Override
//                    public Map<String, String> getHeaders() throws AuthFailureError {
//                        HashMap<String, String> headers = new HashMap<String, String>();
//                        headers.put("x-api-key", "CAV5I2RcCC4oatF4UcHQj6h74a3rMjSf65js6iZr");
//                        headers.put("Content-Type", "application/json");
//                        return headers;
//                    }
//                };
//
//                output = getTranslatedText();
//
//                translatedText = "";

            ///END

        }

        @Override
        public void onNothingSelected(AdapterView<?> parentView) {
            // your code here
        }

    });


    ///END
}

    public void onTranslateClick(View view) {
        final EditText inputTextEditText = (EditText) findViewById(R.id.translationInputText);
        final TextView outputTexttextview = (TextView) findViewById(R.id.translationTextView);
        outputTexttextview.setMovementMethod(new ScrollingMovementMethod());


        String inputText ="";
        output ="";


        ///START
        sourceLanguage = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("sourcelanguage", "en");

        languages = getResources().getStringArray(R.array.Languages);
        displayLanguages = getResources().getStringArray(R.array.displayLanguages);
        languageName = new ArrayList<>();
        languageIso = new ArrayList<>();

        for (String s: languages) {
            String temp[];
            temp = s.split(" ");
            languageName.add(temp[0]);
            languageIso.add(temp[1]);
        }
        displayLanguages = getResources().getStringArray(R.array.displayLanguages);
        String text = inputTextEditText.getText().toString();
        output = translateMessageText2(text,langselected, sourceLanguage);
        Log.d(TAG, "output " + getTranslatedText());
        outputTexttextview.setText(getTranslatedText());


        //when translation completes display
        translateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Log.d(TAG, "Translate  Handler ");
                Log.d("output ", getTranslatedText());
                outputTexttextview.setText(getTranslatedText());

            }
        };

    }

//    public void onTranslateClick(View view) {
//
//        final EditText inputTextEditText = (EditText) findViewById(R.id.translationInputText);
//        final TextView outputTexttextview = (TextView) findViewById(R.id.translationTextView);
//        outputTexttextview.setMovementMethod(new ScrollingMovementMethod());
//
//
//        String inputText ="";
//        output ="";
//
//
//        ///START
//        sourceLanguage = PreferenceManager.getDefaultSharedPreferences(this)
//                .getString("sourcelanguage", "en");
//
//        languages = getResources().getStringArray(R.array.Languages);
//
//        languageName = new ArrayList<>();
//        languageIso = new ArrayList<>();
//
//        for (String s: languages) {
//            String temp[];
//            temp = s.split(" ");
//            languageName.add(temp[0]);
//            languageIso.add(temp[1]);
//        }
//
//
//        Spinner spinner  = (Spinner) findViewById(R.id.spinner2);
//        //spinner.setBackgroundColor(Color.WHITE);
//        // Create an ArrayAdapter using the string array and a default spinner layout
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.Languages, android.R.layout.simple_spinner_item);
//        // Specify the layout to use when the list of choices appears
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        // Apply the adapter to the spinner
//        spinner.setAdapter(adapter);
//
//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
//                Toast.makeText(TranslationSpinner.this,parentView.getItemAtPosition(position).toString() ,Toast.LENGTH_SHORT).show();
//                Log.d("names ", languageName.get(position));
//                Log.d("iso ", languageIso.get(position));
//                langselected =languageIso.get(position);
//                String text = inputTextEditText.getText().toString();
//                output = translateMessageText2(text,langselected, sourceLanguage);
//                Log.d(TAG, "output " + getTranslatedText());
//                outputTexttextview.setText(getTranslatedText());
//
//
//                //when translation completes display
//                translateHandler = new Handler() {
//                    @Override
//                    public void handleMessage(Message msg) {
//                        Log.d(TAG, "Translate  Handler ");
//                        Log.d("output ", getTranslatedText());
//                        outputTexttextview.setText(getTranslatedText());
//
//                    }
//                };
//                //START
//
////                if(langselected.equals(sourceLanguage)){
////                    textview.setText(translatedText);
////                }
////
////                RequestQueue queue = Volley.newRequestQueue(mContext);
////                String url = "https://iueh1tvfn3.execute-api.us-west-2.amazonaws.com/tranlateenpointbeta/";
////                Log.d("translateText()", "");
////
////                JSONObject jsonBody = new JSONObject();
////                try {
////                    jsonBody.put("translatedText", text);
////                    jsonBody.put("targetLanguage", langselected);
////                    jsonBody.put("sourceLanguage", sourceLanguage);
////
////                } catch (JSONException e) {
////                    e.printStackTrace();
////                }
////                String requestBody = jsonBody.toString();
////                Log.d("RequestBody", requestBody);
////                //
////                JsonObjectRequest request = new JsonObjectRequest(
////                        Request.Method.POST,
////                        url,
////                        jsonBody,
////                        new Response.Listener<JSONObject>() {
////                            @Override
////                            public void onResponse(JSONObject response) {
////                                // Do something with response, i.e.
////                                Log.d("translateText", "response: " + response.toString());
////
////                                try {
////                                    Log.d("translateText", "response: " + response.toString());
////                                    JSONObject jsonObject = new JSONObject(response.toString());
////                                    String translatedText = response.getString("translatedText");
////                                    textview.setText(translatedText);
////
////                                    ///String sourceLanguage = response.getString("detectedSourceLanguage");
////                                    Log.d("translateText()", "get response: " + translatedText + " detectedSourceLanguage: " + sourceLanguage);
////                                } catch (JSONException e) {
////                                    e.printStackTrace();
////                                }
////                            }
////                        },
////                        null) {
////
////                    @Override
////                    public Map<String, String> getHeaders() throws AuthFailureError {
////                        HashMap<String, String> headers = new HashMap<String, String>();
////                        headers.put("x-api-key", "CAV5I2RcCC4oatF4UcHQj6h74a3rMjSf65js6iZr");
////                        headers.put("Content-Type", "application/json");
////                        return headers;
////                    }
////                };
////
////                output = getTranslatedText();
////
////                translatedText = "";
//
//                ///END
//
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parentView) {
//                // your code here
//            }
//
//        });
//
//
//        ///END
//
//    }


    public String translateMessageText1(String textIn, String toLanguageIn, String fromLanguageIn) {
        Log.d(TAG, "translateMessageText1()");
        final String text = textIn;
        final String toLanguage = toLanguageIn;
        final String fromLanguage = fromLanguageIn;
        output = "";

        if(toLanguage.equals(fromLanguage)){
            return text;
        }

        Runnable r = new Runnable() {
            @Override
            public void run() {

                final TextView textview = (TextView) findViewById(R.id.translationTextView);


                RequestQueue queue = Volley.newRequestQueue(mContext);
                String url = "https://iueh1tvfn3.execute-api.us-west-2.amazonaws.com/tranlateenpointbeta/";
                Log.d("translateText()", "");

                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("translatedText", text);
                    jsonBody.put("targetLanguage", toLanguage);
                    jsonBody.put("sourceLanguage", fromLanguage);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String requestBody = jsonBody.toString();
                Log.d("RequestBody", requestBody);
                //
                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST,
                        url,
                        jsonBody,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // Do something with response, i.e.
                                Log.d("translateText", "response: " + response.toString());

                                try {
                                    Log.d("translateText", "response: " + response.toString());
                                    JSONObject jsonObject = new JSONObject(response.toString());
                                    String translatedText = response.getString("translatedText");
                                    ///String sourceLanguage = response.getString("detectedSourceLanguage");
                                    setTranslatedText(translatedText);
                                    Log.d("translateText()", "get response: " + translatedText + " detectedSourceLanguage: " + sourceLanguage);
                                    textview.setText(translatedText);
                                    translateHandler.sendEmptyMessage(0);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        null) {

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        HashMap<String, String> headers = new HashMap<String, String>();
                        headers.put("x-api-key", "CAV5I2RcCC4oatF4UcHQj6h74a3rMjSf65js6iZr");
                        headers.put("Content-Type", "application/json");
                        return headers;
                    }
                };


            }
        };

        Thread t = new Thread(r);
        t.start();

        output = getTranslatedText();

        translatedText = "";
        return output;
    }


    public String translateMessageText2(String textIn, String toLanguageIn, String fromLanguageIn) {
        Log.d(TAG, "translateMessageText2()");
        final String text = textIn;
        final String toLanguage = toLanguageIn;
        final String fromLanguage = fromLanguageIn;

        if(toLanguage.equals(fromLanguage)){
            return text;
        }
        output = "";


        Runnable r = new Runnable() {
            @Override
            public void run() {
                RequestQueue queue = Volley.newRequestQueue(mContext);
                String url = "https://iueh1tvfn3.execute-api.us-west-2.amazonaws.com/tranlateenpointbeta/";
                Log.d("translateText()", "");

                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("translatedText", text);
                    jsonBody.put("targetLanguage", toLanguage);
                    jsonBody.put("sourceLanguage", fromLanguage);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String requestBody = jsonBody.toString();
                Log.d("RequestBody", requestBody);
                //

                RequestFuture<JSONObject> future = RequestFuture.newFuture();
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody, future, future){
                    @Override
                    public HashMap<String, String> getHeaders() {
                        HashMap<String, String> headers = new HashMap<>();
                        headers.put("x-api-key", "CAV5I2RcCC4oatF4UcHQj6h74a3rMjSf65js6iZr");
                        headers.put("Content-Type", "application/json");
                        //headers.put("Accept", "application/json");
                        return headers;
                    }
                };
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
                    ///String sourceLanguage = response.getString("detectedSourceLanguage");
                    setTranslatedText(translatedText);
                    Log.d("translateText()", "get response: " + translatedText + " detectedSourceLanguage: " + sourceLanguage);
                    translateHandler.sendEmptyMessage(0);
                } catch (ExecutionException e) {
                    // Do something with error, i.e.
                } catch (TimeoutException e) {
                    // Do something with timeout, i.e.
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                output = getTranslatedText();
                Log.d(TAG, "INSIDE TRANSLATE output: " + output);
                Log.d(TAG, "INSIDE TRANSLATE translatedText: " + translatedText);
                Log.d(TAG, "INSIDE TRANSLATE getTranslatedText: " + getTranslatedText());
            }
        };

        Thread t = new Thread(r);
        t.start();




        translatedText = "";
        return output;
    }

//    public void onTranslateClick(View view) {
//
//        EditText translateEditText = (EditText) findViewById(R.id.editText);
////        final TextView textview = (TextView) findViewById(R.id.translationTestView);
////        final Typeface huhTypeface = Typeface.createFromAsset(getAssets(), "arial-unicode-ms.ttf");
//        String inputText = translateEditText.getText().toString();
//        Log.d("translateText input", inputText);
//
//        translateText(inputText,"ja");
//
//    }



//    public void translateText(String text, String toLanguage) {
//
//        RequestQueue queue = Volley.newRequestQueue(this);
//        final TextView textview = (TextView) findViewById(R.id.translationTestView);
//        final Typeface huhTypeface = Typeface.createFromAsset(getAssets(), "arial-unicode-ms.ttf");
//        String url = "https://iueh1tvfn3.execute-api.us-west-2.amazonaws.com/tranlateenpointbeta/";
//        final String output = "";
//        Log.d("translateText()", "++++++++++++++++++++");
//
//        JSONObject jsonBody = new JSONObject();
//        try {
//            jsonBody.put("translatedText", text);
//            jsonBody.put("targetLanguage", toLanguage);
//            jsonBody.put("sourceLanguage", toLanguage);
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        String requestBody = jsonBody.toString();
//        Log.d("RequestBody", requestBody);
//        //
//
//        JsonObjectRequest jsObjRequest = new JsonObjectRequest
//                (Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
//
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        try {
//                            Log.d("translateText", "response: " + response.toString());
//                            JSONObject jsonObject = new JSONObject(response.toString());
//                            Log.d("translateText()", response.toString());
//
//                            //  String translatedText = jsonObject.getString("translatedText");
//                            // String tolanguage = jsonObject.get("targetLanguage").toString();
//                            //   String sourceLanguage = jsonObject.get("detectedSourceLanguage").toString();
//
//                            String translatedText = response.getString("translatedText");
//                            // String tolanguage = response.get("targetLanguage").toString();
//                            String sourceLanguage = response.getString("detectedSourceLanguage");
//
//                            Log.d("translateText()", "get response: " + translatedText + " detectedSourceLanguage: " + sourceLanguage);
//
//                            textview.setText(translatedText);
//
//                        } catch (JSONException e) {
//                            Log.d("Trans JSON ex: ", e.getMessage());
//                        }
//                        catch(Exception e){
//                            Log.d("Translation Exception ", e.getMessage());
//                            e.printStackTrace();
//                        }
//
//                    }
//                }, new Response.ErrorListener() {
//
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.i("Trans Exception ", error.getMessage());
//
//                    }
//                });
//        //
//
//        queue.add(jsObjRequest);
//
//        //return output;
//    }
//
//    public String translateMessageText(String text, String toLanguage) {
//
//        RequestQueue queue = Volley.newRequestQueue(this);
//        final TextView textview = (TextView) findViewById(R.id.translationTestView);
//        final Typeface huhTypeface = Typeface.createFromAsset(getAssets(), "arial-unicode-ms.ttf");
//        String url = "https://iueh1tvfn3.execute-api.us-west-2.amazonaws.com/tranlateenpointbeta/";
//        final String output = "";
//        Log.d("translateText()", "");
//
//        JSONObject jsonBody = new JSONObject();
//        try {
//            jsonBody.put("translatedText", text);
//            jsonBody.put("targetLanguage", toLanguage);
//            jsonBody.put("sourceLanguage", toLanguage);
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        String requestBody = jsonBody.toString();
//        Log.d("RequestBody", requestBody);
//        //
//
//        JsonObjectRequest jsObjRequest = new JsonObjectRequest
//                (Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
//
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        try {
//                            Log.d("translateText", "response: " + response.toString());
//                            JSONObject jsonObject = new JSONObject(response.toString());
//
//                            String translatedText = response.getString("translatedText");
//                            // String tolanguage = response.get("targetLanguage").toString();
//                            String sourceLanguage = response.getString("detectedSourceLanguage");
//                            setTranslatedText(translatedText);
//                            Log.d("translateText()", "get response: " + translatedText + " detectedSourceLanguage: " + sourceLanguage);
//
//                        } catch (JSONException e) {
//                            Log.d("Trans JSON ex: ", e.getMessage());
//                        }
//                        catch(Exception e){
//                            Log.d("Translation Exception ", e.getMessage());
//                            e.printStackTrace();
//                        }
//
//                    }
//                }, new Response.ErrorListener() {
//
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.i("Trans Exception ", error.getMessage());
//
//                    }
//                });
//        //
//
//        queue.add(jsObjRequest);
//
//        return getTranslatedText();
//    }
}