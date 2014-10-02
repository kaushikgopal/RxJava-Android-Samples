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
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.morihacky.android.rxjava.app.R;
import java.util.ArrayList;
import java.util.List;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class PollingFragment
    extends Fragment {

  @InjectView(R.id.list_threading_log) ListView _logsList;
  @InjectView(R.id.btn_start_simple_polling) Button _btnSimplePolling;

  private LogAdapter _adapter;
  private List<String> _logs;
  private CompositeSubscription _subscriptions;

  @Override
  public void onDestroy() {
    super.onDestroy();
    _subscriptions.unsubscribe();
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
    View layout = inflater.inflate(R.layout.fragment_polling, container, false);
    ButterKnife.inject(this, layout);
    return layout;
  }

  @OnClick(R.id.btn_start_simple_polling)
  public void onStartSimplePollingClicked() {
    
  }


  // -----------------------------------------------------------------------------------
  // Method that help wiring up the example (irrelevant to RxJava)

  private void _doSomeLongOperation_thatBlocksCurrentThread() {
    _log("performing long operation");

    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      Timber.d("Operation was interrupted");
    }
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