package com.morihacky.android.rxjava.rxbus;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.morihacky.android.rxjava.MainActivity;
import com.morihacky.android.rxjava.R;
import com.morihacky.android.rxjava.fragments.BaseFragment;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observables.ConnectableObservable;
import rx.subscriptions.CompositeSubscription;

public class RxBusDemo_Bottom3Fragment
      extends BaseFragment {

    @Bind(R.id.demo_rxbus_tap_txt) TextView _tapEventTxtShow;
    @Bind(R.id.demo_rxbus_tap_count) TextView _tapEventCountShow;
    private RxBus _rxBus;
    private CompositeSubscription _subscriptions;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
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
        _subscriptions = new CompositeSubscription();

        ConnectableObservable<Object> tapEventEmitter = _rxBus.toObserverable().publish();

        _subscriptions//
              .add(tapEventEmitter.subscribe(new Action1<Object>() {
                  @Override
                  public void call(Object event) {
                      if (event instanceof RxBusDemoFragment.TapEvent) {
                          _showTapText();
                      }
                  }
              }));

        _subscriptions//
              .add(tapEventEmitter.publish(new Func1<Observable<Object>, Observable<List<Object>>>() {
                  @Override
                  public Observable<List<Object>> call(Observable<Object> stream) {
                      return stream.buffer(stream.debounce(1, TimeUnit.SECONDS));
                  }
              }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<List<Object>>() {
                  @Override
                  public void call(List<Object> taps) {
                      _showTapCount(taps.size());
                  }
              }));

        _subscriptions.add(tapEventEmitter.connect());

    }

    @Override
    public void onStop() {
        super.onStop();
        _subscriptions.clear();
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
