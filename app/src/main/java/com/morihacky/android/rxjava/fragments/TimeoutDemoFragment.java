package com.morihacky.android.rxjava.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.morihacky.android.rxjava.R;
import com.morihacky.android.rxjava.wiring.LogAdapter;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import timber.log.Timber;

public class TimeoutDemoFragment extends BaseFragment {

  @BindView(R.id.list_threading_log)
  ListView _logsList;

  private LogAdapter _adapter;
  private DisposableObserver<String> _disposable;
  private List<String> _logs;

  @Override
  public void onDestroy() {
    super.onDestroy();

    if (_disposable == null) {
      return;
    }

    _disposable.dispose();
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View layout = inflater.inflate(R.layout.fragment_subject_timeout, container, false);
    ButterKnife.bind(this, layout);
    return layout;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    _setupLogger();
  }

  @OnClick(R.id.btn_demo_timeout_1_2s)
  public void onStart2sTask() {
    _disposable = _getEventCompletionObserver();

    _getObservableTask_2sToComplete()
        .timeout(3, TimeUnit.SECONDS)
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(_disposable);
  }

  @OnClick(R.id.btn_demo_timeout_1_5s)
  public void onStart5sTask() {
    _disposable = _getEventCompletionObserver();

    _getObservableTask_5sToComplete()
        .timeout(3, TimeUnit.SECONDS, _onTimeoutObservable())
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(_disposable);
  }

  // -----------------------------------------------------------------------------------
  // Main Rx entities

  private Observable<String> _getObservableTask_5sToComplete() {
    return Observable.create(
        new ObservableOnSubscribe<String>() {
          @Override
          public void subscribe(ObservableEmitter<String> subscriber) throws Exception {
            _log(String.format("Starting a 5s task"));
            subscriber.onNext("5 s");
            try {
              Thread.sleep(5_000);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
            subscriber.onComplete();
          }
        });
  }

  private Observable<String> _getObservableTask_2sToComplete() {
    return Observable.create(
        new ObservableOnSubscribe<String>() {
          @Override
          public void subscribe(ObservableEmitter<String> subscriber) throws Exception {
            _log(String.format("Starting a 2s task"));
            subscriber.onNext("2 s");
            try {
              Thread.sleep(2_000);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
            subscriber.onComplete();
          }
        });
  }

  private Observable<? extends String> _onTimeoutObservable() {
    return Observable.create(
        new ObservableOnSubscribe<String>() {

          @Override
          public void subscribe(ObservableEmitter<String> subscriber) throws Exception {
            _log("Timing out this task ...");
            subscriber.onError(new Throwable("Timeout Error"));
          }
        });
  }

  private DisposableObserver<String> _getEventCompletionObserver() {
    return new DisposableObserver<String>() {
      @Override
      public void onNext(String taskType) {
        _log(String.format("onNext %s task", taskType));
      }

      @Override
      public void onError(Throwable e) {
        _log(String.format("Dang a task timeout"));
        Timber.e(e, "Timeout Demo exception");
      }

      @Override
      public void onComplete() {
        _log(String.format("task was completed"));
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
      new Handler(Looper.getMainLooper())
          .post(
              () -> {
                _adapter.clear();
                _adapter.addAll(_logs);
              });
    }
  }

  private boolean _isCurrentlyOnMainThread() {
    return Looper.myLooper() == Looper.getMainLooper();
  }
}
