package com.indooratlas.android.sdk.examples;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.indooratlas.android.sdk.examples.AccountActivity.LoginActivity;
import com.indooratlas.android.sdk.examples.googlemaps.MapsActivity;
import com.indooratlas.android.sdk.examples.wayfinding.WayfindingOverlayActivity;

import java.util.ArrayList;
import java.util.Locale;

public class OutdoorActivity extends AppCompatActivity implements LocationListener {
    LocationManager locationManager;
    LocationListener locationListener;
    Context context;
    String provider,destination;
    Double latitude, longitude;
    boolean gps_enabled, network_enabled;
    Button b1, b2;
    TextToSpeech t1;
    private SpeechRecognizer speechRecognizer;
    private Intent speechIntent;
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outdoor_select);

        b1 = (Button) findViewById(R.id.buttonGuardian);
        b2 = (Button) findViewById(R.id.buttonBlind);


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        b1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), LoginActivity.class);
                startActivityForResult(myIntent, 0);
            }});

        b2.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    //text to voice and search in google
                    //Uri gmmIntentUri = Uri.parse("geo:0,0?q=restaurants");
                    /*
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                Uri.parse("https://www.google.com/maps"));
                        startActivity(intent);

                     */
                    //t1.speak("speak your destination", TextToSpeech.QUEUE_FLUSH, null);
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Do something after 1000ms
                            //take voice
                            initSpeechRecognizer();

                        }
                    }, 1000);


                }});

        }
    @Override
    public void onLocationChanged(Location location) {

        latitude=location.getLatitude();
        longitude=location.getLongitude();
    }
    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude","disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude","enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude","status");
    }
    private void initSpeechRecognizer() {

        // Create the speech recognizer and set the listener
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(recognitionListener);

        // Create the intent with ACTION_RECOGNIZE_SPEECH
        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US);

        listen();
    }


    public void listen() {

        // startListening should be called on Main thread
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable myRunnable = () -> speechRecognizer.startListening(speechIntent);
        mainHandler.post(myRunnable);
    }
    RecognitionListener recognitionListener=new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {

        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onError(int error) {
            Toast.makeText(OutdoorActivity.this,
                    "error "+error,
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResults(Bundle results) {
            Toast.makeText(OutdoorActivity.this,
                    results.toString(),
                    Toast.LENGTH_SHORT).show();
            ArrayList<String> myVoice = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            process(myVoice);
        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    };
    private void process(ArrayList<String> result){
        StringBuffer sb=new StringBuffer();
        for(String s:result)
        {
            sb.append(s+" ");
        }
        Toast.makeText(OutdoorActivity.this,
                "searching for "+result.get(0),
                Toast.LENGTH_SHORT).show();
        searchLocation(result.get(0));
    }
    private void searchLocation(String location){
        Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitude + "?q="+location);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

}

