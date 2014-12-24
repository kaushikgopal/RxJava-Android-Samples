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
import rx.Subscription;
import rx.android.observables.AndroidObservable;
import rx.functions.Action1;

import static com.morihacky.android.rxjava.rxbus.RxBusFrag1.TapEvent;

public class RxBusFrag2
    extends Fragment {

  private RxBus _rxBus;
  private Subscription _subscription;

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
    AndroidObservable.bindFragment(this, _rxBus.toObserverable()).subscribe(new Action1<Object>() {
      @Override
      public void call(Object event) {
        if (event instanceof TapEvent) {
          _showTapText();
        }
      }
    });
  }

  private void _showTapText() {
    _tapEventTxtShow.setVisibility(View.VISIBLE);
    _tapEventTxtShow.setScaleX(1f);
    _tapEventTxtShow.setScaleY(1f);

    ViewCompat.animate(_tapEventTxtShow)
        .scaleX(0f)
        .scaleY(0f)
        .setDuration(400)
        /*.withEndAction(new Runnable() {
          @Override
          public void run() {
            _tapEventTxtShow.setVisibility(View.INVISIBLE);
            _tapEventTxtShow.setAlpha(1f);
          }
        })*/;
  }

  @Override
  public void onStop() {
    super.onStop();
    _subscription.unsubscribe();
  }
}
