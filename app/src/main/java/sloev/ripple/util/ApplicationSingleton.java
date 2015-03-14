package sloev.ripple.util;


import android.content.SharedPreferences;
import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.listeners.QBRosterListener;
import com.quickblox.users.model.QBUser;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.*;
import sloev.ripple.R;
import sloev.ripple.chat.PrivateChatManager;
import sloev.ripple.users.UserStructure;

/**
 * Created by johannes on 09/03/15.
 * based on DataHolder from com.quickblox.sample.user.helper;
 */
public class ApplicationSingleton {


    public static final String PREFS_NAME = "RipplePreferences";
    public static final String USERS = "users";
    public static final String SIGNED_IN_USER = "user";




    private static ApplicationSingleton dataHolder;
    private Map<Integer, QBUser> qpUsersMap = new HashMap<Integer, QBUser>();

    private Map<String, UserStructure> map = new HashMap<String, UserStructure>();

    private QBUser signInQbUser;


    private PrivateChatManager privateChatManager;

    public static synchronized ApplicationSingleton getDataHolder() {
        if (dataHolder == null) {
            dataHolder = new ApplicationSingleton();
        }
        return dataHolder;
    }

    public PrivateChatManager getPrivateChatManager() {
        return privateChatManager;
    }

    public void setPrivateChatManager(PrivateChatManager privateChatManager) {
        this.privateChatManager = privateChatManager;
    }

    public void loadUsersFromPreferences(Context context) throws JSONException, InvalidKeySpecException, NoSuchAlgorithmException {
        SharedPreferences settings = context.getSharedPreferences(ApplicationSingleton.PREFS_NAME, 0);
        //fp fat pp signed in user
        String jsonString = settings.getString(SIGNED_IN_USER, "");
        JSONObject jsonObject = new JSONObject(jsonString);


        //fp fat alle andre brugere
        jsonString = settings.getString(USERS, "");
        JSONArray jsonArray = new JSONArray(jsonString);

        //JSONArray jsonArray = jsonData.getJSONArray(USERS);
        int userId;
        Boolean enabled;
        for(int i=0; i<jsonArray.length(); i++) {
            JSONObject json_data = jsonArray.getJSONObject(i);
            userId = jsonObject.getInt("user_id");
            enabled = json_data.getBoolean("enabled");
            //map.put(fingerprint, new UserStructure(public_exponent, public_modulus, enabled));
        }

        //map.put("dog", "type of animal")
        //a user is a fingerprint, big integer, modulus integer, enabled
        //for all users from json:
        //

    }

    public void saveUsersToPreferences(Context context){
        //map.get("dog")
    }
    public String getQBUserName(int userId) {
        return qpUsersMap.get(userId).getFullName();
    }

    public List<String> getQbUserTags(int userId) {
        return qpUsersMap.get(userId).getTags();
    }

    public QBUser getQBUser(int userId) {
        return qpUsersMap.get(userId);
    }

    public void addQbUserToList(int userId, QBUser qbUser) {
        qpUsersMap.put(userId,qbUser);
    }

    public QBUser getSignInQbUser() {
        return signInQbUser;
    }

    public void setSignInQbUser(QBUser singInQbUser) {
        this.signInQbUser = singInQbUser;
    }

    public String getSignInUserPassword() {
        return signInQbUser.getPassword();
    }

    public int getSignInUserId() {
        return signInQbUser.getId();
    }

    public void setSignInUserPassword(String singInUserPassword) {
        signInQbUser.setPassword(singInUserPassword);
    }

    public String getSignInUserLogin() {
        return signInQbUser.getLogin();
    }

    public String getSignInUserFullName() {
        return signInQbUser.getFullName();
    }

}

