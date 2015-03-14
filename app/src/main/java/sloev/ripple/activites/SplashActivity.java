package sloev.ripple.activites;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBSettings;

import java.util.List;

import sloev.ripple.chat.PrivateChatManager;
import sloev.ripple.util.ApplicationSingleton;
import sloev.ripple.R;
import sloev.ripple.util.Credentials;


public class SplashActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        // Restore preferences
        // Initialize QuickBlox application with credentials.
        //
        // Initialize QuickBlox application with credentials.
        //
        Credentials.getDataHolder().QBAuthorize();
        //QBSettings.getInstance().fastConfigInit(Credentials., AUTH_KEY, AUTH_SECRET);


        // Create QuickBlox session
        //
        QBAuth.createSession(new QBEntityCallbackImpl<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                signUpOrSignIn();
            }

            @Override
            public void onError(List<String> errors) {
                System.out.println(errors);
            }
        });

    }
    private void signUpOrSignIn(){
        PrivateChatManager privateChatManager = new PrivateChatManager(this);
        ApplicationSingleton.getDataHolder().setPrivateChatManager(privateChatManager);
        SharedPreferences settings = getSharedPreferences(ApplicationSingleton.PREFS_NAME, 0);
        boolean signedUp = settings.getBoolean("signedUp", false);

        Intent intent;
        if (signedUp){
            intent = new Intent(this, SigninActivity.class);
            System.out.println("signed up allready");
        }else{
            intent = new Intent(this, SignupActivity.class);
        }
        startActivity(intent);
    }


}
