package com.morihacky.android.rxjava.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.morihacky.android.rxjava.R;
import com.morihacky.android.rxjava.RxUtils;
import com.morihacky.android.rxjava.wiring.LogAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class TimeoutDemoFragment
      extends BaseFragment {

    @Bind(R.id.list_threading_log) ListView _logsList;

    private LogAdapter _adapter;
    private List<String> _logs;

    private Subscription _subscription;

    @Override
    public void onDestroy() {
        super.onDestroy();
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
        View layout = inflater.inflate(R.layout.fragment_subject_timeout, container, false);
        ButterKnife.bind(this, layout);
        return layout;
    }

    @OnClick(R.id.btn_demo_timeout_1_2s)
    public void onStart2sTask() {
        _subscription = _getObservableTask_2sToComplete()//
              .observeOn(AndroidSchedulers.mainThread())//
              .subscribe(_getEventCompletionObserver());
    }

    @OnClick(R.id.btn_demo_timeout_1_5s)
    public void onStart5sTask() {
        _subscription = _getObservableFor5sTask()//
              .timeout(2, TimeUnit.SECONDS, _getTimeoutObservable())
              .subscribeOn(Schedulers.computation())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(_getEventCompletionObserver());
    }

    // -----------------------------------------------------------------------------------
    // Main Rx entities

    private Observable<String> _getObservableFor5sTask() {
        return Observable.create(new Observable.OnSubscribe<String>() {

            @Override
            public void call(Subscriber<? super String> subscriber) {
                _log(String.format("Starting a 5s task"));
                subscriber.onNext("5 s");
                try {
                    Thread.sleep(1200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                subscriber.onCompleted();
            }
        });
    }

    private Observable<String> _getObservableTask_2sToComplete() {
        return Observable.create(new Observable.OnSubscribe<String>() {

            @Override
            public void call(Subscriber<? super String> subscriber) {
                _log(String.format("Starting a 2s task"));
                subscriber.onNext("2 s");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.computation()).timeout(3, TimeUnit.SECONDS);
    }

    private Observable<? extends String> _getTimeoutObservable() {
        return Observable.create(new Observable.OnSubscribe<String>() {

            @Override
            public void call(Subscriber<? super String> subscriber) {
                _log("Timing out this task ...");
                subscriber.onCompleted();
            }
        });
    }

    private Observer<String> _getEventCompletionObserver() {
        return new Observer<String>() {

            @Override
            public void onCompleted() {
                _log(String.format("task was completed"));
            }

            @Override
            public void onError(Throwable e) {
                _log(String.format("Dang a task timeout"));
                onCompleted();
                Timber.e(e, "Timeout Demo exception");
            }

            @Override
            public void onNext(String taskType) {
                _log(String.format("onNext %s task", taskType));
            }
        };
    }

    // -----------------------------------------------------------------------------------
    // Method that help wiring up the example (irrelevant to RxJava)

    private void _setupLogger() {
        _logs = new ArrayList<>();
        _adapter = new LogAdapter(getActivity(), new ArrayList<>());
        _logsList.setAdapter(_adapter);
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

    private boolean _isCurrentlyOnMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }
}