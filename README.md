# Touch Tap See : A Navigation Assistant for the Visually Impaired person
## IndoorAtlas SDK voice navigation with route distance and direction
Link to site: https://greedyshardul.github.io/blindproj_android/
<br>Technical paper<br>
<iframe src="https://drive.google.com/file/d/1SD3QEmIHCyNNTpRkzIDXoVqFM5E6Eaky/preview" width="640" height="480"></iframe>
<br><br>
[Technical paper](https://github.com/greedyshardul/blindproj_android/blob/master/ICACM_284.pdf)
<br>Forked from https://github.com/IndoorAtlas/android-sdk-examples

This app uses IARoute.Leg to find distance and direction between each leg and gives a voice message and notification. The following code is added in [WayfindingOverlayActivity.java](https://github.com/greedyshardul/android-sdk-examples/blob/master/Basic/src/main/java/com/indooratlas/android/sdk/examples/wayfinding/WayfindingOverlayActivity.java). 

## Direction calculation
IARoute.Leg.getDirection() returns direction in ENU coordinates or bearings. In simple words, North is 0 degree, East is 90. Marker.getRotation() gives direction in same format. We find the difference and use the below logic in left, right, back, straight. Some extra logic is needed for negative angle.

![alt bearings](http://academic.brooklyn.cuny.edu/geology/leveson/core/graphics/mapgraphics/circ-360newsx.gif)
 
```
@Override
        public void onWayfindingUpdate(IARoute route) {
            mCurrentRoute = route; //add legs here
            //find nearest leg
      
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
```
## angle to direction
I've used the Range function from the library [Guava](https://github.com/google/guava). To access this function add the following to your build.gradle
```
dependencies{
   ...
   compile 'com.google.guava:guava:27.0.1-android'
}
```
If delta falls within a range then that direction is set. Range for straight was not necessary.
```
    Range<Integer> rRight=Range.closedOpen(45,135);
    Range<Integer> rRightN=Range.closedOpen(-335,-225);
    Range<Integer> rBack=Range.closedOpen(135,225);
    Range<Integer> rBackN=Range.closedOpen(-225,-135);
    Range<Integer> rLeft=Range.closedOpen(225,335);
    Range<Integer> rLeftN=Range.closedOpen(-135,-45);
```

## arrival at a leg
I've set the threshold to 8m. If leg is within reach then it returns true. This function is called by onWayfindingUpdate() method above.
```
private boolean atLeg(IARoute.Leg leg)
    {
        /*
        IARoute.Point currentPt=leg.getEnd();
        LatLng pt=new LatLng(currentPt.getLatitude(),currentPt.getLongitude());
        return (mHeadingMarker.getPosition().equals(pt));
        */
        double threshold=8;
        return leg.getLength()<threshold;

    }
```
## getting distance
IARoute consists of legs having different distances and directions. However there is a start leg and end leg which effectively adds 2 ghost legs to your journey. Here I am considering the first leg till the N-2th leg. The second last leg is not needed as it will only give distance to the last ghost leg.
```
 if (count < route.getLegs().size() - 2)
                        count++;
```
I am keeping track of legs using count variable. It is defined as 0 in onMapClick method as follows.
```
@Override
    public void onMapClick(LatLng point) {
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
    }
```
## avoid multiple same messages
We have defined an integer list msgList to ensure a single message per leg.
```
private List<Integer> msgList=new ArrayList<>();
```
As a leg is reached, the corrosponding value is set to 1.
```
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
```
This msgList is cleared every time a new location is clicked on map, i.e onMapClick() function given above is called.

Setup istructions are same as below. Add your own IndoorAtlas keys. The code is ready to run.

-----------------------------------------------------------------------------------------------------------

[IndoorAtlas](https://www.indooratlas.com/) provides a unique Platform-as-a-Service (PaaS) solution that runs a disruptive geomagnetic positioning in its full-stack hybrid technology for accurately pinpointing a location inside a building. The IndoorAtlas SDK enables app developers to use high-accuracy indoor positioning in venues that have been fingerprinted.

This example app showcases the IndoorAtlas SDK features and acts as a reference implementation for many of the basic SDK features. Getting started requires you to set up a free developer account and fingerprint your indoor venue using the IndoorAtlas MapCreator tool.

There are also similar examples for iOS in [Objective-C](https://github.com/IndoorAtlas/ios-sdk-examples) and [Swift](https://github.com/IndoorAtlas/ios-sdk-swift-examples).

* [Getting Started](#getting-started)
    * [Set up your account](#set-up-your-account)
    * [Set up your API keys](#set-up-your-api-keys)    
* [Features](#features)
* [Documentation](#documentation)
* [SDK Changelog](#sdk-changelog)
* [License](#license)


## Getting Started

### Set up your account

* Set up your [free developer account](https://app.indooratlas.com) in the IndoorAtlas developer portal. Help with getting started is available in the [Quick Start Guide](http://docs.indooratlas.com/quick-start-guide.html).
* To enable IndoorAtlas indoor positioning in a venue, the venue needs to be fingerprinted with the [IndoorAtlas MapCreator 2](https://play.google.com/store/apps/details?id=com.indooratlas.android.apps.jaywalker) tool.
* To start developing your own app, create an [API key](https://app.indooratlas.com/apps).

### Set up your API keys

To run the examples you need to configure your IndoorAtlas API keys. If you do not have keys yet, go to https://app.indooratlas.com and sign up.

Once you have API keys, edit them into `gradle.properties` in the project root level.

## Examples


### Simple Example

* [Simple Example](https://github.com/IndoorAtlas/android-sdk-examples/tree/master/Basic/src/main/java/com/indooratlas/android/sdk/examples/simple): This is the hello world of IndoorAtlas SDK. Displays received location updates as log entries.

![](/example-screenshots/simple_01.jpg)


### Imageview Example

* [ImageView](https://github.com/IndoorAtlas/android-sdk-examples/tree/master/Basic/src/main/java/com/indooratlas/android/sdk/examples/imageview): Automatically downloads the floor plan that user has entered and displays it using Dave Morrissey's 
https://github.com/davemorrissey/subsampling-scale-image-view. This is a great library for handling large images!

![](/example-screenshots/imageview_02.jpg)


### Google Maps - Basic Example

* [Google Maps - Basic](https://github.com/IndoorAtlas/android-sdk-examples/tree/master/Basic/src/main/java/com/indooratlas/android/sdk/examples/googlemaps): This is the hello world of IndoorAtlas SDK + Google Map. Shows received locations on world map. Does not retrieve floor plans.

![](/example-screenshots/googlemaps&#32;-&#32;basic_03.jpg)


### Google Maps - Overlay Example

* [Google Maps](https://github.com/IndoorAtlas/android-sdk-examples/tree/master/Basic/src/main/java/com/indooratlas/android/sdk/examples/googlemapsindoor) - Overlay: Just like *Google Maps - Basic* but demonstrates how to place floor plan on world map by coordinates.

![](/example-screenshots/googlemaps&#32;-&#32;overlay_04.jpg)


### Open Street Map Overlay Example

* [Overlay with Open Street Map](https://github.com/IndoorAtlas/android-sdk-examples/tree/master/Basic/src/main/java/com/indooratlas/android/sdk/examples/osmdroid): Similar to Google maps examples, but uses Open Street Maps instead

![](/example-screenshots/open-street-map_08.jpg)


### Automatic Venue and Floor Detection Example

* [Automatic Venue and Floor Detection](https://github.com/IndoorAtlas/android-sdk-examples/tree/master/Basic/src/main/java/com/indooratlas/android/sdk/examples/regions): Demonstrates automatic region changes i.e. automatic venue detection and floor detection.

![](/example-screenshots/regions_07.jpg)


### Wayfinding Example

* [Wayfinding Example](https://github.com/IndoorAtlas/android-sdk-examples/blob/master/Basic/src/main/java/com/indooratlas/android/sdk/examples/wayfinding/WayfindingOverlayActivity.java#L260): In this example, a wayfinding graph json file is loaded. On the UI, you'll see your current location, and when you tap another point on the floorplan, you'll be shown a wayfinding route to that location. 

* Note: to setup, you need to draw a wayfinding graph for your venue using app.indooratlas.com and save it. Obviously you also need to fingerprint the venue and generate a map. 

![](/example-screenshots/wayfinding_12.jpg)


### Location Sharing aka "Find your friend" Example

* [Location sharing](https://github.com/IndoorAtlas/android-sdk-examples/tree/master/Basic/src/main/java/com/indooratlas/android/sdk/examples/sharelocation): Demonstrates sharing location via 3rd party cloud service. Can be used as an example of an multidot application.

![](/example-screenshots/sharelocation-05.jpg)


### Background Positioning Example

* [Background mode](https://github.com/IndoorAtlas/android-sdk-examples/tree/master/Basic/src/main/java/com/indooratlas/android/sdk/examples/background): Demonstrates running IndoorAtlas positioning in the background.

![](/example-screenshots/background-mode_08.jpg)


### Geofences Example

* [Geofences](https://github.com/IndoorAtlas/android-sdk-examples/tree/master/Basic/src/main/java/com/indooratlas/android/sdk/examples/geofence): Demonstrates how to set geofences and receive the geofence events.

![](/example-screenshots/geofences_10.jpg)


### Orientation Example

* [Orientation](https://github.com/IndoorAtlas/android-sdk-examples/tree/master/Basic/src/main/java/com/indooratlas/android/sdk/examples/orientation): Demonstrates IndoorAtlas 3D Orientation API.

![](/example-screenshots/orientation_09.jpg)


### Set Credentials from Code Example

* [Set credentials](https://github.com/IndoorAtlas/android-sdk-examples/tree/master/Basic/src/main/java/com/indooratlas/android/sdk/examples/credentials): Demonstrates how to set IndoorAtlas credentials from code in runtime.

![](/example-screenshots/set-credentials_06.jpg)


## Documentation

The IndoorAtlas SDK API documentation is available in the documentation portal: http://docs.indooratlas.com/android/

## SDK Changelog

http://docs.indooratlas.com/android/CHANGELOG.html

## License

Copyright 2015-2017 IndoorAtlas Ltd. The IndoorAtlas SDK Examples are released under the Apache License. See the [LICENSE.md](https://github.com/IndoorAtlas/android-sdk-examples/blob/master/LICENSE.md) file for details.


