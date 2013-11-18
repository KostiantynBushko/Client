package com.example.common;

import android.content.Context;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;


public class JSONParser {
	
	static InputStream inputStream = null;
	static JSONObject retJsonObject = null;
	static String jsonString = null;
	
	private Context appContext = null;
	
	public void JSONParcer(Context context){
		appContext = context;
	}
	
	public JSONObject getJSONFromUrl(String url, JSONObject jsonObject){
		Log.i("info", " i: JSONParcer [ getJSONFromURL ] url( " + url + " )");
		Log.i("info", " i: Object = " + jsonObject.toString());
		
		try{
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
			HttpConnectionParams.setSoTimeout(httpParams, 10000);
			HttpClient httpClient = new DefaultHttpClient(httpParams);
			
			HttpPost httpPost = new HttpPost(url.toString());
			httpPost.setHeader("Content-type", "application/json");
			
			StringEntity stringEntity = new StringEntity(jsonObject.toString(),HTTP.UTF_8); 
			stringEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			httpPost.setEntity(stringEntity);
			
			HttpResponse httpResponse = httpClient.execute(httpPost);
			String response = "";
            HttpEntity entity = httpResponse.getEntity();
            response = EntityUtils.toString(entity);
			
            Log.i("info", " JSONParser response = " + response.toString());
            
            try {
            	retJsonObject = new JSONObject(response.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}catch(ClientProtocolException e){
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return retJsonObject;
	}
}
