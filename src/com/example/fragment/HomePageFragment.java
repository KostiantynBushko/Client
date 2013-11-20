package com.example.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.client.MainActivity;
import com.example.client.R;
import com.example.client.SApplication;
import com.example.client.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by kbushko on 11/6/13.
 */
public class HomePageFragment extends Fragment implements View.OnClickListener{

    private JSONObject User = null;
    EditText eUserName = null;
    EditText eName = null;
    EditText eLastName = null;
    EditText eEmail = null;

    private ImageView icon;
    private Uri imageUri = null;
    private Bitmap bitmap = null;
    private String imageName = null;

    public static Fragment newInstance(Context context) {
        HomePageFragment homePageFragment = new HomePageFragment();
        return homePageFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        ViewGroup root = (ViewGroup)inflater.inflate(R.layout.home_page,null);

        Button SignOut = (Button)root.findViewById(R.id.bSignOut);
        SignOut.setOnClickListener(this);
        eUserName = (EditText)root.findViewById(R.id.eUserName);
        eName = (EditText)root.findViewById(R.id.eName);
        eLastName = (EditText)root.findViewById(R.id.eLastName);
        eEmail = (EditText)root.findViewById(R.id.eEmail);

        icon = (ImageView)root.findViewById(R.id.icon);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 0);
            }
        });

        icon.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new DownloadImage().execute(URL.host + "/load/");
                return true;
            }
        });

        Button upload = (Button)root.findViewById(R.id.upload);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new UploadImage().execute(URL.host + "/upload/");
            }
        });
        new DownloadImage().execute(URL.host + "/load/");

        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        if (resultCode == getActivity().RESULT_OK){
            imageUri = intent.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getActivity().getContentResolver().query(imageUri,filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            Log.i("info","File name = " + picturePath);
            imageName = null;
            if (picturePath != null){
                File file = new File(picturePath.toString());
                imageName = file.getName();
                Log.i("info"," Image name = " + imageName.toString());
            }else{
                SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
                String dt = "IMG_" + format.format(new Date()) + ".jpg";
                Log.i("info"," -- Datae format  = " + dt);
            }

            try {
                bitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(imageUri));
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 20, out);
                icon.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.i("info"," -- HomePageFragment [ onSavedInstanceState ]");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume(){
        Log.i("info"," -- HomePageFragment [ onResume ]");
        super.onResume();
        new LoadUserData().execute(URL.host + "/user/");
    }

    @Override
    public void onStart(){
        Log.i("info", " -- HomePageFragment [ onStart ]");
        super.onStart();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            Log.i("info","LANDSCAPE");
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bSignOut:
                new SignOutTask().execute(URL.host + "/logout/");
                break;
            default: break;
        }
    }

    // Async Task //
    private class LoadUserData extends AsyncTask<String, Void, Boolean> {
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
            Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            getActivity().finish();
        }
    }


    // UPLOADING FILE //
    class UploadImage extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... url) {

            if (bitmap == null)
                return false;

            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpContext loadContext = new BasicHttpContext();
            HttpPost httpPost = new HttpPost(url[0]);

            MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 20, bos);
            byte[] data = bos.toByteArray();

            try {
                entity.addPart("photoId", new StringBody("photoId"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            entity.addPart("file", new ByteArrayBody(data, "image.png"/*imageName.toString()*/ ));
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
        public void onPostExecute(Boolean result) {
            Log.i("info", " Execute result = " + result.toString());
        }
    }

    public static String getMimeType(String url) {
        String extension = url.substring(url.lastIndexOf("."));
        String mimeTypeMap = MimeTypeMap.getFileExtensionFromUrl(extension);
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(mimeTypeMap);
        return mimeType;
    }

    class DownloadImage extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... url) {
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams,10000);
            HttpConnectionParams.setSoTimeout(httpParams,10000);

            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url[0]);

            HttpContext httpContext = new BasicHttpContext();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, SApplication.cookieStore);

            try {
                HttpResponse httpResponse = httpClient.execute(httpGet,httpContext);
                byte[] image = EntityUtils.toByteArray(httpResponse.getEntity());
                Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                return bitmap;
            }catch(IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void onPostExecute(Bitmap image) {
            if (image != null) {
                bitmap = image;
                icon.setImageBitmap(bitmap);
            }
        }
    }

    private static Bitmap codec(Bitmap src) {
        //Bitmap src,
        // Bitmap.CompressFormat format,
        int quality = 20;

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        src.compress(Bitmap.CompressFormat.PNG, quality, os);

        byte[] array = os.toByteArray();
        return BitmapFactory.decodeByteArray(array, 0, array.length);
    }
}