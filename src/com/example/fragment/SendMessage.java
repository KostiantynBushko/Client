package com.example.fragment;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.client.R;
import com.example.client.SApplication;
import com.example.client.URL;

import org.apache.http.NameValuePair;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kbushko on 11/21/13.
 */

public class SendMessage extends Fragment {

    private Bitmap image = null;
    private String username = "username";
    private String email = "email";

    ImageView imageView = null;
    TextView tv_username = null;
    TextView tv_email = null;

    EditText messageField = null;

    public SendMessage(){}
    public SendMessage(Bitmap image, String username, String email) {
        this.image = image;
        if (username != null)
            this.username = username;
        if (email != null)
            this.email = email;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null){
            username = savedInstanceState.getString("name");
            email = savedInstanceState.getString("email");
            byte[] byteArray = savedInstanceState.getByteArray("icon");
            image = BitmapFactory.decodeByteArray(byteArray,0,byteArray.length);
        }

        ViewGroup root = (ViewGroup)inflater.inflate(R.layout.message_layout, null);
        imageView = (ImageView)root.findViewById(R.id.icon);
        tv_username = (TextView)root.findViewById(R.id.text1);
        tv_email = (TextView)root.findViewById(R.id.text2);
        if (image != null)
            imageView.setImageBitmap(image);
        tv_username.setText(username);
        tv_email.setText(email);

        messageField = (EditText)root.findViewById(R.id.messageField);

        root.findViewById(R.id.bSend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SendMessageTask().execute(URL.host + "/send_message/");
            }
        });
        return root;
    }


    class SendMessageTask extends AsyncTask<String, Void, Boolean> {
        String errorMessage = "";
        @Override
        protected Boolean doInBackground(String... strings) {
            if (messageField.getText().length() == 0){
                errorMessage = "... type message";
                return false;
            }

            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
            HttpConnectionParams.setSoTimeout(httpParams,10000);

            List<NameValuePair>nameValuePairList = new ArrayList<NameValuePair>(2);
            nameValuePairList.add(new BasicNameValuePair("recipient",username));
            nameValuePairList.add(new BasicNameValuePair("message",messageField.getText().toString()));

            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
            HttpPost httpPost = new HttpPost(URL.host + "/send_message/");
            Log.i("info"," url = " + URL.host + "/send_message/");
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairList));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return false;
            }

            HttpContext httpContext = new BasicHttpContext();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, SApplication.cookieStore);

            try {
                httpClient.execute(httpPost,httpContext);
                return true;
            } catch (IOException e) {
                errorMessage = "... failed connection";
                e.printStackTrace();
            }
            return false;
        }
        @Override
        public void onPostExecute(Boolean result) {
            if(result == false) {
                Toast.makeText(getActivity(),errorMessage,Toast.LENGTH_SHORT).show();
            } else{
                messageField.getText().clear();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("name",username);
        outState.putString("email",email);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        outState.putByteArray("icon",byteArray);
    }
}
