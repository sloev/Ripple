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


import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/*
* TODO: implement: http://stackoverflow.com/questions/15700808/setting-max-zoom-level-in-google-maps-android-api-v2
 */

public class MapActivity extends ActionBarActivity implements ChatListener, LocationListener {
    Button button;
    ApplicationSingleton dataholder;

    boolean mapLoaded = false;

    private Context context;
    private Resources resources;
    private GoogleMap googleMap;
    private Location lastLocation;
    private Map<Marker, LocationData> storageMap = new HashMap<Marker, LocationData>();
    private Marker myMarker;
    private DialogInterface.OnClickListener checkInPositiveButton;
    private DialogInterface.OnClickListener checkInNegativeButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        findViewById(R.id.map_fragment).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mapLoaded = true;
                //and write code, which you can see in answer above
            }
        });


        System.out.println("in map activity");
        button = (Button) findViewById(R.id.button);
        dataholder = ApplicationSingleton.getDataHolder();
        dataholder.getPrivateChatManager().initChatListener();

        initGooglePlayStatus();

    }
    public void send(View v){

        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setBody("hello world");
        chatMessage.setDateSent(new Date().getTime() / 1000);
        try {
            /*
            if (first) {
                first = false;

                dataholder.getPrivateChatManager().newChat(2526157);
                dataholder.getPrivateChatManager().addListener(this);
            }*/
            dataholder.getPrivateChatManager().sendMessage(2526157, chatMessage);
            System.out.println("message send");
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
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
    public void gpsReceived(int userId, float lat, float lon) {
        System.out.println("gpsRECEIVED" + userId + "," + lat + ", " + lon);
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
                            LocationData data = storageMap.get(marker);
                            message = "user:" + data.getUserName() +
                                    " status:" + (data
                                    .getUserStatus() != null ? data.getUserStatus() : "empty");
                        }
                        DialogUtils.showLong(context, message);
                        return false;
                    }
                });
            }
        }
    }
    public CameraUpdate getCameraUpdate() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : storageMap.keySet()) {
            builder.include(marker.getPosition());
        }
        builder.include(myMarker.getPosition());
        LatLngBounds bounds = builder.build();
        int padding = 100; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        return cu;
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

            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        } else {
            myMarker.setPosition(latLng);
        }
        if(mapLoaded) {

            googleMap.animateCamera(getCameraUpdate());
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(1));
        }
    }
}
