package com.example.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.client.R;
import com.example.services.ShortMessageService;

/**
 * Created by kbushko on 11/22/13.
 */
public class ServiceManager extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup)inflater.inflate(R.layout.services_manager, null);

        Button run = (Button)root.findViewById(R.id.run);
        Button stop = (Button)root.findViewById(R.id.stop);

        run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().startService(new Intent(getActivity(), ShortMessageService.class));
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().stopService(new Intent(getActivity(), ShortMessageService.class));
            }
        });
        return root;
    }
}
