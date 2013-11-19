package com.example.client;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
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
    private int itemPosition = 0;

    final String[] fragments ={
            "com.example.fragment.HomePageFragment",
            "com.example.fragment.ContactPageFragment",
            "com.example.fragment.FileExplorerFragment",
            "com.example.fragment.OpenFileFragment",
            "com.example.fragment.ContactPageFragment",
            "com.example.fragment.ContactPageFragment",
            "com.example.fragment.ContactPageFragment"
    };


    @Override
    protected void onCreate(Bundle savedInstaseState) {
        super.onCreate(savedInstaseState);
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
                    public void onDrawerSlide(View drawerView, float slideOffset) {
                        super.onDrawerSlide(drawerView, slideOffset);
                    }

                    @Override
                    public void onDrawerOpened(View drawerView) {
                        super.onDrawerOpened(drawerView);
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                        super.onDrawerClosed(drawerView);
                        if (itemPosition != position){
                            itemPosition = position;
                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.content_frame, Fragment.instantiate(NavigationDrawer.this, fragments[itemPosition]));
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

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, Fragment.instantiate(NavigationDrawer.this, fragments[itemPosition]));
        transaction.commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
    }
}
