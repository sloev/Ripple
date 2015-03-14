package sloev.ripple.activites;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;

import java.util.List;

import sloev.ripple.R;
import sloev.ripple.chat.PrivateChatManager;
import sloev.ripple.util.ApplicationSingleton;
import sloev.ripple.util.DialogUtils;


public class SigninActivity extends ActionBarActivity {
    private ApplicationSingleton dataholder;
    private EditText passwordField;
    private TextView fingerprintView;
    private SharedPreferences settings;
    private String loginName;
    private Context context;
    QBChatService chatService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        context = this;
        SharedPreferences settings = getSharedPreferences(ApplicationSingleton.PREFS_NAME, 0);

        dataholder = ApplicationSingleton.getDataHolder();
        fingerprintView = (TextView) findViewById(R.id.fingerprintView);
        loginName = "bot";//(String) settings.getString("loginFingerPrint", "");
        fingerprintView.setText(loginName);


        passwordField = (EditText) findViewById(R.id.passwordField);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_signin, menu);
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

    public void signIn(View v){
        QBUser qbUser = new QBUser(loginName, passwordField.getText().toString());
        QBUsers.signIn(qbUser, new QBEntityCallbackImpl<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {

                setResult(RESULT_OK);
                dataholder.setSignInQbUser(qbUser);
                dataholder.setSignInUserPassword(passwordField.getText().toString());
                signinSuccess();
                //finish();


            }

            @Override
            public void onError(List<String> errors) {
                System.out.println("sign in error");
                //create dialog asking if you want to create new account instead
                DialogInterface.OnClickListener checkInPositiveButton;
                DialogInterface.OnClickListener checkInNegativeButton;
                checkInPositiveButton = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        signUpInstead();
                    }
                };

                checkInNegativeButton = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                };
                final android.app.Dialog checkInAlert = DialogUtils.createDialog(SigninActivity.this, R.string.sign_in_refused,
                        R.string.sign_in_refused_sollution, checkInPositiveButton, checkInNegativeButton);

                checkInAlert.show();
            }
        });

    }
    private void signUpInstead(){
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }
    private void signinSuccess(){

        chatService = dataholder.getPrivateChatManager().getChatService();

        chatService.login(dataholder.getSignInQbUser(), new QBEntityCallbackImpl() {
            @Override
            public void onSuccess() {
                System.out.println("SIGNINSUCCESS");
                try {
                    chatService.startAutoSendPresence(30);
                } catch (SmackException.NotLoggedInException e) {
                    e.printStackTrace();
                }                Intent intent = new Intent(context, MapActivity.class);
                startActivity(intent);

                finish();
            }

            @Override
            public void onError(List errors) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(SigninActivity.this);
                dialog.setMessage("chat login errors: " + errors).create().show();
            }
        });

    }
}
