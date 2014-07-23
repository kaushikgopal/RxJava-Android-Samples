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
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class BufferDemoFragment
    extends Fragment {

    @InjectView(R.id.list_threading_log) ListView _logsList;

    private LogAdapter _adapter;
    private List<String> _logs;
    private int _tapCount = 0;

    private Observable<List<Integer>> _bufferedObservable;
    private Observer<List<Integer>> _observer;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        _setupLogger();

        _bufferedObservable = _getBufferedObservable();
        _observer = _getObserver();
    }


    @OnClick(R.id.btn_start_operation)
    public void onButtonTapped() {
        Timber.d("--------- GOT A TAP");
        _tapCount += 1;
        _log("GOT A TAP");
        _bufferedObservable.subscribeOn(Schedulers.io())
                         .observeOn(AndroidSchedulers.mainThread())
                         .subscribe(_observer);
    }

    // -----------------------------------------------------------------------------------
    // Main Rx entities

    private Observable<List<Integer>> _getBufferedObservable() {
        return Observable.create(new Observable.OnSubscribe<Integer>() {


            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(1);
            }

        }).buffer(2, TimeUnit.SECONDS);
    }

    private Observer<List<Integer>> _getObserver() {
        return new Observer<List<Integer>>() {


            @Override
            public void onCompleted() {
                _log(String.format("%d taps", _tapCount));
                _tapCount = 0;
            }

            @Override
            public void onError(Throwable e) {
                Timber.e(e, "--------- Woops on error!");
                _log(String.format("Dang error. check your logs"));
            }

            @Override
            public void onNext(List<Integer> integers) {
                Timber.d("--------- onNext");

                if (integers.size() > 0) {
                    for (int i : integers) {
                        _tapCount += i;
                    }
                    onCompleted();
                } else {
                    Timber.d("--------- No taps received ");
                }
            }
        };
    }

    // -----------------------------------------------------------------------------------
    // Method that help wiring up the example (irrelevant to RxJava)

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_buffer, container, false);
        ButterKnife.inject(this, layout);
        return layout;
    }

    private void _setupLogger() {
        _logs = new ArrayList<String>();
        _adapter = new LogAdapter(getActivity(), new ArrayList<String>());
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
            new Handler(Looper.getMainLooper()).post(new Runnable() {


                @Override
                public void run() {
                    _adapter.clear();
                    _adapter.addAll(_logs);
                }
            });
        }
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
