package com.morihacky.android.rxjava.rxbus;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.morihacky.android.rxjava.MainActivity;
import com.morihacky.android.rxjava.R;
import com.morihacky.android.rxjava.fragments.BaseFragment;
import io.reactivex.disposables.CompositeDisposable;

public class RxBusDemo_Bottom1Fragment extends BaseFragment {

  @BindView(R.id.demo_rxbus_tap_txt)
  TextView _tapEventTxtShow;

  private CompositeDisposable _disposables;
  private RxBus _rxBus;

  @Override
  public View onCreateView(
      LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View layout = inflater.inflate(R.layout.fragment_rxbus_bottom, container, false);
    ButterKnife.bind(this, layout);
    return layout;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    _rxBus = ((MainActivity) getActivity()).getRxBusSingleton();
  }

  @Override
  public void onStart() {
    super.onStart();
    _disposables = new CompositeDisposable();

    _disposables.add(
        _rxBus
            .asFlowable()
            .subscribe(
                event -> {
                  if (event instanceof RxBusDemoFragment.TapEvent) {
                    _showTapText();
                  }
                }));
  }

  @Override
  public void onStop() {
    super.onStop();
    _disposables.clear();
  }

  private void _showTapText() {
    _tapEventTxtShow.setVisibility(View.VISIBLE);
    _tapEventTxtShow.setAlpha(1f);
    ViewCompat.animate(_tapEventTxtShow).alphaBy(-1f).setDuration(400);
  }
}
