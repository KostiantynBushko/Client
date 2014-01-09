package com.example.client;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dialog.OpenFileDialog;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kbushko on 1/2/14.
 */
public class UploadAppActivity extends Activity implements OpenFileDialog.onButtonClickListener{
    ImageView appIcon;
    Bitmap iconBitmap;
    Button uploadApk;
    String filePath = "";
    String serverApkPath = null;

    String appName = null;
    String description = "";
    String packageName = null;
    String versionName = null;
    String versionCode = null;
    String url = "";
    Context context;

    int stackLavel = 1;
    @Override
    public void onCreate(Bundle savedInstanseState){
        super.onCreate(savedInstanseState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.upload_app_layout);
        appIcon = (ImageView)findViewById(R.id.imageView);
        uploadApk = (Button)findViewById(R.id.run);

        context = this;

        //Upload button
        uploadApk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                description = ((EditText)findViewById(R.id.editText)).getText().toString();
                url = ((EditText)findViewById(R.id.editText2)).getText().toString();
                String[] param = {URL.host + "/add_app/"};
                new CreateAppRepo(context).execute(param);
            }
        });

        ((Button)findViewById(R.id.button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                DialogFragment newFragment = OpenFileDialog.newInstance(stackLavel);
                newFragment.show(ft, "dialog");
            }
        });
    }

    @Override
    public void onPositiveButtonClick(String filePath) {
        this.filePath = filePath;
        File file = new File(filePath);
        if (file != null){
            PackageManager pm = getPackageManager();
            PackageInfo packageInfo = pm.getPackageArchiveInfo(filePath,PackageManager.GET_ACTIVITIES);
            if(packageInfo != null) {
                ApplicationInfo appInfo = packageInfo.applicationInfo;
                if (Build.VERSION.SDK_INT >= 8) {
                    appInfo.sourceDir = filePath;
                    appInfo.publicSourceDir = filePath;
                }
                // App Icon
                Drawable icon = appInfo.loadIcon(getPackageManager());
                appIcon.setImageDrawable(icon);
                iconBitmap = ((BitmapDrawable)icon).getBitmap();

                //App name
                appName = (String)(appIcon != null ? pm.getApplicationLabel(appInfo) : "unknown");
                ((TextView)findViewById(R.id.textView2)).setText(appName);

                //Package name
                packageName = appInfo.packageName;
                ((TextView)findViewById(R.id.pakName)).setText(packageName);

                //Version name
                versionName = packageInfo.versionName;
                versionCode = Integer.toString(packageInfo.versionCode);
                ((TextView)findViewById(R.id.versionName)).setText(versionName);
                ((TextView)findViewById(R.id.versionCode)).setText(versionCode);

                uploadApk.setVisibility(View.VISIBLE);

            }
        }
    }

    /**********************************************************************************************/
    /* Create repository */
    /**********************************************************************************************/
    class CreateAppRepo extends AsyncTask<String, Void, Boolean> {
        ProgressDialog progressDialog = null;
        private Context context;
        String response = "";
        public CreateAppRepo(Context context){
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(String ... param) {
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 20000);
            HttpConnectionParams.setSoTimeout(httpParams, 20000);

            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
            HttpPost httpPost = new HttpPost(param[0]);

            List<NameValuePair>nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("appName",appName));
            nameValuePairs.add(new BasicNameValuePair("packageName",packageName));
            nameValuePairs.add(new BasicNameValuePair("versionName",versionName));
            nameValuePairs.add(new BasicNameValuePair("versionCode",versionCode));
            nameValuePairs.add(new BasicNameValuePair("description",description));
            nameValuePairs.add(new BasicNameValuePair("url",url));

            try{
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            }catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }
            try{
                HttpContext httpContext = new BasicHttpContext();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, SApplication.cookieStore);
                HttpResponse httpResponse = httpClient.execute(httpPost,httpContext);
                HttpEntity httpEntity = httpResponse.getEntity();
                response = EntityUtils.toString(httpEntity);
                return true;
            }catch (IOException e){
                e.printStackTrace();
            }
            return false;
        }
        @Override
        protected void onPreExecute(){
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("create repository...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.i("info"," responce = " + response);
            try {
                JSONArray array = new JSONArray(response.toString());
                JSONObject repo = array.getJSONObject(0);
                repo = repo.getJSONObject("fields");
                serverApkPath = repo.getString("path");
                new UploadImage(context).execute(URL.host + "/upload_data/");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            progressDialog.dismiss();
            progressDialog.cancel();
        }
    }
    /**********************************************************************************************/
    /* Upload image file  */
    /**********************************************************************************************/
    class UploadImage extends AsyncTask<String, Void, Boolean> {

        ProgressDialog progressDialog = null;
        private Context context;
        String response = "";
        public UploadImage(Context context){
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(String... url) {
            if (iconBitmap == null)
                return false;

            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpContext loadContext = new BasicHttpContext();
            HttpPost httpPost = new HttpPost(url[0]);

            //MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            //builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            iconBitmap.compress(Bitmap.CompressFormat.PNG, 20, bos);
            byte[] data = bos.toByteArray();

            entity.addPart("file_name", new ByteArrayBody(data, "image.png"));
            try {
                entity.addPart("file_path", new StringBody(serverApkPath));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            httpPost.setEntity(entity);

            /*builder.addPart("file", new ByteArrayBody(data, "image.png"));
            builder.addTextBody("path",serverApkPath);

            HttpEntity httpEntity = builder.build();
            httpPost.setEntity(httpEntity);*/

            HttpContext httpContext = new BasicHttpContext();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, SApplication.cookieStore);


            try {
                HttpResponse httpResponse = httpClient.execute(httpPost, httpContext);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        @Override
        protected void onPreExecute(){
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("upload image...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progressDialog.dismiss();
            progressDialog.cancel();
            if (result){
                new UploadApp(context).execute(URL.host + "/upload_data/");
            }
        }
    }
    /**********************************************************************************************/
    /**********************************************************************************************/
    /* Upload image file  */
    /**********************************************************************************************/
    class UploadApp extends AsyncTask<String, Void, Boolean> {

        ProgressDialog progressDialog = null;
        private Context context;
        String response = "";
        public UploadApp(Context context){
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(String... url) {
            if (iconBitmap == null)
                return false;

            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpContext loadContext = new BasicHttpContext();
            HttpPost httpPost = new HttpPost(url[0]);

            //MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            //builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            byte[] fileContext;
            File apk = new File(filePath);
            try {
                FileInputStream fileInputStream = new FileInputStream(apk);
                fileContext = new byte[(int)apk.length()];
                fileInputStream.read(fileContext);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            /*builder.addPart("file", new ByteArrayBody(fileContext,"app.apk"));
            builder.addTextBody("path",serverApkPath);*/

            //HttpEntity httpEntity = builder.build();
            //httpPost.setEntity(httpEntity);

            MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            entity.addPart("file_name", new ByteArrayBody(fileContext, "app.apk" ));
            try {
                entity.addPart("file_path", new StringBody(serverApkPath));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            httpPost.setEntity(entity);

            HttpContext httpContext = new BasicHttpContext();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, SApplication.cookieStore);

            try {
                HttpResponse httpResponse = httpClient.execute(httpPost, httpContext);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        @Override
        protected void onPreExecute(){
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("upload apk...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progressDialog.dismiss();
            progressDialog.cancel();
        }
    }
    /**********************************************************************************************/
}
