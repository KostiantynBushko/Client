package com.example.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.client.AppActivity;
import com.example.client.R;
import com.example.client.SApplication;
import com.example.client.URL;
import com.example.common.FileHelper;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by kbushko on 12/31/13.
 */
public class AppStoreFragment extends Fragment {

    private final int GET_ITEM_LIMIT = 6;
    private JSONArray contacts;
    private int offset = 0;
    private int limit = GET_ITEM_LIMIT;
    private int countItems = 0;

    ArrayList<HashMap<String, Object>> listApp;
    private static final String NAME  = "name";
    private static final String PATH = "path";
    private static final String U_ID  = "u_id";
    private static final String ICON  = "icon";
    private static final String DESCRIPTION  = "description";
    private ListView listView;

    private int firstVisibleItem = 0;
    private int visibleCountItem = 0;

    boolean isRunning = false;
    LruCache<String, Bitmap> mMemoryCach;
    boolean mShowingBack = false;
    ViewGroup root;
    LoadContactTask loadContactTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = (ViewGroup)inflater.inflate(R.layout.contact_layout, null);
        return root;
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCach.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        Bitmap bitmap = mMemoryCach.get(key);
        return bitmap;
    }

    @Override
    public void onResume() {
        countItems = 0;
        offset = 0;
        limit = GET_ITEM_LIMIT;

        final int maxMemory  = (int)(Runtime.getRuntime().maxMemory()/1024);
        final int cacheSize = maxMemory;
        mMemoryCach = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap){
                return bitmap.getByteCount() /1024;
            }
        };

        listView = (ListView)root.findViewById(R.id.listView);
        listView.setDivider(null);
        listView.setDividerHeight(15);
        listApp = new ArrayList<HashMap<String, Object>>();
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {}
            @Override
            public void onScroll(AbsListView absListView, int firstVisible, int visibleCount, int totalCount) {
                firstVisibleItem = firstVisible;
                visibleCountItem = visibleCount;
                for (int i=0; i<visibleCount; i++){
                    View v = absListView.getChildAt(i);
                    ImageView iv = (ImageView)v.findViewById(R.id.icon);
                    Bitmap bitmap = getBitmapFromMemCache(Integer.toString(i+firstVisibleItem));
                    if (iv != null) {
                        if (bitmap != null){
                            iv.setImageBitmap(bitmap);
                        }
                    }
                }
                if (++firstVisible + visibleCount > totalCount && totalCount > 0){
                    if (!isRunning){
                        isRunning = true;
                        new LoadContactTask().execute(URL.host + "/app_list/");
                    }
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int item, long l) {
                HashMap<String, Object>object = listApp.get(item);
                Bitmap image = getBitmapFromMemCache(Integer.toString(item));
                String name = (String)object.get(NAME);
                String path = (String)object.get(PATH);
                String description = (String)object.get(DESCRIPTION);


                Intent intent = new Intent(getActivity(), AppActivity.class);
                intent.putExtra("image",image);
                intent.putExtra("name",name);
                intent.putExtra("path",path);
                intent.putExtra("description",description);

                startActivity(intent);
            }
        });

        loadContactTask = new LoadContactTask();
        loadContactTask.execute(URL.host + "/app_list/");
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        loadContactTask.cancel(true);
    }


    /**********************************************************************************************/
    class LoadContactTask extends AsyncTask<String, Void, Boolean> {
        String response = "";
        @Override
        protected Boolean doInBackground(String... url) {

            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
            HttpConnectionParams.setSoTimeout(httpParams, 10000);

            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
            HttpGet httpGet = new HttpGet(url[0] + "?offset="+Integer.toString(offset)+"&limit="+Integer.toString(limit));

            HttpContext httpContext = new BasicHttpContext();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, SApplication.cookieStore);

            try {
                HttpResponse httpResponse = httpClient.execute(httpGet, httpContext);
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
            super.onPostExecute(result);
            if (result){
                JSONObject App;
                try {
                    contacts = new JSONArray(response.toString());
                    HashMap<String, Object>resurce;
                    int id = offset;
                    Log.i("info", " contact length = " + Integer.toString(contacts.length()));
                    if (contacts.length() > 0){
                        offset += contacts.length();
                        for (int i = 0; i< contacts.length(); i++) {
                            App = contacts.getJSONObject(i);
                            App = App.getJSONObject("fields");
                            resurce = new HashMap<String, Object>();
                            resurce.put(NAME,App.opt("name"));
                            resurce.put(PATH, App.opt("path"));
                            resurce.put(ICON, R.drawable.app_icon);
                            resurce.put(U_ID,Integer.toString(id++));
                            resurce.put(DESCRIPTION,App.opt("description"));
                            listApp.add(resurce);
                            new DownloadImage(countItems++).execute(URL.host + "/app_image/?path=" + App.opt("path"));
                        }

                        if (listView.getAdapter() == null) {
                            SimpleAdapter adapter = new SimpleAdapter(getActivity(), listApp,R.layout.app_list_item,
                                    new String[]{NAME,PATH,ICON,U_ID},
                                    new int[]{R.id.text1, R.id.text2,R.id.icon, R.id.u_id});
                            listView.setAdapter(adapter);
                            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                        }else {
                            BaseAdapter adapter = (BaseAdapter)listView.getAdapter();
                            adapter.notifyDataSetChanged();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                Toast toast = Toast.makeText(getActivity().getApplicationContext(),"Error connection...",Toast.LENGTH_SHORT);
                toast.show();
            }
            isRunning = false;
        }
    }
    /**********************************************************************************************/
    /* Download image task */
    class DownloadImage extends AsyncTask<String, Void, Bitmap> {
        int item = 0;
        Bitmap bitmap = null;
        public DownloadImage(int item) {
            this.item = item;
        }
        @Override
        protected Bitmap doInBackground(String... url) {
            Log.i("info"," path = " + url[0]);
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
            HttpConnectionParams.setSoTimeout(httpParams,10000);

            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = null;
            String m_url = url[0].replace(" ","%20");
            httpGet = new HttpGet(m_url);

            HttpContext httpContext = new BasicHttpContext();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, SApplication.cookieStore);

            try {
                HttpResponse httpResponse = httpClient.execute(httpGet,httpContext);
                byte[] image = EntityUtils.toByteArray(httpResponse.getEntity());
                bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                if (bitmap != null) {
                    addBitmapToMemoryCache(Integer.toString(item), bitmap);
                }else{
                    return null;
                }
                return bitmap;

            }catch(IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        public void onPostExecute(Bitmap image) {
            if (image != null) {
                View view = listView.getChildAt(item - firstVisibleItem);
                if (view != null) {
                    ImageView imageView = (ImageView)view.findViewById(R.id.icon);
                    if (imageView != null) {
                        imageView.setImageBitmap(image);
                    }
                }
            }
        }
    }

    /* open file */
    class OpenFileTask extends AsyncTask<String, Void, Boolean> {
        ProgressDialog progressDialog = null;
        @Override
        protected void onPreExecute(){
            Context context = getActivity();
            progressDialog = new ProgressDialog(getActivity());
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
            HttpConnectionParams.setConnectionTimeout(httpParams,10000);
            HttpConnectionParams.setSoTimeout(httpParams,10000);

            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
            HttpGet httpGet = null;
            try {
                httpGet = new HttpGet(URL.host + "/get_app/?path=" + URLEncoder.encode(file[0], "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return false;
            }

            HttpContext httpContext = new BasicHttpContext();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, SApplication.cookieStore);

            try {
                HttpResponse httpResponse = httpClient.execute(httpGet,httpContext);
                Log.i("info", httpResponse.getFirstHeader("Content-length").toString());
                byte[] _file_ = EntityUtils.toByteArray(httpResponse.getEntity());
                File f = new File(get_cache_path(), file[2]);
                FileOutputStream fos = new FileOutputStream(f);
                fos.write(_file_, 0, _file_.length);
                fos.flush();
                fos.close();
                byte[] newByte = new byte[15];
                System.arraycopy(_file_, 0, newByte, 0, 15);
                Log.i("info"," - files data = " + new String(_file_,"UTF-8"));
                Intent intent = FileHelper.openFileIntent(f);
                if(intent != null) {
                    startActivity(FileHelper.openFileIntent(f));
                    return true;
                }else {
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

    }
    private String get_cache_path(){
        File cacheDir;
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)){
            cacheDir=new File(android.os.Environment.getExternalStorageDirectory(),"Client");
        }else {
            cacheDir=getActivity().getCacheDir();
        }

        if(!cacheDir.exists())
            cacheDir.mkdirs();
        return cacheDir.getAbsolutePath();
    }
}
