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
import java.util.concurrent.TimeUnit;
import rx.Subscription;
import rx.android.observables.AndroidObservable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.morihacky.android.rxjava.rxbus.RxBusFrag1.TapEvent;

public class RxBusFrag2
    extends Fragment {

  private RxBus _rxBus;
  private Subscription _subscription1_tapListen;

  @InjectView(R.id.demo_rxbus_tap_txt) TextView _tapEventTxtShow;

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

    _subscription1_tapListen = AndroidObservable.bindFragment(this, _rxBus.toObserverable())
        .subscribe(new Action1<Object>() {
          @Override
          public void call(Object event) {
            if (event instanceof TapEvent) {
              _showTapText();
            }
          }
        });

    _rxBus.toObserverable().debounce(400, TimeUnit.MILLISECONDS, Schedulers.io()).subscribe();

  }

  private void _showTapText() {
    _tapEventTxtShow.setVisibility(View.VISIBLE);
    _tapEventTxtShow.setAlpha(1f);
    ViewCompat.animate(_tapEventTxtShow).alphaBy(-1f).setDuration(400);
  }

  @Override
  public void onStop() {
    super.onStop();
    _subscription1_tapListen.unsubscribe();
  }
}
