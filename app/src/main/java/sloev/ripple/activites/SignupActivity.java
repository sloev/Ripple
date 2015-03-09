package sloev.ripple.activites;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.List;

import sloev.ripple.R;
import sloev.ripple.util.ApplicationSingleton;


public class SignupActivity extends ActionBarActivity {
    SharedPreferences settings;
    EditText passwordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        settings = getSharedPreferences(ApplicationSingleton.PREFS_NAME, 0);
        passwordField = (EditText) findViewById(R.id.passwordField);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_signup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void singupSuccess(QBUser qbUser){
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("signedUp", true);
        editor.putString("loginFingerPrint", qbUser.getLogin());
        // Commit the edits!
        editor.commit();
        ApplicationSingleton.getDataHolder().setSignInQbUser(qbUser);
        System.out.println("signup success");
    }

    private void signupFail(){
        System.out.println("signup failz");
    }

    public void signUp(View v){
        String loginFingerPrint = "johannesdf";
        QBUser qbUser = new QBUser();
        qbUser.setLogin(loginFingerPrint);
        qbUser.setPassword(passwordField.getText().toString());
        QBUsers.signUpSignInTask(qbUser, new QBEntityCallbackImpl<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                singupSuccess(qbUser);
                finish();
            }

            @Override
            public void onError(List<String> strings) {
                signupFail();
            }
        });
    }
}

/*

 */
