package com.morihacky.android.rxjava.volley;

import android.content.Context;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Helper class that is used to provide references to initialized RequestQueue(s) and ImageLoader(s)
 */
public class MyVolley {
  private static RequestQueue mRequestQueue;

  private MyVolley() {
    // no instances
  }

  public static void init(Context context) {
    mRequestQueue = Volley.newRequestQueue(context);
  }

  static RequestQueue getRequestQueue() {
    if (mRequestQueue != null) {
      return mRequestQueue;
    } else {
      throw new IllegalStateException("RequestQueue not initialized");
    }
  }
}
