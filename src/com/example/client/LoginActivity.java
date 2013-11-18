package com.example.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kbushko on 11/4/13.
 */

public class LoginActivity extends Activity implements View.OnClickListener{

    EditText eUserName = null;
    EditText ePassword = null;

    private String message = "";
    private String title = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login);

        eUserName = (EditText)findViewById(R.id.eUsername);
        ePassword = (EditText)findViewById(R.id.ePassword);

        Button SigIn = (Button)findViewById(R.id.bSigIn);
        Button Registered = (Button)findViewById(R.id.bRegistration);
        SigIn.setOnClickListener(this);
        Registered.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bSigIn:
                if(eUserName.length() == 0 || ePassword.length() == 0){
                    title = "Worning";
                    message = "Please complate fields";
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setTitle(title);
                    builder.setMessage(message);
                    builder.setCancelable(true);
                    builder.setPositiveButton("cancel",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }else{
                    new LoginTask().execute(URL.host + "/login/");//"http://192.168.12.122:8002/login/"
                }
                break;
            case R.id.bRegistration:
                Intent intent = new Intent(this, RegisteredActivity.class);
                startActivity(intent);
                break;
            default: break;
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        overridePendingTransition(R.anim.in_a,R.anim.out_a);
    }

    /**********************************************************************************************/
    /* Login async task */
    /**********************************************************************************************/
    class LoginTask extends AsyncTask<String, Void, Boolean> {
        private ProgressDialog progressDialog = null;
        boolean result = false;
        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(LoginActivity.this,"Is running"," please wait...",true);
        }

        @Override
        protected Boolean doInBackground(String... url) {
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 25000);
            HttpConnectionParams.setSoTimeout(httpParams, 25000);

            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
            HttpPost httpPost = new HttpPost(url[0]);
            Log.i("info",url[0]);

            List<NameValuePair> nameValuePairsList = new ArrayList<NameValuePair>(2);
            nameValuePairsList.add(new BasicNameValuePair("username", eUserName.getText().toString()));
            nameValuePairsList.add(new BasicNameValuePair("password",ePassword.getText().toString()));

            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairsList));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            try {
                HttpContext httpContext = new BasicHttpContext();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, SApplication.cookieStore);

                HttpResponse httpResponse = httpClient.execute(httpPost,httpContext);
                HttpEntity httpEntity = httpResponse.getEntity();

                Header[] header = httpResponse.getAllHeaders();
                for (Header h : header){
                    Log.i("info"," Key : " + h.getName().toString() + " Value : " + h.getValue().toString());
                }

                title = "Message";
                message = EntityUtils.toString(httpEntity);
                Log.i("info", message);

                if (message.equals("Invalid login or password") || message.equals("Dissable account")){
                    result = false;
                }else{
                    result = true;
                }

            } catch (IOException e) {
                title = "Worning";
                message = "Server don't response";
                result = false;
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            progressDialog.cancel();

            if (result == true) {
                Log.i("info","Success login");
                Intent intent = new Intent(getApplicationContext(), NavigationDrawer.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }else{
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle(title);
                builder.setMessage(message);
                builder.setCancelable(true);
                builder.setPositiveButton("cancel",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
    }
}
