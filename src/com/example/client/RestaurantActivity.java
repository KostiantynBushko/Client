package com.example.client;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * Created by kbushko on 12/19/13.
 */
public class RestaurantActivity extends Activity {

    final Uri R_URI = Uri.parse("content://com.example.client.object/restaurants");
    final String R_NAME = "name";
    final String R_EMAIL = "email";
    Cursor cursor;

    int count = 0;

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
                ContentValues cv = new ContentValues();
                cv.put(R_NAME, "name " + count);
                cv.put(R_EMAIL,"email " + count);
                count++;
                Uri newUri = getContentResolver().insert(R_URI, cv);
                Log.i("info"," uri = " + newUri.toString());
            }
        });
    }

    @Override
    public void onStop() {
        stopManagingCursor(cursor);
        super.onStop();
    }

}
