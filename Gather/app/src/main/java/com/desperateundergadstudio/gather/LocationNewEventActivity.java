package com.desperateundergadstudio.gather;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class LocationNewEventActivity extends Activity implements OnMapClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_new_event);

        GoogleMap MapFrag = ((MapFragment) getFragmentManager().findFragmentById(R.id.createEvent_map)).getMap();
        MapFrag.setOnMapClickListener((OnMapClickListener) this);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        Button button = new Button(this);
        button.setText("Submit");
        addContentView(button, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        button.setX(button.getX() + 50f);
        button.setY(size.y/2 + 400f);

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                LocationNewEventActivity.this.finish();
            }
        });
    }

    @Override
    public void onMapClick(LatLng point) {
        GoogleMap MapFrag = ((MapFragment) getFragmentManager().findFragmentById(R.id.createEvent_map)).getMap();
            MapFrag.addMarker(new MarkerOptions()
            .position(point)
            .title("New Event"));
        SharedPreferences prefs = getSharedPreferences(Constants.session_prefs,0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat("NewEventLatitude", (float)point.latitude);
        editor.putFloat("NewEventLongitude", (float)point.longitude);
        editor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_location_new_event, menu);
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
}
