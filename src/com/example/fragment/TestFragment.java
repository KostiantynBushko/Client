package com.example.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.client.R;

/**
 * Created by kbushko on 1/10/14.
 */
public class TestFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstabseState){
        ViewGroup root = (ViewGroup)inflater.inflate(R.layout.app_detail_layout,null);
        return root;
    }
}
