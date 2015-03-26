package sloev.ripple.fragments;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import sloev.ripple.R;
import sloev.ripple.chat.ChatListener;
import sloev.ripple.model.UserDataStructure;
import sloev.ripple.util.ApplicationSingleton;
import sloev.ripple.util.DialogUtils;
import sloev.ripple.util.MapCamera;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyMapFragment extends Fragment implements ChatListener, LocationListener, ViewTreeObserver.OnGlobalLayoutListener, MyMapFragmentInterface{
    //constants
    int gpsRefreshRateMs = 15000;

    //stuff
    ApplicationSingleton dataholder;
    com.google.android.gms.maps.MapFragment FragmentWithMap;
    private Context context;
    private Resources resources;
    private GoogleMap googleMap;

    //gui
    View rootView;
    ToggleButton focusToggle;
    private EditText userField;
    private TextView useridField;
    Button button;

    //state vars
    private boolean mapLoaded = false;
    private boolean gotFirstFix = false;


    Handler handler = new Handler();

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


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     */
    public static MyMapFragment newInstance(String param1, String param2) {
        MyMapFragment fragment = new MyMapFragment();
        Bundle args = new Bundle();

        //args.putString(ARG_PARAM2, param2);
        //fragment.setArguments(args);
        return fragment;
    }

    public MyMapFragment() {
        // Required empty public constructor
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentManager fm = getChildFragmentManager();
        FragmentWithMap = (com.google.android.gms.maps.MapFragment) fm.findFragmentById(R.id.google_map_fragment);
        if (FragmentWithMap == null) {
            FragmentWithMap = com.google.android.gms.maps.MapFragment.newInstance();
            fm.beginTransaction().replace(R.id.google_map_fragment, FragmentWithMap).commit();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }*/

        dataholder = ApplicationSingleton.getDataHolder();
        dataholder.getPrivateChatManager().initChatListener();
        dataholder.getPrivateChatManager().addListener(this);
        camera = new MapCamera();

    }
    @Override
    public void onGlobalLayout() {
        if (!mapLoaded){
            mapLoaded = true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_map, container, false);

        // Inflate the layout for this fragment
        focusToggle = (ToggleButton) rootView.findViewById(R.id.focusToggle);
        userField = (EditText) rootView.findViewById(R.id.userField);
        useridField = (TextView) rootView.findViewById(R.id.userIdField);
        useridField.setText(Integer.toString(dataholder.getSignInUserId()));
        //rootView.findViewById(R.id.map_fragment).getViewTreeObserver().addOnGlobalLayoutListener(this);
        //MapActivityWithFragments.fragmentManager.findFragmentById(R.id.map_fragment).getActivity().findViewById(R.id.google_map_fragment).addOnAttachStateChangeListener(this);
        return rootView;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            //mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
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
    public void locationsUpdate() {
        handler.post(locationsUpdatedRunnable);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    private void focusCamera() {
        if (gotFirstFix) {
            googleMap.animateCamera(camera.zoomIn());//default zoom=0.001
        } else {
            System.out.println("cant focus/zoom since lacking gps self");
        }
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

    public void setUpMapIfNeeded() {
        if (googleMap == null) {

            googleMap = FragmentWithMap.getMap();
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
}
