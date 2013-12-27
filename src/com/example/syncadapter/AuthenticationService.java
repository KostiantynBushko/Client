package com.example.syncadapter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by kbushko on 12/23/13.
 */

public class AuthenticationService extends Service {
    private static final Object lock = new Object();
    private Authenticator auth;

    @Override
    public void onCreate() {
        synchronized(lock) {
            if (auth == null) {
                auth = new Authenticator(this);
            }
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("info"," AuthenticationService ... ");
        return auth.getIBinder();
    }
}
