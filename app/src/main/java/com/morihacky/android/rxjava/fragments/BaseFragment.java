package com.morihacky.android.rxjava.fragments;

import androidx.fragment.app.Fragment;
import com.morihacky.android.rxjava.MyApp;
import com.squareup.leakcanary.RefWatcher;

public class BaseFragment extends Fragment {

  @Override
  public void onDestroy() {
    super.onDestroy();
    RefWatcher refWatcher = MyApp.getRefWatcher();
    refWatcher.watch(this);
  }
}
