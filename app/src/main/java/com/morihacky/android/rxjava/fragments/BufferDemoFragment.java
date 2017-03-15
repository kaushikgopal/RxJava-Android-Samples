package com.morihacky.android.rxjava.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.jakewharton.rxbinding2.view.RxView;
import com.morihacky.android.rxjava.R;
import com.morihacky.android.rxjava.wiring.LogAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import timber.log.Timber;

/**
 * This is a demonstration of the `buffer` Observable.
 *
 * <p>The buffer observable allows taps to be collected only within a time span. So taps outside the
 * 2s limit imposed by buffer will get accumulated in the next log statement.
 *
 * <p>If you're looking for a more foolproof solution that accumulates "continuous" taps vs a more
 * dumb solution as show below (i.e. number of taps within a timespan) look at {@link
 * com.morihacky.android.rxjava.rxbus.RxBusDemo_Bottom3Fragment} where a combo of `publish` and
 * `buffer` is used.
 *
 * <p>Also http://nerds.weddingpartyapp.com/tech/2015/01/05/debouncedbuffer-used-in-rxbus-example/
 * if you're looking for words instead of code
 */
public class BufferDemoFragment extends BaseFragment {

  @BindView(R.id.list_threading_log)
  ListView _logsList;

  @BindView(R.id.btn_start_operation)
  Button _tapBtn;

  private LogAdapter _adapter;
  private List<String> _logs;

  private Disposable _disposable;
  private Unbinder unbinder;

  @Override
  public void onResume() {
    super.onResume();
    _disposable = _getBufferedDisposable();
  }

  @Override
  public void onPause() {
    super.onPause();
    _disposable.dispose();
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    _setupLogger();
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View layout = inflater.inflate(R.layout.fragment_buffer, container, false);
    unbinder = ButterKnife.bind(this, layout);
    return layout;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
  }

  // -----------------------------------------------------------------------------------
  // Main Rx entities

  private Disposable _getBufferedDisposable() {
    return RxView.clicks(_tapBtn)
        .map(
            onClickEvent -> {
              Timber.d("--------- GOT A TAP");
              _log("GOT A TAP");
              return 1;
            })
        .buffer(2, TimeUnit.SECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeWith(
            new DisposableObserver<List<Integer>>() {

              @Override
              public void onComplete() {
                // fyi: you'll never reach here
                Timber.d("----- onCompleted");
              }

              @Override
              public void onError(Throwable e) {
                Timber.e(e, "--------- Woops on error!");
                _log("Dang error! check your logs");
              }

              @Override
              public void onNext(List<Integer> integers) {
                Timber.d("--------- onNext");
                if (integers.size() > 0) {
                  _log(String.format("%d taps", integers.size()));
                } else {
                  Timber.d("--------- No taps received ");
                }
              }
            });
  }

  // -----------------------------------------------------------------------------------
  // Methods that help wiring up the example (irrelevant to RxJava)

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
