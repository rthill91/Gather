package com.desperateundergadstudio.logintest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.net.URL;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupLoginButton();
    }

    private void setupLoginButton() {
        // 1. Get a reference to the button
        Button loginButton = (Button)findViewById(R.id.button_login);
        // 2. Set the click listener to run code
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
        public void onClick(View v) {
                EditText username = (EditText)findViewById(R.id.textfield_username);
                EditText password = (EditText)findViewById(R.id.textfield_password);
                try {
                    JSONObject json = new JSONObject();
                    json.put("username", username.getText().toString());
                    json.put("password", password.getText().toString());

                    new login(MainActivity.this).execute(json.toString());
                } catch (JSONException e) {

                }
            }
        });

    }


    private class login extends AsyncTask<String, String, String> {
        Context context;
        private login(Context context) {
            this.context = context.getApplicationContext();
        }
        @Override
        protected String doInBackground(String... params) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost post = new HttpPost(Constants.api_base + Constants.login);
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

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();

            Intent intent = new Intent(context, SecondActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
