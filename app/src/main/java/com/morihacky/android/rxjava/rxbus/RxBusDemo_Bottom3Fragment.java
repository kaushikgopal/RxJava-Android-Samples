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
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.flowables.ConnectableFlowable;
import java.util.concurrent.TimeUnit;

public class RxBusDemo_Bottom3Fragment extends BaseFragment {

  @BindView(R.id.demo_rxbus_tap_txt)
  TextView _tapEventTxtShow;

  @BindView(R.id.demo_rxbus_tap_count)
  TextView _tapEventCountShow;

  private RxBus _rxBus;
  private CompositeDisposable _disposables;

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

    ConnectableFlowable<Object> tapEventEmitter = _rxBus.asFlowable().publish();

    _disposables //
        .add(
        tapEventEmitter.subscribe(
            event -> {
              if (event instanceof RxBusDemoFragment.TapEvent) {
                _showTapText();
              }
            }));

    _disposables.add(
        tapEventEmitter
            .publish(stream -> stream.buffer(stream.debounce(1, TimeUnit.SECONDS)))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                taps -> {
                  _showTapCount(taps.size());
                }));

    _disposables.add(tapEventEmitter.connect());
  }

  @Override
  public void onStop() {
    super.onStop();
    _disposables.clear();
  }

  // -----------------------------------------------------------------------------------
  // Helper to show the text via an animation

  private void _showTapText() {
    _tapEventTxtShow.setVisibility(View.VISIBLE);
    _tapEventTxtShow.setAlpha(1f);
    ViewCompat.animate(_tapEventTxtShow).alphaBy(-1f).setDuration(400);
  }

  private void _showTapCount(int size) {
    _tapEventCountShow.setText(String.valueOf(size));
    _tapEventCountShow.setVisibility(View.VISIBLE);
    _tapEventCountShow.setScaleX(1f);
    _tapEventCountShow.setScaleY(1f);
    ViewCompat.animate(_tapEventCountShow)
        .scaleXBy(-1f)
        .scaleYBy(-1f)
        .setDuration(800)
        .setStartDelay(100);
  }
}
