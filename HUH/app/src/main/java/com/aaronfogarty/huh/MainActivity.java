package com.aaronfogarty.huh;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.RequestQueue;

public class MainActivity extends AppCompatActivity {

    EditText translateEditText;
    TextView textview;
    RequestQueue requestQueue;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.d(TAG, "onCreate()");

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

    public void goToTranslate(View view){

        Intent intent = new Intent(this, TranslationSpinner.class);
        startActivity(intent);
    }

    public void goToJIDLogin(View view){

        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
    }

    public void goToSendMessage(View view){

        Intent intent = new Intent(this, SendMessageActivity.class);
        startActivity(intent);
    }

    public void goToLogin(View view){

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        //finish();
    }

    public void goToContacts(View view){

        Intent intent = new Intent(this, ContactListActivity.class);
        startActivity(intent);
    }

    public void goToContactsListView(View view){

        Intent intent = new Intent(this, ContactsListActivity.class);
        startActivity(intent);
    }
    public void goToRegisterView(View view){

        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    public void goToLanguages(View view){

        Intent intent = new Intent(this, LanguageSelectActivity.class);
        startActivity(intent);
    }

    public void goToPersonView(View view){

        Intent intent = new Intent(this, PersonalityInsightsActivity.class);
        startActivity(intent);
    }
}