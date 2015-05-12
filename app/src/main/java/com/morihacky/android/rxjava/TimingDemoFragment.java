package com.morihacky.android.rxjava;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import butterknife.OnClick;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import timber.log.Timber;

public class TimingDemoFragment
      extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_timing_demo, container, false);
        ButterKnife.inject(this, layout);
        return layout;
    }

    @OnClick(R.id.btn_start_demo)
    public void onStartButtonClicked() {
        Timber.d("-------------------- Shutter clicked");

        Observable.just(1).flatMap(new Func1<Integer, Observable<Object>>() {
            @Override
            public Observable<Object> call(Integer integer) {
                return _getCountdownFlashObservable();
            }
        }).repeat(3).subscribe();
    }

    private Observable<Object> _getCountdownFlashObservable() {
        return Observable.interval(1, TimeUnit.SECONDS)
              .map(new Func1<Long, Object>() {
                  @Override
                  public Object call(Long aLong) {
                      Timber.d("-------------------- Number to show %s",
                            String.valueOf(3l - aLong));
                      return 3l - aLong;
                  }
              })
              .take(3)
              .observeOn(AndroidSchedulers.mainThread())
              .doOnNext(new Action1<Object>() {
                  @Override
                  public void call(Object o) {
                      Timber.d("-------------------- %s", String.valueOf(o));
                  }
              })
              .delay(1, TimeUnit.SECONDS)
              .observeOn(AndroidSchedulers.mainThread())
              .doOnCompleted(new Action0() {
                  @Override
                  public void call() {
                      Timber.d("-------------------- FLASH!");
                  }
              });
    }
}
