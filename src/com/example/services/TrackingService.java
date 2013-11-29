package com.example.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

/**
 * Created by kbushko on 11/29/13.
 */

public class TrackingService extends Service implements LocationListener{

    public final static String BROADCAST_ACTION = "com.example.service.trackingservice.location";
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.i("info"," TrackingService [ onCreate ]");
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                onLocationChanged(location);
            }
            Toast.makeText(getApplicationContext(),"...tracking started",Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(),"gps service is not enable",Toast.LENGTH_SHORT).show();
        }
        runTask();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.i("info"," TrackingService [ onDestroy ]");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flag, int startId) {
        Log.i("info"," TrakingService [ onStartCommand ]");
        //return super.onStartCommand(intent,flag,startId);
        return START_STICKY;
    }


    private void runTask() {
        Log.i("info"," Run Task");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(BROADCAST_ACTION);
                int i = 0;
                while(i<20) {
                    Log.i("info"," i = " + Integer.toString(i));
                    intent.putExtra("i",i);
                    sendBroadcast(intent);
                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    i++;
                }
                stopSelf();
            }
        }).start();
    }

    /* Location listener */

    @Override
    public void onLocationChanged(Location location) {
        Log.i("info"," TrackingService location = " + Double.toString(location.getLatitude()) + " " + Double.toString(location.getLongitude()));
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
