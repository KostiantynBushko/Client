package com.example.client;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by kbushko on 1/13/14.
 */
public class MyApplication extends Activity {

    @Override
    public void onCreate(Bundle savedInstanseState) {
        super.onCreate(savedInstanseState);
        setContentView(R.layout.my_app_list);
    }
}
