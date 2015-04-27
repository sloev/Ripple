package sloev.ripple.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.ui.IconGenerator;
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
    private IconGenerator iconGenerator = null;

    public boolean isUpdated_with_info() {
        return updated_with_info;
    }

    public void setUpdated_with_info(boolean updated_with_info) {
        this.updated_with_info = updated_with_info;
    }

    private boolean updated_with_info = false;
    private boolean enabled;

    private int time_to_live = 1;

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

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
        if ( isSignInUser) {
            time_to_live = 6;
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

    public MarkerOptions getMarkerOptions(Context context) {
        if (iconGenerator == null) {
            iconGenerator = new IconGenerator(context);
        }
        if (isSignInUser) {
            iconGenerator.setColor(Color.RED);
        }else{
            if (time_to_live > 3) {
                iconGenerator.setColor(Color.GREEN);
            }else{
                iconGenerator.setColor(Color.YELLOW);
            }
            time_to_live--;
        }
        Bitmap icon = iconGenerator.makeIcon(snippet);
        return new MarkerOptions().position(position).icon(
                BitmapDescriptorFactory.fromBitmap(icon)).title(Integer.toString(userId)).snippet(snippet);
    }

    public void extend_life(){
        time_to_live = 6;
    }
    public boolean is_alive(){
        return time_to_live > 0;
    }





}
