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
import com.quickblox.users.model.QBUser;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.*;
import sloev.ripple.R;
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
    private List<QBUser> qbUsersList = new ArrayList<QBUser>();
    private Map<String, UserStructure> map = new HashMap<String, UserStructure>();

    private QBUser signInQbUser;

    private QBChatService chatService;


    public static synchronized ApplicationSingleton getDataHolder() {
        if (dataHolder == null) {
            dataHolder = new ApplicationSingleton();
        }
        return dataHolder;
    }

    public QBChatService getChatServiceInstance(Context context){
        QBChatService.setDebugEnabled(false);
        if (!QBChatService.isInitialized()) {
            QBChatService.init(context);
        }
        return chatService.getInstance();
    }


    public void loadUsersFromPreferences(Context context) throws JSONException, InvalidKeySpecException, NoSuchAlgorithmException {
        SharedPreferences settings = context.getSharedPreferences(ApplicationSingleton.PREFS_NAME, 0);
        //fp fat pp signed in user
        String jsonString = settings.getString(SIGNED_IN_USER, "");
        JSONObject jsonObject = new JSONObject(jsonString);

        String fingerprint = jsonObject.getString("fingerprint");
        String public_exponent = jsonObject.getString("public_exponent");
        String public_modulus = jsonObject.getString("public_modulus");
        String private_exponent = jsonObject.getString("private_exponent");
        String private_modulus = jsonObject.getString("private_modulus");
        Boolean enabled = true;

        //fp fat alle andre brugere
        jsonString = settings.getString(USERS, "");

        JSONArray jsonArray = new JSONArray(jsonString);

        //JSONArray jsonArray = jsonData.getJSONArray(USERS);

        for(int i=0; i<jsonArray.length(); i++) {
            JSONObject json_data = jsonArray.getJSONObject(i);
            fingerprint = json_data.getString("fingerprint");
            public_exponent = jsonObject.getString("public_exponent");
            public_modulus = jsonObject.getString("public_modulus");
            enabled = json_data.getBoolean("enabled");
            map.put(fingerprint, new UserStructure(public_exponent, public_modulus, enabled));
        }

        //map.put("dog", "type of animal")
        //a user is a fingerprint, big integer, modulus integer, enabled
        //for all users from json:
        //

    }

    public void saveUsersToPreferences(Context context){
        //map.get("dog")
    }

    public void setQbUsersList(List<QBUser> qbUsersList) {
        this.qbUsersList = qbUsersList;
    }
    /*public void setDialogsUsers(List<QBUser> setUsers) {
        dialogsUsers.clear();

        for (QBUser user : setUsers) {
            dialogsUsers.put(user.getId(), user);
        }
    }*/

    public int getQBUserListSize() {
        return qbUsersList.size();
    }

    public String getQBUserName(int index) {
        return qbUsersList.get(index).getFullName();
    }

    public List<String> getQbUserTags(int index) {
        return qbUsersList.get(index).getTags();
    }

    public QBUser getQBUser(int index) {
        return qbUsersList.get(index);
    }

    public QBUser getLastQBUser() {
        return qbUsersList.get(qbUsersList.size() - 1);
    }

    public void addQbUserToList(QBUser qbUser) {
        qbUsersList.add(qbUser);
    }

    public QBUser getSignInQbUser() {
        return signInQbUser;
    }

    public void setSignInQbUser(QBUser singInQbUser) {
        this.signInQbUser = singInQbUser;
    }

    public String getSignInUserOldPassword() {
        return signInQbUser.getOldPassword();
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

    public String getSignInUserEmail() {
        return signInQbUser.getEmail();
    }

    public String getSignInUserFullName() {
        return signInQbUser.getFullName();
    }

    public String getSignInUserPhone() {
        return signInQbUser.getPhone();
    }

    public String getSignInUserWebSite() {
        return signInQbUser.getWebsite();
    }

}

