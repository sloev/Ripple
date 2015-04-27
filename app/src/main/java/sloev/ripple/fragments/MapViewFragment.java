package sloev.ripple.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.util.HashMap;
import java.util.Map;

import sloev.ripple.R;
import sloev.ripple.chat.ChatListener;
import sloev.ripple.chat.MainActivityListener;
import sloev.ripple.model.UserDataStructure;
import sloev.ripple.util.ApplicationSingleton;
import sloev.ripple.util.MapCamera;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link MapViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 * inspired by:
 * http://stackoverflow.com/questions/15098878/using-locationlistener-within-a-fragment
 * og
 * http://michalu.eu/wordpress/android-mapfragment-nested-in-parent-fragment/
 * og
 * http://stackoverflow.com/questions/13728041/move-markers-in-google-map-v2-android
 * og
 * https://blog.codecentric.de/en/2014/05/android-gps-positioning-location-strategies/
 */
public class MapViewFragment extends SupportMapFragment implements LocationListener, MainActivityListener{
    public Handler handler = null;
    private Criteria criteria;
    private ApplicationSingleton dataholder = null;
    private Location old_location = null;

    public boolean autofocusEnabled = true;

    private int gpsRefreshRateMs = 1500;
    private long minDistance = 10; // Minimum distance change for update in meters, i.e. 10 meters.
    static final int TIME_DIFFERENCE_THRESHOLD = 10000;

    private MapCamera camera;


    Runnable locationsUpdatedRunnable = new Runnable() {
        @Override
        public void run() {
            {
                    GoogleMap googleMap = getMap();
                    if (googleMap != null) {
                        googleMap.clear();
                        for (UserDataStructure userData : dataholder.getContacts()) {
                            if (userData.hasGpsFix()) {
                                MarkerOptions marker_options = userData.getMarkerOptions(getActivity());

                                Marker marker = googleMap.addMarker(marker_options);
                            }
                        }
                        if (autofocusEnabled) {
                            try {
                                //googleMap.animateCamera(camera.zoomIn(), 500, null);//default zoom=0.001
                            }catch(IllegalStateException e){
                                System.out.println("error in camera");
                                System.out.println(e.getStackTrace());
                            }
                        }
                    }



                handler.postDelayed(this, 500);
            }

        }
    };
    private void update_markers(){
        GoogleMap googleMap = getMap();
        if (googleMap != null) {
            googleMap.clear();
            for (UserDataStructure userData : dataholder.getContacts()) {
                if (userData.hasGpsFix()) {
                    MarkerOptions marker_options = userData.getMarkerOptions(getActivity());

                    Marker marker = googleMap.addMarker(marker_options);
                    marker.setVisible(true);
                }
            }
        }
    }


    Runnable sendGpsRunnable = new Runnable() {

        @Override
        public void run() {
            sendLocationToContacts();
        }
    };


    public MapViewFragment() {
        super();
    }


    public static MapViewFragment newInstance() {
        MapViewFragment frag = new MapViewFragment();
        return frag;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        View v = super.onCreateView(inflater, viewGroup, bundle);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_map, menu);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        System.out.println("attaching map fragment");
        if (dataholder == null) {
            dataholder = ApplicationSingleton.getDataHolder();
        }

        if (camera == null) {
            camera = new MapCamera();
        }
        try {

            int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
            if (status == ConnectionResult.SUCCESS) {
                LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
                    /*if (get_old_location() == null) {
                        String provider = LocationManager.NETWORK_PROVIDER;
                        // Returns last known location, this is the fastest way to get a location fix.

                        set_old_location(locationManager.getLastKnownLocation(provider));
                    }*/

                criteria = new Criteria();
                criteria.setPowerRequirement(Criteria.POWER_LOW); // Chose your desired power consumption level.
                criteria.setAccuracy(Criteria.ACCURACY_FINE); // Choose your accuracy requirement.
                criteria.setSpeedRequired(true); // Chose if speed for first location fix is required.
                criteria.setAltitudeRequired(false); // Choose if you use altitude.
                criteria.setBearingRequired(false); // Choose if you use bearing.
                criteria.setCostAllowed(false); // Choose if this provider can waste money :-)

                // Provide your criteria and flag enabledOnly that tells
                // LocationManager only to return active providers.
                locationManager.getBestProvider(criteria, true);
                String provider = locationManager.getBestProvider(criteria, true);

                do_gps_work(locationManager.getLastKnownLocation(provider));

                locationManager.requestLocationUpdates(provider, gpsRefreshRateMs, 0, this);

            }

            /*
            mListener = (SignUpListener) activity;
            if (settings == null){
                settings = activity.getSharedPreferences(ApplicationSingleton.PREFS_NAME, 0);
            }*/
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    private void do_gps_work(Location location) {
        System.out.println("DO_GPS_WORK");
        if (location == null){
            System.out.println("location is null");
            return;
        }
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng position = new LatLng(latitude, longitude);

        try {

            UserDataStructure userData = dataholder.getSignInUserData();
            userData.setPosition(position);
            if (handler == null) {
                handler = new Handler();
                    handler.post(locationsUpdatedRunnable);
                System.out.println("send gps runnable");
                handler.postDelayed(sendGpsRunnable, 0); //TODO: change so transmission only occurs if there is any friends to transmit to
            }
            //locationsUpdate();
        } catch (NullPointerException e) {
            //LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            //locationManager.removeUpdates(this);
            System.out.println("no user is logged in");
        }

        set_old_location(location);
    }

    private void set_old_location(Location location) {
        old_location = location;
    }

    private Location get_old_location() {
        return old_location;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        System.out.println("detaching gps");
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        // Stop listening to location updates, also stops providers.
        locationManager.removeUpdates(this);
        handler.removeCallbacksAndMessages(null);
        handler = null;
        /*
        mListener = null;
        */
    }

    private void focusCamera() {
        if (handler != null) {
            getMap().animateCamera(camera.zoomIn(), 800, null);//default zoom=0.001
        } else {
            System.out.println("cant focus/zoom since lacking gps self");
        }
    }

    public void sendLocationToContacts() {
        System.out.println("sending locations");
        UserDataStructure signedInUserData = dataholder.getSignInUserData();

        if (signedInUserData == null) {
            return;
        }
        if (!signedInUserData.hasGpsFix()) {
            return;
        }
        try {

            LatLng myPosition = signedInUserData.getPosition();


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
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        handler.postDelayed(sendGpsRunnable, gpsRefreshRateMs); //TODO: change so transmission only occurs if there is any friends to transmit to

    }


    public void locationsUpdate() {
        System.out.println("locations update");

    }

    @Override
    public void onLocationChanged(Location location) {
        do_gps_work(location);
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
    public void autofocusEnabled(boolean enabled) {
        System.out.println("autofocus called in frag");
        autofocusEnabled = enabled;
    }
}