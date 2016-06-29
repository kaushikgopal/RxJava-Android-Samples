package com.morihacky.android.rxjava.fragments;

import android.os.Looper;
import android.support.v4.app.Fragment;
import com.morihacky.android.rxjava.MyApp;
import com.squareup.leakcanary.RefWatcher;

public class BaseFragment
      extends Fragment {

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = MyApp.getRefWatcher();
        refWatcher.watch(this);
    }

    protected boolean _isCurrentlyOnMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }
}
