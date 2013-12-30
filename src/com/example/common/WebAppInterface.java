package com.example.common;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

/**
 * Created by kbushko on 12/30/13.
 */
public class WebAppInterface {
    Context context;

    public WebAppInterface(Context context){
       this.context = context;
    }
    @JavascriptInterface
    public void showToast(String toast){
        Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
        Log.i("info"," Java script interface");
    }
}
