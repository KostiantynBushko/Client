package com.example.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
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

    private final int GET_ITEM_LIMIT = 6;
    private JSONArray contacts;
    private int offset = 0;
    private int limit = GET_ITEM_LIMIT;
    private int countItems = 0;

    ArrayList<HashMap<String, Object>>listContact;
    private static final String NAME  = "name";
    private static final String EMAIL = "email";
    private static final String U_ID  = "u_id";
    private static final String ICON  = "icon";
    private ListView listView;

    private int firstVisibleItem = 0;
    private int visibleCountItem = 0;

    boolean isRunning = false;
    LruCache<String, Bitmap>mMemoryCach;
    boolean mShowingBack = false;
    ViewGroup root;
    LoadContactTask loadContactTask;

    public static Fragment newInstance(Context contewxt) {
        ContactPageFragment contactPageFragment = new ContactPageFragment();
        return contactPageFragment;
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        root = (ViewGroup)layoutInflater.inflate(R.layout.contact_layout, null);

        final int maxMemory  = (int)(Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory;
        Log.i("info","maxMemory = " + Integer.toString(maxMemory / 1024) + "Kb");
        Log.i("info","cacheSize = " + Integer.toString(cacheSize / 1024) + "Kb");

        FragmentManager fragmentManager = getActivity().getFragmentManager();
        FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
        fragmentTransaction.addToBackStack(ContactPageFragment.class.getName());


        mMemoryCach = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap){
                return bitmap.getByteCount() /1024;
            }
        };
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
        mShowingBack = false;
        countItems = 0;
        offset = 0;
        limit = GET_ITEM_LIMIT;

        listView = (ListView)root.findViewById(R.id.listView);
        listContact = new ArrayList<HashMap<String, Object>>();
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
                        new LoadContactTask().execute(URL.host + "/user_list/");
                    }
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int item, long l) {
                HashMap<String, Object>object = listContact.get(item);
                Bitmap image = getBitmapFromMemCache(Integer.toString(item));
                String username = (String)object.get(NAME);
                String email = (String)object.get(EMAIL);

                getActivity().getFragmentManager().beginTransaction().setCustomAnimations(
                        R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                        R.animator.card_flip_left_in, R.animator.card_flip_left_out)
                        .replace(R.id.content_frame, new SendMessage(image,username,email))
                        .addToBackStack(null)
                        .commit();
            }
        });

        loadContactTask = new LoadContactTask();
        loadContactTask.execute(URL.host + "/user_list/");
        super.onResume();
    }
    @Override
    public void onStop() {
        super.onStop();
        loadContactTask.cancel(true);
    }

    /* Load contact task */
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
                    HashMap<String, Object>resurce;
                    int id = offset;
                    Log.i("info"," contact length = " + Integer.toString(contacts.length()));
                    if (contacts.length() > 0){
                        offset += contacts.length();
                        for (int i = 0; i< contacts.length(); i++) {
                            User = contacts.getJSONObject(i);
                            User = User.getJSONObject("fields");
                            resurce = new HashMap<String, Object>();
                            resurce.put(NAME,User.opt("username"));
                            resurce.put(EMAIL,User.get("email"));
                            resurce.put(ICON, R.drawable.default_user_icon_profile);
                            resurce.put(U_ID,Integer.toString(id++));
                            listContact.add(resurce);
                            new DownloadImage(countItems++).execute(URL.host + "/load/?username=" + User.opt("username"));
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
                Toast toast = Toast.makeText(getActivity().getApplicationContext(),"Error connection...",Toast.LENGTH_SHORT);
                toast.show();
            }
            isRunning = false;
        }
    }

    /* Download image task */
    class DownloadImage extends AsyncTask<String, Void, Bitmap> {
        int item = 0;
        Bitmap bitmap = null;
        public DownloadImage(int item) {
            this.item = item;
        }
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
}
