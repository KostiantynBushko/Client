package com.example.fragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.client.R;
import com.example.services.ShortMessageService;

/**
 * Created by kbushko on 11/22/13.
 */
public class ServiceManager extends Fragment {
    private TextView textView;
    private int count = 0;

    private final String BROADCAST_ACTION = "com.example.service.trackingservice.location";
    private BroadcastReceiver broadcastReceiver = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup)inflater.inflate(R.layout.services_manager, null);

        textView = (TextView)root.findViewById(R.id.textView);
        ((Button)root.findViewById(R.id.run)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textView.setText(Integer.toString(++count));
                getActivity().startService(new Intent(getActivity(), ShortMessageService.class));
                //getActivity().startService(new Intent(getActivity(), TrackingService.class));
            }
        });
        ((Button)root.findViewById(R.id.stop)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().stopService(new Intent(getActivity(), ShortMessageService.class));
                //getActivity().stopService(new Intent(getActivity(), TrackingService.class));
            }
        });

        /* Create and registered broadcast reciver */
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("info"," ... broadcast reciver :" + Integer.toString(intent.getIntExtra("i",0)));
            }
        };

        IntentFilter intentFilter = new IntentFilter(BROADCAST_ACTION);
        getActivity().registerReceiver(broadcastReceiver, intentFilter);

        return root;
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }
}
