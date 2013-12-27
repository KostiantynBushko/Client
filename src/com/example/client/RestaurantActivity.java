package com.example.client;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.example.contentprovider.AsyncTasksService;

/**
 * Created by kbushko on 12/19/13.
 */
public class RestaurantActivity extends Activity {

    final Uri R_URI = Uri.parse("content://com.example.client.object/restaurants");
    final String R_NAME = "name";
    final String R_EMAIL = "email";
    final String R_ID = "_id";
    Cursor cursor;

    int count = 0;

    String[] projection = {
            R_ID,
            R_NAME,
            R_EMAIL
    };
    String selection = null;
    String[] selectionArgs = {""};

    private ServiceConnection serviceConnection;
    private boolean isBound = false;
    private final Intent intent = new Intent("com.exemple.contentprovider.AsyncTasksService");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.restaurant_list);

        Cursor cursor = getContentResolver().query(R_URI,null, null, null,null);
        startManagingCursor(cursor);

        String from[] = {"name","email"};
        int to[] = {android.R.id.text1, android.R.id.text2};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2,cursor,from, to);
        ListView listView = (ListView)findViewById(R.id.listView);
        listView.setAdapter(adapter);

        ((Button)findViewById(R.id.button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cursor cursor = getContentResolver().query(R_URI, null, null, null, null, null);
                ContentValues cv = new ContentValues();
                cv.put(R_NAME, "name " + cursor.getCount());
                cv.put(R_EMAIL,"email " + cursor.getCount());
                Uri newUri = getContentResolver().insert(R_URI, cv);
                Log.i("info"," uri = " + newUri.toString());
            }
        });
        ((Button)findViewById(R.id.button4)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService(new Intent(getApplicationContext(), AsyncTasksService.class));
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Cursor cursor = getContentResolver().query(R_URI, projection, null, null, null, null);
                if (cursor.getCount() == 0){
                    Log.i("info"," : not found");
                }else {
                    cursor.moveToFirst();
                    cursor.move(i);
                    Log.i("info", Integer.toString(i) + " : " + cursor.getCount() + " : " + cursor.getString(0) + " : "
                            + cursor.getString(1) + " : ");
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Cursor cursor = getContentResolver().query(R_URI, projection, null, null, null, null);
                cursor.moveToFirst();
                cursor.move(i);

                Uri uri = Uri.parse(R_URI.toString() + "/" + cursor.getString(0));
                Log.i("info"," delate : " + uri);
                getContentResolver().delete(uri,null,null);
                return false;
            }
        });

        /******************************************************************************************/
        /* Bind service */
        /******************************************************************************************/
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.i("info"," is connected ... ");
                isBound = true;
            }
            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.i("info"," is disconnect ...");
                isBound = false;
            }
        };
        ((Button)findViewById(R.id.start)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService(intent);
            }
        });
        ((Button)findViewById(R.id.stop)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService(intent);
            }
        });
        ((Button)findViewById(R.id.bind)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bindService(intent, serviceConnection, BIND_AUTO_CREATE);
                isBound = true;
            }
        });
        ((Button)findViewById(R.id.unbind)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isBound)
                    return;
                unbindService(serviceConnection);
                isBound = false;
            }
        });
    }

    /**********************************************************************************************/
    /* Life cycle */
    /**********************************************************************************************/
    @Override
    public void onStop() {
        stopManagingCursor(cursor);
        super.onStop();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        isBound = false;
    }
}
