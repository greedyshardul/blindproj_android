package com.indooratlas.android.sdk.examples.firebaseLocation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
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
    private LocationManager mLocationManager = null;
    String TAG="sendService";
    int i=0;
    private FusedLocationProviderClient mFusedLocationClient;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.


        blind.put("name","gawar");
        blind.put("latitude",70);
        blind.put("longitude",70);
        blind.put("count",i++);
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

        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }

}