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
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.client.R;
import com.example.client.SApplication;
import com.example.client.URL;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kbushko on 11/21/13.
 */

public class SendMessage extends Fragment {

    private Bitmap image = null;
    private String username = "username";
    private String email = "email";
    private ImageView imageView = null;
    private TextView tv_username = null;
    private TextView tv_email = null;
    private EditText messageField = null;
    byte[] byteArray;


    private final String NAME = "name";
    private final String MSG = "msg";
    private ListView listView;
    private ArrayList<HashMap<String, Object>> listMessage;

    public SendMessage(){}
    public SendMessage(Bitmap image, String username, String email) {
        this.image = image;
        if (this.image != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 10, stream);
            byteArray = stream.toByteArray();
        }

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
            byteArray = savedInstanceState.getByteArray("icon");
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

        listMessage = new ArrayList<HashMap<String, Object>>();
        listView = (ListView)root.findViewById(R.id.listView);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        new GetMessageListtask().execute(username);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("name",username);
        outState.putString("email",email);
        outState.putByteArray("icon",byteArray);
    }

    /* Add message to list */
    private void addMessageToListView(String username, String message){
        HashMap<String, Object>item = new HashMap<String, Object>();
        item.put(NAME,username);
        item.put(MSG,message);
        listMessage.add(item);

        if(listView.getAdapter() == null){
            SimpleAdapter adapter = new SimpleAdapter(getActivity(),listMessage,R.layout.message_item,
                    new String[]{ NAME,MSG },
                    new int[]{ R.id.text1, R.id.text2 });
            listView.setAdapter(adapter);
        }else{
            ((SimpleAdapter)listView.getAdapter()).notifyDataSetChanged();
            listView.setSelection(listMessage.size());
        }
    }

    /* Send message task */

    class SendMessageTask extends AsyncTask<String, Void, Boolean> {
        String errorMessage = "";
        String response = "";
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
                HttpResponse httpResponse = httpClient.execute(httpPost, httpContext);
                response = EntityUtils.toString(httpResponse.getEntity());
                Log.i("info"," response = " + response);
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
                addMessageToListView(username,messageField.getText().toString());
                messageField.getText().clear();
            }
        }
    }


    /* Message list */

    class GetMessageListtask extends AsyncTask<String, Void, Boolean> {
        String response = "";
        @Override
        protected Boolean doInBackground(String... strings) {
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams,10000);
            HttpConnectionParams.setSoTimeout(httpParams,10000);

            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
            HttpGet httpGet = new HttpGet(URL.host + "/msg_list/?recipient=" + strings[0]);

            HttpContext httpContext = new BasicHttpContext();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, SApplication.cookieStore);

            try {
                HttpResponse httpResponse = httpClient.execute(httpGet,httpContext);
                response = EntityUtils.toString(httpResponse.getEntity());
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            if (result){
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    if (jsonArray.length() > 0) {
                        for(int i=0; i<jsonArray.length(); i++) {
                            JSONObject object = jsonArray.getJSONObject(i).getJSONObject("fields");
                            Log.i("info"," - msg = " + object.getString("message") + " | " + object.getString("sender"));
                            addMessageToListView(object.getString("sender"),object.getString("message"));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else {

            }
        }
    }
}