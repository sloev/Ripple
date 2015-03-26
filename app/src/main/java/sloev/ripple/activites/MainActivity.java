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

import sloev.ripple.R;
import sloev.ripple.fragments.MapViewFragment;

public class MainActivity extends FragmentActivity {
    FragmentManager fragmentManager;
    ToggleButton autofocusToggle;

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
            ft.replace(R.id.map_container, frag, "map_view_fragment");
            ft.commit();
            fragmentManager.executePendingTransactions();
        }
    }
    public void focusEnable(View v){
        ((MapViewFragment) fragmentManager.findFragmentByTag("map_view_fragment")).autofocusEnabled = autofocusToggle.isEnabled();
    }
}


