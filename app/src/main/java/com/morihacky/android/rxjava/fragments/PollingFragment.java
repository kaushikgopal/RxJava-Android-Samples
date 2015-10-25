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
import butterknife.ButterKnife;
import butterknife.Bind;
import butterknife.OnClick;
import com.morihacky.android.rxjava.R;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class PollingFragment
      extends BaseFragment {

    public static final int INITIAL_DELAY = 0;
    public static final int POLLING_INTERVAL = 1000;
    @Bind(R.id.list_threading_log) ListView _logsList;

    private LogAdapter _adapter;
    private List<String> _logs;
    private CompositeSubscription _subscriptions;
    private int _counter = 0;

    @Override
    public void onDestroy() {
        super.onDestroy();
        _subscriptions.unsubscribe();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        _subscriptions = new CompositeSubscription();
        _setupLogger();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_polling, container, false);
        ButterKnife.bind(this, layout);
        return layout;
    }

    @OnClick(R.id.btn_start_simple_polling)
    public void onStartSimplePollingClicked() {
        _subscriptions.add(Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> observer) {

                Schedulers.newThread().createWorker() //
                      .schedulePeriodically(new Action0() {
                          @Override
                          public void call() {
                              observer.onNext(_doNetworkCallAndGetStringResult());
                          }
                      }, INITIAL_DELAY, POLLING_INTERVAL, TimeUnit.MILLISECONDS);
            }
        }).take(10).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                _log(String.format("String polling - %s", s));
            }
        }));
    }

    // -----------------------------------------------------------------------------------
    // Method that help wiring up the example (irrelevant to RxJava)

    private String _doNetworkCallAndGetStringResult() {

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Timber.d("Operation was interrupted");
        }
        _counter++;

        return String.valueOf(_counter);
    }

    private void _log(String logMsg) {
        if (_isCurrentlyOnMainThread()) {
            _logs.add(0, logMsg + " (main thread) ");
            _adapter.clear();
            _adapter.addAll(_logs);
        } else {
            _logs.add(0, logMsg + " (NOT main thread) ");

            // You can only do below stuff on main thread.
            new Handler(Looper.getMainLooper()).post(new Runnable() {

                @Override
                public void run() {
                    _adapter.clear();
                    _adapter.addAll(_logs);
                }
            });
        }
    }

    private void _setupLogger() {
        _logs = new ArrayList<String>();
        _adapter = new LogAdapter(getActivity(), new ArrayList<String>());
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