package com.indooratlas.android.sdk.examples;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.indooratlas.android.sdk.examples.firebaseLocation.sendService;
import com.indooratlas.android.sdk.examples.wayfinding.WayfindingOverlayActivity;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;


/**
 * Main entry into IndoorAtlas examples. Displays all example activities as list and opens selected
 * activity. Activities are populated from AndroidManifest metadata.
 */
public class ListExamplesActivity extends AppCompatActivity {

    private static final String TAG = "IAExample";

    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
    Button b1, b2, b3;
    FloatingActionButton fb, cb;
    EditText input;
    String guardianName;
    AlertDialog.Builder builder;
    Context mContext;
    TextView gEmail, gNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        b1 = (Button) findViewById(R.id.buttonOutdoor);
        b2 = (Button) findViewById(R.id.buttonIndoor);
        fb = findViewById(R.id.floatButton);
        cb = findViewById(R.id.buttonCall);
        gEmail = findViewById(R.id.gEmail);
        gNumber = findViewById(R.id.gNumber);
        mContext = this;
        /*
        guardian.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                guardianName=s.toString();
                if(!runtime_permissions()&&!guardianName.matches("")) {
                    enableShare(); //add disable share when text is blank later

                }

            }
        });
        */

        b1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), OutdoorActivity.class);
                startActivityForResult(myIntent, 0);
            }

        });
        b2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), WayfindingOverlayActivity.class);
                startActivityForResult(myIntent, 0);
            }

        });

        cb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_DIAL);
                i.setData(Uri.parse("tel:"+gNumber.getText()));
                startActivity(i);
            }

        });
        fb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //create alertbox here
                builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Enter guardian name");
                input = new EditText(mContext);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setMessage("enter email");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        guardianName = input.getText().toString(); //guardianName is email
                        if (guardianName.matches("^(?!\\.)[0-9a-zA-Z\\.]+[0-9a-zA-Z]@(?!\\.)[0-9a-zA-Z\\.]+[0-9a-zA-Z]$")) {
                            Toast.makeText(ListExamplesActivity.this,
                                    "accepting" + guardianName,
                                    Toast.LENGTH_SHORT).show();

                            Intent i = new Intent(getApplicationContext(), sendService.class);
                            i.putExtra("guardian", guardianName);
                            startService(i);
                            //get SoS number
                            RequestQueue queue = Volley.newRequestQueue(mContext);
                            String url = "https://blindproject-2fe16.firebaseapp.com/api/v1/phone/" + guardianName;

                            // Request a string response from the provided URL.
                            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                                    new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            // set phoneNumber here
                                            gNumber.setText(response);
                                            gEmail.setText(guardianName);
                                        }
                                    }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        gNumber.setText("Account not yet created");
                                    }
                            });
                            queue.add(stringRequest);
                            // Add the request to the RequestQueue.
                            //later check for valid email
                        } else {
                            Toast.makeText(ListExamplesActivity.this,
                                    "enter valid email" + guardianName,
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                    //from here


                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }

        });

        if (!isSdkConfigured()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.configuration_incomplete_title)
                    .setMessage(R.string.configuration_incomplete_message)
                    .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).show();
            return;
        }

        ensurePermissions();
        /*
        Toast.makeText(ListExamplesActivity.this,
                "share enabled, guardian "+guardianName,
                Toast.LENGTH_SHORT).show();

        if(!runtime_permissions()&&!guardianName.matches("")) {
            Toast.makeText(ListExamplesActivity.this,
                    "share enabled, guardian ",
                    Toast.LENGTH_LONG).show();
            enableShare();

        }
        */
    }

    /**
     * Checks that we have access to required information, if not ask for users permission.
     */
    private void ensurePermissions() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // We dont have access to FINE_LOCATION (Required by Google Maps example)
            // IndoorAtlas SDK has minimum requirement of COARSE_LOCATION to enable WiFi scanning
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                new AlertDialog.Builder(this)
                        .setTitle(R.string.location_permission_request_title)
                        .setMessage(R.string.location_permission_request_rationale)
                        .setPositiveButton(R.string.permission_button_accept, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TAG, "request permissions");
                                ActivityCompat.requestPermissions(ListExamplesActivity.this,
                                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                                Manifest.permission.ACCESS_FINE_LOCATION},
                                        REQUEST_CODE_ACCESS_COARSE_LOCATION);
                            }
                        })
                        .setNegativeButton(R.string.permission_button_deny, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(ListExamplesActivity.this,
                                        R.string.location_permission_denied_message,
                                        Toast.LENGTH_LONG).show();
                            }
                        })
                        .show();

            } else {

                // ask user for permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_ACCESS_COARSE_LOCATION);

            }

        }
    }

    /*

    private void enableShare() {
        b3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent i =new Intent(getApplicationContext(),sendService.class);
                i.putExtra("guardian",guardianName);
                startService(i);
            }

        });

    }
    */
    private boolean runtime_permissions() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);

            return true;
        }
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case REQUEST_CODE_ACCESS_COARSE_LOCATION:

                if (grantResults.length == 0
                        || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, R.string.location_permission_denied_message,
                            Toast.LENGTH_LONG).show();
                }

                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, sendService.class));
        /*
        if(broadcastReceiver != null){
            unregisterReceiver(broadcastReceiver);
        }
        */
    }

    /**
     * Adapter for example activities.
     */


    private boolean isSdkConfigured() {
        return !"api-key-not-set".equals(getString(R.string.indooratlas_api_key))
                && !"api-secret-not-set".equals(getString(R.string.indooratlas_api_secret));
    }


}
