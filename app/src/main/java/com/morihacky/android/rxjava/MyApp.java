package com.morihacky.android.rxjava;

import android.support.multidex.MultiDexApplication;
import com.morihacky.android.rxjava.volley.MyVolley;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import timber.log.Timber;

public class MyApp extends MultiDexApplication {

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

    if (LeakCanary.isInAnalyzerProcess(this)) {
      // This process is dedicated to LeakCanary for heap analysis.
      // You should not init your app in this process.
      return;
    }

    _instance = (MyApp) getApplicationContext();
    _refWatcher = LeakCanary.install(this);

    // for better RxJava debugging
    //RxJavaHooks.enableAssemblyTracking();

    // Initialize Volley
    MyVolley.init(this);

    Timber.plant(new Timber.DebugTree());
  }
}
