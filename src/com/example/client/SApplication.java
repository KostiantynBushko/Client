package com.example.client;

import android.app.Application;
import android.util.Log;

import org.apache.http.client.CookieStore;



/**
 * Created by kbushko on 10/31/13.
 */
public class SApplication extends Application {

    private static SApplication instance;

    public static SApplication getInstance() {
        return instance;
    }
    public static CookieStore cookieStore;

    @Override
    public final void onCreate() {
        Log.i("info"," Run SApplication singleton");
        super.onCreate();
        instance = this;
        cookieStore = new PersistentCookieStore(this.getApplicationContext());
    }
}
