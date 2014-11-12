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
import android.widget.EditText;
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


public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setupLoginButton();
        setupRegisterButton();
    }

    private void setupLoginButton() {
        Button loginButton = (Button)findViewById(R.id.button_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText username = (EditText)findViewById(R.id.textfield_username);
                EditText password = (EditText)findViewById(R.id.textfield_password);
                try {
                    JSONObject json = new JSONObject();
                    json.put("username", username.getText().toString());
                    json.put("password", password.getText().toString());

                    new login(LoginActivity.this).execute(json.toString());
                } catch (JSONException e) {

                }
            }
        });
    }

    private void setupRegisterButton() {
        Button registerButton = (Button)findViewById(R.id.button_register);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                LoginActivity.this.startActivity(intent);
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
        protected void onPostExecute(String resultString) {
            try {
                JSONObject result = new JSONObject(resultString);
                if(!result.getString("type").equals("error")) {
                    SharedPreferences prefs = getSharedPreferences(Constants.session_prefs, 0);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("SessionID", result.getString("message"));
                    editor.commit();



                    Intent intent = new Intent(context, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    LoginActivity.this.finish();
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
        getMenuInflater().inflate(R.menu.menu_login, menu);
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
