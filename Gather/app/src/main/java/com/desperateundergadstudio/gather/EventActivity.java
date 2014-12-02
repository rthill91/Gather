package com.desperateundergadstudio.gather;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;


public class EventActivity extends Activity {

    JSONObject event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        try {
            event = new JSONObject(getIntent().getStringExtra("currentEvent"));
            TextView eventTitle = (TextView)findViewById(R.id.event_textView_eventTitle);
            TextView eventDescription = (TextView)findViewById(R.id.event_textView_eventDescription);
            eventTitle.setText(event.getString("Title"));
            eventDescription.setText(event.getString("Description"));
        } catch(JSONException e) {

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_event, menu);
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
