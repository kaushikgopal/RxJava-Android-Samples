package com.morihacky.android.rxjava;

import android.support.annotation.Nullable;

import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;


public class RxUtils {

    public static void unsubscribeIfNotNull(Subscription subscription) {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    public static <T> rx.Observable.Transformer<T, T> applyLoadingFlipping(@Nullable final ShowsLoader view) {
        return new rx.Observable.Transformer<T, T>() {
            @Override
            public rx.Observable<T> call(rx.Observable<T> observable) {
                return observable
                        .doOnSubscribe(new Action0() {
                            @Override
                            public void call() {
                                if (view != null) {
                                    view.showLoader(true);
                                }
                            }
                        }).doOnError(new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                if (view != null) {
                                    view.showLoader(false);
                                }
                            }
                        })
                        .doOnNext(new Action1<T>() {
                            @Override
                            public void call(T t) {
                                if (view != null) {
                                    view.showLoader(false);
                                }
                            }
                        });
            }
        };
    }
}
