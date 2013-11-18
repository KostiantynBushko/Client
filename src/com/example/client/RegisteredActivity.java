package com.example.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kbushko on 10/28/13.
 */
public class RegisteredActivity extends Activity implements View.OnClickListener{

    private EditText eUserName = null;
    private EditText eName = null;
    private EditText eSecondName = null;
    private EditText eEmail = null;
    private EditText ePassword_1 = null;
    private EditText ePassword_2 = null;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.registration);

        eUserName = (EditText)findViewById(R.id.UserName);
        eName = (EditText)findViewById(R.id.Name);
        eSecondName = (EditText)findViewById(R.id.SecondName);
        eEmail = (EditText)findViewById(R.id.Email);
        ePassword_1 = (EditText)findViewById(R.id.Password);
        ePassword_2 = (EditText)findViewById(R.id.ConfirmPassword);

        Button Registered = (Button)findViewById(R.id.bSend);
        Registered.setOnClickListener(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
        overridePendingTransition(R.anim.in,R.anim.out);
    }

    @Override
    protected void onPause(){
        super.onPause();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bSend:
                new runHttpPost().execute("http://192.168.12.122:8002/new_user/");
                break;
            default: break;
        }
    }


    class runHttpPost extends AsyncTask<String, Void, Boolean>{

        private String message = "";
        private String title = "";
        ProgressDialog progressDialog = null;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(RegisteredActivity.this,"Registered"," please wait...",true);
        }
        @Override
        protected Boolean doInBackground(String... url) {
            if(ePassword_1.length() == 0 || ePassword_2.length() == 0 ||
                    eEmail.length() == 0 || eName.length() == 0 || eSecondName.length() == 0){
                title = "Worning";
                message = "Please complate all fields";
                return false;
            }

            if(!ePassword_1.getText().toString().equals(ePassword_2.getText().toString()) ||
                    ePassword_1.getText().length() == 0) {
                title = "Worning";
                message = "Incorrect password";
                return false;
            }

            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 25000);
            HttpConnectionParams.setSoTimeout(httpParams, 25000);

            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
            HttpPost httpPost = new HttpPost(url[0]);

            List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(5);
            nameValuePair.add(new BasicNameValuePair("user_name",eUserName.getText().toString()));
            nameValuePair.add(new BasicNameValuePair("name",eName.getText().toString()));
            nameValuePair.add(new BasicNameValuePair("second_name",eSecondName.getText().toString()));
            nameValuePair.add(new BasicNameValuePair("email",eEmail.getText().toString()));
            nameValuePair.add(new BasicNameValuePair("password",ePassword_1.getText().toString()));

            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            try {
                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();

                List<Cookie> cookies = httpClient.getCookieStore().getCookies();

                message = EntityUtils.toString(httpEntity);
                title = "Message";

                Log.i("info", cookies.toString());
                Log.i("info",message);

                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            title = "Worning";
            message = "Server don't response";
            return false;
        }
        @Override
        protected void onPostExecute(Boolean result){
            super.onPostExecute(result);
            progressDialog.dismiss();
            progressDialog.cancel();

            AlertDialog.Builder builder = new AlertDialog.Builder(RegisteredActivity.this);
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
