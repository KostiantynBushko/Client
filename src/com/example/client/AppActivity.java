package com.example.client;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by kbushko on 12/31/13.
 */
public class AppActivity extends Activity {

    Bitmap image;
    String path;
    String name;
    String description;
    String packageName;
    String userName;
    String url;
    String versionName;
    Context context;
    Boolean alredyInstalled = false;
    Button actionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.app_layout);

        context = this;

        Intent intent = getIntent();
        image = (Bitmap)intent.getParcelableExtra("image");
        ImageView imageView = (ImageView)findViewById(R.id.icon);
        name = intent.getStringExtra("name");
        path = intent.getStringExtra("path");
        description = intent.getStringExtra("description");
        packageName = intent.getStringExtra("packageName");
        userName = intent.getStringExtra("developer");
        versionName = intent.getStringExtra("versionName");
        url = intent.getStringExtra("url");

        Log.i("info"," Package name = " + packageName);

        android.content.pm.PackageManager mPm = getPackageManager();
        try {
            PackageInfo info = mPm.getPackageInfo(packageName, 0);
            alredyInstalled = info != null;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (image != null)
            imageView.setImageBitmap(image);

        TextView tv_name = (TextView)findViewById(R.id.text1);
        TextView tv_path = (TextView)findViewById(R.id.text2);
        TextView tv_description = (TextView)findViewById(R.id.description);
        ((TextView)findViewById(R.id.userName)).setText(userName);
        ((TextView)findViewById(R.id.url)).setText(url);
        ((TextView)findViewById(R.id.versionName)).setText(versionName);

        //RatingBar rating = (RatingBar)findViewById(R.id.ratingBar);

        actionButton = (Button)findViewById(R.id.action);
        if (alredyInstalled){
            actionButton.setText("uninstall");
            actionButton.setBackgroundResource(R.drawable.button_rect_red);
        }else{
            actionButton.setText("install");
        }

        tv_description.setGravity(Gravity.CENTER_HORIZONTAL);
        tv_name.setText(name);
        tv_path.setText(path);
        tv_description.setText(description);


        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (alredyInstalled){
                    Uri uri = Uri.parse("package:"+packageName);
                    Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, uri);
                    startActivity(intent);
                }else {
                    String[] param = {path,name};
                    new OpenFileTask(context).execute(param);
                }
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        if (alredyInstalled){
            actionButton.setText("uninstall");
            actionButton.setBackgroundResource(R.drawable.button_rect_red);
        }else{
            actionButton.setText("install");
        }
    }

    private String get_cache_path(){
        File cacheDir;
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)){
            cacheDir=new File(android.os.Environment.getExternalStorageDirectory(),"Client");
        }else {
            cacheDir = getCacheDir();
        }

        if(!cacheDir.exists())
            cacheDir.mkdirs();
        return cacheDir.getAbsolutePath();
    }
    /**********************************************************************************************/
    /* open file */
    /**********************************************************************************************/
    class OpenFileTask extends AsyncTask<String, Void, Boolean> {
        ProgressDialog progressDialog = null;
        private Context context;

        public OpenFileTask(Context context) {
            this.context = context;
        }
        @Override
        protected void onPreExecute(){
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progressDialog.dismiss();
            progressDialog.cancel();
        }

        @Override
        protected Boolean doInBackground(String... file) {
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
            HttpConnectionParams.setSoTimeout(httpParams,10000);

            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
            HttpGet httpGet = null;
            try {
                Log.i("info"," get file path = " + file[0]);
                httpGet = new HttpGet(URL.host + "/get_app/?path=" + URLEncoder.encode(file[0], "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return false;
            }

            HttpContext httpContext = new BasicHttpContext();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, SApplication.cookieStore);

            try {
                HttpResponse httpResponse = httpClient.execute(httpGet,httpContext);
                //Log.i("info", httpResponse.getFirstHeader("Content-length").toString());
                byte[] _file_ = EntityUtils.toByteArray(httpResponse.getEntity());
                File f = new File(get_cache_path(), file[1]);
                FileOutputStream fos = new FileOutputStream(f);
                fos.write(_file_, 0, _file_.length);
                fos.flush();
                fos.close();
                byte[] newByte = new byte[15];
                System.arraycopy(_file_, 0, newByte, 0, 15);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(f), "application/vnd.android.package-archive");
                startActivity(intent);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

    }
}
