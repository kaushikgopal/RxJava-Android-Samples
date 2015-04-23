package com.morihacky.android.rxjava;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import com.morihacky.android.rxjava.app.R;
import rx.Subscription;

public class PseudoCacheConcatFragment
    extends Fragment {

  private Subscription _subscription = null;

  @Override
  public View onCreateView(LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    View layout = inflater.inflate(R.layout.fragment_form_validation_comb_latest, container, false);
    ButterKnife.inject(this, layout);
    return layout;
  }

  @Override
  public void onPause() {
    super.onPause();
    if (_subscription != null) {
      _subscription.unsubscribe();
    }
  }
}
