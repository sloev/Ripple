package sloev.ripple.activites;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

import sloev.ripple.R;
import sloev.ripple.chat.MainActivityListener;
import sloev.ripple.fragments.MapViewFragment;
import sloev.ripple.model.UserDataStructure;
import sloev.ripple.util.ApplicationSingleton;

public class MainDrawerActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    private ToggleButton autofocusToggle;
    private MapViewFragment frag;
    private List<MainActivityListener> listeners = new ArrayList<MainActivityListener>();


    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private ApplicationSingleton dataholder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dataholder = ApplicationSingleton.getDataHolder();
/*
        dataholder.addUserToContacts(21, new UserDataStructure(21,true, "lol"));
        dataholder.addUserToContacts(32, new UserDataStructure(32,false, "sdf"));
        dataholder.addUserToContacts(44, new UserDataStructure(44,true, "rgegr"));
        dataholder.addUserToContacts(55, new UserDataStructure(55,true, "sfgasdgrt"));

        dataholder.saveContacts(this);
*/
        setContentView(R.layout.activity_main_drawer);
        autofocusToggle = (ToggleButton) findViewById(R.id.autofocusToggle);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        FragmentManager fragmentManager = getSupportFragmentManager();
        MapViewFragment mapViewFragment = (MapViewFragment) fragmentManager.findFragmentByTag("map_view_fragment");

        if (mapViewFragment == null) {
            FragmentTransaction ft = fragmentManager.beginTransaction();
            frag = MapViewFragment.newInstance();
            addListener(frag);
            ft.replace(R.id.map_container_new, frag, "map_view_fragment");
            ft.commit();
            fragmentManager.executePendingTransactions();
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        dataholder.saveContacts(this);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
       /* // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
                */
    }

    public void onSectionAttached(int number) {
     /*   switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
        */
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
       // actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(false);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main_drawer, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void focusEnable(View v){
        for ( MainActivityListener mainActivityListener : listeners){
            mainActivityListener.autofocusEnabled(autofocusToggle.isEnabled());
        }
    }

    public void addListener(MainActivityListener toAdd) {
        listeners.add(toAdd);
    }

    public void removeListener(MainActivityListener toAdd) {
        listeners.remove(toAdd);
    }


}
