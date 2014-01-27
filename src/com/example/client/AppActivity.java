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
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    String date;
    int appId;
    long downloads;
    long size;
    long rating;

    Context context;
    Boolean alredyInstalled = false;
    Button actionButton;
    GridLayout gridLayout;
    RelativeLayout ratingLayout;
    RatingBar ratingBar;

    private final int INSTALL_APPLICATION = 1001;
    private final int UNINSTALL_APPLICATION = 1002;

    private final String TIME_FORMATER = "HH:mm yyyy/MM/dd";
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TIME_FORMATER);

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.application_layout);

        context = this;

        Intent intent = getIntent();
        image = (Bitmap)intent.getParcelableExtra("image");
        ImageView imageView = (ImageView)findViewById(R.id.imageView);
        name = intent.getStringExtra("name");
        path = intent.getStringExtra("path");
        description = intent.getStringExtra("description");
        packageName = intent.getStringExtra("packageName");
        userName = intent.getStringExtra("developer");
        versionName = intent.getStringExtra("versionName");
        url = intent.getStringExtra("url");
        date = intent.getStringExtra("date");
        appId = intent.getIntExtra("appId",-1);
        size = intent.getLongExtra("size", 0);
        downloads = intent.getLongExtra("downloads", 0);
        rating = intent.getLongExtra("total_rating", 0);

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        try{
            Date d = inputFormat.parse(date);
            ((TextView)findViewById(R.id.date)).setText(simpleDateFormat.format(d));
        }catch (ParseException e){
            e.printStackTrace();
        }

        android.content.pm.PackageManager mPm = getPackageManager();

        try {
            PackageInfo info = mPm.getPackageInfo(packageName, 0);
            alredyInstalled = info != null;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (image != null)
            imageView.setImageBitmap(image);

        TextView tv_name = (TextView)findViewById(R.id.textView);
        TextView tv_path = (TextView)findViewById(R.id.textView2);
        TextView tv_description = (TextView)findViewById(R.id.description);
        ((TextView)findViewById(R.id.userName)).setText(userName);
        ((TextView)findViewById(R.id.url)).setText(url);
        ((TextView)findViewById(R.id.versionName)).setText(versionName);
        ((TextView)findViewById(R.id.textView3)).setText(Integer.toString(appId));
        ((TextView)findViewById(R.id.downloads)).setText(Long.toString(downloads));

        double dSize = size;
        String strSize = String.format("%.0f",dSize) + " B";
        if (dSize > 2048){
            dSize = dSize / 1024;
            strSize = String.format("%.1f",dSize)+ " Kb";
        }
        if (dSize > 2048)
            strSize = String.format("%.2f",dSize/1024) + " Mb";
        ((TextView)findViewById(R.id.appSize)).setText(strSize);

        gridLayout = (GridLayout)findViewById(R.id.gridLayout);
        ratingLayout = (RelativeLayout)findViewById(R.id.ratingLayout);
        ratingBar = (RatingBar)findViewById(R.id.ratingBar);

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
                    startActivityForResult(intent, UNINSTALL_APPLICATION);
                }else {
                    String[] param = {path,name};
                    new OpenFileTask(context).execute(param);
                }
            }
        });

        addImageToGrid(null);
        addImageToGrid(null);
        addImageToGrid(null);
        addImageToGrid(null);

        if (!alredyInstalled){
            ratingLayout.setVisibility(View.GONE);
        }
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float value, boolean b) {
                Log.i("info","Rate = " + Float.toString(value));
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == INSTALL_APPLICATION) {
            alredyInstalled = true;
            actionButton.setText("uninstall");
            actionButton.setBackgroundResource(R.drawable.button_rect_red);
            ratingLayout.setVisibility(View.VISIBLE);
            ratingBar.setRating(0);
        }else if(requestCode == UNINSTALL_APPLICATION) {
            alredyInstalled = false;
            actionButton.setText("install");
            actionButton.setBackgroundResource(R.drawable.button_rect);
            ratingLayout.setVisibility(View.GONE);
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
    private void addImageToGrid(Bitmap bitmap){
        int c = gridLayout.getColumnCount();
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.default_user_icon_profile);

        GridLayout.LayoutParams param = new GridLayout.LayoutParams();
        param.height = GridLayout.LayoutParams.WRAP_CONTENT;
        param.width = GridLayout.LayoutParams.WRAP_CONTENT;
        param.columnSpec = GridLayout.spec(c+1);
        param.rowSpec = GridLayout.spec(0);
        param.rightMargin = 5;
        param.leftMargin = 5;
        imageView.setLayoutParams(param);

        gridLayout.addView(imageView);
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
                startActivityForResult(intent, INSTALL_APPLICATION);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
    }
    /**********************************************************************************************/
    /* */
    /**********************************************************************************************/
    class GetResourcesFilesList extends AsyncTask<String, Void, Boolean> {
        private String response = "Server does not response";
        @Override
        protected Boolean doInBackground(String... strings) {
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams,20000);
            HttpConnectionParams.setSoTimeout(httpParams, 20000);

            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
            HttpPost httpPost = new HttpPost(URL.host + "/res_files");
            List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>();
            nameValuePairList.add(new BasicNameValuePair("name",strings[0]));

            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairList));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return false;
            }

            HttpContext httpContext = new BasicHttpContext();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, SApplication.cookieStore);

            try {
                HttpResponse httpResponse = httpClient.execute(httpPost,httpContext);
                HttpEntity httpEntity = httpResponse.getEntity();
                response = EntityUtils.toString(httpEntity);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        @Override
        protected void onPostExecute(Boolean result){
            if (result){
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    String objectType = jsonObject.optString("model");
                    objectType = objectType.substring(objectType.lastIndexOf("."));
                    Log.i("info"," Object type = " + objectType);
                    if (!objectType.isEmpty() && objectType.equals(".error")){

                    }else{
                        Log.i("info"," JSON Object = " + jsonObject.toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
