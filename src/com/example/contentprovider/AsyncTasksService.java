package com.example.contentprovider;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by kbushko on 12/26/13.
 */
public class AsyncTasksService extends Service {

    ExecutorService executorService;

    @Override
    public void onCreate() {
        Log.i("info", " AsyncTasksServices [ onCreate ]");
        super.onCreate();
        executorService = Executors.newFixedThreadPool(10);
    }

    @Override
    public int onStartCommand(Intent intent, int flag, int startId) {
        Log.i("info"," AsyncTasksServices [ onStartCommand ] startId = " + Integer.toString(startId));
        executorService.execute(new RunTask(startId));
        return super.onStartCommand(intent, flag, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("info"," AsyncTasksService [ onDestroy ]");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("info"," AsyncTasksService [ onBind ]");
        return new Binder();
    }
    @Override
    public void onRebind(Intent intent) {
        Log.i("info"," AsyncTasksService [ onRebind ]");
        super.onRebind(intent);
    }
    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("info"," AsyncTaskService [ onUnbind ]");
        return super.onUnbind(intent);
    }

    /**********************************************************************************************/
    class RunTask implements Runnable {
        private int id;
        public RunTask(int startId) { id = startId; }
        @Override
        public void run() {
            Log.i("info"," RunTask start id  = " + Integer.toString(id));
            SystemClock.sleep(5000);
            Log.i("info"," RunTask finish id = " + Integer.toString(id));

            stopSelf(id);
        }
    }
}
