package com.milton.archwilio.common;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Milton on 30/03/2016.
 */
public class VolleySingleton {
    private static VolleySingleton mInstance;

    private Context mContext;
    private RequestQueue mRequestQueue;

    /**
     * Constructor
     *
     * @param context Context
     */
    private VolleySingleton(Context context) {
        mContext = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized VolleySingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleySingleton(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // Application context ensures the request queue will last for the lifetime of the app,
            // instead of being recreated every time the activity is recreated
            mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
