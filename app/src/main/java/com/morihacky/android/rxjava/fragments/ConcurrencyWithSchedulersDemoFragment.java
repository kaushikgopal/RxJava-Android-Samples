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
import android.widget.ListView;
import android.widget.ProgressBar;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.morihacky.android.rxjava.R;

import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;
import timber.log.Timber;

public class ConcurrencyWithSchedulersDemoFragment extends BaseFragment {

  @BindView(R.id.progress_operation_running)
  ProgressBar _progress;

  @BindView(R.id.list_threading_log)
  ListView _logsList;

  private LogAdapter _adapter;
  private List<String> _logs;
  private CompositeDisposable _disposables = new CompositeDisposable();
  private Unbinder unbinder;

  @Override
  public void onDestroy() {
    super.onDestroy();
    unbinder.unbind();
    _disposables.clear();
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    _setupLogger();
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View layout = inflater.inflate(R.layout.fragment_concurrency_schedulers, container, false);
    unbinder = ButterKnife.bind(this, layout);
    return layout;
  }

  @OnClick(R.id.btn_start_operation)
  public void startLongOperation() {

    _progress.setVisibility(View.VISIBLE);
    _log("Button Clicked");

    DisposableObserver<Boolean> d = _getDisposableObserver();

    _getObservable()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(d);

    _disposables.add(d);
  }

  private Observable<Boolean> _getObservable() {
    return Observable.just(true)
        .map(
            aBoolean -> {
              _log("Within Observable");
              _doSomeLongOperation_thatBlocksCurrentThread();
              return aBoolean;
            });
  }

  /**
   * Observer that handles the result through the 3 important actions:
   *
   * <p>1. onCompleted 2. onError 3. onNext
   */
  private DisposableObserver<Boolean> _getDisposableObserver() {
    return new DisposableObserver<Boolean>() {

      @Override
      public void onComplete() {
        _log("On complete");
        _progress.setVisibility(View.INVISIBLE);
      }

      @Override
      public void onError(Throwable e) {
        Timber.e(e, "Error in RxJava Demo concurrency");
        _log(String.format("Boo! Error %s", e.getMessage()));
        _progress.setVisibility(View.INVISIBLE);
      }

      @Override
      public void onNext(Boolean bool) {
        _log(String.format("onNext with return value \"%b\"", bool));
      }
    };
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
      new Handler(Looper.getMainLooper())
          .post(
              () -> {
                _adapter.clear();
                _adapter.addAll(_logs);
              });
    }
  }

  private void _setupLogger() {
    _logs = new ArrayList<>();
    _adapter = new LogAdapter(getActivity(), new ArrayList<>());
    _logsList.setAdapter(_adapter);
  }

  private boolean _isCurrentlyOnMainThread() {
    return Looper.myLooper() == Looper.getMainLooper();
  }

  private class LogAdapter extends ArrayAdapter<String> {

    public LogAdapter(Context context, List<String> logs) {
      super(context, R.layout.item_log, R.id.item_log, logs);
    }
  }
}
