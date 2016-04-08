package com.morihacky.android.rxjava.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.morihacky.android.rxjava.MainActivity;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.observables.ConnectableObservable;

public class RotationPersist1WorkerFragment
      extends Fragment {

    private IAmYourMaster _masterFrag;
    private ConnectableObservable<Integer> _storedIntsObservable;
    private Subscription _storedIntsSubscription;

    /**
     * Hold a reference to the activity -> caller fragment
     * this way when the worker frag kicks off
     * we can talk back to the master and send results
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        List<Fragment> frags = ((MainActivity) context).getSupportFragmentManager().getFragments();
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
                    .map(Long::intValue)//
                    .take(20);

        // -----------------------------------------------------------------------------------
        // Making our observable "HOT" for the purpose of the demo.

        //_intsObservable = _intsObservable.share();
        _storedIntsObservable = intsObservable.replay();

        _storedIntsSubscription = _storedIntsObservable.connect();

        // Do not do this in production!
        // `.share` is "warm" not "hot"
        // the below forceful subscription fakes the heat
        //_intsObservable.subscribe();
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
