package sloev.ripple.util;


import android.content.SharedPreferences;
import android.content.Context;
import android.content.res.Resources;
import android.os.DropBoxManager;

import com.quickblox.users.model.QBUser;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.*;

import sloev.ripple.R;
import sloev.ripple.chat.PrivateChatManager;
import sloev.ripple.model.UserDataStructure;
/**
 * Created by johannes on 09/03/15.
 * based on DataHolder from com.quickblox.sample.user.helper;
 */
public class ApplicationSingleton {


    public static final String PREFS_NAME = "RipplePreferences";
    public static final String USERS = "users";
    public static final String SIGNED_IN_USER = "user";

    private static ApplicationSingleton dataHolder;

    private Map<Integer, UserDataStructure> userContacts = new HashMap<Integer, UserDataStructure>();
    private List<Integer> indexToUserId = new ArrayList<Integer>();
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


    public void loadContacts(Context context)  {
        try {

            SharedPreferences settings = context.getSharedPreferences(ApplicationSingleton.PREFS_NAME, 0);
            //fp fat pp signed in user
            //String jsonString = settings.getString(SIGNED_IN_USER, "");
            //JSONObject jsonObject = new JSONObject(jsonString);


            //fp fat alle andre brugere
            String jsonString = settings.getString(USERS, "");
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(USERS);


            //JSONArray jsonArray = jsonData.getJSONArray(USERS);
            int userId;
            Boolean enabled;
            String snippet;
            boolean is_signin;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json_data = jsonArray.getJSONObject(i);
                userId = json_data.getInt("user_id");
                enabled = json_data.getBoolean("enabled");
                snippet = json_data.getString("snippet");
                is_signin = json_data.getBoolean("is_sign_in");
                UserDataStructure userDataStructure = new UserDataStructure(userId, enabled, snippet);
                userContacts.put(userId, userDataStructure);
                indexToUserId.add(userId);
                //map.put(fingerprint, new UserStructure(public_exponent, public_modulus, enabled));
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void saveContacts(Context context) {
        JSONArray userArray = new JSONArray();
        String jsonString = "";
        try {

            for (UserDataStructure userDataStructure : getContacts()) {
                JSONObject userObject = new JSONObject();
                userObject.put("user_id", userDataStructure.getUserId());
                userObject.put("enabled", userDataStructure.isEnabled());
                userObject.put("snippet", userDataStructure.getSnippet());
                userObject.put("is_sign_in", userDataStructure.isSignInUser());

                userArray.put(userObject);
            }

            jsonString = new JSONObject().put(USERS,userArray).toString();
        }catch (JSONException e) {
            e.printStackTrace();
        }
        SharedPreferences settings = context.getSharedPreferences(ApplicationSingleton.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(USERS, jsonString);
        editor.commit();
    }

    public Collection<UserDataStructure> getContacts(){
        return userContacts.values();
    }



    public boolean contactsContainsUser(int userId){
        return userContacts.containsKey(userId);
    }
    public boolean userIsEnabled(int userId){
        return userContacts.get(userId).isEnabled();
    }
    public UserDataStructure getUserData(int userId) {
        if(!contactsContainsUser(userId)){
            return null;
        }else {
            return userContacts.get(userId);
        }
    }
    public UserDataStructure getUserByIndex(int index){
        return userContacts.get(indexToUserId.get(index));
    }

    public void addUserToContacts(int userId, UserDataStructure userData) {
        userContacts.put(userId, userData);
        if(!userData.isSignInUser()) {
            indexToUserId.add(userId);
        }
    }
    public List<Integer> getIndexList(){
        return indexToUserId;
    }

    public void removeUserToContacts(int userId) {
        userContacts.remove(userId);
        indexToUserId.remove(userId);
    }

    public UserDataStructure getSignInUserData(){
        return userContacts.get(signInQbUser.getId());
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

    public void setSignInUserPassword(String singInUserPassword) {
        signInQbUser.setPassword(singInUserPassword);
    }
    public int getSignInUserId() {
        return signInQbUser.getId();
    }

    public String getSignInUserLogin() {
        return signInQbUser.getLogin();
    }

    public boolean DeleteAccount(Context context){
            signInQbUser = null;
            userContacts.clear();
            indexToUserId.clear();
            SharedPreferences settings = context.getSharedPreferences(ApplicationSingleton.PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            Resources res = context.getResources();
            editor.remove(res.getString(R.string.IS_SIGNED_IN));
            editor.remove(res.getString(R.string.SIGNED_IN_USER));
            editor.commit();
            return true;

    }
}

