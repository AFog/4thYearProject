package com.aaronfogarty.huh;

import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    EditText translateEditText;
    TextView textview;
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

       /* FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
        try {
             inputText = URLEncoder.encode(translateEditText.getText().toString(), "UTF-8");
            // translateLang = URLEncoder.encode(translateLang, "UTF-8");
        }
        catch (UnsupportedEncodingException i){
             textview.setText(i.getMessage().toString());
        }

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://an6e7or256.execute-api.us-west-2.amazonaws.com/test/huhtranslateapigateway?inputTextToTranslate="+inputText+"&inputTranslationLanguage="+translateLang;
                     // https://an6e7or256.execute-api.us-west-2.amazonaws.com/test/huhtranslateapigateway?inputTextToTranslate=hello%20world&inputTranslationLanguage=fr
        // Request a string response from the provided URL.

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Lambda Response check", "Checking Lambda Result");
                        Log.i("Lamba Response:", response);
                        TextView textview = (TextView) findViewById(R.id.translationTestView);
                        textview.setTypeface(huhTypeface);
                        textview.setText(response);
                        //TODO response text to be displayed
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

    protected boolean isEmpty(EditText editText){
        return editText.getText().toString().trim().length() == 0;
}

}
