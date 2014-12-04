package com.desperateundergadstudio.gather;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;


public class EventActivity extends Activity {

    private static JSONObject event;
    private static SharedPreferences prefs;

    private JSONArray eventComments;
    private ListView commentList;
    private CommentListAdapter commentArrayAdapter;
    private ArrayList<JSONObject> comments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        prefs = getSharedPreferences(Constants.session_prefs, 0);

        try {
            event = new JSONObject(getIntent().getStringExtra("currentEvent"));
            TextView eventTitle = (TextView)findViewById(R.id.event_textView_eventTitle);
            TextView attendees = (TextView)findViewById(R.id.event_textView_attendees);
            TextView eventDescription = (TextView)findViewById(R.id.event_textView_eventDescription);
            eventTitle.setText(event.getString("Title"));
            attendees.setText("Current Number Of Attendees: " + event.getString("NumAttending"));
            eventDescription.setText(event.getString("Description"));
        } catch(JSONException e) {

        }
        amAttending();

        updateComments();

        // Create comment list
        commentList = (ListView)findViewById(R.id.event_listView_comments);
        comments = new ArrayList<JSONObject>();
        commentArrayAdapter = new CommentListAdapter(EventActivity.this, R.layout.comments_listitems, comments);
        commentList.setAdapter(commentArrayAdapter);

        setupButtons();
    }

    private void setupButtons() {
        final Button attendBTN = (Button)findViewById(R.id.event_button_attend);
        Button commentBTN = (Button)findViewById(R.id.event_button_comment);

        try {
            JSONArray attending = new JSONArray(prefs.getString("AttendingEvents", null));
            for (int i = 0; i < attending.length(); i++) {
                JSONObject j = attending.getJSONObject(i);
                if (j.getString("EventID").equals(event.getString("EventID"))) {
                    attendBTN.setText("UnAttend");
                    break;
                }
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }

        attendBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    JSONObject json = new JSONObject();
                    SharedPreferences prefs = getSharedPreferences(Constants.session_prefs, 0);
                    json.put("username", prefs.getString("UserName", null));
                    json.put("sessionid", prefs.getString("SessionID", null));
                    json.put("eventid", event.getString("EventID"));

                    new attendEvent(EventActivity.this).execute(json.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        commentBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment frag = new commentFragment();
                frag.show(getFragmentManager(), "CommentFragment");
            }
        });
    }

    private void amAttending() {
        try {
            String attendString = prefs.getString("AttendingEvents", null);
            JSONArray attending = new JSONArray(attendString);

            for(int i=0; i<attending.length(); i++) {
                JSONObject j = attending.getJSONObject(i);
                if(j.getString("EventID").equals(event.getString("EventID"))) {
                    Button attendBtn = (Button)findViewById(R.id.event_button_attend);
                    attendBtn.setText("UnAttend");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onSubmitComment(String comment) {
        try {
            JSONObject json = new JSONObject();
            json.put("username", prefs.getString("UserName", null));
            json.put("sessionid", prefs.getString("SessionID", null));
            json.put("eventid", event.getString("EventID"));
            json.put("comment", comment);
            new addComment(EventActivity.this).execute(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateComments() {
        try {
            JSONObject json = new JSONObject();
            json.put("eventid", event.getString("EventID"));
            new getComments(EventActivity.this).execute(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class getComments extends AsyncTask<String, String, String> {
        Context context;
        private getComments(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... params) {
            if(noInternet()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(EventActivity.this, R.string.noInternet, Toast.LENGTH_LONG).show();
                    }
                });
                this.cancel(true);
            }
            if(!isCancelled()) {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost post = new HttpPost(Constants.api_base + Constants.getEventComments);
                try {
                    String postParams = params[0];
                    StringEntity se = new StringEntity(postParams);
                    post.setEntity(se);
                    post.setHeader("Accept", "application/json");
                    post.setHeader("Content-type", "application/json");

                    HttpResponse response = httpclient.execute(post);
                    return EntityUtils.toString(response.getEntity());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String resultString) {
            try {
                JSONObject result = new JSONObject(resultString);
                if(!result.getString("type").equals("error")) {
                    eventComments = new JSONArray(result.getString("message"));
                    populateCommentList();
                } else {
                    Toast.makeText(context, result.getString("message"), Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class addComment extends AsyncTask<String, String, String> {
        Context context;
        private addComment(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... params) {
            if(noInternet()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, R.string.noInternet, Toast.LENGTH_LONG).show();
                    }
                });
                this.cancel(true);
            }

            if(!isCancelled()) {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost post = new HttpPost(Constants.api_base + Constants.addComment);
                try {
                    String postParams = params[0];
                    StringEntity se = new StringEntity(postParams);
                    post.setEntity(se);
                    post.setHeader("Accept", "application/json");
                    post.setHeader("Content-type", "application/json");

                    HttpResponse response = httpclient.execute(post);
                    return EntityUtils.toString(response.getEntity());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String resultString) {
            try {
                JSONObject result = new JSONObject(resultString);
                if (!result.getString("type").equals("error")) {
                    Toast.makeText(context, result.getString("message"), Toast.LENGTH_SHORT).show();
                    updateComments();
                } else {
                    Toast.makeText(context, result.getString("message"), Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class attendEvent extends AsyncTask<String, String, String> {
        Context context;
        private  attendEvent(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... params) {
            if(noInternet()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, R.string.noInternet, Toast.LENGTH_LONG).show();
                    }
                });
                this.cancel(true);
            }
            if(!isCancelled()) {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost post;
                Button attendBtn = (Button)findViewById(R.id.event_button_attend);
                if(attendBtn.getText().equals("Attend")) {
                    post = new HttpPost(Constants.api_base + Constants.attendEvent);
                } else {
                    post = new HttpPost(Constants.api_base + Constants.unattendEvent);
                }

                try {
                    String postParams = params[0];
                    StringEntity se = new StringEntity(postParams);
                    post.setEntity(se);
                    post.setHeader("Accept", "application/json");
                    post.setHeader("Content-type", "application/json");

                    HttpResponse response = httpclient.execute(post);
                    return EntityUtils.toString(response.getEntity());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String resultString) {
            try {
                JSONObject result = new JSONObject(resultString);
                if (!result.getString("type").equals("error")) {
                    Button attendBtn = (Button)findViewById(R.id.event_button_attend);
                    TextView attendees = (TextView)findViewById(R.id.event_textView_attendees);
                    String[] splitText = attendees.getText().toString().split(" ");
                    int updatedValue = Integer.parseInt(splitText[splitText.length-1]);
                    if(attendBtn.getText().equals("Attend")) {
                        attendBtn.setText("UnAttend");
                        updatedValue += 1;
                    } else {
                        attendBtn.setText("Attend");
                        updatedValue -= 1;
                    }
                    attendees.setText("Current Number Of Attendees: " + String.valueOf(updatedValue));
                } else {
                    Toast.makeText(context, result.getString("message"), Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void populateCommentList() {
        try {
            comments.clear();
            JSONObject j;
            for(int i=0; i<eventComments.length(); i++) {
                j = eventComments.getJSONObject(i);
                comments.add(j);
            }
            commentArrayAdapter.notifyDataSetChanged();
        } catch(Exception e) {

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

    public static class commentFragment extends DialogFragment implements DialogInterface.OnClickListener {
        private EditText commentField;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            commentField = new EditText(getActivity());
            commentField.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            return new AlertDialog.Builder(getActivity()).setTitle(R.string.app_name).setMessage("Enter your comment")
                    .setPositiveButton("Submit", this).setNegativeButton("Cancel", null).setView(commentField).create();
        }

        @Override
    public void onClick(DialogInterface dialog, int position) {
            String value = commentField.getText().toString();
            EventActivity callingActivity = (EventActivity)getActivity();
            callingActivity.onSubmitComment(value);
            dialog.dismiss();
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
