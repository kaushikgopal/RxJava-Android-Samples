package com.morihacky.android.rxjava.rxbus;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.morihacky.android.rxjava.MainActivity;
import com.morihacky.android.rxjava.app.R;
import java.util.List;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Subscription;
import rx.android.observables.AndroidObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

import static com.morihacky.android.rxjava.rxbus.RxBusFrag1.TapEvent;

public class RxBusFrag2
    extends Fragment {

  private RxBus _rxBus;
  private Subscription _subscription1_tapListen;
  private Subscription _subscription2_tapCollector;

  @InjectView(R.id.demo_rxbus_tap_txt) TextView _tapEventTxtShow;
  @InjectView(R.id.demo_rxbus_tap_count) TextView _tapEventCountShow;

  @Override
  public View onCreateView(LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    View layout = inflater.inflate(R.layout.fragment_rxbus_frag2, container, false);
    ButterKnife.inject(this, layout);
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

    // .share = publish + refcount
    Observable<Object> tapEventEmitter = _rxBus.toObserverable().share();

    _subscription1_tapListen = AndroidObservable.bindFragment(this, tapEventEmitter)
             .subscribe(new Action1<Object>() {
               @Override
               public void call(Object event) {
                 if (event instanceof TapEvent) {
                   _showTapText();
                 }
               }
             });

    Observable<Object> debouncedEventEmitter = tapEventEmitter.debounce(1, TimeUnit.SECONDS);
    tapEventEmitter.buffer(debouncedEventEmitter)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action1<List<Object>>() {
          @Override
          public void call(List<Object> taps) {
            _showTapCount(taps.size());
          }
        });
  }

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
        .setDuration(400)
        .setStartDelay(100);
  }

  @Override
  public void onStop() {
    super.onStop();
    _subscription1_tapListen.unsubscribe();
  }
}
