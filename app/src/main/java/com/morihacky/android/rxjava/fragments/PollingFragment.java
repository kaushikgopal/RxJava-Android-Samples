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

import com.morihacky.android.rxjava.R;
import com.morihacky.android.rxjava.RxUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
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
    private Scheduler.Worker _worker;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        _subscriptions = new CompositeSubscription();
        _worker = Schedulers.newThread()
                .createWorker();
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        RxUtils.unsubscribeIfNotNull(_subscriptions);
        ButterKnife.unbind(this);
    }

    @OnClick(R.id.btn_start_simple_polling)
    public void onStartSimplePollingClicked() {
        _setupLogger();
        _log(String.format("Simple String polling - %s", _counter));
        _subscriptions.add(Observable.create(
                        new Observable.OnSubscribe<String>() {
                            @Override
                            public void call(final Subscriber<? super String> subscriber) {
                                Subscription subscription = _worker
                                        .schedulePeriodically(new Action0() {
                                            @Override
                                            public void call() {
                                                subscriber.onNext(_doNetworkCallAndGetStringResult());
                                            }
                                        }, INITIAL_DELAY, POLLING_INTERVAL, TimeUnit.MILLISECONDS);
                                subscriber.add(subscription);
                            }
                        })
                        .take(10)
                        .subscribe(new Action1<String>() {
                            @Override
                            public void call(String s) {
                                _log(String.format("String polling - %s", s));
                            }
                        })
        );
    }

    @OnClick(R.id.btn_start_increasingly_delayed_polling)
    public void onStartIncreasinglyDelayedPolling() {
        _setupLogger();
        _log(String.format("Increasingly delayed String polling - %s", _counter));
        continueIncreasinglyDelayedPolling(1000, 10);
    }

    private void continueIncreasinglyDelayedPolling(final int delay, final int limit) {
        _subscriptions = _unsubscribeAndGetNewCompositeSub(_subscriptions);
         Observable.create(
                new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(final Subscriber<? super String> subscriber) {

                        Subscription subscription = _worker.schedule(new Action0() {
                                    @Override
                                    public void call() {
                                        subscriber.onNext(_doNetworkCallAndGetStringResult());
                                    }
                        }, delay, TimeUnit.MILLISECONDS);
                        subscriber.add(subscription);
                    }
                })
                .take(limit)
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        continueIncreasinglyDelayedPolling(delay + 1000, limit - 1);
                        Timber.d("delay of %d", delay);
                        _log(String.format("String polling - %s", s));
                    }
                });
    }
    // -----------------------------------------------------------------------------------
    // Method that help wiring up the example (irrelevant to RxJava)

    private static CompositeSubscription _unsubscribeAndGetNewCompositeSub(CompositeSubscription subscription){
        RxUtils.unsubscribeIfNotNull(subscription);
        return  RxUtils.getNewCompositeSubIfUnsubscribed(subscription);
    }

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
        _logs = new ArrayList<>();
        _adapter = new LogAdapter(getActivity(), new ArrayList<String>());
        _logsList.setAdapter(_adapter);
        _subscriptions = _unsubscribeAndGetNewCompositeSub(_subscriptions);
        _counter = 0;
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