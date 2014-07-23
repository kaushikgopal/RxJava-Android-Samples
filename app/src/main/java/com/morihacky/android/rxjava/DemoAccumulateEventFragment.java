package com.morihacky.android.rxjava;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.morihacky.android.rxjava.app.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.android.observables.AndroidObservable;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;

public class DemoAccumulateEventFragment
    extends Fragment
    implements Observer<Integer> {

    @InjectView(R.id.accumulated_event_list) ListView _logsListView;

    private LogAdapter _adapter;
    private List<String> _logs;
    private final Handler _mainThreadHandler = new Handler(Looper.getMainLooper());

    private BehaviorSubject<Observable<Integer>> _loggerSubject;            // Special Rx entity that modifies Observable behavior for aggregation
    private int _counter = 0;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        _setupLogAdapter();

        // Create the subscription
        AndroidObservable.bindFragment(this, _getObservable_ForTaps())     // Observable
            .observeOn(Schedulers.io())
            .subscribe(_getSubscriber_ForTaps());                          // Subscriber
    }


    @OnClick(R.id.accumulated_event_btn)
    public void onButtonTapped() {
        // BehaviorSubject takes in Observable inputs.
        // So send 1 tap as an observable
        _counter += 1;
        _loggerSubject.onNext(Observable.from(_counter));
    }

    // -----------------------------------------------------------------------------------
    // Main Rx entities

    /**
     * Gets the Observable as emitted from BehaviorSubject
     *
     * It begins by emitting the item most recently emitted by source Observable
     * (or seed/default if none has yet been emitted - which is the case here)
     *
     * https://github.com/Netflix/RxJava/wiki/Subject#behaviorsubject
     */
    private Observable<Integer> _getObservable_ForTaps() {
        if (_loggerSubject == null) {
            _loggerSubject = BehaviorSubject.create(Observable.from(0));
        }

        return Observable.switchOnNext(_loggerSubject);
    }

    /**
     * Subscriber that has the 3 important actions
     * 1. onNext
     * 2. onError
     * 3. onCompleted
     */
    private Observer<? super Integer> _getSubscriber_ForTaps() {
        return this;
    }

    @Override
    public void onCompleted() {
        Timber.d("--------- completed ");

        _mainThreadHandler.post(new Runnable() {


            @Override
            public void run() {
                _addLogToAdapter(String.format("%d taps", _counter));
                _counter = 0;
            }
        });
    }

    @Override
    public void onNext(Integer integer) {
        Timber.d("--------- on next %d", integer);
        _simulateLongOperation();
        onCompleted();
    }

    @Override
    public void onError(Throwable e) {
        Timber.e(e, "--------- Woops on error!");
    }

    private void _simulateLongOperation() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // -----------------------------------------------------------------------------------
    // Method that help wiring up the example (irrelevant to RxJava)

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_accumulate, container, false);
        ButterKnife.inject(this, layout);
        return layout;
    }

    private void _setupLogAdapter() {
        _logs = new ArrayList<String>();
        _adapter = new LogAdapter(getActivity(), new ArrayList<String>());
        _logsListView.setAdapter(_adapter);
    }

    private void _addLogToAdapter(String logMsg) {
        _logs.add(0, logMsg);
        _adapter.clear();
        _adapter.addAll(_logs);
    }

    private class LogAdapter
        extends ArrayAdapter<String> {

        public LogAdapter(Context context, List<String> logs) {
            super(context, R.layout.item_log, R.id.item_log, logs);
        }
    }
}
