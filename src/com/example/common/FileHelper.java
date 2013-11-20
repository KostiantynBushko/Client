package com.example.common;

import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.io.File;

/**
 * Created by kbushko on 11/18/13.
 */

public class FileHelper {
    public static Intent openFileIntent(File file) {
        String type = getMimeType(file.getName());
        if (type == null)
            type = "*/*";
        Intent intent = new Intent();
        intent.setDataAndType(Uri.parse("file://" + file.getAbsolutePath()),type);
        intent.setAction(Intent.ACTION_DEFAULT);
        if(type.equals("application/zip"))
            return null;
        return intent;
    }

    public static String getMimeType(String url) {
        if(url.indexOf(".") == -1)
            return "*/*";
        String extension = url.substring(url.lastIndexOf("."));
        String mimeTypeMap = MimeTypeMap.getFileExtensionFromUrl(extension);
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(mimeTypeMap);
        return mimeType;
    }
}