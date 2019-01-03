package com.indooratlas.android.sdk.examples.wayfinding;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.Range;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.indooratlas.android.sdk.IALocation;
import com.indooratlas.android.sdk.IALocationListener;
import com.indooratlas.android.sdk.IALocationManager;
import com.indooratlas.android.sdk.IALocationRequest;
import com.indooratlas.android.sdk.IAOrientationListener;
import com.indooratlas.android.sdk.IAOrientationRequest;
import com.indooratlas.android.sdk.IARegion;
import com.indooratlas.android.sdk.IARoute;
import com.indooratlas.android.sdk.IAWayfindingListener;
import com.indooratlas.android.sdk.IAWayfindingRequest;
import com.indooratlas.android.sdk.examples.R;
import com.indooratlas.android.sdk.examples.SdkExample;
import com.indooratlas.android.sdk.resources.IAFloorPlan;
import com.indooratlas.android.sdk.resources.IALatLng;
import com.indooratlas.android.sdk.resources.IALocationListenerSupport;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

@SdkExample(description = R.string.example_wayfinding_description)
public class WayfindingOverlayActivity extends FragmentActivity
        implements GoogleMap.OnMapClickListener, OnMapReadyCallback {
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 42;

    private static final String TAG = "IndoorAtlasExample";

    /* used to decide when bitmap should be downscaled */
    private static final int MAX_DIMENSION = 2048;
    private static final int REQUEST_CODE = 0;
    private static String[] PERMISSIONS_AUDIO = {Manifest.permission.RECORD_AUDIO};
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    //Range<Integer> rStraight1=Range.closedOpen(335,360);
    //Range<Integer> rStraight2=Range.closedOpen(0,45);
    Range<Integer> rRight=Range.closedOpen(45,135);
    Range<Integer> rRightN=Range.closedOpen(-335,-225);
    Range<Integer> rBack=Range.closedOpen(135,225);
    Range<Integer> rBackN=Range.closedOpen(-225,-135);
    Range<Integer> rLeft=Range.closedOpen(225,335);
    Range<Integer> rLeftN=Range.closedOpen(-135,-45);
    private Button bSearch,bCancel;
    private Circle mCircle;
    private IARegion mOverlayFloorPlan = null;
    private GroundOverlay mGroundOverlay = null;
    private IALocationManager mIALocationManager;
    private Target mLoadTarget;
    private boolean mCameraPositionNeedsUpdating = true; // update on first location
    private Marker mDestinationMarker;
    private Marker mHeadingMarker;
    private ArrayList<Marker> markerList=new ArrayList<Marker>();
    private List<Polyline> mPolylines = new ArrayList<>();
    private TextToSpeech t1;
    private List<Integer> msgList=new ArrayList<>();
    private int count;
    private String dir,msg;
    private IARoute mCurrentRoute;
    private LatLng currentPos;
    private IAWayfindingRequest mWayfindingDestination;
    //speech recognition
    private SpeechRecognizer speechRecognizer;
    private Intent speechIntent;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Map<String,Object> locations;
    private IAWayfindingListener mWayfindingListener = new IAWayfindingListener() {


        @Override
        public void onWayfindingUpdate(IARoute route) {
            mCurrentRoute = route; //add legs here

            int delta=(int)(route.getLegs().get(count).getDirection()-mHeadingMarker.getRotation());
            double dist;
            dir="straight";
            if(rRight.contains(delta)||rRightN.contains(delta))
                dir="right";
            else if(rBack.contains(delta)||rBackN.contains(delta))
                dir="back";
            else if(rLeft.contains(delta)||rLeftN.contains(delta))
                dir="left";

            if (atLeg(route.getLegs().get(count))) {
                if(msgList.get(count)==0) {
                    dist=Math.round(route.getLegs().get(count).getLength()*100.0)/100.0; //round off dist
                    msg="You are at leg " + count + ". Travel" + dist + " metre " + dir;
                    showInfo("at leg " + count + ". travel" + dist + " " + dir+" delta="+delta);
                    //mCurrentRoute = null;
                    t1.speak(msg, TextToSpeech.QUEUE_FLUSH, null);
                    if (count < route.getLegs().size() - 2)
                        count++;
                    msgList.set(count,1);
                }
            }
            if (hasArrivedToDestination(route)) {
                // stop wayfinding
                showInfo("You're there!");
                mCurrentRoute = null;
                mWayfindingDestination = null;
                mIALocationManager.removeWayfindingUpdates();
            }
            updateRouteVisualization();
        }

    };

    private IAOrientationListener mOrientationListener = new IAOrientationListener() {
        @Override
        public void onHeadingChanged(long timestamp, double heading) {
            updateHeading(heading);
        }

        @Override
        public void onOrientationChange(long timestamp, double[] quaternion) {
            // we do not need full device orientation in this example, just the heading
        }
    };

    private int mFloor;

    private void showLocationCircle(LatLng center, double accuracyRadius) {
        if (mCircle == null) {
            // location can received before map is initialized, ignoring those updates
            if (mMap != null) {
                mCircle = mMap.addCircle(new CircleOptions()
                        .center(center)
                        .radius(accuracyRadius)
                        .fillColor(0x201681FB)
                        .strokeColor(0x500A78DD)
                        .zIndex(1.0f)
                        .visible(true)
                        .strokeWidth(5.0f));
                mHeadingMarker = mMap.addMarker(new MarkerOptions()
                        .position(center)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_blue_dot))
                        .anchor(0.5f, 0.5f)
                        .flat(true));
            }
        } else {
            // move existing markers position to received location
            mCircle.setCenter(center);
            mHeadingMarker.setPosition(center);
            mCircle.setRadius(accuracyRadius);
        }
    }

    private void updateHeading(double heading) {
        if (mHeadingMarker != null) {
            mHeadingMarker.setRotation((float)heading);
        }
    }

    /**
     * Listener that handles location change events.
     */
    private IALocationListener mListener = new IALocationListenerSupport() {

        /**
         * Location changed, move marker and camera position.
         */
        @Override
        public void onLocationChanged(IALocation location) {

            Log.d(TAG, "new location received with coordinates: " + location.getLatitude()
                    + "," + location.getLongitude());

            if (mMap == null) {
                // location received before map is initialized, ignoring update here
                return;
            }

            final LatLng center = new LatLng(location.getLatitude(), location.getLongitude());
            currentPos=center;
            final int newFloor = location.getFloorLevel();
            if (mFloor != newFloor) {
                updateRouteVisualization();
            }
            mFloor = newFloor;

            showLocationCircle(center, location.getAccuracy());

            // our camera position needs updating if location has significantly changed
            if (mCameraPositionNeedsUpdating) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(center, 25.5f)); //17.5f

                mCameraPositionNeedsUpdating = false;
            }
        }
    };

    /**
     * Listener that changes overlay if needed
     */
    private IARegion.Listener mRegionListener = new IARegion.Listener() {
        @Override
        public void onEnterRegion(IARegion region) {
            if (region.getType() == IARegion.TYPE_FLOOR_PLAN) {
                Log.d(TAG, "enter floor plan " + region.getId());
                mCameraPositionNeedsUpdating = true; // entering new fp, need to move camera
                if (mGroundOverlay != null) {
                    mGroundOverlay.remove();
                    mGroundOverlay = null;
                }
                mOverlayFloorPlan = region; // overlay will be this (unless error in loading)
                fetchFloorPlanBitmap(region.getFloorPlan());
            }
        }

        @Override
        public void onExitRegion(IARegion region) {
        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wayfinding_layout);

        // prevent the screen going to sleep while app is on foreground
        findViewById(android.R.id.content).setKeepScreenOn(true);

        // instantiate IALocationManager
        mIALocationManager = IALocationManager.create(this);

        // disable indoor-outdoor detection (assume we're indoors)
        mIALocationManager.lockIndoors(true);
        bSearch=findViewById(R.id.buttonSearch);
        bCancel=findViewById(R.id.buttonCancel);
        //add permission
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)
                !=getPackageManager().PERMISSION_GRANTED){
            requestAudioPermission();

        }
        else{
            Log.i(TAG,"AUDIO PERMISSION GRANTED");
        }


        bSearch.setOnClickListener((View v) -> {
                Toast.makeText(WayfindingOverlayActivity.this,
                        "searching in "+mOverlayFloorPlan.getName(),
                        Toast.LENGTH_SHORT).show();
                // search for point here, works!
            initSpeechRecognizer();

        });
        bCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(WayfindingOverlayActivity.this,
                        "cancel",
                        Toast.LENGTH_SHORT).show();
                //cancel route
                mCurrentRoute = null;
                mWayfindingDestination = null;
                mIALocationManager.removeWayfindingUpdates();
                //updateRouteVisualization();
                clearRouteVisualization();
            }
        });
        // Request GPS locations
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_ACCESS_FINE_LOCATION);
            return;
        }

        // Try to obtain the map from the SupportMapFragment.
        ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map))
                .getMapAsync(this);
        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });
        addPoints();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // remember to clean up after ourselves
        mIALocationManager.destroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // start receiving location updates & monitor region changes
        mIALocationManager.requestLocationUpdates(IALocationRequest.create(), mListener);
        mIALocationManager.registerRegionListener(mRegionListener);
        mIALocationManager.registerOrientationListener(
                // update if heading changes by 1 degrees or more
                new IAOrientationRequest(1, 0),
                mOrientationListener);

        if (mWayfindingDestination != null) {
            mIALocationManager.requestWayfindingUpdates(mWayfindingDestination, mWayfindingListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // unregister location & region changes
        mIALocationManager.removeLocationUpdates(mListener);
        mIALocationManager.unregisterRegionListener(mRegionListener);
        mIALocationManager.unregisterOrientationListener(mOrientationListener);

        if (mWayfindingDestination != null) {
            mIALocationManager.removeWayfindingUpdates();
        }
        if(t1 !=null){
            t1.stop();
            t1.shutdown();
        }
    }
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // do not show Google's outdoor location
        mMap.setMyLocationEnabled(false);
        mMap.setOnMapClickListener(this);
        addPoints();
    }

    /**
     * Sets bitmap of floor plan as ground overlay on Google Maps
     */
    private void setupGroundOverlay(IAFloorPlan floorPlan, Bitmap bitmap) {

        if (mGroundOverlay != null) {
            mGroundOverlay.remove();
        }

        if (mMap != null) {
            BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
            IALatLng iaLatLng = floorPlan.getCenter();
            LatLng center = new LatLng(iaLatLng.latitude, iaLatLng.longitude);
            GroundOverlayOptions fpOverlay = new GroundOverlayOptions()
                    .image(bitmapDescriptor)
                    .zIndex(0.0f)
                    .position(center, floorPlan.getWidthMeters(), floorPlan.getHeightMeters())
                    .bearing(floorPlan.getBearing());

            mGroundOverlay = mMap.addGroundOverlay(fpOverlay);
        }
    }

    /**
     * Download floor plan using Picasso library.
     */
    private void fetchFloorPlanBitmap(final IAFloorPlan floorPlan) {

        if (floorPlan == null) {
            Log.e(TAG, "null floor plan in fetchFloorPlanBitmap");
            return;
        }

        final String url = floorPlan.getUrl();
        Log.d(TAG, "loading floor plan bitmap from "+url);

        mLoadTarget = new Target() {

            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Log.d(TAG, "onBitmap loaded with dimensions: " + bitmap.getWidth() + "x"
                        + bitmap.getHeight());
                if (mOverlayFloorPlan != null && floorPlan.getId().equals(mOverlayFloorPlan.getId())) {
                    Log.d(TAG, "showing overlay");
                    setupGroundOverlay(floorPlan, bitmap);
                }
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                // N/A
            }

            @Override
            public void onBitmapFailed(Drawable placeHolderDrawable) {
                showInfo("Failed to load bitmap");
                mOverlayFloorPlan = null;
            }
        };

        RequestCreator request = Picasso.with(this).load(url);

        final int bitmapWidth = floorPlan.getBitmapWidth();
        final int bitmapHeight = floorPlan.getBitmapHeight();

        if (bitmapHeight > MAX_DIMENSION) {
            request.resize(0, MAX_DIMENSION);
        } else if (bitmapWidth > MAX_DIMENSION) {
            request.resize(MAX_DIMENSION, 0);
        }

        request.into(mLoadTarget);
    }

    private void showInfo(String text) {
        final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), text,
                Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.button_close, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    @Override
    public void onMapClick(LatLng point) {
        /*
        if (mMap != null) {
            count=0;
            msgList.clear();
            for(int i=0;i<10;i++)
                msgList.add(i,0);
            mWayfindingDestination = new IAWayfindingRequest.Builder()
                    .withFloor(mFloor)
                    .withLatitude(point.latitude)
                    .withLongitude(point.longitude)
                    .build();

            mIALocationManager.requestWayfindingUpdates(mWayfindingDestination, mWayfindingListener);

            if (mDestinationMarker == null) {
                mDestinationMarker = mMap.addMarker(new MarkerOptions()
                        .position(point)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            } else {
                mDestinationMarker.setPosition(point);
            }
            Log.d(TAG, "Set destination: (" + mWayfindingDestination.getLatitude() + ", " +
                    mWayfindingDestination.getLongitude() + "), floor=" +
                    mWayfindingDestination.getFloor());
        }
        */
    }

    private boolean hasArrivedToDestination(IARoute route) {
        // empty routes are only returned when there is a problem, for example,
        // missing or disconnected routing graph
        if (route.getLegs().size() == 0) {
            return false;
        }

        final double FINISH_THRESHOLD_METERS = 3.0; //8
        double routeLength = 0;
        for (IARoute.Leg leg : route.getLegs()) routeLength += leg.getLength();
        return routeLength < FINISH_THRESHOLD_METERS;
    }
    //to display initial
    private boolean hasStarted(IARoute route)
    {
        if (route.getLegs().size() == 0) {
            return false;
        }
        final double FINISH_THRESHOLD_METERS = 8.0;
        return route.getLegs().get(1).getLength()< FINISH_THRESHOLD_METERS;

    }
    //arrived at point
    private boolean atLeg(IARoute.Leg leg)
    {

        double threshold=8;
        return leg.getLength()<threshold;

    }

    /**
     * Clear the visualizations for the wayfinding paths
     */
    private void clearRouteVisualization() {
        for (Polyline pl : mPolylines) {
            pl.remove();
        }
        mPolylines.clear();
    }

    /**
     * Visualize the IndoorAtlas Wayfinding route on top of the Google Maps.
     */
    private void updateRouteVisualization() {

        clearRouteVisualization();

        if (mCurrentRoute == null) {
            return;
        }

        for (IARoute.Leg leg : mCurrentRoute.getLegs()) {

            if (leg.getEdgeIndex() == null) {
                // Legs without an edge index are, in practice, the last and first legs of the
                // route. They connect the destination or current location to the routing graph.
                // All other legs travel along the edges of the routing graph.

                // Omitting these "artificial edges" in visualization can improve the aesthetics
                // of the route. Alternatively, they could be visualized with dashed lines.
                continue;
            }

            PolylineOptions opt = new PolylineOptions();
            opt.add(new LatLng(leg.getBegin().getLatitude(), leg.getBegin().getLongitude()));
            opt.add(new LatLng(leg.getEnd().getLatitude(), leg.getEnd().getLongitude()));

            // Here wayfinding path in different floor than current location is visualized in
            // a semi-transparent color
            if (leg.getBegin().getFloor() == mFloor && leg.getEnd().getFloor() == mFloor) {
                opt.color(0xFF0000FF);
            } else {
                opt.color(0x300000FF);
            }

            mPolylines.add(mMap.addPolyline(opt));

        }

    }
    //speech recognition
    private void requestAudioPermission(){
        Log.i(TAG,"not granted, requesting");
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.RECORD_AUDIO)){
            Snackbar.make(findViewById(android.R.id.content),"give permission",Snackbar.LENGTH_INDEFINITE)
                    .setAction("ok", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(WayfindingOverlayActivity.this,
                                    new String[]{Manifest.permission.RECORD_AUDIO},REQUEST_CODE );
                        }
                    })
                    .show();
        }
        else
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},REQUEST_CODE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE) {
            // BEGIN_INCLUDE(permission_result)
            // Received permission result for camera permission.
            Log.i(TAG, "Received response for permission request.");

            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission has been granted, preview can be displayed
                Log.i(TAG, "audio permission has now been granted. Showing preview.");
                Snackbar.make(findViewById(android.R.id.content), "audio available",
                        Snackbar.LENGTH_SHORT).show();
            } else {
                Log.i(TAG, "CAMERA permission was NOT granted.");
                Snackbar.make(findViewById(android.R.id.content), "not granted",
                        Snackbar.LENGTH_SHORT).show();

            }
            // END_INCLUDE(permission_result)



        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
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
            Toast.makeText(WayfindingOverlayActivity.this,
                    "error "+error,
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResults(Bundle results) {
            Toast.makeText(WayfindingOverlayActivity.this,
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
    private void process(ArrayList<String> result){
        StringBuffer sb=new StringBuffer();
        for(String s:result)
        {
            sb.append(s+" ");
        }
        Toast.makeText(WayfindingOverlayActivity.this,
                "searching for "+result.get(0),
                Toast.LENGTH_SHORT).show();
        searchLocation(result.get(0));
    }
    private void searchLocation(String result) {
        //add points in oncreate and search later
        for(Marker m:markerList){
            if(m.getTitle().equals(result)){
                Toast.makeText(WayfindingOverlayActivity.this,
                        "found "+m.getTitle(),
                        Toast.LENGTH_SHORT).show();
                LatLng pt=new LatLng(m.getPosition().latitude,m.getPosition().longitude);

                /** add listener **/
                if (mMap != null) {
                    count=0;
                    msgList.clear();
                    for(int i=0;i<10;i++)
                        msgList.add(i,0);
                    LatLng point=pt;
                    mWayfindingDestination = new IAWayfindingRequest.Builder()
                            .withFloor(mFloor)
                            .withLatitude(point.latitude)
                            .withLongitude(point.longitude)
                            .build();

                    mIALocationManager.requestWayfindingUpdates(mWayfindingDestination, mWayfindingListener);

                    if (mDestinationMarker == null) {
                        mDestinationMarker = mMap.addMarker(new MarkerOptions()
                                .position(point)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    } else {
                        mDestinationMarker.setPosition(point);
                    }
                    Log.d(TAG, "Set destination: (" + mWayfindingDestination.getLatitude() + ", " +
                            mWayfindingDestination.getLongitude() + "), floor=" +
                            mWayfindingDestination.getFloor());
                }
                //end part

                return;
            }


        }
        Toast.makeText(WayfindingOverlayActivity.this,
                "not found",
                Toast.LENGTH_SHORT).show();

    }

    private void addPoints() {
        DocumentReference docRef = db.collection("locations").document("home");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getId());
                        locations=document.getData();
                        for(Map.Entry<String,Object> entry:locations.entrySet()){
                            Log.d(TAG,entry.getKey()+":"+entry.getValue());
                            GeoPoint g=(GeoPoint) entry.getValue();
                            Log.d(TAG,"latitude extracted: "+g.getLatitude());
                            LatLng extracted=new LatLng(g.getLatitude(),g.getLongitude());
                            Marker m=mMap.addMarker(new MarkerOptions().position(extracted).title(entry.getKey()));
                            if(m!=null)
                                markerList.add(m);

                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }


            }
        });
    }

}
