package com.example.client;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;

/**
 * Created by kbushko on 11/1/13.
 */
public class HttpClientFactory {

    private static DefaultHttpClient client = null;

    public synchronized static DefaultHttpClient getThreadSafeClient() {

        if (client != null)
            return client;


        client = new DefaultHttpClient();

        ClientConnectionManager manager = client.getConnectionManager();
        HttpParams params = client.getParams();
        client = new DefaultHttpClient(
                (HttpParams) new ThreadSafeClientConnManager(params, manager.getSchemeRegistry()));

        return client;
    }
}
