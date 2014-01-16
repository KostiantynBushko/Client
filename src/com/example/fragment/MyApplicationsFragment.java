package com.example.fragment;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.client.R;
import com.example.client.SApplication;
import com.example.client.URL;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by kbushko on 1/13/14.
 */
public class MyApplicationsFragment extends Fragment {

    private static final String NAME  = "name";
    private static final String PATH = "path";
    private static final String U_ID  = "u_id";
    private static final String ICON  = "icon";
    private static final String DESCRIPTION  = "description";
    private static final String PACKAGE = "package";
    private static final String APP_URL = "url";
    private static final String DEVELOPER = "developer";
    private static final String VERSION_NAME = "versionname";
    private static final String DATE_TIME = "date_time";

    private final int GET_ITEM_LIMIT = 6;
    private JSONArray applicationArray;
    private int offset = 0;
    private int countItems = 0;

    ArrayList<HashMap<String, Object>> listApp;
    private ListView listView;
    private int firstVisibleItem = 0;
    private int visibleCountItem = 0;
    boolean isRunning = false;
    LruCache<String, Bitmap> mMemoryCach;

    ViewGroup root;

    private final String TIME_FORMATER = "HH:mm yyyy/MM/dd";
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TIME_FORMATER);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = (ViewGroup)inflater.inflate(R.layout.my_app_list, null);
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
    public void onResume(){
        super.onResume();
        countItems = 0;
        offset = 0;

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
                        new LoadAppTask().execute(URL.host + "/user_app_list");
                    }
                }
            }
        });
        new LoadAppTask().execute(URL.host + "/user_app_list");
    }


    /**********************************************************************************************/
    /* Load list application */
    /**********************************************************************************************/
    class LoadAppTask extends AsyncTask<String, Void, Boolean> {
        String response = "";
        @Override
        protected Boolean doInBackground(String... url) {

            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
            HttpConnectionParams.setSoTimeout(httpParams, 10000);

            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
            HttpGet httpGet = new HttpGet(url[0] + "?offset="+Integer.toString(offset)+"&limit="+Integer.toString(GET_ITEM_LIMIT));

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
                    applicationArray = new JSONArray(response.toString());
                    HashMap<String, Object>resurce;
                    int id = offset;
                    Log.i("info", " contact length = " + Integer.toString(applicationArray.length()));
                    if (applicationArray.length() > 0){
                        offset += applicationArray.length();
                        for (int i = 0; i< applicationArray.length(); i++) {
                            App = applicationArray.getJSONObject(i);
                            App = App.getJSONObject("fields");
                            resurce = new HashMap<String, Object>();
                            resurce.put(NAME,App.opt("name"));
                            resurce.put(PATH, App.opt("path"));
                            resurce.put(ICON, R.drawable.app_icon);
                            resurce.put(U_ID,Integer.toString(id++));
                            resurce.put(DESCRIPTION,App.opt("description"));
                            resurce.put(PACKAGE,App.opt("packageName"));
                            resurce.put(DEVELOPER,App.opt("user"));
                            resurce.put(APP_URL,App.opt("url"));
                            resurce.put(VERSION_NAME,App.opt("versionName"));

                            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                            try{
                                Date d = inputFormat.parse(App.opt("date").toString());
                                resurce.put(DATE_TIME,simpleDateFormat.format(d));
                            }catch (ParseException e){
                                e.printStackTrace();
                            }
                            listApp.add(resurce);

                            //new DownloadImage(countItems++).execute(URL.host + "/app_image/?path=" + App.opt("path"));
                        }

                        if (listView.getAdapter() == null) {
                            SimpleAdapter adapter = new SimpleAdapter(getActivity(), listApp,R.layout.app_list_item_2,
                                    new String[]{NAME,PACKAGE,DATE_TIME,ICON,U_ID},
                                    new int[]{R.id.text1, R.id.textView,R.id.textView2,R.id.icon, R.id.u_id});
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
}
