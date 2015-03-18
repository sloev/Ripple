package sloev.ripple.util;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

import sloev.ripple.model.UserDataStructure;

/**
 * Created by johannes on 18/03/15.
 */
public class MapCamera {
    private double deltaLat;
    private double deltaLon;
    private LatLng sw;
    private LatLng ne;
    private LatLngBounds.Builder builder;
    private ApplicationSingleton dataholder;

    public MapCamera() {
        dataholder = ApplicationSingleton.getDataHolder();
    }

    private double defaultMaxZoom = 0.001;

    public CameraUpdate zoomIn() {
        return zoomIn(defaultMaxZoom);
    }

    public CameraUpdate zoomIn(double maxZoom) {
        UserDataStructure signInUserData = dataholder.getSignInUserData();
        if (!signInUserData.hasMarker()) {
            System.err.println("from:ZoomIn - sign in user has no marker");
            return null;
        } else {
            Marker myMarker = signInUserData.getMarker();

            int padding = 100; // offset from edges of the map in pixels

            //based on http://stackoverflow.com/questions/15700808/setting-max-zoom-level-in-google-maps-android-api-v2
            builder = new LatLngBounds.Builder();
            for (UserDataStructure userData : dataholder.getContacts()) {
                if (userData.isEnabled() & userData.hasMarker()) {
                    builder.include(userData.getPosition());
                }
            }
            builder.include(myMarker.getPosition());
            LatLngBounds bounds = builder.build();

            sw = bounds.southwest;
            ne = bounds.northeast;
            deltaLat = Math.abs(sw.latitude - ne.latitude);
            deltaLon = Math.abs(sw.longitude - ne.longitude);

            if (deltaLat < maxZoom) {
                sw = new LatLng(sw.latitude - (maxZoom - deltaLat / 2), sw.longitude);
                ne = new LatLng(ne.latitude + (maxZoom - deltaLat / 2), ne.longitude);
                bounds = new LatLngBounds(sw, ne);
            } else if (deltaLon < maxZoom) {
                sw = new LatLng(sw.latitude, sw.longitude - (maxZoom - deltaLon / 2));
                ne = new LatLng(ne.latitude, ne.longitude + (maxZoom - deltaLon / 2));
                bounds = new LatLngBounds(sw, ne);
            }
            return CameraUpdateFactory.newLatLngBounds(bounds, padding);
        }
    }
}
