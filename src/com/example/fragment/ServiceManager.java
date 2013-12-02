package com.example.fragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.client.R;
import com.example.services.TrackingService;

/**
 * Created by kbushko on 11/22/13.
 */
public class ServiceManager extends Fragment {
    private TextView textView, lat, lon;
    private int count = 0;
    private BroadcastReceiver broadcastReceiver = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup)inflater.inflate(R.layout.services_manager, null);

        textView = (TextView)root.findViewById(R.id.textView);
        ((Button)root.findViewById(R.id.run)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //textView.setText(Integer.toString(++count));
                //getActivity().startService(new Intent(getActivity(), ShortMessageService.class));
                getActivity().startService(new Intent(getActivity(), TrackingService.class));
            }
        });
        ((Button)root.findViewById(R.id.stop)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //getActivity().stopService(new Intent(getActivity(), ShortMessageService.class));
                getActivity().stopService(new Intent(getActivity(), TrackingService.class));
            }
        });

        lat = (TextView)root.findViewById(R.id.lat);
        lon = (TextView)root.findViewById(R.id.lon);

        /* Create and registered broadcast reciver */
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                textView.setText(Integer.toString(intent.getIntExtra("i",0)));
                lat.setText(Float.toString(intent.getFloatExtra("latitude",0)));
                lon.setText(Float.toString(intent.getFloatExtra("longitude",0)));
            }
        };
        IntentFilter intentFilter = new IntentFilter(TrackingService.BROADCAST_LOCATION_CHANGE_ACTION);
        getActivity().registerReceiver(broadcastReceiver, intentFilter);
        return root;
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }
}
