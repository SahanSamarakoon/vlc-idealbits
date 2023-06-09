package com.example.vlc_project;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class SendActivity extends AppCompatActivity {

    TextInputEditText textInputEditText;
    Button sendButton;
    TextView textView;
    ProgressBar progressBar;
    private CameraManager cameraManager;
    private String cameraId;
    private final int DOT_DURATION = 50; // Duration of a dot in milliseconds
    private final int DASH_DURATION = DOT_DURATION * 3; // Duration of a dash (3 dots)
    private final int SPACE_DURATION = DOT_DURATION * 7;

    private final int CHARACTER_DURATION = DOT_DURATION * 3;

    // Morse code mappings
    private char[] characters = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', ' '};
    private String[] morseCodes = {".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....", "..", ".---", "-.-", ".-..", "--", "-.", "---", ".--.", "--.-", ".-.", "...", "-", "..-", "...-", ".--", "-..-", "-.--", "--..", " "};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        textInputEditText = findViewById(R.id.textInputEditText);
        sendButton = findViewById(R.id.button);
        textView = findViewById(R.id.sendPreview);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        textView.setText("Enter Message to Send !");
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textView.setText("Sending data....");
                progressBar.setVisibility(View.VISIBLE);
                String data = String.valueOf(textInputEditText.getText());
                new SendDataAsyncTask().execute(data);
            }
        });
    }

    private class SendDataAsyncTask extends AsyncTask<String, Integer, Void> {
        @Override
        protected Void doInBackground(String... params) {
            String data = params[0];

            // Send data here and update progress
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //Convert characters to morse code
            StringBuilder morseData = new StringBuilder();
            for (int i = 0; i < data.length(); i++) {
                char c = Character.toLowerCase(data.charAt(i));
                String morseCode = getMorseCode(c);
                if (!morseCode.isEmpty()) {
                    morseData.append(morseCode).append(" ");
                    ;
                }
            }
            Log.d("developer", "morseData: " + morseData);
            String morseDataString = "-.-.- " + morseData + " .-.-";
            Log.d("developer", "Modified morseData: " + morseDataString);

            // Send morse code
            try {
                for (int i = 0; i < morseDataString.length(); i++) {
                    char c = morseDataString.charAt(i);
                    if (c == '.') {
                        turnFlashlightOn();
                        Thread.sleep(DOT_DURATION);
                        turnFlashlightOff();
                    } else if (c == '-') {
                        turnFlashlightOn();
                        Thread.sleep(DASH_DURATION);
                        turnFlashlightOff();
                    } else if (c == ' ') {
                        // Delay between words
                        Thread.sleep(SPACE_DURATION);
                    }
                    Thread.sleep(CHARACTER_DURATION);
                    publishProgress((i + 1) * 100 / morseData.length());
                }
                turnFlashlightOff();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Void result) {
            textView.setText("Data Sent Successfully!");
            progressBar.setVisibility(View.INVISIBLE);
            progressBar.setProgress(0);

            textView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    textView.setText("Enter Message to Send");
                }
            }, 5000);
        }
    }

    // Returns morse code for a character
    private String getMorseCode(char c) {
        for (int i = 0; i < characters.length; i++) {
            if (c == characters[i]) {
                return morseCodes[i];
            }
        }
        return ""; // Return empty string for unsupported characters
    }

    //Turn on flashlight
    private void turnFlashlightOn() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            }
            cameraId = cameraManager.getCameraIdList()[0]; // use the first camera
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraId, true);
            } else {
                Camera camera = Camera.open();
                Camera.Parameters parameters = camera.getParameters();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(parameters);
                camera.startPreview();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Turn off flashlight
    private void turnFlashlightOff() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraId, false);
            } else {
                Camera camera = Camera.open();
                Camera.Parameters parameters = camera.getParameters();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(parameters);
                camera.stopPreview();
                camera.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
