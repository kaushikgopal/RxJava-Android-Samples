package com.morihacky.android.rxjava.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.morihacky.android.rxjava.MainActivity;
import java.util.List;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Subscription;
import rx.functions.Func1;
import rx.observables.ConnectableObservable;

public class RotationPersist2WorkerFragment
      extends Fragment {

    private IAmYourMaster _masterFrag;
    private ConnectableObservable<Integer> _storedIntsObservable;
    private Subscription _storedIntsSubscription;

    /**
     * FIXME:
     *
     * 1. dangerous techniques such as replay and connectable observables (both code smells in Rx).
     * 2. you terminate the interval in a non-Rx manner using a global subscription variable  instead of using takeUntil
     * 3. ? you use the technique of making the parent implement a callback interface where it is possible to also do that in an Rx-based manner.
     */

    /**
     * Hold a reference to the activity -> caller fragment
     * this way when the worker frag kicks off
     * we can talk back to the master and send results
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        List<Fragment> frags = ((MainActivity) activity).getSupportFragmentManager().getFragments();
        for (Fragment f : frags) {
            if (f instanceof IAmYourMaster) {
                _masterFrag = (IAmYourMaster) f;
            }
        }

        if (_masterFrag == null) {
            throw new ClassCastException("We did not find a master who can understand us :(");
        }
    }

    /**
     * This method will only be called once when the retained Fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        if (_storedIntsObservable != null) {
            return;
        }

        Observable<Integer> intsObservable =//
              Observable.interval(1, TimeUnit.SECONDS)//
                    .map(new Func1<Long, Integer>() {
                        @Override
                        public Integer call(Long aLong) {
                            return aLong.intValue();
                        }
                    })//
                    .take(20);

        // -----------------------------------------------------------------------------------
        // Making our observable "HOT" for the purpose of the demo.
        // we can simply use subjects for this.
        // Connecteable Observables are messy and a code smell

        //_intsObservable = _intsObservable.share();
        _storedIntsObservable = intsObservable.replay();
        // we use replay to turn the observable "hot"

        _storedIntsSubscription = _storedIntsObservable.connect();


    }

    /**
     * The Worker fragment has started doing it's thing
     */
    @Override
    public void onResume() {
        super.onResume();
        _masterFrag.observeResults(_storedIntsObservable);
    }

    /**
     * Set the callback to null so we don't accidentally leak the
     * Activity instance.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        _masterFrag = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        _storedIntsSubscription.unsubscribe();
    }

    public interface IAmYourMaster {
        void observeResults(ConnectableObservable<Integer> intsObservable);
    }
}
