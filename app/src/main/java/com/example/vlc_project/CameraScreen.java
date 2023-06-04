package com.example.vlc_project;

import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.COLOR_RGBA2GRAY;
import static org.opencv.imgproc.Imgproc.RETR_TREE;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.findContours;
import static org.opencv.imgproc.Imgproc.threshold;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CameraScreen extends CameraActivity {

    CameraBridgeViewBase cameraBridgeViewBase;
    TextView resultTextView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        if (OpenCVLoader.initDebug()) Log.d("LOADED", "Success");
        else Log.d("LOADED", "Err");
        Log.d("FLASH", "OnCreate");

        getPermission();

        cameraBridgeViewBase = findViewById(R.id.cameraView);
        resultTextView = findViewById(R.id.resultTextView);
        cameraBridgeViewBase.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            private boolean isFlashlightOn = false;
            private int illuminatedFrameCount = 0;
            private int darkFrameCount = 0;
            private final StringBuilder morseCodeBuilder = new StringBuilder();
            private int spaceCount = 0;
            private List<String> morseCodeList = new ArrayList<>();
            private List<String> wordList = new ArrayList<>();

            @Override
            public void onCameraViewStarted(int width, int height) {
            }

            @Override
            public void onCameraViewStopped() {
            }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

                // Convert input frame to grayscale
                Mat gray = new Mat();
                cvtColor(inputFrame.rgba(), gray, COLOR_RGBA2GRAY);

                // Apply threshold to convert to binary image
                Mat binary = new Mat();
                threshold(gray, binary, 150, 255, THRESH_BINARY);

                // Find contours in the binary image
                List<MatOfPoint> contours = new ArrayList<>();

                Mat hierarchy = new Mat();
                findContours(binary, contours, hierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE);

                double maxArea = 0;

                // Iterate through all the detected contours
                for (int i = 0; i < contours.size(); i++) {
                    double area = contourArea(contours.get(i));
                    if (area > maxArea) {
                        maxArea = area;
                    }
                }

                // Adjust this threshold based on flashlight beam size
                if (maxArea > 500000) {
                    Log.d("developer", "Message: illuminatedFrameCount: " + illuminatedFrameCount);
                    illuminatedFrameCount++;
                    isFlashlightOn = true;
                    darkFrameCount = 0;
                    spaceCount = 0;
                } else {
                    if (isFlashlightOn) {
                        if (illuminatedFrameCount >= 3) {
                            morseCodeBuilder.append('-');
                            Log.d("developer", "Message: Appended - Dash && " + morseCodeBuilder.toString());
                        } else if (illuminatedFrameCount >= 1) {
                            morseCodeBuilder.append('.');
                            Log.d("developer", "Message: Appended . Dot && " + morseCodeBuilder.toString());
                        } else morseCodeBuilder.append("");
                        Log.d("developer", "Message: Appended nothing && " + morseCodeBuilder.toString());
                    } else {
                        if (!morseCodeBuilder.toString().equals("")) {
                            darkFrameCount++;
                            Log.d("developer", "Message: darkFrameCount: " + darkFrameCount);
                            if (darkFrameCount >= 8) {
                                spaceCount++;
                                Log.d("developer", "Message: spaceCount: " + spaceCount);
                                if (spaceCount >= 30) {
                                    morseCodeBuilder.append('_');
                                    Log.d("developer", "Message: Appended _ Underscore && " + morseCodeBuilder.toString());
                                    spaceCount = 0;
                                } else if (spaceCount == 1 && !morseCodeBuilder.toString().endsWith("_")) {
                                    morseCodeBuilder.append(' ');
                                    Log.d("developer", "Message: Appended  Space && " + morseCodeBuilder.toString());
                                    String[] characterList = morseCodeBuilder.toString().substring(0, morseCodeBuilder.length()).split(" ");
                                    Log.d("test", "Message: characterList " + Arrays.toString(characterList));
                                    List<String> decodedCharacterList = new ArrayList<>();
                                    for (String character : characterList) {
                                        if (character.contains("_")) decodedCharacterList.add(" ");
                                        decodedCharacterList.add(decodeMorseCode(character.replaceAll("_", " ")));
                                    }
                                    resultTextView.setText(String.join("", decodedCharacterList));
                                    Log.d("test", "Message: decodedCharacterList " + decodedCharacterList.toString());
                                }
                            }
                        }
                    }
                    illuminatedFrameCount = 0;
                    isFlashlightOn = false;
                }


                if (!isFlashlightOn) {
                    // Decode Morse code
                    String morseCode = morseCodeBuilder.toString().trim();
                    Log.d("test", "Test morseCode:" + morseCode);

                    //Check for end condition
                    if (morseCode.contains(" .-.-")) {
                        Log.d("test", "End Passed morseCode:" + morseCode);
                        //Check for start condition
                        if (morseCode.contains("-.-.- ")) {
                            Log.d("test", "Start Passed morseCode:" + morseCode);
                            morseCode = morseCode.substring(6, morseCode.length() - 5);
                            Log.d("developer", "Our Type morseCode modified:" + morseCode);
                            morseCodeList = Arrays.asList(morseCode.split("_"));
                            for (String morseCodeWord : morseCodeList) {
                                wordList.add(decodeMorseCode(morseCodeWord));
                            }
                            String message = String.join(" ", wordList);
                            Log.d("developer", "MorseCode Message: " + message);
                            resultTextView.setText(message);
                            Intent intent = new Intent(CameraScreen.this, ReceiveActivity.class);
                            intent.putExtra("key", message);
                            startActivity(intent);
                        } else {
                            resultTextView.setText("");
                        }
                        morseCodeBuilder.setLength(0);
                        morseCodeList = new ArrayList<>();
                    }
                }

                //Camera preview
                Mat rgba = inputFrame.rgba();
                Mat rotatedRgba = new Mat(rgba.cols(), rgba.rows(), CvType.CV_8UC4);
                Point center = new Point(rgba.cols() / 2, rgba.rows() / 2);
                double angle = -90.0;
                double scale = 1.0;
                Size rotatedSize = new Size(rgba.rows(), rgba.cols());
                Mat rotationMatrix = Imgproc.getRotationMatrix2D(center, angle, scale);
                Imgproc.warpAffine(rgba, rotatedRgba, rotationMatrix, rotatedSize);
                return rotatedRgba;
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraBridgeViewBase.enableView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraBridgeViewBase.disableView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraBridgeViewBase.disableView();
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(cameraBridgeViewBase);
    }

    void getPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 3);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 3 && grantResults.length > 0) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                getPermission();
            }
        }
    }

    private String decodeMorseCode(String morseCode) {
        char[] characters = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', ' '};
        String[] morseCodes = {".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....", "..", ".---", "-.-", ".-..", "--", "-.", "---", ".--.", "--.-", ".-.", "...", "-", "..-", "...-", ".--", "-..-", "-.--", "--..", " "};
        StringBuilder builder = new StringBuilder();
        String[] codeWords = morseCode.split(" ");
        for (String codeWord : codeWords) {
            int index = Arrays.asList(morseCodes).indexOf(codeWord);
            if (index != -1) {
                builder.append(characters[index]);
            }
        }
        return builder.toString();
    }
}
