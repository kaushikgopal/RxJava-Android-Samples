package com.morihacky.android.rxjava.rxbus;

import com.jakewharton.rxrelay2.PublishRelay;
import com.jakewharton.rxrelay2.Relay;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

/** courtesy: https://gist.github.com/benjchristensen/04eef9ca0851f3a5d7bf */
public class RxBus {

  private final Relay<Object> _bus = PublishRelay.create().toSerialized();

  public void send(Object o) {
    _bus.accept(o);
  }

  public Flowable<Object> asFlowable() {
    return _bus.toFlowable(BackpressureStrategy.LATEST);
  }

  public boolean hasObservers() {
    return _bus.hasObservers();
  }
}
