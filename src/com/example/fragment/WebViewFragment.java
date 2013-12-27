package com.example.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.client.R;

/**
 * Created by kbushko on 12/27/13.
 */
public class WebViewFragment extends Fragment {
    private WebView webView;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup)inflater.inflate(R.layout.web_view_layout,null);

        webView = (WebView)root.findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                Log.i("info"," Load : " + url);
                view.loadUrl(url);
                return false;
            }
        });
        webView.loadUrl("file:///android_asset/index.html");
        //webView.loadUrl("http://developer.android.com/");
        return root;
    }
}
