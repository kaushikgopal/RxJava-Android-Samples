package com.morihacky.android.rxjava;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.morihacky.android.rxjava.wiring.LogAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Observer;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.observables.MathObservable;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static android.os.Looper.getMainLooper;

public class ExponentialBackoffFragment
      extends BaseFragment {

    @InjectView(R.id.list_threading_log) ListView _logList;
    private LogAdapter _adapter;
    private List<String> _logs;

    private CompositeSubscription _subscriptions = new CompositeSubscription();

    @Override
    public void onResume() {
        super.onResume();
        _subscriptions = RxUtils.getNewCompositeSubIfUnsubscribed(_subscriptions);
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
        View layout = inflater.inflate(R.layout.fragment_exponential_backoff, container, false);
        ButterKnife.inject(this, layout);
        return layout;
    }

    @Override
    public void onPause() {
        super.onPause();

        RxUtils.unsubscribeIfNotNull(_subscriptions);
    }

    // -----------------------------------------------------------------------------------

    @OnClick(R.id.btn_eb_retry)
    public void startRetryingWithExponentialBackoffStrategy() {
        _logs = new ArrayList<>();
        _adapter.clear();
    }

    // -----------------------------------------------------------------------------------

    @OnClick(R.id.btn_eb_delay)
    public void startExecutingWithExponentialBackoffDelay() {

        _logs = new ArrayList<>();
        _adapter.clear();

        _subscriptions.add(//

              Observable.range(1, 4)//
                    .delay(new Func1<Integer, Observable<Integer>>() {
                        @Override
                        public Observable<Integer> call(final Integer integer) {
                            // Rx-y way of doing the Fibonnaci :P
                            return MathObservable//
                                  .sumInteger(Observable.range(1, integer))
                                  .flatMap(new Func1<Integer, Observable<Integer>>() {
                                      @Override
                                      public Observable<Integer> call(Integer targetSecondDelay) {
                                          return Observable.just(integer)
                                                .delay(targetSecondDelay, TimeUnit.SECONDS);
                                      }
                                  });
                        }
                    })//
                    .doOnSubscribe(new Action0() {
                        @Override
                        public void call() {
                            _log(String.format("Execute 4 tasks with delay - time now: [xx:%2d]",
                                  _getSecondHand()));
                        }
                    })//
                    .subscribe(new Observer<Integer>() {
                        @Override
                        public void onCompleted() {
                            Timber.d("onCompleted");
                            _log("Completed");
                        }

                        @Override
                        public void onError(Throwable e) {
                            Timber.d(e, "arrrr. Error");
                            _log("Error");
                        }

                        @Override
                        public void onNext(Integer integer) {
                            Timber.d("emitting %d [xx:%2d]", integer, _getSecondHand());
                            _log(String.format("emitting %d  [xx:%2d]", integer, _getSecondHand()));

                        }
                    }));
    }

    // -----------------------------------------------------------------------------------

    private int _getSecondHand() {
        long millis = System.currentTimeMillis();
        return (int) (TimeUnit.MILLISECONDS.toSeconds(millis) -
                      TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    private void _setupLogger() {
        _logs = new ArrayList<>();
        _adapter = new LogAdapter(getActivity(), new ArrayList<String>());
        _logList.setAdapter(_adapter);
    }

    private void _log(String logMsg) {
        _logs.add(logMsg);

        // You can only do below stuff on main thread.
        new Handler(getMainLooper()).post(new Runnable() {

            @Override
            public void run() {
                _adapter.clear();
                _adapter.addAll(_logs);
            }
        });
    }
}