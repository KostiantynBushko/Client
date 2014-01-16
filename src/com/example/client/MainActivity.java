package com.example.client;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends FragmentActivity {

    EditText eUserName = null;
    EditText ePassword = null;

    private String message = "";
    private String title = "";
    TextView textView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
        textView = (TextView)this.findViewById(R.id.textView);

        /*Session.openActiveSession(this, true, new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                textView.setText("-/-/-");/
                if(session.isOpened()){
                    textView.setText("-/--/-");
                    Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {
                        @Override
                        public void onCompleted(GraphUser user, Response response) {
                            textView.setText("-/-");
                            if (user != null) {
                                textView.setText(user.getName());
                            }
                        }
                    });
                }
            }
        });*/

        new ChecAuthentication().execute(URL.host + "/check/");
	}

    /*@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }*/

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
    }

    class ChecAuthentication extends AsyncTask<String, Void, Boolean> {
        JSONObject User;
        @Override
        protected Boolean doInBackground(String... urlString) {
            SystemClock.sleep(1000);
            Boolean result = false;
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 25000);
            HttpConnectionParams.setSoTimeout(httpParams, 25000);

            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
            HttpPost httpPost = new HttpPost(urlString[0]);
            Log.i("info",urlString[0]);

            HttpContext httpContext = new BasicHttpContext();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, SApplication.cookieStore);

            try {
                HttpResponse httpResponse = httpClient.execute(httpPost, httpContext);
                HttpEntity httpEntity = httpResponse.getEntity();
                String message = EntityUtils.toString(httpEntity);

                if (message.equals("Error")){
                    result = false;
                }else{
                    result = true;
                    JSONArray array = new JSONArray(message.toString());
                    User = array.getJSONObject(0);
                    User = User.getJSONObject("fields");
                }
                Log.i("info", message);

            } catch (IOException e) {
                result = false;
                e.printStackTrace();
            } catch (JSONException e) {
                result = false;
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            boolean value = result.booleanValue();
            if (value){
                Log.i("info","Success login");
                Intent intent = new Intent(getApplicationContext(), NavigationDrawer.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("first_name",User.optString("first_name"));
                intent.putExtra("last_name",User.optString("last_name"));
                intent.putExtra("username",User.optString("username"));
                intent.putExtra("email",User.optString("email"));
                startActivity(intent);
                finish();
            }else {
                Log.i("info","Error login");
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        }
    }
}
