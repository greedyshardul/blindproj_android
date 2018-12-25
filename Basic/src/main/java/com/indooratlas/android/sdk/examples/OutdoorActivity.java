package com.indooratlas.android.sdk.examples;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.indooratlas.android.sdk.examples.AccountActivity.LoginActivity;
import com.indooratlas.android.sdk.examples.googlemaps.MapsActivity;

public class OutdoorActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outdoor_select);

        Button b1 = (Button) findViewById(R.id.buttonGuardian);
        Button b2 = (Button) findViewById(R.id.buttonBlind);
        b1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), LoginActivity.class);
                startActivityForResult(myIntent, 0);
            }

        });
        b2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), MapsActivity.class);
                startActivityForResult(myIntent, 0);
            }

        });



    }
}
