package com.aaronfogarty.huh;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class SourceLanguageSelectActivity extends AppCompatActivity {

    ArrayList<String> languageName = null;
    ArrayList<String>  languageIso = null;
    String[] languages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_source_language_select);

        languages = getResources().getStringArray(R.array.Languages);

        languageName = new ArrayList<>();
        languageIso = new ArrayList<>();

        for (String s: languages) {
            String temp[];
            temp = s.split(" ");
            languageName.add(temp[0]);
            languageIso.add(temp[1]);
        }

        //converts languages array into list item
        ListAdapter languageAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, languageName);
        ListView languagelistview = (ListView) findViewById(R.id.sourcelanguagelistview);
        languagelistview.setAdapter(languageAdapter);

        languagelistview.setOnItemClickListener(
                new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String languageSelected = String.valueOf(parent.getItemAtPosition(position));
                        Toast.makeText(SourceLanguageSelectActivity.this,languageSelected,Toast.LENGTH_SHORT).show();
                        Log.d("names ", languageName.get(position));
                        Log.d("iso ", languageIso.get(position));
                        saveLanguageSelect(languageIso.get(position));
                    }
                }
        );
    }
    private void saveLanguageSelect(String language) {

        Log.d("Language select: ","saveLanguageSelect() called.");
        //Toast.makeText(getApplicationContext(), TAG + ": saveCredentialsAndLogin() called.", Toast.LENGTH_LONG).show();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit()
                .putString("sourcelanguage",language)
                .commit();

    }
}
