package com.desperateundergadstudio.gather;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TimePicker;
import android.widget.Toast;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import android.support.v4.app.Fragment;
import com.google.android.gms.maps.SupportMapFragment;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.Map;


public class CreateEventActivity extends Activity {

    private ProgressBar spinner;
    private JSONObject currentUser;

    public static LatLng eventLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        try {
            currentUser = new JSONObject(getIntent().getStringExtra("currentUser"));
        } catch (JSONException e) {}


        spinner = (ProgressBar)findViewById(R.id.progress_submitSpinner);
        spinner.setVisibility(View.GONE);

        setupButtons();
    }

    private void setupButtons() {
        // Submit Button
        Button submitButton = (Button)findViewById(R.id.createEvent_button_submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText eventTitle = (EditText)findViewById(R.id.createEvent_editText_eventTitle);
                EditText eventDescription = (EditText)findViewById(R.id.createEvent_editText_eventDescription);
                Button eventDate = (Button)findViewById(R.id.createEvent_button_date);
                Button eventTime = (Button)findViewById(R.id.createEvent_button_time);
                try {
                    JSONObject json = new JSONObject();
                    json.put("eventTitle", eventTitle.getText().toString());
                    json.put("eventDescription", eventDescription.getText().toString());
                    json.put("username", currentUser.getString("username"));
                    json.put("sessionid", currentUser.getString("sessionid"));
                    json.put("eventDate", eventDate.getText().toString());
                    json.put("eventTime", eventTime.getText().toString());

                    new submit(CreateEventActivity.this).execute(json.toString());
                } catch(JSONException e) {}
            }
        });

        // Date Button
        final Button dateButton = (Button)findViewById(R.id.createEvent_button_date);

        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dateFragment = new DatePickerFragment();
                dateFragment.show(getFragmentManager(), "datePicker");
            }
        });

        // Time Button
        final Button timeButton = (Button)findViewById(R.id.createEvent_button_time);

        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment timeFragment = new TimePickerFragment();
                timeFragment.show(getFragmentManager(), "timePicker");
            }
        });

        Button mapButton = (Button)findViewById(R.id.createEvent_button_map);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateEventActivity.this, LocationNewEventActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                CreateEventActivity.this.startActivity(intent);
            }
        });
/*
        Button mapButton = (Button)findViewById(R.id.createEvent_button_map);


        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment frag = new locationFragment();
                frag.show(getFragmentManager(), "LocationFragment");
            }
        });*/

       // mapButton.setOnClickListener((view) -> {
        //       DialogFragment locationFragment = new LocationPickFragment();
        //});
    }

    private class submit extends AsyncTask<String, String, String> {
        Context context;
        private submit(Context context){
            this.context = context.getApplicationContext();
        }

        @Override
        protected String doInBackground(String... params) {
            if(noInternet()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CreateEventActivity.this, R.string.noInternet, Toast.LENGTH_LONG).show();
                        spinner.setVisibility(View.GONE);
                    }
                });
                this.cancel(true);
            }

            if(!isCancelled()) {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost post = new HttpPost(Constants.api_base + Constants.createEvent);
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
                    Toast.makeText(context, result.getString("message"), Toast.LENGTH_SHORT).show();
                    CreateEventActivity.this.finish();
                } else {
                    Toast.makeText(getApplicationContext(), result.getString("message"), Toast.LENGTH_LONG).show();
                }
            } catch(JSONException e) {
                Toast.makeText(getApplicationContext(), "DEBUG: SOME ERROR", Toast.LENGTH_LONG).show();
                spinner.setVisibility(View.GONE);
            }
            spinner.setVisibility(View.GONE);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_event, menu);
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

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Button dateButton = (Button)this.getActivity().findViewById(R.id.createEvent_button_date);
            dateButton.setText(String.valueOf(month+1) + "/" + String.valueOf(day) + "/" + String.valueOf(year));
        }
    }

    //public static class locationFragment extends DialogFragment  implements DialogInterface.OnClickListener {
       /* View mMap =  ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getView();

        public Dialog onCreateDialog(Bundle savedInstanceState){
            return new AlertDialog.Builder(getActivity()).setPositiveButton("Submit", this).setView(mMap).create();
        }

        @Override
        public void onClick(DialogInterface dialog, int position) {
            dialog.dismiss();
        }*/
    //}

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Button timeButton = (Button)this.getActivity().findViewById(R.id.createEvent_button_time);
            timeButton.setText(timeFormatter(hourOfDay, minute, DateFormat.is24HourFormat(getActivity())));
        }

        private String timeFormatter(int hour, int minute, Boolean is24) {
            String ret = "";
            if(is24) {
                ret = String.valueOf(hour) + ":" + String.valueOf(minute);
            } else if(hour > 12) {
                ret += String.valueOf(hour - 12) + ":";
                if(minute < 10) {
                    ret += "0";
                }
                ret += String.valueOf(minute);
                ret += " PM";
            } else {
                ret += String.valueOf(hour) + ":";
                if(minute < 10) {
                    ret += "0";
                }
                ret += String.valueOf(minute);
                ret += " AM";
            }
            return ret;
        }
    }

    private boolean noInternet() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if(ni == null) {
            // No Internet
            return true;
        }
        return false;
    }
}

