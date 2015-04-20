package sloev.ripple.model;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.quickblox.users.model.QBUser;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

import sloev.ripple.R;

/**
 * Created by johannes on 12/03/15.
 */
public class UserDataStructure {
    private LatLng position;
    private LatLng old_position = null;

    private boolean enabled;
    private String snippet;
    private boolean isSignInUser;
    private int userId;
    private boolean gpsFixed = false;

    private int iconId;

    public UserDataStructure(int userId, boolean enabled, String snippet){
        this.userId = userId;
        this.enabled = enabled;
        position = null;
        this.snippet=snippet;
        setSignInUser(false);
    }
    public UserDataStructure(String serializedSelf){
        String[] chunks = serializedSelf.split(" ");
        enabled = Boolean.parseBoolean(chunks[0]);
    }

    public String toString(){
        String serializedSelf = Boolean.toString(enabled) + " ";
        return serializedSelf;
    }

    public boolean isSignInUser(){
        return isSignInUser;
    }
    public void setSignInUser(boolean isSignInUser){
        this.isSignInUser = isSignInUser;
        if ( isSignInUser){
            iconId = R.drawable.map_marker_my;
        }else{
            iconId = R.drawable.map_marker_other;
        }
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        if (! gpsFixed){
            gpsFixed = true;
        }
        this.position = position;
    }
    public boolean hasGpsFix(){
        return gpsFixed;
    }
    public int getUserId() {
        return userId;
    }

    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled){
        this.enabled=enabled;
    }
    public String getSnippet(){
        return snippet;
    }
    public void enable() {
        this.enabled = true;
    }
    public void disable() {
        this.enabled = false;
    }

    public MarkerOptions getMarkerOptions() {
       return new MarkerOptions().position(position).icon(
                BitmapDescriptorFactory.fromResource(iconId)).title(Integer.toString(userId)).snippet(snippet);
    }
    public MarkerOptions getOldMarkerOptions() {
        MarkerOptions options = null;
        if (old_position != null){
             options = new MarkerOptions().position(old_position).icon(
                    BitmapDescriptorFactory.fromResource(iconId)).title(Integer.toString(userId)).snippet(snippet);
        }
        this.old_position = this.position;
        return options;
    }


}
