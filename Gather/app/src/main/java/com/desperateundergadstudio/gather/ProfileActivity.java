package com.desperateundergadstudio.gather;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class ProfileActivity extends Activity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        prefs = getSharedPreferences(Constants.session_prefs, 0);

        setupLogoutButton();
        populateUserInfo();
    }

    private void setupLogoutButton() {
        Button logoutButton = (Button)findViewById(R.id.profile_button_logout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    SharedPreferences prefs = getSharedPreferences(Constants.session_prefs, 0);
                    String username = prefs.getString("UserName", null);
                    String sessionid = prefs.getString("SessionID", null);

                    JSONObject json = new JSONObject();
                    json.put("username", username);
                    json.put("sessionid", sessionid);

                    new logout(ProfileActivity.this).execute(json.toString());
                } catch (JSONException e) {

                }
            }
        });
    }

    private void populateUserInfo() {
        TextView helloText = (TextView)findViewById(R.id.profile_textView_username);
        helloText.setText("Hello " + prefs.getString("UserName", "DEFAULT") + "!");
    }

    private class logout extends AsyncTask<String, String, String> {
        Context context;
        private logout(Context context) {
            this.context = context.getApplicationContext();
        }

        protected String doInBackground(String... params) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost post = new HttpPost(Constants.api_base + Constants.logout);
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
            return null;
        }

        protected void onPostExecute(String resultString) {
            try {
                JSONObject result = new JSONObject(resultString);
                if(!result.getString("type").equals("error")) {
                    SharedPreferences prefs = getSharedPreferences(Constants.session_prefs, 0);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.remove("UserName");
                    editor.remove("Picture");
                    editor.remove("CurrentEvents");
                    editor.remove("NotifyEvents");
                    editor.remove("SessionID");
                    editor.apply();

                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(intent);
//                    ProfileActivity.this.finish();
                } else {
                    Toast.makeText(getApplicationContext(), result.getString("message"), Toast.LENGTH_LONG).show();
                }
            } catch(JSONException e) {

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
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
