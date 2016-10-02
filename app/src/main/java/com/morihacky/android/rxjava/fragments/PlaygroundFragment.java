package com.morihacky.android.rxjava.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.morihacky.android.rxjava.R;
import com.morihacky.android.rxjava.RxUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class PlaygroundFragment
      extends BaseFragment {

    @Bind(R.id.progress_operation_running) ProgressBar _progress;
    @Bind(R.id.list_threading_log) ListView _logsList;

    private LogAdapter _adapter;
    private List<String> _logs;
    private Subscription _subscription;
    private int _attempt = 0;

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        RxUtils.unsubscribeIfNotNull(_subscription);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        _setupLogger();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_concurrency_schedulers, container, false);
        ButterKnife.bind(this, layout);
        return layout;
    }

    @OnClick(R.id.btn_start_operation)
    public void startOperation() {

        _logs.clear();
        _log("Button Clicked");

        _subscription =//
              Observable//
                    .from(Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l"))//
//                    .from(Arrays.asList("a", "b", "c", "d"))//
                    .flatMap(s ->//
                                Observable//
                                      .just(s)//
                                      .onBackpressureBuffer(3)
                                      .flatMap(s1 -> {

                                          _log(s1 + "start");

                                          if (s1.equalsIgnoreCase("b") && _attempt < 5) {
                                              _attempt++;
                                              return Observable.error(new Throwable("b can't be processed ("+(_attempt - 1)+")"));
                                          }

                                          if (s1.equalsIgnoreCase("c") || s1.equalsIgnoreCase("f")) {
                                              return Observable.just(s1);
                                          } else {
                                              return Observable.timer(2, TimeUnit.SECONDS).map(l -> s1);
                                          }
                                      })
                                      .retryWhen(source -> source.delay(8, TimeUnit.SECONDS))
                          /*, 3*/)
                    .doOnNext(s -> _log(s + "stop"))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe();
    }

    // -----------------------------------------------------------------------------------
    // Method that help wiring up the example (irrelevant to RxJava)

    private void _doSomeLongOperation_thatBlocksCurrentThread() {
        _log("performing long operation");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Timber.d("Operation was interrupted");
        }
    }

    private void _log(String logMsg) {

        if (_isCurrentlyOnMainThread()) {
            _logs.add(0, logMsg + " (main thread) ");
            _adapter.clear();
            _adapter.addAll(_logs);
        } else {
            _logs.add(0, logMsg + " (NOT main thread) ");

            // You can only do below stuff on main thread.
            new Handler(Looper.getMainLooper()).post(() -> {
                _adapter.clear();
                _adapter.addAll(_logs);
            });
        }
    }

    private void _setupLogger() {
        _logs = new ArrayList<>();
        _adapter = new LogAdapter(getActivity(), new ArrayList<>());
        _logsList.setAdapter(_adapter);
    }

    private boolean _isCurrentlyOnMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    private class LogAdapter
          extends ArrayAdapter<String> {

        public LogAdapter(Context context, List<String> logs) {
            super(context, R.layout.item_log, R.id.item_log, logs);
        }
    }
}