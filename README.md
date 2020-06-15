# Touch Tap See: A Navigation Assistant for the Visually Impaired person
## Acknowledgements
Final year thesis by Shardul Aeer, Saket Gupta and Khyatee Thakkar. This project was possible with support from Prof. Deepti Patole, Prof. Sunayana Jadhav and KJ Somaiya College of Engineering. The codebase is forked from [IndoorAtlas/
android-sdk-examples](https://github.com/IndoorAtlas/android-sdk-examples).

## Technical summary
This app has 2 modules:
1. **Outdoor location tracking and SOS**:
    - The visually impaired person can pair his/her device with a guardian. The person's location is shared with the guardian in real time. Additionally, the person can press an *SOS button* to send an alert to the guardian.
    - Tech stack: App has a serverless Google Firebase backend
        1. **Firebase auth**: For managing guardian's account.
        2. **Firestore and firebase functions**: To store and retrieve user and location related data. The guardian pairs his device by searching for the visually impaired person's phone number.
    
2. **Indoor navigation**: Uses [IndoorAtlas](https://www.indooratlas.com/), an indoor navigation and positioning service.

    [![Watch the video](https://user-images.githubusercontent.com/49580849/84658941-480a0d80-af34-11ea-8b5c-448819ffc625.png)](https://youtu.be/jzoTyC7cZbE)
    
    [Watch demo on Youtube](https://youtu.be/jzoTyC7cZbE)

    1. Create account on IndoorAtlas website, upload floor plan and define navigable routes.
        ![image](https://user-images.githubusercontent.com/49580849/84659460-1180c280-af35-11ea-8dae-7a2fcd84f136.png)
        
    2. Train map data using [MapCreator2 app](https://play.google.com/store/apps/details?id=com.indooratlas.android.apps.jaywalker&hl=en_IN). It involves walking around the venue in presence of WiFi and Bluetooth beacon signals.
        ![image](https://user-images.githubusercontent.com/49580849/84659652-5d336c00-af35-11ea-867e-ccd651113b73.png)
        
    3. Integrate trained map in app using the IndoorAtlas SDK. The visually impaired can navigate to any of the pre-defined locations using voice commands.
    
        **Show current location**
        
        ![image](https://user-images.githubusercontent.com/49580849/84660807-05960000-af37-11ea-94c2-c13ab9d6bab2.png)
        
        
        **Search by voice**
        
        ![image](https://user-images.githubusercontent.com/49580849/84660818-0c247780-af37-11ea-8ed6-1a5688d7c603.png)
        
        
# Usability considerations
Care is taken to meet special needs of end users.
1. Large buttons covering half-sides of screen.
2. Google accessability suite: This is run along with the app. It narrates the button text. The button text explains where the buttons are placed, eg. ```guardian button on left``` and ```blind button on right```.
3. Indoor location search is done via voice. This is implemented using Android speech to text API.
4. Distance and direction of destination is conveyed to user via voice.

# Issues faced
1. **High network and battery usage**: Real time location data is pushed to server and read by guardian by polling the Firestore HTTP API. This is an inefficient approach. A two way protocol like ```Websockets``` or ```MQTT``` should have been used.
2. **Firebase function cold starts**
3. **Inaccurate voice to text processing**: A cloud based API should'be been used instead of the native android API.

# Finding distance and direction
Distance and direction must be conveyed to the user in audatory format. IndoorAtlas SDK returns direction data in [ENU coordinates](https://en.wikipedia.org/wiki/Local_tangent_plane_coordinates#Local_east,_north,_up_(ENU)_coordinates) or degrees.

![alt bearings](http://academic.brooklyn.cuny.edu/geology/leveson/core/graphics/mapgraphics/circ-360newsx.gif)

This is converted in a simple left-right-front-back format using the below algorithm:

```java
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
