package com.desperateundergadstudio.gather;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends Activity implements LocationListener {

    private JSONObject currentUser;

    private ProgressBar homeSpinner;
    private ProgressBar browseSpinner;
    private SharedPreferences prefs;

    private JSONArray attendingEvents;
    private ListView homeList;
    private MainListAdapter homeArrayAdapter;
    private ArrayList<JSONObject> homeEvents = null;

    private JSONArray allEvents;
    private ListView browseList;
    private MainListAdapter browseArrayAdapter;
    private ArrayList<JSONObject> browseEvents = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(Constants.session_prefs, 0);

        try {
            currentUser = new JSONObject(getIntent().getStringExtra("currentUser"));
        } catch (JSONException e) {}

        TabHost tabHost = (TabHost)findViewById(R.id.tabHost);
        tabHost.setup();

        // Home Tab
        TabHost.TabSpec tabSpec = tabHost.newTabSpec("home");
        tabSpec.setContent(R.id.tabHome);
        tabSpec.setIndicator("Home");
        tabHost.addTab(tabSpec);
        homeSpinner = (ProgressBar)findViewById(R.id.progress_homeSpinner);
        homeSpinner.setVisibility(View.GONE);

        // Browse Tab
        tabSpec = tabHost.newTabSpec("browse");
        tabSpec.setContent(R.id.tabBrowse);
        tabSpec.setIndicator("Browse");
        tabHost.addTab(tabSpec);
        browseSpinner = (ProgressBar)findViewById(R.id.progress_browseSpinner);
        browseSpinner.setVisibility(View.GONE);

        // Map Tab
        tabSpec = tabHost.newTabSpec("map");
        tabSpec.setContent(R.id.tabMap);
        tabSpec.setIndicator("Map");
        tabHost.addTab(tabSpec);

        setupButtons();

        //Create home event list
        homeList = (ListView)findViewById(R.id.main_listView_homeList);
        homeEvents = new ArrayList<JSONObject>();
        homeArrayAdapter = new MainListAdapter(MainActivity.this, R.layout.listitems, homeEvents);
        homeList.setAdapter(homeArrayAdapter);
        homeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), EventActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("currentEvent", homeList.getItemAtPosition(i).toString());
                getApplicationContext().startActivity(intent);
            }
        });

        new loadAttendingEvents(MainActivity.this).execute(currentUser.toString());

        //Create browse event list
        browseList = (ListView)findViewById(R.id.main_listView_browseList);
        browseEvents = new ArrayList<JSONObject>();
        browseArrayAdapter = new MainListAdapter(MainActivity.this, R.layout.listitems, browseEvents);
        browseList.setAdapter(browseArrayAdapter);
        browseList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), EventActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("currentEvent", browseList.getItemAtPosition(i).toString());
                getApplicationContext().startActivity(intent);
            }
        });

        new loadAllEvents(MainActivity.this).execute(currentUser.toString());



//        LocationManager locationManager;
//        // Get the LocationManager Object
//        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
//        // Create a criteria object needed to retrieve the provider
//        Criteria criteria = new Criteria();
//        // Get the name of the best available provider
//        String provider = locationManager.getBestProvider(criteria, true);
//        // Use provider immediately to get LKL
//        Location location = locationManager.getLastKnownLocation(provider);
//        // request that the provider send this activity GPS updates every 20 seconds
//        locationManager.requestLocationUpdates(provider, 20000, 0, this);
    }

    private void setupButtons() {
        Button createEvent = (Button)findViewById(R.id.button_createEvent);
        createEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CreateEventActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("currentUser", currentUser.toString());
                getApplicationContext().startActivity(intent);
            }
        });
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
            case R.id.action_refresh:
                homeSpinner.setVisibility(View.VISIBLE);
                new loadAttendingEvents(MainActivity.this).execute(currentUser.toString());
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void onBackPressed() {
        // Do nothing for the moment
    }

    private void populateHomeList() {
        try {
            homeEvents.clear();
            JSONObject j;
            for(int i=0; i<attendingEvents.length(); i++) {
                j = attendingEvents.getJSONObject(i);
                homeEvents.add(j);
            }
            homeArrayAdapter.notifyDataSetChanged();
        } catch(Exception e) {

        }
    }

    private void populateBrowseList() {
        try {
            browseEvents.clear();
            JSONObject j;
            for(int i=0; i<allEvents.length(); i++) {
                j = allEvents.getJSONObject(i);
                browseEvents.add(j);
            }
            browseArrayAdapter.notifyDataSetChanged();;
        } catch(Exception e) {

        }
    }

    private void launchProfileActivity() {
        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(intent);
    }

    private class loadAttendingEvents extends AsyncTask<String, String, String> {
        Context context;
        private loadAttendingEvents(Context context){
            this.context = context.getApplicationContext();
        }

        @Override
        protected String doInBackground(String... params) {
            if(noInternet()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, R.string.noInternet, Toast.LENGTH_LONG).show();
                        homeSpinner.setVisibility(View.GONE);
                    }
                });
                this.cancel(true);
            }
            if(!isCancelled()) {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost post = new HttpPost(Constants.api_base + Constants.getAttendingEvents);
                try {
                    String postParams = params[0];
                    StringEntity se = new StringEntity(postParams);
                    post.setEntity(se);
                    post.setHeader("Accept", "application/json");
                    post.setHeader("Content-type", "application/json");

                    HttpResponse response = httpclient.execute(post);
                    // Get the response message as a string and return it
                    // for access in postExecute
                    return EntityUtils.toString(response.getEntity());

                } catch (IOException e) {

                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String resultString) {
            try {
                JSONObject result = new JSONObject(resultString);
                if(!result.getString("type").equals("error")) {
                    attendingEvents = new JSONArray(result.getString("message"));
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("AttendingEvents", result.getString("message"));

                    editor.apply();
                    populateHomeList();
                } else {
                    Toast.makeText(context, result.getString("message"), Toast.LENGTH_LONG).show();
                }
            } catch(JSONException e) {
                Toast.makeText(context, "DEBUG: SOME ERROR", Toast.LENGTH_LONG).show();
                homeSpinner.setVisibility(View.GONE);
            }
            homeSpinner.setVisibility(View.GONE);
        }
    }

    private class loadAllEvents extends AsyncTask<String, String, String> {
        Context context;
        private loadAllEvents(Context context) {
            this.context = getApplicationContext();
        }

        protected String doInBackground(String... params) {
            if(noInternet()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, R.string.noInternet, Toast.LENGTH_LONG).show();
                        browseSpinner.setVisibility(View.GONE);
                    }
                });
                this.cancel(true);
            }
            if(!isCancelled()) {
                HttpClient httpclient = new DefaultHttpClient();
                HttpGet get = new HttpGet(Constants.api_base + Constants.getAllEvents);
                try {
                    HttpResponse response = httpclient.execute(get);
                    return EntityUtils.toString(response.getEntity());
                } catch (IOException e) {

                }
            }
            return null;
        }

        protected void onPostExecute(String resultString) {
            try {
                JSONObject result = new JSONObject(resultString);
                if(!result.getString("type").equals("error")) {
                    allEvents = new JSONArray(result.getString("message"));
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("AllEvents", result.getString("message"));

                    editor.apply();
                    populateBrowseList();
                } else {
                    Toast.makeText(context, result.getString("message"), Toast.LENGTH_LONG).show();
                }
            } catch(JSONException e) {
                Toast.makeText(context, "DEBUG: SOME ERROR", Toast.LENGTH_LONG).show();
                browseSpinner.setVisibility(View.GONE);
            }
            browseSpinner.setVisibility(View.GONE);
        }
    }


    // Map Helper Functions
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


    private boolean noInternet() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if(ni == null) {
            // no internet
            return true;
        }
        return false;
    }


}
