package com.aaronfogarty.huh;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

//import com.google.gson.Gson;

public class TranslationSpinner extends AppCompatActivity {


    private Context context;
    String url;
    private String translatedText;

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
    }

//    public void onTranslateClick(View view) {
//
//        EditText translateEditText = (EditText) findViewById(R.id.editText);
//        final TextView textview = (TextView) findViewById(R.id.translationTestView);
//        final Typeface huhTypeface = Typeface.createFromAsset(getAssets(), "arial-unicode-ms.ttf");
//        Spinner spinner = (Spinner) findViewById(R.id.spinner2);
//        String langSelected = spinner.getSelectedItem().toString();
//        String translateLang = "";
//
//        if(langSelected.equals("Select a Language")){
//            translateLang = "en";
//        }else if(langSelected.equals("German")){
//            translateLang = "de";
//        }else if(langSelected.equals("French")){
//            translateLang = "fr";
//        }else if(langSelected.equals("Irish")) {
//            translateLang = "ga";
//        }
//
//        String inputText ="";
//
//      //  translateText("hello world", "French");
//
//        try {
//            inputText = URLEncoder.encode(translateEditText.getText().toString(), "UTF-8");
//            // translateLang = URLEncoder.encode(translateLang, "UTF-8");
//           // inputText =  inputText.replaceAll("%0A","+++");
//            Log.d("inputText: ", inputText);
//        }
//        catch (UnsupportedEncodingException i){
//            textview.setText(i.getMessage().toString());
//        }
//
//        RequestQueue queue = Volley.newRequestQueue(this);
//        String url = "https://an6e7or256.execute-api.us-west-2.amazonaws.com/test/huhtranslateapigateway?inputTextToTranslate="+inputText+"&inputTranslationLanguage="+translateLang;
//        // https://an6e7or256.execute-api.us-west-2.amazonaws.com/test/huhtranslateapigateway?inputTextToTranslate=hello%20world&inputTranslationLanguage=fr
//        //https://translation.googleapis.com/language/translate/v2?key=YOUR_API_KEY&source=en&target=de&q=Hello%20world&q=My%20name%20is%20Jeff
//
//        // Request a string response from the provided URL.
//
//        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
//
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        Log.d("Lambda Response check", "Checking Lambda Result");
//                        Log.i("Lamba Response:", response);
//                        TextView textview = (TextView) findViewById(R.id.translationTestView);
//                        textview.setTypeface(huhTypeface);
//                        String outputText = response.replaceAll("%0A", "\n");
//                        textview.setText(response);
//                        Log.d("outputText: ", outputText);
//
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                textview.setText(error.getMessage().toString());
//            }
//        }) {
//
//        };
//// Add the request to the RequestQueue.
//        queue.add(stringRequest);
//
//    }

    public void onTranslateClick(View view) {

        EditText translateEditText = (EditText) findViewById(R.id.editText);
//        final TextView textview = (TextView) findViewById(R.id.translationTestView);
//        final Typeface huhTypeface = Typeface.createFromAsset(getAssets(), "arial-unicode-ms.ttf");
        String inputText = translateEditText.getText().toString();
        Log.d("translateText input", inputText);

        translateText(inputText,"ja");

    }



    public void translateText(String text, String toLanguage) {

        RequestQueue queue = Volley.newRequestQueue(this);
        final TextView textview = (TextView) findViewById(R.id.translationTestView);
        final Typeface huhTypeface = Typeface.createFromAsset(getAssets(), "arial-unicode-ms.ttf");
        String url = "https://iueh1tvfn3.execute-api.us-west-2.amazonaws.com/tranlateenpointbeta/";
        final String output = "";
        Log.d("translateText()", "++++++++++++++++++++");

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
                            Log.d("translateText()", response.toString());

                            //  String translatedText = jsonObject.getString("translatedText");
                            // String tolanguage = jsonObject.get("targetLanguage").toString();
                            //   String sourceLanguage = jsonObject.get("detectedSourceLanguage").toString();

                            String translatedText = response.getString("translatedText");
                            // String tolanguage = response.get("targetLanguage").toString();
                            String sourceLanguage = response.getString("detectedSourceLanguage");

                            Log.d("translateText()", "get response: " + translatedText + " detectedSourceLanguage: " + sourceLanguage);

                            textview.setText(translatedText);

                        } catch (JSONException e) {
                            Log.d("Trans JSON ex: ", e.getMessage());
                        }
                        catch(Exception e){
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

        //return output;
    }

    public String translateMessageText(String text, String toLanguage) {

        RequestQueue queue = Volley.newRequestQueue(this);
        final TextView textview = (TextView) findViewById(R.id.translationTestView);
        final Typeface huhTypeface = Typeface.createFromAsset(getAssets(), "arial-unicode-ms.ttf");
        String url = "https://iueh1tvfn3.execute-api.us-west-2.amazonaws.com/tranlateenpointbeta/";
        final String output = "";
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
                        }
                        catch(Exception e){
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

        return getTranslatedText();
    }
}