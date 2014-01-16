package com.example.client;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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

import java.io.IOException;

/**
 * Created by kbushko on 11/6/13.
 */
public class NavigationDrawer extends FragmentActivity {

    private String[] drawerItems;
    private ListView drawerListView;
    private DrawerLayout drawerLayout;
    private LinearLayout drawerLeftLayout;
    private ActionBarDrawerToggle drawerToggle;

    private int currentFragment = 0;
    private final String CURRENT_FRAGMNET = "current_fragment";
    private final String USER_AVATAR = "user_avatar";

    private String first_name = "name";
    private String last_name = "last name";
    private String email = "email";
    private String username = "user name";
    private ImageView icon = null;
    private Bitmap bitmap = null;

    final String[] fragments ={
            "com.example.fragment.HomePageFragment",
            /*"com.example.fragment.FileExplorerFragment",*/
            /*"com.example.fragment.GMapFragment",*/
            "com.example.fragment.AppStoreFragment",
            "com.example.fragment.MyApplicationsFragment"
            /*"com.example.fragment.RestaurantList",
            "com.example.fragment.WebViewFragment"*/
    };
    final String[] fragmentName = {
            "Home",
            "App Store",
            "My App"
    };

    @Override
    protected void onCreate(Bundle savedInstaceState) {
        Log.i("info"," NavigationDrawer [ onCreate ]");
        super.onCreate(savedInstaceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        first_name = intent.getStringExtra("first_name");
        last_name = intent.getStringExtra("last_name");
        username = intent.getStringExtra("username");
        email = intent.getStringExtra("email");

        ((TextView)findViewById(R.id.textView1)).setText(first_name + " " + last_name);
        ((TextView)findViewById(R.id.textView2)).setText(email);
        icon = (ImageView)findViewById(R.id.imageView);

        drawerLayout= (DrawerLayout)findViewById(R.id.drawer_layout);
        drawerItems = getResources().getStringArray(R.array.items);
        drawerLeftLayout = (LinearLayout)findViewById(R.id.left_drawer);

        drawerListView = (ListView)findViewById(R.id.left_drawer_list);
        drawerListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,drawerItems));
        drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {
                if (currentFragment != position){
                    currentFragment = position;
                    getActionBar().setTitle(fragmentName[currentFragment]);
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.content_frame, Fragment.instantiate(NavigationDrawer.this, fragments[currentFragment]),
                            fragments[currentFragment]);
                    transaction.addToBackStack(fragments[currentFragment]);
                    transaction.commit();
                    drawerLayout.closeDrawers();
                }
            }
        });

        if (savedInstaceState == null){
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            currentFragment = sharedPreferences.getInt(CURRENT_FRAGMNET,0);
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.content_frame, Fragment.instantiate(NavigationDrawer.this, fragments[currentFragment]),
                    fragments[currentFragment]);
            transaction.addToBackStack(fragments[currentFragment]);
            transaction.commit();
        }

        drawerToggle = new ActionBarDrawerToggle(this,drawerLayout,R.drawable.ic_navigation_drawer,R.string.drawer_open,R.string.drawer_close){
            public void onDrawerClosed(View view) { Log.i("info"," on closed"); }
            public void onDrawerOpened(View drawerView) { Log.i("info"," on open"); }
        };
        drawerLayout.setDrawerListener(drawerToggle);
        getActionBar().setTitle(fragmentName[currentFragment]);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        drawerToggle.syncState();
        new DownloadImage().execute(URL.host + "/load/");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(drawerToggle.onOptionsItemSelected(item)){
            Log.i("info"," Item selected");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onResume(){
        Log.i("info"," NavigationDrawer [ onResume ]");
        super.onResume();
    }

    @Override
    public void onStop(){
        /* Save last opened fragment in the preferences */
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(CURRENT_FRAGMNET,currentFragment);
        editor.commit();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    class DownloadImage extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... url) {
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
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
                Bitmap tmp = image;
                bitmap = Bitmap.createBitmap(tmp.getWidth(),tmp.getHeight(),Bitmap.Config.ARGB_8888);
                BitmapShader shader = new BitmapShader(tmp, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                Paint paint = new Paint();
                paint.setShader(shader);
                Canvas canvas = new Canvas(bitmap);
                canvas.drawCircle(bitmap.getWidth()/2, bitmap.getHeight()/2, bitmap.getWidth()/2, paint);
                icon.setImageBitmap(bitmap);
            }
        }
    }
}
