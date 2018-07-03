package io.sece.vlc.rcvr;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;


public class APIClient {
    private static APIClient instance;
    private RequestQueue queue;


    private APIClient(Context context) {
        queue = Volley.newRequestQueue(context.getApplicationContext());
    }


    public static synchronized APIClient getInstance(Context context) {
        if (null == instance)
            instance = new APIClient(context);
        return instance;
    }

    public void addToRequestQueue(Request req) {
        queue.add(req);
    }


    public void cancelAll(Object tag) {
        queue.cancelAll(tag);
    }
}
