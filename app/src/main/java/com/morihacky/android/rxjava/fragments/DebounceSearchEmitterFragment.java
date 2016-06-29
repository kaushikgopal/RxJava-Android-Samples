package com.morihacky.android.rxjava.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;
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
import rx.functions.Func1;
import timber.log.Timber;

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

        _subscription = RxTextView.textChangeEvents(_inputSearchText)//
              .debounce(400, TimeUnit.MILLISECONDS)// default Scheduler is Computation
              .map(new Func1<TextViewTextChangeEvent, String>() {
                  @Override
                  public String call(TextViewTextChangeEvent textViewTextChangeEvent) {
                      return textViewTextChangeEvent.text().toString();
                  }
              })
              .filter(new Func1<String, Boolean>() {
                  @Override
                  public Boolean call(String changes) {
                      return !TextUtils.isEmpty(changes);
                  }
              })
              .distinct()
              .observeOn(AndroidSchedulers.mainThread())//
              .subscribe(_getSearchObserver());
    }

    // -----------------------------------------------------------------------------------
    // Main Rx entities

    private Observer<String> _getSearchObserver() {
        return new Observer<String>() {
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
            public void onNext(String changes) {
                _log(format("Searching for %s", changes));
            }
        };
    }

    // -----------------------------------------------------------------------------------
    // Method that help wiring up the example (irrelevant to RxJava)

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

    private class LogAdapter
          extends ArrayAdapter<String> {

        public LogAdapter(Context context, List<String> logs) {
            super(context, R.layout.item_log, R.id.item_log, logs);
        }
    }
}