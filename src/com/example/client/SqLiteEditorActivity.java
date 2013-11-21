package com.example.client;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by kbushko on 11/21/13.
 */
public class SqLiteEditorActivity extends Activity{

    private byte[] header = new byte[16];
    private int pageSize = 0;
    private int fileFormatWrite = 0;
    private int fileFormatRead = 0;
    private int maxPayloadFraction = 0;
    private int minPayloadFraction = 0;
    private int sizeInPages = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sqlite_editor);

        TextView textView = (TextView)findViewById(R.id.textView);
        Intent intent = getIntent();
        Uri data = intent.getData();

        File file = new File(data.getPath());
        byte[] byteAray = new byte[(int) file.length()];
        try {
            FileInputStream fis = new FileInputStream(file);
            fis.read(byteAray);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.arraycopy(byteAray, 0, header, 0, 16);
        String headerString = "";
        try {
            headerString = new String(header, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (headerString.equals("SQLite 3 format")) {
            textView.setText("File format not supported");
            return;
        }

        textView.setText(headerString);
        pageSize ^= byteAray[16] & 0xFF;
        pageSize = pageSize << 8;
        pageSize ^= byteAray[17] & 0xFF;
        fileFormatWrite = byteAray[18];
        fileFormatRead = byteAray[19];
        maxPayloadFraction = byteAray[21];
        minPayloadFraction = byteAray[22];

        sizeInPages ^= byteAray[28] & 0xFF;
        sizeInPages = sizeInPages << 8;
        sizeInPages ^= byteAray[29] & 0xFF;
        sizeInPages = sizeInPages << 8;
        sizeInPages ^= byteAray[30] & 0xFF;
        sizeInPages = sizeInPages << 8;
        sizeInPages ^= byteAray[31] & 0xFF;
    }
}
