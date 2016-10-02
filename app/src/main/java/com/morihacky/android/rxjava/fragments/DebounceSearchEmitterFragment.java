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
import android.widget.EditText;
import android.widget.ListView;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewTextChangeEvent;
import com.morihacky.android.rxjava.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

import static co.kaush.core.util.CoreNullnessUtils.isNotNullOrEmpty;
import static java.lang.String.format;

public class DebounceSearchEmitterFragment
      extends BaseFragment {

    @Bind(R.id.list_threading_log) ListView _logsList;
    @Bind(R.id.input_txt_debounce) EditText _inputSearchText;

    private LogAdapter _adapter;
    private List<String> _logs;

    private Subscription _subscription;

    @Override
    public void onDestroy() {
        super.onDestroy();
        _subscription.unsubscribe();
        ButterKnife.unbind(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_debounce, container, false);
        ButterKnife.bind(this, layout);
        return layout;
    }

    @OnClick(R.id.clr_debounce)
    public void onClearLog() {
        _logs = new ArrayList<>();
        _adapter.clear();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        _setupLogger();

        _subscription = RxTextView.textChangeEvents(_inputSearchText)
              .debounce(400, TimeUnit.MILLISECONDS)// default Scheduler is Computation
              .filter(changes -> isNotNullOrEmpty(_inputSearchText.getText().toString()))
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(_getSearchObserver());
    }

    // -----------------------------------------------------------------------------------
    // Main Rx entities

    private Observer<TextViewTextChangeEvent> _getSearchObserver() {
        return new Observer<TextViewTextChangeEvent>() {
            @Override
            public void onCompleted() {
                Timber.d("--------- onComplete");
            }

            @Override
            public void onError(Throwable e) {
                Timber.e(e, "--------- Woops on error!");
                _log("Dang error. check your logs");
            }

            @Override
            public void onNext(TextViewTextChangeEvent onTextChangeEvent) {
                _log(format("Searching for %s", onTextChangeEvent.text().toString()));
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

    private class LogAdapter
          extends ArrayAdapter<String> {

        public LogAdapter(Context context, List<String> logs) {
            super(context, R.layout.item_log, R.id.item_log, logs);
        }
    }
}