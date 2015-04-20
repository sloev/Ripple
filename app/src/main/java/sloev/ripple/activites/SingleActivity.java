package sloev.ripple.activites;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;

import java.util.List;

import sloev.ripple.R;
import sloev.ripple.chat.ChatListener;
import sloev.ripple.chat.PrivateChatManager;
import sloev.ripple.fragments.ContactListFragment;
import sloev.ripple.fragments.MapViewFragment;
import sloev.ripple.fragments.SignInFragment;
import sloev.ripple.fragments.SignUpFragment;
import sloev.ripple.model.UserDataStructure;
import sloev.ripple.util.ApplicationSingleton;
import sloev.ripple.util.Credentials;
import sloev.ripple.util.DialogUtils;


public class SingleActivity extends ActionBarActivity implements SignInFragment.SignInListener, SignUpFragment.SignUpListener{
    ApplicationSingleton dataholder;
    SharedPreferences settings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single);
        dataholder = ApplicationSingleton.getDataHolder();
        settings = getSharedPreferences(ApplicationSingleton.PREFS_NAME, 0);

        Credentials.getDataHolder().QBAuthorize();
        QBAuth.createSession(new QBEntityCallbackImpl<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
               sign_up_or_in();
            }

            @Override
            public void onError(List<String> errors) {
                System.out.println(errors);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_single, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        System.out.println("button");
        System.out.println(item);
        System.out.println(id);

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_contacts) {
            change_fragment("contact_list_fragment");
            return true;
        }
        if (id == android.R.id.home) {
            System.out.println("home");
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStackImmediate();
                getSupportFragmentManager().beginTransaction().commit();
            }
            displayHomeIfNeeded();//weird doesnt work!
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void sign_up_or_in() {
        if (dataholder.getPrivateChatManager() == null) {
            PrivateChatManager privateChatManager = new PrivateChatManager(this);
            dataholder.setPrivateChatManager(privateChatManager);
        }
        SharedPreferences settings = getSharedPreferences(ApplicationSingleton.PREFS_NAME, 0);
        boolean signed_up = settings.getBoolean(getString(R.string.IS_SIGNED_IN), false);

        if (signed_up) {
            change_fragment("sign_in_fragment");
        } else {
            change_fragment("sign_up_fragment");
        }
    }

    @Override
    public void sign_in(final String username, final String password) {
        QBUser qbUser = new QBUser(username, password);
        QBUsers.signIn(qbUser, new QBEntityCallbackImpl<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                dataholder.setSignInQbUser(qbUser);
                dataholder.setSignInUserPassword(password);
                instantiate_chat(qbUser);
                change_fragment("map_view_fragment");
            }

            @Override
            public void onError(List<String> errors) {
                System.out.println("sign in error");
                DialogInterface.OnClickListener checkInPositiveButton;
                DialogInterface.OnClickListener checkInNegativeButton;
                checkInPositiveButton = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        change_fragment("sign_up_fragment");
                    }
                };

                checkInNegativeButton = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                };
                final android.app.Dialog checkInAlert = DialogUtils.createDialog(SingleActivity.this, R.string.sign_in_refused,
                        R.string.sign_in_refused_sollution, checkInPositiveButton, checkInNegativeButton);

                checkInAlert.show();
            }
        });

    }
    public void sign_up(String username, final String password) {
        QBUser qbUser = new QBUser(username, password);
        QBUsers.signUpSignInTask(qbUser, new QBEntityCallbackImpl<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(getString(R.string.IS_SIGNED_IN), true);
                editor.putString(getString(R.string.SIGNED_IN_USER), qbUser.getLogin());
                editor.commit();
                dataholder.setSignInQbUser(qbUser);
                dataholder.setSignInUserPassword(password);
                instantiate_chat(qbUser);
                change_fragment("map_view_fragment");
            }

            @Override
            public void onError(List<String> errors) {
                System.out.println("sign up error");
                DialogUtils.show(SingleActivity.this, getString(R.string.sign_up_refusal));
            }
        });
    }

    private void instantiate_chat(QBUser qbUser) {
        int userId = qbUser.getId();
        if (!dataholder.contactsContainsUser(userId)) {
            UserDataStructure userData = new UserDataStructure(userId, true, qbUser.getLogin());
            userData.setSignInUser(true);
            dataholder.addUserToContacts(userId, userData);
            System.out.println("login user now in contacts:");
        }
        final QBChatService chatService = dataholder.getPrivateChatManager().getChatService();
        chatService.login(qbUser, new QBEntityCallbackImpl() {
            @Override
            public void onSuccess() {
                try {
                    chatService.startAutoSendPresence(30);
                    dataholder.getPrivateChatManager().initChatListener(); // todo lig dette i initiate chat i single activity
                } catch (SmackException.NotLoggedInException e) {
                    e.printStackTrace();
                }
                //Intent intent = new Intent(context, MainDrawerActivity.class);
                //startActivity(intent);
                //finish();
            }

            @Override
            public void onError(List errors) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(SingleActivity.this);
                dialog.setMessage("chat login errors: " + errors).create().show();
            }
        });

    }

    private void change_fragment(String tag) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment frag = null;
        System.out.println("change fragment");
        System.out.println(tag);
        switch (tag) {
            case "map_view_fragment":
                frag = (Fragment) fm.findFragmentByTag(tag);
                if (frag == null) {
                    frag = MapViewFragment.newInstance();
                    /*dataholder.getPrivateChatManager().initChatListener();
                    dataholder.getPrivateChatManager().addListener((ChatListener) frag);
                    */
                    //ft.addToBackStack(null);
                }
                ft.replace(R.id.single_activity_container, frag, tag);

                break;
            case "sign_in_fragment":
                frag = (Fragment) fm.findFragmentByTag(tag);
                if (frag == null) {
                    frag = SignInFragment.newInstance();
                }
                ft.replace(R.id.single_activity_container, frag, tag);

                break;
            case "sign_up_fragment":
                frag = (Fragment) fm.findFragmentByTag(tag);
                if (frag == null) {
                    frag = SignUpFragment.newInstance();
                }
                ft.replace(R.id.single_activity_container, frag, tag);

                break;
            case "contact_list_fragment":
                frag = (Fragment) fm.findFragmentByTag(tag);
                if (frag == null) {
                    frag = ContactListFragment.newInstance();
                }
                ft.replace(R.id.single_activity_container, frag, tag);
                ft.addToBackStack(null);
                break;
            default:
                break;
        }
        if (frag != null) {
            //ft.replace(R.id.single_activity_container, frag, tag);
            ft.commit();
        }else{
            System.out.println("fragment is null");
        }
        fm.executePendingTransactions();
        displayHomeIfNeeded();
    }

    @Override
    public void onBackPressed() {
        System.out.println("BACK");
    }

    private void displayHomeIfNeeded(){
        FragmentManager fm = getSupportFragmentManager();
        System.out.println("stack");
        System.out.println(fm.getBackStackEntryCount());
        if(fm.getBackStackEntryCount() > 0) {
            this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }else{
            this.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }
}
