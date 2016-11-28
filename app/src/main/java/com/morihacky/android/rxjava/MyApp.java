package com.morihacky.android.rxjava;

import android.app.Application;
import com.morihacky.android.rxjava.volley.MyVolley;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import timber.log.Timber;

public class MyApp
      extends Application {

    private static MyApp _instance;
    private RefWatcher _refWatcher;

    public static MyApp get() {
        return _instance;
    }

    public static RefWatcher getRefWatcher() {
        return MyApp.get()._refWatcher;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        _instance = (MyApp) getApplicationContext();
        _refWatcher = LeakCanary.install(this);

        // for better RxJava debugging
        //RxJavaHooks.enableAssemblyTracking();

        // Initialize Volley
        MyVolley.init(this);

        Timber.plant(new Timber.DebugTree());
    }
}
