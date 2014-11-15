package com.desperateundergadstudio.gather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends Activity implements LocationListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabHost tabHost = (TabHost)findViewById(R.id.tabHost);
        tabHost.setup();

        // Home Tab
        TabHost.TabSpec tabSpec = tabHost.newTabSpec("home");
        tabSpec.setContent(R.id.tabHome);
        tabSpec.setIndicator("Home");
        tabHost.addTab(tabSpec);

        // Browse Tab
        tabSpec = tabHost.newTabSpec("browse");
        tabSpec.setContent(R.id.tabBrowse);
        tabSpec.setIndicator("Browse");
        tabHost.addTab(tabSpec);

        // Map Tab
        tabSpec = tabHost.newTabSpec("map");
        tabSpec.setContent(R.id.tabMap);
        tabSpec.setIndicator("Map");
        tabHost.addTab(tabSpec);

        LocationManager locationManager;
        // Get the LocationManager Object
        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        // Create a criteria object needed to retrieve the provider
        Criteria criteria = new Criteria();
        // Get the name of the best available provider
        String provider = locationManager.getBestProvider(criteria, true);
        // Use provider immediately to get LKL
        Location location = locationManager.getLastKnownLocation(provider);
        // request that the provider send this activity GPS updates every 20 seconds
        locationManager.requestLocationUpdates(provider, 20000, 0, this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        /*if (id == R.id.action_settings) {
            return true;
        }*/
        switch(id) {
            case R.id.action_settings:
                return true;
            case R.id.action_profile:
                launchProfileActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void onBackPressed() {
        // Do nothing for the moment
    }

    private void launchProfileActivity() {
        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(intent);
    }

    @Override
    public void onLocationChanged(Location location) {
        GoogleMap mMap = null;
        try {
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        } catch(Exception e) {

        }
        if(mMap != null)
        {
            drawMarker(location);
        }
    }

    private void drawMarker(Location location) {
        GoogleMap mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        mMap.clear();
        // convert the location object to a latlng object
        LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
        // zoom to the current location
        mMap.animateCamera((CameraUpdateFactory.newLatLngZoom(currentPosition, 16)));
        // add a marker to the map indicating current position
        mMap.addMarker(new MarkerOptions()
            .position(currentPosition)
            .snippet("Lat:" + location.getLatitude() + "Lng:" + location.getLongitude()));
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
