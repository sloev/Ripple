package sloev.ripple.fragments;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import sloev.ripple.R;
import sloev.ripple.model.UserDataStructure;
import sloev.ripple.util.ApplicationSingleton;
import sloev.ripple.util.MapCamera;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapViewFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 * inspired by:
 * http://stackoverflow.com/questions/15098878/using-locationlistener-within-a-fragment
 */
public class MapViewFragment extends SupportMapFragment implements LocationListener {
    private Handler handler = null;
    private UserDataStructure signedInUserData;
    private ApplicationSingleton dataholder;
    public boolean autofocusEnabled = true;

    private int gpsRefreshRateMs = 15000;
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
                            googleMap.addMarker(userData.getMarkerOptions());
                        }
                    }
                    if (autofocusEnabled) {
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


    public MapViewFragment() {
        super();
    }

    public static MapViewFragment newInstance() {
        MapViewFragment frag = new MapViewFragment();
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        View v = super.onCreateView(inflater, viewGroup, bundle);
        dataholder = ApplicationSingleton.getDataHolder();
        camera = new MapCamera();
        signedInUserData = dataholder.getSignInUserData();

        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
// Showing status
        if (status == ConnectionResult.SUCCESS) {
            {
                initLocationManager();
            }
        }


        initMap();
        return v;
    }

    private void initMap() {
        /*
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(mPosition, 15));

        getMap().addMarker(new MarkerOptions().position(mPosition));
        getMap().getUiSettings().setAllGesturesEnabled(true);
        getMap().getUiSettings().setCompassEnabled(true);
        */
    }

    private void focusCamera() {
        if (handler != null) {
            getMap().animateCamera(camera.zoomIn());//default zoom=0.001
        } else {
            System.out.println("cant focus/zoom since lacking gps self");
        }
    }

    public void sendLocationToContacts() {
        if (!signedInUserData.hasGpsFix()) {
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

    private void initLocationManager() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        //LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(provider);
        locationManager.requestLocationUpdates(provider, gpsRefreshRateMs, 0, this);//Constants.LOCATION_MIN_TIME, 0, this);
    }


    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng position = new LatLng(latitude, longitude);

        UserDataStructure userData = dataholder.getSignInUserData();
        userData.setPosition(position);
        if (handler == null) {
            handler = new Handler();
            handler.postDelayed(sendGpsRunnable, 0); //TODO: change so transmission only occurs if there is any friends to transmit to
        }
        locationsUpdate();
    }

    public void locationsUpdate() {
        if (handler != null) {
            handler.post(locationsUpdatedRunnable);
        }
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
}