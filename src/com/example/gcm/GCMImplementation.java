package com.example.gcm;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kbushko on 12/12/13.
 */

public class GCMImplementation {

    private static final String GCM_PROPERTI_REGISTRATION_ID = "gcmRegistrationId";
    private static final String GCM_SENDER_ID = "project number from google console";
    private static final String GCM_PROPERTI_APP_VERSION = "gcmAppVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String URL_BACKEND = "";

    private Context applicationContext = null;
    private Activity activity = null;

    private GoogleCloudMessaging googleCloudMessage = null;
    private String registrationId = null;

    public GCMImplementation(Activity activity) {
        this.activity = activity;
        this.applicationContext = activity.getApplicationContext();
    }

    public boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(applicationContext);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode,activity,PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                activity.finish();
            }
            return false;
        }
        return true;
    }

    private String getRegistrationId() {
        final SharedPreferences sharedPreferences =
                applicationContext.getSharedPreferences(this.getClass().getSimpleName(),
                        Context.MODE_PRIVATE);
        String regId = sharedPreferences.getString(GCM_PROPERTI_REGISTRATION_ID,"");
        if (regId.isEmpty())
            return "";
        int registrationAppVersion = sharedPreferences.getInt(GCM_PROPERTI_APP_VERSION,0);
        int currentAppVersion = getApplicationVersion(applicationContext);
        if (currentAppVersion != registrationAppVersion)
            return "";
        return regId;
    }

    private void storeRegistrationId(String registrationId) {
        final SharedPreferences sharedPreferences =
                applicationContext.getSharedPreferences(this.getClass().getSimpleName(),
                        Context.MODE_PRIVATE);
        int appVersion = getApplicationVersion(applicationContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(GCM_PROPERTI_APP_VERSION, appVersion);
        editor.putString(GCM_PROPERTI_REGISTRATION_ID,"");
        editor.commit();
    } 

    private void sendRegistrationIdToBackend(String regId) {

    }

    public static int getApplicationVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(),0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    class RegisteredTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            try{
                if (googleCloudMessage == null)
                    googleCloudMessage = GoogleCloudMessaging.getInstance(applicationContext);
                registrationId = googleCloudMessage.register(GCM_SENDER_ID);
                storeRegistrationId(registrationId);
                sendRegistrationIdToBackend(registrationId);
                return true;
            }catch(IOException e) {
                e.printStackTrace();
            }
            return false;
        }
    }
    class SendRegIDToBackendTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 1000);
            HttpConnectionParams.setSoTimeout(httpParams, 1000);

            DefaultHttpClient httpClient = new DefaultHttpClient();

            List<NameValuePair>nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("registrationId", registrationId));
            HttpPost httpPost = new HttpPost(URL_BACKEND);
            HttpContext httpContext = new BasicHttpContext();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, null);
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse httpResponse = httpClient.execute(httpPost);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return false;
        }
    }
}
