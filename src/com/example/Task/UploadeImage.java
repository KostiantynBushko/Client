package com.example.Task;

import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by kbushko on 11/13/13.
 */
public class UploadeImage extends AsyncTask<String, Void, Boolean>{
    private Context context;

    public UploadeImage(Context context) {
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(String... url) {
        return null;
    }
}
