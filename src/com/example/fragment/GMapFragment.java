package com.example.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.client.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;

/**
 * Created by kbushko on 11/29/13.
 */

public class GMapFragment extends Fragment {
    private GoogleMap map = null;

    private static View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanseState) {
        Log.i("info"," - GMapFragment [ onCreate ]" );
        //ViewGroup root = (ViewGroup)inflater.inflate(R.layout.map_fragment, container, false);
        if(view!=null) {
            ViewGroup parent = (ViewGroup)view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.map_fragment, container, false);
            map = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
            map.setMapType(1);
            //map.setMyLocationEnabled(true);
            //map.setOnCameraChangeListener();
        }catch (InflateException e) {
            e.printStackTrace();
        }

        return view;
    }

    /* Map Camera*/
    private GoogleMap.OnCameraChangeListener getCameraChangeListener() {
        return new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {

            }
        };
    }
}
