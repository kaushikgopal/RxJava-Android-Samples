package com.morihacky.android.rxjava.rxbus;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.morihacky.android.rxjava.MainActivity;
import com.morihacky.android.rxjava.app.R;

public class RxBusFrag1
    extends Fragment {

  private RxBus _rxBus;

  @Override
  public View onCreateView(LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    View layout = inflater.inflate(R.layout.fragment_rxbus_frag1, container, false);
    ButterKnife.inject(this, layout);
    return layout;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    _rxBus = ((MainActivity) getActivity()).getRxBusSingleton();
  }

  @OnClick(R.id.btn_demo_rxbus_tap)
  public void onTapButtonClicked() {
    _rxBus.send(new TapEvent());
  }

  public static class TapEvent {}
}
