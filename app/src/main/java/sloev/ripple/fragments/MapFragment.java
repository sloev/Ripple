package sloev.ripple.fragments;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import sloev.ripple.R;
import sloev.ripple.model.UserDataStructure;
import sloev.ripple.util.ApplicationSingleton;
import sloev.ripple.util.MapCamera;


public class MapFragment extends Fragment implements LocationListener, GoogleMap.OnMarkerClickListener {
    public Handler handler = null;
    private Criteria criteria = null;

    private ApplicationSingleton dataholder = null;
    private Location old_location = null;
    public boolean autofocusEnabled = true;

    private MapCamera camera;

    private int gpsRefreshRateMs = 1500;


    MapView mapView;
    GoogleMap map;

    Runnable locationsUpdatedRunnable = new Runnable() {
        @Override
        public void run() {
            {
                UpdateAndSendLocaitons();
                handler.postDelayed(this, 500);
            }
        }
    };

    public MapFragment() {
        super();
    }


    public static MapFragment newInstance() {
        MapFragment frag = new MapFragment();
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, container, false);

        // Gets the MapView from the XML layout and creates it
        mapView = (MapView) v.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        map = mapView.getMap();
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setMyLocationEnabled(false);
        map.setOnMarkerClickListener(this);
        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        try {
            MapsInitializer.initialize(this.getActivity());
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }

       /* // Updates the location and zoom of the MapView
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(43.1, -87.9), 10);
        map.animateCamera(cameraUpdate);
*/
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        System.out.println("creating menu");
        inflater.inflate(R.menu.menu_map, menu);
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (dataholder == null) {
            dataholder = ApplicationSingleton.getDataHolder();
        }
        if (camera == null) {
            camera = new MapCamera();
        }
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (status == ConnectionResult.SUCCESS) {
            if (criteria == null) {
                criteria = new Criteria();
                criteria.setPowerRequirement(Criteria.POWER_LOW); // Chose your desired power consumption level.
                criteria.setAccuracy(Criteria.ACCURACY_FINE); // Choose your accuracy requirement.
                criteria.setSpeedRequired(true); // Chose if speed for first location fix is required.
                criteria.setAltitudeRequired(false); // Choose if you use altitude.
                criteria.setBearingRequired(false); // Choose if you use bearing.
                criteria.setCostAllowed(false); // Choose if this provider can waste money :-)
            }
            LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
            locationManager.getBestProvider(criteria, true);
            String provider = locationManager.getBestProvider(criteria, true);

            do_gps_work(locationManager.getLastKnownLocation(provider));

            locationManager.requestLocationUpdates(provider, gpsRefreshRateMs, 0, this);

        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        // Stop listening to location updates, also stops providers.
        locationManager.removeUpdates(this);
        handler.removeCallbacksAndMessages(null);
        handler = null;
    }

    public void UpdateAndSendLocaitons() {

        System.out.println("sending locations");
        LatLng myPosition = null;
        UserDataStructure signedInUserData = dataholder.getSignInUserData();
        if (map != null) {
            map.clear();
        }
        if (signedInUserData != null) {
            if (signedInUserData.hasGpsFix()) {
                myPosition = signedInUserData.getPosition();
            }
        }
        if (myPosition == null) {
            System.out.println("no my position");
            return;
        }

        for (UserDataStructure userData : dataholder.getContacts()) {
            int userId = userData.getUserId();

            LatLng position = userData.getPosition();
            System.out.println(String.format("sending to user:%d", userId));
            try {
                dataholder.getPrivateChatManager().newChat(userId);
                dataholder.getPrivateChatManager().sendLatLng(userId, myPosition);
                System.out.println("message send");
            } catch (XMPPException e2) {
                e2.printStackTrace();
            } catch (SmackException.NotConnectedException e2) {
                e2.printStackTrace();
            } catch (NullPointerException e3) {
                e3.printStackTrace();
            }
            if (!userData.isSignInUser()) {
            } else {
                System.out.println("not sending to self user");
            }
            if (userData.hasGpsFix()) {

                if (map != null) {
                    /*MarkerOptions marker_options = userData.getOldMarkerOptions();
                    if (marker_options == null) {
                    }*/
                    MarkerOptions marker_options = userData.getMarkerOptions(getActivity());

                    Marker marker = map.addMarker(marker_options);
                }
            }
        }
        if (autofocusEnabled) {
            try {
                map.animateCamera(camera.zoomIn(), 500, null);//default zoom=0.001
            } catch (IllegalStateException e) {
                System.out.println("error in camera");
                System.out.println(e.getStackTrace());
            }
        }
    }


    private void do_gps_work(Location location) {
        System.out.println("DO_GPS_WORK");
        if (location == null) {
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
            }
            //locationsUpdate();
        } catch (NullPointerException e) {
            //LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            //locationManager.removeUpdates(this);
            System.out.println("no user is logged in");
            System.out.println(e.getMessage());

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
    public void onLocationChanged(Location location) {
        do_gps_work(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(getActivity(), "marker clicked", Toast.LENGTH_SHORT).show();
        return true;
    }
}