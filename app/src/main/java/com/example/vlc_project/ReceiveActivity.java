package com.example.vlc_project;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ReceiveActivity extends Activity {

    Button scanBtn;
    Button menuBtn;
    TextView textData;
    String prevMsg = "";

    @SuppressLint("SetTextI18n")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("MAINX", "addlist");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);

        textData = findViewById(R.id.chatText);
        scanBtn = (Button) findViewById(R.id.scanBtn);
        menuBtn = (Button) findViewById(R.id.menuBtn);
        Log.d("LOADEDE", "addlist");
        scanBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Log.d("FLASH", "Onlclick");
                Intent intent = new Intent(ReceiveActivity.this, CameraScreen.class);
                startActivity(intent);
            }
        });

        menuBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Log.d("FLASH", "Onlclick");
                Intent intent = new Intent(ReceiveActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        try {
            String data = getIntent().getStringExtra("key");
            if (data.length() > 0) {
                prevMsg = prevMsg + data + "\n";
                Log.d("MAINX", data);
                textData.setText(prevMsg);
            }
        } catch (Exception e) {
            Log.d("MAINX", "ERROR");
        }
    }
}