package sloev.ripple.activites;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import sloev.ripple.fragments.MapFragment;
import sloev.ripple.fragments.MapViewFragment;
import sloev.ripple.fragments.SignInFragment;
import sloev.ripple.fragments.SignUpFragment;
import sloev.ripple.model.UserDataStructure;
import sloev.ripple.util.ApplicationSingleton;
import sloev.ripple.util.Credentials;
import sloev.ripple.util.DialogUtils;

/*
implement otr based on:
https://code.google.com/p/otr4j/wiki/QuickStart
 */

public class SingleActivity extends ActionBarActivity implements ChatListener, ContactListFragment.ContactListListener, SignInFragment.SignInListener, SignUpFragment.SignUpListener{
    ApplicationSingleton dataholder;
    SharedPreferences settings;
    private ProgressBar spinner;

    Handler handler = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single);
        setTitle("");
        spinner = (ProgressBar)findViewById(R.id.progressBar1);
        spinner.setVisibility(View.GONE);

        dataholder = ApplicationSingleton.getDataHolder();
        handler = new Handler();

        settings = getSharedPreferences(ApplicationSingleton.PREFS_NAME, 0);

        spinner.setVisibility(View.VISIBLE);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_contacts) {
            change_fragment("contact_list_fragment");
            return true;
        }
        if(id== R.id.action_delete_account){
            delete_account();
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
            privateChatManager.addListener(this);
            dataholder.setPrivateChatManager(privateChatManager);
        }
        SharedPreferences settings = getSharedPreferences(ApplicationSingleton.PREFS_NAME, 0);
        boolean signed_up = settings.getBoolean(getString(R.string.IS_SIGNED_IN), false);

        if (signed_up) {
            setTitle("sign in");
            change_fragment("sign_in_fragment");
        } else {
            setTitle("sign up");
            change_fragment("sign_up_fragment");
        }
    }

    @Override
    public void sign_in(final String username, final String password) {
        QBUser qbUser = new QBUser(username, password);
        spinner.setVisibility(View.VISIBLE);

        QBUsers.signIn(qbUser, new QBEntityCallbackImpl<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                spinner.setVisibility(View.GONE);
                dataholder.setSignInQbUser(qbUser);
                dataholder.setSignInUserPassword(password);
                instantiate_chat(qbUser);
                String s = String.format("%s: %d",qbUser.getLogin(), qbUser.getId());
                setTitle(s);
            }

            @Override
            public void onError(List<String> errors) {
                spinner.setVisibility(View.GONE);

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
        spinner.setVisibility(View.VISIBLE);

        QBUsers.signUpSignInTask(qbUser, new QBEntityCallbackImpl<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(getString(R.string.IS_SIGNED_IN), true);
                editor.putString(getString(R.string.SIGNED_IN_USER), qbUser.getLogin());
                editor.commit();
                dataholder.setSignInQbUser(qbUser);
                dataholder.setSignInUserPassword(password);
                sign_up_or_in();
                //instantiate_chat(qbUser);
            }

            @Override
            public void onError(List<String> errors) {
                System.out.println("sign up error");
                spinner.setVisibility(View.GONE);
                DialogUtils.show(SingleActivity.this, getString(R.string.sign_up_refusal));
            }
        });
    }

    private void instantiate_chat(QBUser qbUser) {
        load_contacts();
        int userId = qbUser.getId();
        if (!dataholder.contactsContainsUser(userId)) {
            UserDataStructure userData = new UserDataStructure(userId, true, qbUser.getLogin());
            userData.setSignInUser(true);
            dataholder.addUserToContacts(userId, userData);
            System.out.println("login user now in contacts:");
        }else{
            UserDataStructure userData = dataholder.getUserData(userId);
            userData.setSignInUser(true);
        }
        final QBChatService chatService = dataholder.getPrivateChatManager().getChatService();
        chatService.login(qbUser, new QBEntityCallbackImpl() {
            @Override
            public void onSuccess() {
                try {
                    dataholder.getPrivateChatManager().initChatListener(); // todo lig dette i initiate chat i single activity
                    chatService.startAutoSendPresence(30);
                    spinner.setVisibility(View.GONE);
                    change_fragment("map_fragment");


                } catch (SmackException.NotLoggedInException e) {
                    e.printStackTrace();
                }
                //Intent intent = new Intent(context, MainDrawerActivity.class);
                //startActivity(intent);
                //finish();
            }

            @Override
            public void onError(List errors) {
                spinner.setVisibility(View.GONE);

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
            case "map_fragment":
                frag = (Fragment) fm.findFragmentByTag(tag);
                if (frag == null) {
                    frag = MapFragment.newInstance();
                    //frag.setRetainInstance(true);

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
        spinner.setVisibility(View.GONE);

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

    private void delete_account(){
        spinner.setVisibility(View.VISIBLE);
        QBUser user = ApplicationSingleton.getDataHolder().getSignInQbUser();
        if (user != null) {
            QBUsers.deleteUser(user.getId(), new QBEntityCallbackImpl() {
                @Override
                public void onSuccess() {
                    System.out.println("deleted self");
                    SharedPreferences settings = getSharedPreferences(ApplicationSingleton.PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.remove(getString(R.string.IS_SIGNED_IN));
                    editor.commit();
                    /*Fragment fragment = getSupportFragmentManager().findFragmentByTag("map_view_fragment");
                    if(fragment != null)
                        System.out.println("deleting map fragment");
                        getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                    */
                    sign_up_or_in();
                }

                @Override
                public void onError(List errors) {
                    spinner.setVisibility(View.GONE);
                    Toast.makeText(SingleActivity.this, getString(R.string.error_on_user_delete), Toast.LENGTH_SHORT).show();
                    System.out.println(getString(R.string.error_on_user_delete));
                }
            });
        } else {
            Toast.makeText(SingleActivity.this, getString(R.string.error_on_user_delete), Toast.LENGTH_SHORT).show();
        }
    }
    public void load_contacts(){
        dataholder.loadContacts(this);
    }

    @Override
    public void save_contacts() {
        dataholder.saveContacts(this);
    }

    @Override
    public void update_user_data(final int userId) {
        final UserDataStructure user_data = dataholder.getUserData(userId);
        user_data.setUpdated_with_info(true);
        Runnable update_user_info = new Runnable() {
            @Override
            public void run() {
                {
                    QBUsers.getUser(userId, new QBEntityCallbackImpl<QBUser>() {
                        @Override
                        public void onSuccess(QBUser user, Bundle args) {
                            user_data.setSnippet(user.getLogin());
                        }

                        @Override
                        public void onError(List<String> errors) {
                            user_data.setUpdated_with_info(false);
                        }
                    });
                }
            }
        };
        handler.postDelayed(update_user_info, 0);

    }
}
