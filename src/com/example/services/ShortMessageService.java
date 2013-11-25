package com.example.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by kbushko on 11/25/13.
 */
public class ShortMessageService extends Service{
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("info"," - ShortMessageService [ onCreate ]");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("info"," - ShortMessageServices [ onDestroy ]");
    }
    @Override
    public int onStartCommand(Intent intent, int flag, int startId) {
        Log.i("info"," - ShortMessageService [ onStartCommand ]");
        return super.onStartCommand(intent, flag, startId);
    }

    private void runTask() {

    }
}
