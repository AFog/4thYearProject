package com.huh;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.mysampleapp.R;

public class ConnectedActivity extends AppCompatActivity {
    private static final String TAG = "ConnectedActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected);

        TextView textview = (TextView) findViewById(R.id.ConnectTestView);
        textview.setText("Connected!!");
        Log.d(TAG,"Connected with server ***************************************************");

    }
}
