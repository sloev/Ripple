package sloev.ripple.activites;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

import sloev.ripple.R;
import sloev.ripple.chat.ChatListener;
import sloev.ripple.chat.MainActivityListener;
import sloev.ripple.fragments.MapViewFragment;

public class MainActivity extends FragmentActivity {
    private FragmentManager fragmentManager;
    private ToggleButton autofocusToggle;
    private List<MainActivityListener> listeners = new ArrayList<MainActivityListener>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        autofocusToggle = (ToggleButton) findViewById(R.id.autofocusToggle);

        fragmentManager = getSupportFragmentManager();
        MapViewFragment mapViewFragment = (MapViewFragment) fragmentManager.findFragmentByTag("map_view_fragment");

        if (mapViewFragment == null) {
            FragmentTransaction ft = fragmentManager.beginTransaction();
            MapViewFragment frag = MapViewFragment.newInstance();
            addListener(frag);
            ft.replace(R.id.map_container, frag, "map_view_fragment");
            ft.commit();
            fragmentManager.executePendingTransactions();
        }
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


