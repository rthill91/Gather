package com.desperateundergadstudio.gather;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
    private ProgressBar spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //TODO: Debug code, remove this later
        EditText username = (EditText)findViewById(R.id.login_editText_username);
        EditText password = (EditText)findViewById(R.id.login_editText_password);
        username.setText("user");
        password.setText("user");
        //TODO: END todo
        setupButtons();
        spinner = (ProgressBar)findViewById(R.id.login_progress_spinner);
        spinner.setVisibility(View.GONE);
    }

    private void setupButtons() {
        // Login Button
        Button loginButton = (Button)findViewById(R.id.login_button_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                spinner.setVisibility(View.VISIBLE);
                EditText username = (EditText)findViewById(R.id.login_editText_username);
                EditText password = (EditText)findViewById(R.id.login_editText_password);
                try {
                    JSONObject json = new JSONObject();
                    json.put("username", username.getText().toString());
                    json.put("password", password.getText().toString());

                    new login(LoginActivity.this).execute(json.toString());
                } catch (JSONException e) {

                }
            }
        });

        // Register Button
        Button registerButton = (Button)findViewById(R.id.login_button_register);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                LoginActivity.this.startActivity(intent);
            }
        });

    }

    // Override back button
//    public void onBackPressed() {
//        this.finish();
//        Intent intent = new Intent(Intent.ACTION_MAIN);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        intent.addCategory(Intent.CATEGORY_HOME);
//        startActivity(intent);
//    }


    private class login extends AsyncTask<String, String, String> {
        Context context;
        private login(Context context) {
            this.context = context.getApplicationContext();
        }

        @Override
        protected String doInBackground(String... params) {
            if(noInternet()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginActivity.this, R.string.noInternet, Toast.LENGTH_LONG).show();
                        spinner.setVisibility(View.GONE);
                    }
                });
                this.cancel(true);
            }
            if(!isCancelled()) {
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
                    JSONObject message = result.getJSONObject("message");
                    SharedPreferences prefs = getSharedPreferences(Constants.session_prefs, 0);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("UserName", message.getString("UserName"));
                    editor.putString("Picture", message.getString("Picture"));
                    editor.putString("SessionID", message.getString("SessionID"));
                    editor.putString("UserID", message.getString("UserID"));
                    editor.apply();

                    JSONObject json = new JSONObject();
                    json.put("username", message.getString("UserName"));
                    json.put("sessionid", message.getString("SessionID"));

                    Intent intent = new Intent(context, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("currentUser", json.toString());
                    context.startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), result.getString("message"), Toast.LENGTH_LONG).show();
                    spinner.setVisibility(View.GONE);
                }
            } catch(Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "ERROR: " + e.getMessage(), Toast.LENGTH_LONG).show();
                spinner.setVisibility(View.GONE);
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
