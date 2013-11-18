package com.example.client;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by kbushko on 11/4/13.
 */
public class HomePageActivity extends Activity implements View.OnClickListener, ActionBar.TabListener{

    private JSONObject User = null;
    EditText eUserName = null;
    EditText eName = null;
    EditText eLastName = null;
    EditText eEmail = null;

    private ImageView icon;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.home_page);



        /*ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        ActionBar.Tab actionTab = actionBar.newTab();
        actionTab.setText("Page");
        actionTab.setTabListener(this);
        actionBar.addTab(actionTab);

        actionTab = actionBar.newTab();
        actionTab.setText("Users");
        actionTab.setTabListener(this);
        actionBar.addTab(actionTab);

        actionTab = actionBar.newTab();
        actionTab.setText("Setup");
        actionTab.setTabListener(this);
        actionBar.addTab(actionTab);*/


        Button SignOut = (Button)findViewById(R.id.bSignOut);
        SignOut.setOnClickListener(this);
        eUserName = (EditText)findViewById(R.id.eUserName);
        eName = (EditText)findViewById(R.id.eName);
        eLastName = (EditText)findViewById(R.id.eLastName);
        eEmail = (EditText)findViewById(R.id.eEmail);

        new LoadUserData().execute("http://192.168.12.122:8002/user/");

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bSignOut:
                new SignOutTask().execute("http://192.168.12.122:8002/logout/");
                break;
            default: break;
        }
    }

    // Action Bar tab listener
    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }


    // Async Task
    class LoadUserData extends AsyncTask<String, Void, Boolean> {
        String response = "";
        @Override
        protected Boolean doInBackground(String... url) {
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
            HttpConnectionParams.setSoTimeout(httpParams, 10000);

            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
            HttpGet httpGet = new HttpGet(url[0]);

            HttpContext httpContext = new BasicHttpContext();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, SApplication.cookieStore);

            try {
                HttpResponse httpResponse = httpClient.execute(httpGet,httpContext);
                HttpEntity httpEntity = httpResponse.getEntity();
                response = EntityUtils.toString(httpEntity);
                Log.i("info",response.toString());

            } catch (IOException e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result){
            try {
                JSONArray array = new JSONArray(response.toString());
                User = array.getJSONObject(0);
                User = User.getJSONObject("fields");
                eUserName.setText(User.getString("username"));
                eName.setText(User.getString("first_name"));
                eLastName.setText(User.getString("last_name"));
                eEmail.setText(User.getString("email"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    class SignOutTask extends AsyncTask<String, Void, Boolean> {

       @Override
       protected Boolean doInBackground(String... url) {
           HttpParams httpParams = new BasicHttpParams();
           HttpConnectionParams.setConnectionTimeout(httpParams, 25000);
           HttpConnectionParams.setSoTimeout(httpParams, 25000);

           DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
           HttpGet httpGet = new HttpGet(url[0]);

           HttpContext httpContext = new BasicHttpContext();
           httpContext.setAttribute(ClientContext.COOKIE_STORE, SApplication.cookieStore);

           try {

               HttpResponse httpResponse = httpClient.execute(httpGet, httpContext);
               HttpEntity httpEntity = httpResponse.getEntity();
               Log.i("info", EntityUtils.toString(httpEntity));

           } catch (IOException e) {
               e.printStackTrace();
           }
           SApplication.cookieStore.clear();
           return false;
       }

       @Override
       protected void onPostExecute(Boolean result) {
           Intent intent = new Intent(getApplicationContext(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
           startActivity(intent);
           finish();
       }
    }
}
