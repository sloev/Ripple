package sloev.ripple.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.quickblox.users.model.QBUser;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

/**
 * Created by johannes on 12/03/15.
 */
public class UserDataStructure {


    private Marker marker;

    private boolean enabled;

    private int userId;

    public UserDataStructure(int userId, boolean enabled){
        this.userId = userId;
        this.enabled = enabled;
        marker = null;
    }
    public UserDataStructure(String serializedSelf){
        String[] chunks = serializedSelf.split(" ");
        enabled = Boolean.parseBoolean(chunks[0]);
    }

    public String toString(){
        String serializedSelf = Boolean.toString(enabled) + " ";
        return serializedSelf;

    }
    public LatLng getPosition() {
        return marker.getPosition();
    }

    public void setPosition(LatLng position) {
        marker.setPosition(position);
    }
    public Marker getMarker() {
        return marker;
    }
    public boolean hasMarker() {
        return marker != null;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }
    public int getUserId() {
        return userId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void enable() {
        this.enabled = true;
    }
    public void disable() {
        this.enabled = false;
    }
}
