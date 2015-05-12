package com.morihacky.android.rxjava;

import android.support.v4.app.Fragment;
import com.squareup.leakcanary.RefWatcher;

public class BaseFragment
      extends Fragment {

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = MyApp.getRefWatcher();
        refWatcher.watch(this);
    }
}
