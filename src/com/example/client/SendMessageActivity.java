package com.example.client;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by kbushko on 12/30/13.
 */
public class SendMessageActivity extends Activity {

    private Bitmap image = null;
    private String username = "username";
    private String email = "email";
    private ImageView imageView = null;
    private TextView tv_username = null;
    private TextView tv_email = null;
    private EditText messageField = null;
    byte[] byteArray;

    private final String TIME_FORMATER = "HH:mm:s yyyy/MM/dd";
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TIME_FORMATER);

    private final String NAME = "name";
    private final String MSG = "msg";
    private final String DATE = "date";
    private ListView listView;
    private ArrayList<HashMap<String, Object>> listMessage;

    FrameLayout frameLayout;

    private int firstVisibleItem = 0;
    private int visibleCountItem = 0;
    private int totalCountItem = 0;
    private boolean isVisible = true;
    private boolean scrollStatus = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null){
            username = savedInstanceState.getString("name");
            email = savedInstanceState.getString("email");
            byteArray = savedInstanceState.getByteArray("icon");
            image = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        }
    }
}
