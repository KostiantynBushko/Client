package com.example.fragment;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.client.R;
import com.example.client.RestaurantActivity;

/**
 * Created by kbushko on 12/18/13.
 */
public class RestaurantList extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    final Uri R_URI = Uri.parse("content://com.example.client.object/restaurants");
    ViewGroup root;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle savedInstanceState){
        root = (ViewGroup)layoutInflater.inflate(R.layout.restaurant_list, null);
        ((Button)root.findViewById(R.id.button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), RestaurantActivity.class);
                getActivity().startActivity(intent);
            }
        });
        return root;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        CursorLoader cursorLoader = new CursorLoader(getActivity(),R_URI, null, null, null,null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }
}
