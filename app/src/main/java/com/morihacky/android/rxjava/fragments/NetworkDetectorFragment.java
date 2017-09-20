package com.morihacky.android.rxjava.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.morihacky.android.rxjava.R;

import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.processors.PublishProcessor;
import java.util.ArrayList;
import java.util.List;

public class NetworkDetectorFragment extends BaseFragment {

  @BindView(R.id.list_threading_log)
  ListView logsList;

  private LogAdapter adapter;
  private BroadcastReceiver broadcastReceiver;
  private List<String> logs;
  private Disposable disposable;
  private PublishProcessor<Boolean> publishProcessor;
  private Unbinder unbinder;

  @Override
  public void onDestroy() {
    super.onDestroy();
    unbinder.unbind();
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View layout = inflater.inflate(R.layout.fragment_network_detector, container, false);
    unbinder = ButterKnife.bind(this, layout);
    return layout;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    setupLogger();
  }

  @Override
  public void onStart() {
    super.onStart();

    publishProcessor = PublishProcessor.create();

    disposable =
        publishProcessor
            .startWith(getConnectivityStatus(getActivity()))
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                online -> {
                  if (online) {
                    log("You are online");
                  } else {
                    log("You are offline");
                  }
                });

    listenToNetworkConnectivity();
  }

  @Override
  public void onStop() {
    super.onStop();

    disposable.dispose();
    getActivity().unregisterReceiver(broadcastReceiver);
  }

  private void listenToNetworkConnectivity() {

    broadcastReceiver =
        new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
            publishProcessor.onNext(getConnectivityStatus(context));
          }
        };

    final IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
    getActivity().registerReceiver(broadcastReceiver, intentFilter);
  }

  private boolean getConnectivityStatus(Context context) {
    ConnectivityManager cm =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
    return networkInfo != null && networkInfo.isConnected();
  }

  // -----------------------------------------------------------------------------------
  // Method that help wiring up the example (irrelevant to RxJava)

  private void log(String logMsg) {

    if (isCurrentlyOnMainThread()) {
      logs.add(0, logMsg + " (main thread) ");
      adapter.clear();
      adapter.addAll(logs);
    } else {
      logs.add(0, logMsg + " (NOT main thread) ");

      // You can only do below stuff on main thread.
      new Handler(Looper.getMainLooper())
          .post(
              () -> {
                adapter.clear();
                adapter.addAll(logs);
              });
    }
  }

  private void setupLogger() {
    logs = new ArrayList<>();
    adapter = new LogAdapter(getActivity(), new ArrayList<>());
    logsList.setAdapter(adapter);
  }

  private boolean isCurrentlyOnMainThread() {
    return Looper.myLooper() == Looper.getMainLooper();
  }

  private class LogAdapter extends ArrayAdapter<String> {

    public LogAdapter(Context context, List<String> logs) {
      super(context, R.layout.item_log, R.id.item_log, logs);
    }
  }
}
