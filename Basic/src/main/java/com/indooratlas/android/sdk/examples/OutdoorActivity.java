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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.indooratlas.android.sdk.examples.AccountActivity.LoginActivity;
import com.indooratlas.android.sdk.examples.googlemaps.MapsActivity;

public class OutdoorActivity extends AppCompatActivity implements LocationListener {
    LocationManager locationManager;
    LocationListener locationListener;
    Context context;
    String provider;
    Double latitude, longitude;
    boolean gps_enabled, network_enabled;
    Button b1, b2;
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
                    Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitude + "?q=directions to ghatkopar");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);


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

}

