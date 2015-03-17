package sloev.ripple.activites;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sloev.ripple.R;
import sloev.ripple.chat.ChatListener;
import sloev.ripple.model.LocationData;
import sloev.ripple.util.ApplicationSingleton;
import sloev.ripple.util.DialogUtils;

import android.os.Handler;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.model.LatLngBounds;

import sloev.ripple.model.UserDataStructure;
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

    private Context context;
    private Resources resources;
    private GoogleMap googleMap;
    private Location lastLocation;
    //private Map<Marker, LocationData> storageMap = new HashMap<Marker, LocationData>();
    private Map<Integer, Marker> userMarkers = new HashMap<Integer, Marker>();
    private Marker myMarker;
    private DialogInterface.OnClickListener checkInPositiveButton;
    private DialogInterface.OnClickListener checkInNegativeButton;

    private EditText userField;


    Handler handler = new Handler();
    Runnable sendGpsRunnable;
    Runnable receiveGpsRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        dataholder = ApplicationSingleton.getDataHolder();
        dataholder.getPrivateChatManager().initChatListener();
        dataholder.getPrivateChatManager().addListener(this);

        sendGpsRunnable = new Runnable() {

            @Override
            public void run() {
                sendLocationToContacts();
                handler.postDelayed(this, 5000);
            }
        };
        focusToggle = (ToggleButton) findViewById(R.id.focusToggle);

        initGooglePlayStatus();
        findViewById(R.id.map_fragment).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (!mapLoaded){
                    focusToggle.setChecked(true);
                    focusCamera();
                }
                mapLoaded = true;
                handler.postDelayed(sendGpsRunnable, 0);
            }
        });

        userField = (EditText) findViewById(R.id.userField);

    }

    public void sendLocationToContacts() {
        try {
            for (UserDataStructure user : dataholder.getContacts()) {
                if (user.isEnabled() & myMarker != null) {
                    System.out.println("sending to user");
                    dataholder.getPrivateChatManager().newChat(user.getUserId());
                    dataholder.getPrivateChatManager().sendLatLng(user.getUserId(), myMarker.getPosition());
                } else {
                    System.out.println("not sending to user");
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
    public void gpsReceived(final int userId, final LatLng position) {

        receiveGpsRunnable = new Runnable() {
            @Override
            public void run() {
                {
                    System.out.println("gps RECEIVED:" + userId + " :" + position.latitude + ", " + position.longitude);
                    if (!dataholder.contactsContainsUser(userId)) {
                        UserDataStructure userData = new UserDataStructure(userId, true);
                        dataholder.addUserToContacts(userId, userData);
                        System.out.println("user now in contacts:");
                    }
                    UserDataStructure userData = dataholder.getUserData(userId);
                    if (!userData.hasMarker()) {
                        Marker marker = googleMap.addMarker(new MarkerOptions().position(position).icon(
                                BitmapDescriptorFactory.fromResource(R.drawable.map_marker_other)));
                        marker.setTitle(Integer.toString(userId));
                        userData.setMarker(marker);
                    }
                    userData.setPosition(position);
                    if (focusToggle.isChecked()) {
                        focusCamera();
                    }
                }
            }
        };

        handler.post(receiveGpsRunnable);

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
                        String message;
                        if (marker.equals(myMarker)) {
                            message = "It is me";//resources.getString(R.string.dlg_it_is_me);
                        } else {
                            UserDataStructure userData = dataholder.getUserData(Integer.parseInt(marker.getTitle()));
                            message = "user:" + userData.getUserId();
                        }
                        DialogUtils.showLong(context, message);
                        return false;
                    }
                });
            }
        }
    }

    private CameraUpdate getCameraUpdate(double maxZoom) {

        LatLngBounds bounds = adjustBoundsForMaxZoomLevel(maxZoom);
        int padding = 100; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        return cu;
    }

    private LatLngBounds adjustBoundsForMaxZoomLevel(double zoomN) {
        //based on http://stackoverflow.com/questions/15700808/setting-max-zoom-level-in-google-maps-android-api-v2
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (UserDataStructure userData : dataholder.getContacts()) {
            if (userData.isEnabled() & userData.hasMarker()) {
                builder.include(userData.getPosition());
            }
        }
        builder.include(myMarker.getPosition());
        LatLngBounds bounds = builder.build();

        LatLng sw = bounds.southwest;
        LatLng ne = bounds.northeast;
        double deltaLat = Math.abs(sw.latitude - ne.latitude);
        double deltaLon = Math.abs(sw.longitude - ne.longitude);

        if (deltaLat < zoomN) {
            sw = new LatLng(sw.latitude - (zoomN - deltaLat / 2), sw.longitude);
            ne = new LatLng(ne.latitude + (zoomN - deltaLat / 2), ne.longitude);
            bounds = new LatLngBounds(sw, ne);
        } else if (deltaLon < zoomN) {
            sw = new LatLng(sw.latitude, sw.longitude - (zoomN - deltaLon / 2));
            ne = new LatLng(ne.latitude, ne.longitude + (zoomN - deltaLon / 2));
            bounds = new LatLngBounds(sw, ne);
        }
        return bounds;
    }

    private void initLocationManager() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            onLocationChanged(location);
        }
        locationManager.requestLocationUpdates(provider, 20000, 0, this);//Constants.LOCATION_MIN_TIME, 0, this);
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
        lastLocation = location;
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);

        if (myMarker == null) {
            myMarker = googleMap.addMarker(new MarkerOptions().position(latLng).icon(
                    BitmapDescriptorFactory.fromResource(R.drawable.map_marker_my)));

            //googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        } else {
            myMarker.setPosition(latLng);
        }
        if (focusToggle.isChecked()) {
            focusCamera();
        }
    }

    public void focus(View v) {
        if (focusToggle.isChecked()) {
            if (mapLoaded) {
                focusCamera();
            }
        }
        //focusToggle.toggle();
    }

    private void focusCamera() {
        googleMap.animateCamera(getCameraUpdate(0.001));
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(sendGpsRunnable);
        handler.removeCallbacks(receiveGpsRunnable);
    }

    public void addUserToContacts(View v) {
        int userId = Integer.parseInt(userField.getText().toString());
        UserDataStructure userData = new UserDataStructure(userId, true);
        dataholder.addUserToContacts(userId, userData);
        System.out.println("user added to contacts");
    }
}
