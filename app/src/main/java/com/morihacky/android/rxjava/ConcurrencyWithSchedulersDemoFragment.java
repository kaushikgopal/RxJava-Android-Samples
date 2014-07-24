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
import android.widget.ProgressBar;

import com.morihacky.android.rxjava.app.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.observables.AndroidObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class ConcurrencyWithSchedulersDemoFragment
    extends Fragment {


    @InjectView(R.id.progress_operation_running) ProgressBar _progress;
    @InjectView(R.id.list_threading_log) ListView _logsList;

    private LogAdapter _adapter;
    private List<String> _logs;
    private Subscription _subscription;

    @OnClick(R.id.btn_start_operation)
    public void startLongOperation() {

        _progress.setVisibility(View.VISIBLE);
        _log("Button Clicked");

        _subscription = AndroidObservable.bindFragment(this, _getObservable())      // Observable
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(_getObserver());                             // Observer
    }

    // -----------------------------------------------------------------------------------
    // Main Rx entities

    private Observable<Boolean> _getObservable() {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {


            @Override
            public void call(Subscriber<? super Boolean> observer) {

                _log("Within Observable");

                _doSomeLongOperation_thatBlocksCurrentThread();
                observer.onNext(true);
                observer.onCompleted();
            }
        });
    }

    /**
     * Observer that handles the result List<Integer> from Observable
     * through the 3 important actions:
     *
     * 1. onCompleted
     * 2. onError
     * 3. onNext
     */
    private Observer<Boolean> _getObserver() {
        return new Observer<Boolean>() {


            @Override
            public void onCompleted() {
                _log("On complete");
                _progress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onError(Throwable e) {
                Timber.e(e, "Error in RxJava Demo concurrency");
                _log(String.format("Boo Error %s", e.getMessage()));
                _progress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onNext(Boolean aBoolean) {
                _log(String.format("onNext with return value \"%b\"", aBoolean));
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        _subscription.unsubscribe();
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

    // -----------------------------------------------------------------------------------
    // Method that help wiring up the example (irrelevant to RxJava)

    private void _doSomeLongOperation_thatBlocksCurrentThread() {
        _log("performing long operation");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
        ButterKnife.inject(this, layout);
        return layout;
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
