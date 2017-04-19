package com.aaronfogarty.huh;

import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class SendRecieveLanguageActivity extends AppCompatActivity {
    ArrayList<String> languageName = null;
    ArrayList<String>  languageIso = null;
    String[] languages;
    private TextView baseLangselect;
    private TextView sourceLangSelect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_recieve_language);

        baseLangselect = (TextView) findViewById(R.id.recevelangtextView);
        sourceLangSelect = (TextView) findViewById(R.id.sendlangtextView);
        setLanguages();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setLanguages();
    }

    public void setLanguages(){

        String baseLanguage = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("language", "en");
        String sourceLanguage = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("sourcelanguage", "en");

        languages = getResources().getStringArray(R.array.Languages);

        languageName = new ArrayList<>();
        languageIso = new ArrayList<>();

        for (String s: languages) {
            String temp[];
            temp = s.split(" ");
            languageName.add(temp[0]);
            languageIso.add(temp[1]);
        }

        for(int i = 0; i < languageIso.size(); i++){

            if(baseLanguage.equals(languageIso.get(i))){
                baseLangselect.setText(languageName.get(i));
            }
        }

        for(int i = 0; i < languageIso.size(); i++){

            if(sourceLanguage.equals(languageIso.get(i))){
                sourceLangSelect.setText(languageName.get(i));
            }
        }
    }

    public void goToLanguages(View view){

        Intent intent = new Intent(this, LanguageSelectActivity.class);
        startActivity(intent);
    }

    public void goToSourceLanguages(View view){

        Intent intent = new Intent(this, SourceLanguageSelectActivity.class);
        startActivity(intent);
    }
}