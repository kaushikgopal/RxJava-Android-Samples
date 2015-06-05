package com.morihacky.android.rxjava;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * credit to @tomrozb for this implementation:
 * http://stackoverflow.com/questions/24922610/incorrect-understanding-of-buffer-in-rxjava
 *
 * An alternate mechanism of achieving the same result would be to use a {@link rx.subjects.PublishSubject}
 * as demonstrated in the case of {@link com.morihacky.android.rxjava.SubjectDebounceSearchEmitterFragment}
 */
public class BufferDemoFragment
      extends BaseFragment {

    @InjectView(R.id.list_threading_log) ListView _logsList;
    @InjectView(R.id.btn_start_operation) Button _tapBtn;

    private LogAdapter _adapter;
    private List<String> _logs;
    private int _tapCount = 0;

    private Subscription _subscription;

    @Override
    public void onResume() {
        super.onResume();
        _subscription = _getBufferedObservable().subscribe(_getObserver());
    }

    @Override
    public void onPause() {
        super.onPause();
        _subscription.unsubscribe();
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
        View layout = inflater.inflate(R.layout.fragment_buffer, container, false);
        ButterKnife.inject(this, layout);
        return layout;
    }

    // -----------------------------------------------------------------------------------
    // Main Rx entities

    private Observable<List<Integer>> _getBufferedObservable() {

        return Observable.create(new Observable.OnSubscribe<Integer>() {

            @Override
            public void call(final Subscriber<? super Integer> subscriber) {
                _tapBtn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        subscriber.onNext(++_tapCount);
                        Timber.d("--------- GOT A TAP");
                        _log("GOT A TAP");
                    }
                });
            }
        })
              .buffer(2, TimeUnit.SECONDS)
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread());
    }

    private Action1<List<Integer>> _getObserver() {
        return new Action1<List<Integer>>() {
            @Override
            public void call(final List<Integer> integers) {
                if (_tapCount > 0) {
                    _log(String.format("%d taps", _tapCount));
                    _tapCount = 0;
                }
            }
        };
    }

    // -----------------------------------------------------------------------------------
    // Methods that help wiring up the example (irrelevant to RxJava)

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
