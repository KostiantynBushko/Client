package com.example.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.client.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

/**
 * Created by kbushko on 11/29/13.
 */
public class GMapFragment extends Fragment {

    private GoogleMap map = null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanseState) {
        ViewGroup root = (ViewGroup)inflater.inflate(R.layout.map_fragment, null, false);
        map = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setMapType(1);
        map.setMyLocationEnabled(true);


        return root;
    }

    @Override
    public void onDestroy() {
        Log.i("ifno"," onDestroy");
        super.onDestroy();
        /*Fragment fragment = getFragmentManager().findFragmentById(R.id.map);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.remove(fragment);
        fragmentTransaction.commit();*/
    }
}
