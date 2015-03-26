package sloev.ripple.activites;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;


import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;



import sloev.ripple.R;
import sloev.ripple.chat.ChatListener;
import sloev.ripple.util.ApplicationSingleton;
import sloev.ripple.util.DialogUtils;

import android.os.Handler;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import sloev.ripple.model.UserDataStructure;
import sloev.ripple.util.MapCamera;
/*
* based on http://stackoverflow.com/questions/15700808/setting-max-zoom-level-in-google-maps-android-api-v2
* and location example from quickblox sdk
*
*
* mulige løsninger på trouble in mainthread:
* måske har dte noget at gøre med at google maps bliver tilføjet markers fra privatechatmanagers inititativ
* måske er det noget med at jeg kører en timer ogen handler.
*
 */

public class MapActivity extends ActionBarActivity implements ChatListener, LocationListener {
    Button button;
    ApplicationSingleton dataholder;

    ToggleButton focusToggle;
    boolean mapLoaded = false;
    int gpsRefreshRateMs = 15000;

    private Context context;
    private Resources resources;
    private GoogleMap googleMap;

    private EditText userField;
    private TextView useridField;

    private boolean gotFirstFix = false;


    Handler handler = new Handler();
    ViewTreeObserver.OnGlobalLayoutListener mapLoadedObserver;

    MapCamera camera;

    Runnable locationsUpdatedRunnable = new Runnable() {
        @Override
        public void run() {
            {
                if(mapLoaded){
                googleMap.clear();
                for (UserDataStructure userData : dataholder.getContacts()) {
                    if(userData.hasGpsFix()) {
                        Marker marker = googleMap.addMarker(userData.getMarkerOptions());
                    }
                }
                if (focusToggle.isChecked()) {
                    focusCamera();
                }
            }
            }
        }
    };
    Runnable sendGpsRunnable = new Runnable() {

        @Override
        public void run() {
            sendLocationToContacts();
            handler.postDelayed(this, gpsRefreshRateMs);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        dataholder = ApplicationSingleton.getDataHolder();
        dataholder.getPrivateChatManager().initChatListener();
        dataholder.getPrivateChatManager().addListener(this);
        camera = new MapCamera();


        focusToggle = (ToggleButton) findViewById(R.id.focusToggle);
        userField = (EditText) findViewById(R.id.userField);
        useridField = (TextView) findViewById(R.id.userIdField);
        useridField.setText(Integer.toString(dataholder.getSignInUserId()));


        initGooglePlayStatus();
        mapLoadedObserver = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mapLoaded = true;
                findViewById(R.id.map_fragment).getViewTreeObserver().removeOnGlobalLayoutListener(mapLoadedObserver);
            }
        };
        findViewById(R.id.map_fragment).getViewTreeObserver().addOnGlobalLayoutListener(mapLoadedObserver);


    }

    public void sendLocationToContacts() {
        if(!dataholder.getSignInUserData().hasGpsFix()){
            return;
        }
        try {
            LatLng myPosition = dataholder.getSignInUserData().getPosition();


            for (UserDataStructure userData : dataholder.getContacts()) {
                int userId = userData.getUserId();

                if (userData.isSignInUser()) {
                    System.out.println(String.format("not sending to user:%d", userId));
                    continue;
                } else {
                    LatLng position = userData.getPosition();
                    System.out.println(String.format("sending to user:%d", userId));
                    dataholder.getPrivateChatManager().newChat(userId);
                    dataholder.getPrivateChatManager().sendLatLng(userId, myPosition);
                }
            }
        } catch (XMPPException e1) {
            e1.printStackTrace();
        } catch (SmackException.NotConnectedException e1) {
            e1.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void locationsUpdate() {
        handler.post(locationsUpdatedRunnable);
    }

    private void initGooglePlayStatus() {
        // Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

        // Showing status
        if (status != ConnectionResult.SUCCESS) {
            // Google Play Services are not available
            int requestCode = 10;//Constants.PLAY_SERVICE_REQUEST_CODE;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();
        } else {
            // Google Play Services are available
            // Init Map
            setUpMapIfNeeded();
            initLocationManager();
        }
    }

    private void setUpMapIfNeeded() {
        if (googleMap == null) {
            googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment)).getMap();
            if (googleMap != null) {
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        UserDataStructure userData = dataholder.getUserData(Integer.parseInt(marker.getTitle()));
                        DialogUtils.showLong(context, userData.getSnippet());
                        return false;
                    }
                });
            }
        }
    }

    private void initLocationManager() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            onLocationChanged(location);
        }
        locationManager.requestLocationUpdates(provider, gpsRefreshRateMs, 0, this);//Constants.LOCATION_MIN_TIME, 0, this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng position = new LatLng(latitude, longitude);

        UserDataStructure userData = dataholder.getSignInUserData();
        userData.setPosition(position);
        if (!gotFirstFix) {
            gotFirstFix = true;
            handler.postDelayed(sendGpsRunnable, 0); //TODO: change so transmission only occurs if there is any friends to transmit to
        }
        locationsUpdate();

    }

    public void focus(View v) {
        if (focusToggle.isChecked()) {
            if (mapLoaded) {
                focusCamera();
            }
        }
    }

    private void focusCamera() {
        if (gotFirstFix) {
            googleMap.animateCamera(camera.zoomIn());//default zoom=0.001
        } else {
            System.out.println("cant focus/zoom since lacking gps self");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(sendGpsRunnable);
    }

    public void addUserToContacts(View v) {
        int userId = Integer.parseInt(userField.getText().toString());
        UserDataStructure userData = new UserDataStructure(userId, true);
        dataholder.addUserToContacts(userId, userData);
        System.out.println("user added to contacts");
    }
}
