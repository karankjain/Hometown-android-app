package com.example.karanjain.weconnect;

/**
 * Created by karanjain on 4/12/17.
 */

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VolleyQueue {

    private static VolleyQueue mInstance;
    private static Context mContext;
    private RequestQueue mRequestQueue;

    private VolleyQueue(Context context) {
        mContext = context;
        mRequestQueue = this.queue();
    }

    public static synchronized VolleyQueue instance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleyQueue(context);
        }
        return mInstance;
    }

    public RequestQueue queue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void add(Request<T> req) {
        queue().add(req);
    }
}

