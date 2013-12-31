package com.example.client;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by kbushko on 12/31/13.
 */
public class AppActivity extends Activity {

    Bitmap image;
    String path;
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.app_layout);

        Intent intent = getIntent();
        image = (Bitmap)intent.getParcelableExtra("image");
        ImageView imageView = (ImageView)findViewById(R.id.icon);
        name = intent.getStringExtra("name");
        path = intent.getStringExtra("path");
        if (image != null)
            imageView.setImageBitmap(image);

        TextView tv_name = (TextView)findViewById(R.id.text1);
        TextView tv_path = (TextView)findViewById(R.id.text2);
        tv_name.setText(name);
        tv_path.setText(path);

    }
}
