package com.aaronfogarty.huh;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import static java.lang.Integer.parseInt;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.R.attr.value;

public class TranslationSpinner extends AppCompatActivity {


    private RequestQueue queue;
    private Context context;
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translation_spinner);
    }

    public void onTranslateClick(View view) {

        EditText translateEditText = (EditText) findViewById(R.id.editText);
        final TextView textview = (TextView) findViewById(R.id.translationTestView);
        final Typeface huhTypeface = Typeface.createFromAsset(getAssets(), "arial-unicode-ms.ttf");
        Spinner spinner = (Spinner) findViewById(R.id.spinner2);
        String langSelected = spinner.getSelectedItem().toString();
        String translateLang = "";

        if(langSelected.equals("Select a Language")){
            translateLang = "en";
        }else if(langSelected.equals("German")){
            translateLang = "de";
        }else if(langSelected.equals("French")){
            translateLang = "fr";
        }else if(langSelected.equals("Irish")) {
            translateLang = "ga";
        }

        String inputText ="";

      //  translateText("hello world", "French");

        try {
            inputText = URLEncoder.encode(translateEditText.getText().toString(), "UTF-8");
            // translateLang = URLEncoder.encode(translateLang, "UTF-8");
            //inputText =  inputText.replaceAll("%0A","+++");
            Log.d("inputText: ", inputText);
        }
        catch (UnsupportedEncodingException i){
            textview.setText(i.getMessage().toString());
        }

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://an6e7or256.execute-api.us-west-2.amazonaws.com/test/huhtranslateapigateway?inputTextToTranslate="+inputText+"&inputTranslationLanguage="+translateLang;
        // https://an6e7or256.execute-api.us-west-2.amazonaws.com/test/huhtranslateapigateway?inputTextToTranslate=hello%20world&inputTranslationLanguage=fr
        //https://translation.googleapis.com/language/translate/v2?key=YOUR_API_KEY&source=en&target=de&q=Hello%20world&q=My%20name%20is%20Jeff

        // Request a string response from the provided URL.

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Lambda Response check", "Checking Lambda Result");
                        Log.i("Lamba Response:", response);
                        TextView textview = (TextView) findViewById(R.id.translationTestView);
                        textview.setTypeface(huhTypeface);
                        String outputText = response.replaceAll("%0A", "\n");
                        textview.setText(response);
                        Log.d("outputText: ", outputText);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                textview.setText(error.getMessage().toString());
            }
        }) {

        };
// Add the request to the RequestQueue.
        queue.add(stringRequest);

    }


//    public void translateText(String text, String toLanguage)  {
//
//        final Typeface huhTypeface = Typeface.createFromAsset(getAssets(), "arial-unicode-ms.ttf");
//        String translateLang = "";
//        String inputText ="";
//
//        JSONObject jsonBody = new JSONObject();
//        try {
//            jsonBody.put("translateToLanguage", text);
//            jsonBody.put("textToTranslate", toLanguage);
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        String requestBody = jsonBody.toString();
//        Log.d("RequestBody", requestBody);
//
//        String url = "https://8ssr60mlih.execute-api.us-east-1.amazonaws.com/Test/getuserstatistics?username="+text;
//
//        JsonObjectRequest jsObjRequest = new JsonObjectRequest
//                (Request.Method.POST, url, new Response.Listener<JSONObject>() {
//
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        try {
//
//                            JSONObject jsonObject = new JSONObject(response.toString());
//                            String text = jsonObject.get("translatedText").toString();
//                            String language = jsonObject.get("translatedLanguage").toString();
//
//
//                        } catch (JSONException e) {
//                            Log.i("Trans JSON ex: ", e.getMessage());
//                        }
//                        catch(Exception e){
//                            Log.i("Trans Exception ", e.getMessage());
//                        }
//                    }
//                }
//                        , new Response.ErrorListener() {
//
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.i("TransText ","ERROR");
//                    }
//                }
//                );
//        queue.add(jsObjRequest);
//    }


}