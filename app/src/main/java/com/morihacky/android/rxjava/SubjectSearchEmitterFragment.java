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
import android.widget.ListView;

import com.morihacky.android.rxjava.app.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnTextChanged;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.observables.AndroidObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import timber.log.Timber;


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
 *      ? wasteful (not really since we anyway unsubscribe at the end).
 *      less-elegant (as a concept for sure, but not for simplicity vs. the 3 step basic subscription creation)
 *      ? doesn't allow us to debounce/throttle/sample
 *
 */
public class SubjectSearchEmitterFragment
    extends Fragment {

    @InjectView(R.id.list_threading_log) ListView _logsList;

    private LogAdapter _adapter;
    private List<String> _logs;

    private Subscription _subscription;
    private PublishSubject<Observable<String>> _searchTextEmitterSubject;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        _setupLogger();

        _searchTextEmitterSubject = PublishSubject.create();
        _subscription = AndroidObservable.bindFragment(SubjectSearchEmitterFragment.this,
                                                       Observable.switchOnNext(_searchTextEmitterSubject))
                                         .subscribeOn(Schedulers.io())
                                         .observeOn(AndroidSchedulers.mainThread())
                                         .subscribe(_getSearchObserver());

        // .debounce(400, TimeUnit.MILLISECONDS, Schedulers.io())
    }

    @OnTextChanged(R.id.input_txt_subject)
    public void onTextEntered(CharSequence charsEntered) {
        Timber.d("---------- text entered %s", charsEntered);
        _searchTextEmitterSubject.onNext(_getASearchObservableFor(charsEntered.toString()));
    }

    // -----------------------------------------------------------------------------------
    // Main Rx entities

    private Observable<String> _getASearchObservableFor(final String searchText) {
        return Observable.create(new Observable.OnSubscribe<String>() {


            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext(searchText);
                subscriber.onCompleted();
            }
        });
    }

    private Observer<String> _getSearchObserver() {
        return new Observer<String>() {


            @Override
            public void onCompleted() {
                Timber.d("--------- onComplete");
            }

            @Override
            public void onError(Throwable e) {
                Timber.e(e, "--------- Woops on error!");
                _log(String.format("Dang error. check your logs"));
            }

            @Override
            public void onNext(String searchText) {
                Timber.d("--------- onNext");
                _log(String.format("You searched for %s", searchText));
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        _subscription.unsubscribe();
    }

    // -----------------------------------------------------------------------------------
    // Method that help wiring up the example (irrelevant to RxJava)

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_subject, container, false);
        ButterKnife.inject(this, layout);
        return layout;
    }

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