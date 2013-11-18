package com.example.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by kbushko on 11/6/13.
 */
public class ContactPageFragment extends Fragment {

    private JSONArray contacts;
    private int offset = 0;
    private int limit = 10;
    private int countItems = 0;

    ArrayList<HashMap<String, Object>>listContact;
    private static String NAME = "name";
    private static String EMAIL = "email";
    private static String U_ID = "u_id";
    private static final String ICON = "icon";
    private ListView listView;

    private int firstVisibleItem = 0;
    private int visibleCountItem = 0;

    boolean is_runing = false;

    LruCache<String, Bitmap>mMemoryCach;

    public static Fragment newInstance(Context contewxt) {
        ContactPageFragment contactPageFragment = new ContactPageFragment();
        return contactPageFragment;
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup)layoutInflater.inflate(R.layout.contact_layout, null);
        listView = (ListView)root.findViewById(R.id.listView);
        listContact = new ArrayList<HashMap<String, Object>>();

        //Chash Bitmap
        Log.i("info","---------------------------------------------------------------------------");
        Log.i("info","---------------------------------------------------------------------------");
        Log.i("info","---------------------------------------------------------------------------");
        final int maxMemory  = (int)(Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory;
        Log.i("info","maxMemory = " + Integer.toString(maxMemory));
        Log.i("info","cacheSize = " + Integer.toString(cacheSize));

        mMemoryCach = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap){
                return bitmap.getByteCount() /1024;
            }
        };
        Log.i("info","---------------------------------------------------------------------------");



        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {}

            @Override
            public void onScroll(AbsListView absListView, int firstVisible, int visibleCount, int totalCount) {
                firstVisibleItem = firstVisible;
                visibleCountItem = visibleCount;

                Log.i("info"," First visible item = " + Integer.toString(firstVisible));
                Log.i("info"," Vsible count item  = " + Integer.toString(visibleCount));
                Log.i("info"," Total item count   = " + Integer.toString(totalCount));
                Log.i("info","-------------------------------------------------------------------");
                if (++firstVisible + visibleCount > totalCount && totalCount > 0){
                    Log.i("info", " **** Load more contact *** ");
                    if (!is_runing){
                        is_runing = true;
                        new LoadContactTask().execute(URL.host + "/user_list/");
                    }
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int item, long l) {
                Log.i("info"," - " + Integer.toString(item));
                Log.i("info"," - " + Integer.toString(item + firstVisibleItem));
                ImageView i = (ImageView)view.findViewById(R.id.icon);
                Bitmap bitmap = getBitmapFromMemCache(Integer.toString(item));
                i.setImageBitmap(bitmap);
            }
        });

        return root;
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCach.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCach.get(key);
    }

    @Override
    public void onResume(){
        Log.i("info", " -- ContactPageFragment [ onResume ]");
        super.onResume();
    }

    @Override
    public void onStart(){
        Log.i("info"," -- ContactPageFragment [ onStart ]");
        super.onStart();
        if (!is_runing){
            is_runing = true;
            new LoadContactTask().execute(URL.host + "/user_list/");
        }
    }

    //Load contact
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
                JSONObject User;
                try {
                    contacts = new JSONArray(response.toString());
                    Log.i("info",contacts.toString());
                    Log.i("info", " offset = " + Integer.toString(offset));
                    Log.i("info", " contact count = " + Integer.toString(contacts.length()));
                    HashMap<String, Object>resurce;

                    int id = offset;

                    if (contacts.length() > 0){
                        offset += contacts.length();
                        for (int i = 0; i< contacts.length(); i++) {
                            User = contacts.getJSONObject(i);
                            User = User.getJSONObject("fields");
                            resurce = new HashMap<String, Object>();
                            //resurce.put(NAME,"[ " + User.opt("username") + " ] " + User.opt("first_name") + " " + User.opt("last_name"));
                            resurce.put(NAME,User.opt("username"));
                            resurce.put(EMAIL,User.get("email"));
                            resurce.put(ICON, R.drawable.default_user_icon_profile);
                            resurce.put(U_ID,Integer.toString(id++));
                            listContact.add(resurce);

                            //new DownloadImage(countItems++).execute("http://192.168.12.122:8002/load/?username=" + User.opt("username"));
                        }

                        if (listView.getAdapter() == null) {
                            SimpleAdapter adapter = new SimpleAdapter(getActivity(), listContact,R.layout.contact_list_item,
                                    new String[]{NAME,EMAIL,ICON,U_ID},
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
                Toast toast = Toast.makeText(getActivity().getApplicationContext(),"Error coonnection...",Toast.LENGTH_SHORT);
                toast.show();
            }
            is_runing = false;
        }
    }


    class DownloadImage extends AsyncTask<String, Void, Bitmap> {
        private Bitmap bitmap = null;
        int item = 0;

        public DownloadImage(int item){
            this.item = item;
        }

        @Override
        protected Bitmap doInBackground(String... url) {

            Log.i("info","------------------------------------------");
            Log.i("info","Item = " + Integer.toString(item));
            Log.i("info","URL  = " + url[0]);
            Log.i("info","------------------------------------------");

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
                //bitmap = image;
                Log.i("info","Add bit map to cach = " + Integer.toString(item));
                addBitmapToMemoryCache(Integer.toString(item), image);
                /*if (item <= visibleCountItem + firstVisibleItem && item >= firstVisibleItem) {
                    ImageView i = (ImageView)listView.getChildAt(item).findViewById(R.id.icon);
                    i.setImageBitmap(bitmap);
                }*/
                //i.setImageBitmap(bitmap);
                //((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
            }
        }
    }

}
