package com.desperateundergadstudio.gather;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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

    JSONObject event;

    private JSONArray eventComments;
    private ListView commentList;
    private CommentListAdapter commentArrayAdapter;
    private ArrayList<JSONObject> comments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        try {
            event = new JSONObject(getIntent().getStringExtra("currentEvent"));
            TextView eventTitle = (TextView)findViewById(R.id.event_textView_eventTitle);
            TextView attendees = (TextView)findViewById(R.id.event_textView_attendees);
            TextView eventDescription = (TextView)findViewById(R.id.event_textView_eventDescription);
            eventTitle.setText(event.getString("Title"));
            attendees.setText("Current Number Of Attendees: " + event.getString("NumAttending"));
            eventDescription.setText(event.getString("Description"));

            JSONObject json = new JSONObject();
            json.put("eventid", event.getString("EventID"));
            new getComments(EventActivity.this).execute(json.toString());
        } catch(JSONException e) {

        }

        // Create comment list
        commentList = (ListView)findViewById(R.id.event_listView_comments);
        comments = new ArrayList<JSONObject>();
        commentArrayAdapter = new CommentListAdapter(EventActivity.this, R.layout.comments_listitems, comments);
        commentList.setAdapter(commentArrayAdapter);
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
