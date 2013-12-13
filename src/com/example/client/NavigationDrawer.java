package com.example.client;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

/**
 * Created by kbushko on 11/6/13.
 */
public class NavigationDrawer extends FragmentActivity {

    private String[] drawerItems;
    private ListView drawerListView;
    private DrawerLayout drawerLayout;
    private LinearLayout drawerLeftLayout;
    private int currentFragment = 0;
    private final String CURRENT_FRAGMNET = "current_fragment";

    final String[] fragments ={
            "com.example.fragment.HomePageFragment",
            "com.example.fragment.ContactPageFragment",
            "com.example.fragment.FileExplorerFragment",
            "com.example.fragment.OpenFileFragment",
            "com.example.fragment.ContactPageFragment",
            "com.example.fragment.ContactPageFragment",
            "com.example.fragment.GMapFragment",
            "com.example.fragment.ServiceManager"
    };
    @Override
    protected void onCreate(Bundle savedInstaceState) {
        Log.i("info"," NavigationDrawer [ onCreate ]");
        super.onCreate(savedInstaceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        drawerLayout= (DrawerLayout)findViewById(R.id.drawer_layout);
        drawerItems = getResources().getStringArray(R.array.items);
        drawerLeftLayout = (LinearLayout)findViewById(R.id.left_drawer);

        drawerListView = (ListView)findViewById(R.id.left_drawer_list);
        drawerListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,drawerItems));
        drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {
                drawerLayout.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) { super.onDrawerSlide(drawerView, slideOffset); }
                    @Override
                    public void onDrawerOpened(View drawerView) { super.onDrawerOpened(drawerView); }
                    @Override
                    public void onDrawerClosed(View drawerView) {
                        super.onDrawerClosed(drawerView);
                        if (currentFragment != position){
                            currentFragment = position;
                            FragmentTransaction transaction = getFragmentManager().beginTransaction();
                            transaction.replace(R.id.content_frame, Fragment.instantiate(NavigationDrawer.this, fragments[currentFragment]),
                                    fragments[currentFragment]);
                            transaction.commit();
                        }
                    }

                    @Override
                    public void onDrawerStateChanged(int newState) {
                        super.onDrawerStateChanged(newState);
                    }
                });
                drawerLayout.closeDrawer(drawerLeftLayout);
            }
        });

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        currentFragment = sharedPreferences.getInt(CURRENT_FRAGMNET,0);

        if (savedInstaceState == null){
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.content_frame, Fragment.instantiate(NavigationDrawer.this, fragments[currentFragment]),
                    fragments[currentFragment]);
            transaction.commit();
        }

        //GCMImplementation gcmImplementation = new GCMImplementation(this);
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
    /*@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }*/
}
