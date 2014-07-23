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
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

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
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class ConcurrencyWithSchedulersDemoFragment
    extends Fragment {


//    @InjectView(R.id.btn_start_operation) Button _btnStart;
    @InjectView(R.id.progress_operation_running) ProgressBar _progress;
    @InjectView(R.id.list_threading_log) ListView _logsList;

    private LogAdapter _adapter;
    private List<String> _logs;


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
    private Observable<List<Integer>> _getObservable() {
        return Observable.create(new Observable.OnSubscribe<Integer>() {


            @Override
            public void call(Subscriber<? super Integer> observer) {
                observer.onNext(1);
            }
        }).buffer(2, TimeUnit.SECONDS);
    }

    /**
     * Observer that handles the result List<Integer> from Observable
     * through the 3 important actions:
     *
     * 1. onCompleted
     * 2. onError
     * 3. onNext
     */
    private Observer<List<Integer>> _getObserver() {
        return new Observer<List<Integer>>() {


            @Override
            public void onCompleted() {
                _mainThreadHandler.post(new Runnable() {


                    @Override
                    public void run() {
                        _addLogToAdapter(String.format("%d taps", _counter));
                        _counter = 0;
                    }
                });
            }

            @Override
            public void onError(Throwable e) {
                Timber.e(e, "--------- Woops on error!");
            }

            @Override
            public void onNext(List<Integer> integers) {
                for (int i : integers) {
                    _counter += i;
                }

                Timber.d("--------- on next with a count of %d", _counter);
                onCompleted();
            }
        };
    }

    // -----------------------------------------------------------------------------------
    // Method that help wiring up the example (irrelevant to RxJava)

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_concurrency_schedulers, container, false);
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
