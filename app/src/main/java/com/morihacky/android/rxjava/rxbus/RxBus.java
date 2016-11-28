package com.morihacky.android.rxjava.rxbus;

import com.jakewharton.rxrelay.PublishRelay;
import com.jakewharton.rxrelay.Relay;
import hu.akarnokd.rxjava.interop.RxJavaInterop;
import io.reactivex.Flowable;

/**
 * courtesy: https://gist.github.com/benjchristensen/04eef9ca0851f3a5d7bf
 */
public class RxBus {

    private final Relay<Object, Object> _bus = PublishRelay.create().toSerialized();

    public void send(Object o) {
        _bus.call(o);
    }

    public Flowable<Object> asFlowable() {
        // this won't be necessary after https://github.com/JakeWharton/RxRelay/pull/20 is complete
        return RxJavaInterop.toV2Flowable(_bus.asObservable());
    }

    public boolean hasObservers() {
        return _bus.hasObservers();
    }
}