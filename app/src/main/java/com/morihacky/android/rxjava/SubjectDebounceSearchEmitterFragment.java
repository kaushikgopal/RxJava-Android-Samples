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
import android.widget.EditText;
import android.widget.ListView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.OnTextChangeEvent;
import rx.android.widget.WidgetObservable;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static java.lang.String.format;
import static rx.android.app.AppObservable.bindFragment;

/**
 * The reason we use a Subject for tracking the search query is because it emits observables.
 * Because a Subject subscribes to an Observable, it will trigger that Observable to begin emitting items
 * (if that Observable is "cold" — that is, if it waits for a subscription before it begins to emit items).
 * This can have the effect of making the resulting Subject a "hot" Observable variant of the original "cold" Observable.
 *
 * This allows us to create the subject and subscription one time onActivity creation
 * Subsequently we send in Observables to the Subject's subscriber onTextChanged
 *
 * (unlike the way it's done in {@link com.morihacky.android.rxjava.ConcurrencyWithSchedulersDemoFragment#startLongOperation()})
 * where we create the subscription on every single event change (OnClick or OnTextchanged) which is
 *
 * wasteful!                : not really since we anyway unsubscribe in OnDestroyView
 * less-elegant             : as a concept for sure
 * simpler actually         : adds one more step in the 3 step subscription process, where we create emitter, and then send observables to that emitter)
 * incapable of debounce    : this is the primary reason, since creating new observable everytime in subscription disregards debounce on subsequent calls
 */
public class SubjectDebounceSearchEmitterFragment
      extends BaseFragment {

    @InjectView(R.id.list_threading_log) ListView _logsList;
    @InjectView(R.id.input_txt_subject_debounce) EditText _inputSearchText;

    private LogAdapter _adapter;
    private List<String> _logs;

    private Subscription _subscription;

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (_subscription != null) {
            _subscription.unsubscribe();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_subject_debounce, container, false);
        ButterKnife.inject(this, layout);
        return layout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        _setupLogger();

        Observable<OnTextChangeEvent> textChangeObservable = WidgetObservable.text(_inputSearchText);

        _subscription = bindFragment(this,//
              textChangeObservable//
                    .debounce(400, TimeUnit.MILLISECONDS, Schedulers.io())//
                    .observeOn(AndroidSchedulers.mainThread()))//
              .subscribe(_getSearchObserver());
    }

    // -----------------------------------------------------------------------------------
    // Main Rx entities

    private Observer<OnTextChangeEvent> _getSearchObserver() {
        return new Observer<OnTextChangeEvent>() {
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
            public void onNext(OnTextChangeEvent onTextChangeEvent) {
                _log(format("onNext You searched for %s", onTextChangeEvent.text().toString()));
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