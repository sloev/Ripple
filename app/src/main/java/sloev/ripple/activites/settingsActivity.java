package sloev.ripple.activites;

import android.content.Intent;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.List;

import sloev.ripple.R;
import sloev.ripple.util.ApplicationSingleton;
import sloev.ripple.util.DialogUtils;

interface Klick{
    public void execute();
}

public class SettingsActivity extends PreferenceActivity implements Klick{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyPreferenceFragment frag = new MyPreferenceFragment();
        frag.listen(this);
        getFragmentManager().beginTransaction().replace(android.R.id.content, frag).commit();

    }
    private void showError(){
        Toast.makeText(SettingsActivity.this, getString(R.string.error_on_user_delete), Toast.LENGTH_SHORT).show();

    }
    public void execute(){
        QBUser user = ApplicationSingleton.getDataHolder().getSignInQbUser();
        if (user != null) {
            QBUsers.deleteUser(user.getId(), new QBEntityCallbackImpl() {
                @Override
                public void onSuccess() {
                    System.out.println("deleted self");
                    delete_and_sign_up();
                }

                @Override
                public void onError(List errors) {
                    showError();
                    System.out.println(getString(R.string.error_on_user_delete));
                }
            });
        } else {
            showError();
        }
    }
    private void delete_and_sign_up() {

        ApplicationSingleton.getDataHolder().DeleteAccount(this);
        Intent intent = new Intent(this, SplashActivity.class);
        startActivity(intent);
        finish();
    }

    public static class MyPreferenceFragment extends PreferenceFragment {
        private Klick klick = null;
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.layout.settings);
            Preference pref = findPreference("delete_account_key");
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (klick!=null){
                        klick.execute();
                    }
                    return false;
                }
            });
        }
        public void listen(Klick klick){
            this.klick = klick;
        }

    }

}