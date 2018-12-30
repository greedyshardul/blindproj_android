package com.indooratlas.android.sdk.examples.firebaseLocation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import static android.app.Service.START_STICKY;

public class sendService extends Service {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference notesCollectionRef = db.collection("shared_locations");
    Map<String, Object> blind = new HashMap<>();
    private LocationListener listener;
    private LocationManager locationManager;
    String TAG="sendService",guardian;
    int i=0;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //guardian=intent.getExtras().getString()

        if (intent !=null && intent.getExtras()!=null)
            guardian = intent.getExtras().getString("guardian");

        Toast.makeText(this, "in onBind, guardian "+guardian, Toast.LENGTH_LONG).show();
        return null;
    }
    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        if (intent !=null && intent.getExtras()!=null)
            guardian = intent.getExtras().getString("guardian");
        else
            guardian="not found";
        Toast.makeText(this, "in onStartCommand, guardian "+guardian, Toast.LENGTH_LONG).show();
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                blind.put("name","demo");
                blind.put("latitude",location.getLatitude());
                blind.put("longitude",location.getLongitude());

                db.collection("shared_locations").document(guardian)
                        .set(blind)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully written!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error writing document", e);
                            }
                        });


            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        //noinspection MissingPermission
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,3000,0,listener);
        return START_STICKY;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {


        Toast.makeText(this, "in onCreate, guardian "+guardian, Toast.LENGTH_LONG).show();
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                blind.put("name","demo");
                blind.put("latitude",location.getLatitude());
                blind.put("longitude",location.getLongitude());

                db.collection("shared_locations").document(guardian)
                        .set(blind)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully written!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error writing document", e);
                            }
                        });


            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        //noinspection MissingPermission
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,3000,0,listener);

    }

    /*
    @SuppressWarnings("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                blind.put("name","noob");
                blind.put("latitude",location.getLatitude());
                blind.put("longitude",location.getLongitude());
                db.collection("shared_locations").document("guardian")
                        .set(blind)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully written!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error writing document", e);
                            }
                        });
                Intent i = new Intent("location_update");
                i.putExtra("coordinates",location.getLongitude()+" "+location.getLatitude());
                sendBroadcast(i);

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);


        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,3000,0,listener);


        // Let it continue running until it is stopped.




        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        return START_STICKY;
    }
    */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }

}