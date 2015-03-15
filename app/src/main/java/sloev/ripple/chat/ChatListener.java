package sloev.ripple.chat;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by johannes on 14/03/15.
 */
public interface ChatListener {
    public void gpsReceived(int userId, LatLng position);
}
