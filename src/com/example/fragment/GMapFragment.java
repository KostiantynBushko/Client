package com.example.fragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.client.R;
import com.example.services.TrackingService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by kbushko on 11/29/13.
 */

public class GMapFragment extends Fragment {

    private GoogleMap map = null;
    private Marker marker = null;

    private static View view;
    BroadcastReceiver broadcastReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanseState) {
        Log.i("info"," - GMapFragment [ onCreate ]" );
        if(view!=null) {
            ViewGroup parent = (ViewGroup)view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.map_fragment, container, false);
            map = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
            map.setMapType(1);
        }catch (InflateException e) {
            e.printStackTrace();
        }

        return view;
    }

    @Override
    public void onResume() {
        getActivity().startService(new Intent(getActivity(),TrackingService.class));
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LatLng position = new LatLng(intent.getFloatExtra("latitude",0),intent.getFloatExtra("longitude",0));
                if (map != null){
                    if (marker != null){
                        marker.remove();
                    }
                    map.clear();
                    marker = map.addMarker(new MarkerOptions()
                            .position(position)
                            .title("My location"));
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(position)
                            .zoom(17)
                            .tilt(30)
                            .build();
                    map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(TrackingService.BROADCAST_LOCATION_CHANGE_ACTION);
        getActivity().registerReceiver(broadcastReceiver, intentFilter);
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(broadcastReceiver);
        getActivity().stopService(new Intent(getActivity(),TrackingService.class));
    }

    /* Map Camera */
    private GoogleMap.OnCameraChangeListener getCameraChangeListener() {
        return new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                Log.i("info"," Map change listener");
            }
        };
    }
}