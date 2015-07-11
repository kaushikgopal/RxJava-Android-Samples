package com.morihacky.android.rxjava;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import java.util.List;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.functions.Func1;
import timber.log.Timber;

public class RotationPersistWorkerFragment
      extends Fragment {

    private Observable<Integer> _intsObservable;
    private IAmYourMaster _masterFrag;

    /**
     * hold a reference to the activity -> caller fragment
     * this way when the worker frag kicks off
     * we can talk back to the master and send results
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        _masterFrag = (RotationPersistFragment) getActivity().getSupportFragmentManager()
              .findFragmentByTag(RotationPersistFragment.class.getName());

        List<Fragment> frags = ((MainActivity) activity).getSupportFragmentManager().getFragments();

        Timber.d("---- there are currently %d fragments attached", frags.size());

        for (Fragment f : frags) {
            if (f instanceof IAmYourMaster) {
                _masterFrag = (IAmYourMaster) f;
            }
        }

        if (_masterFrag == null) {
            throw new ClassCastException("We did not find a master who can understand us :( !");
        }
    }

    /**
     * This method will only be called once when the retained
     * Fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        if (_intsObservable != null) {
            return;
        }

        // Create and execute the "Hot" observable
        Timber.d("---- Creating a new ints observable");
        _intsObservable =//
              Observable.interval(1, TimeUnit.SECONDS)//
                    .map(new Func1<Long, Integer>() {
                        @Override
                        public Integer call(Long aLong) {
                            return aLong.intValue();
                        }
                    })//
                    .take(20);// cause i don't want this thing to run indefinitely

        // -----------------------------------------------------------------------------------
        // Making our observable "HOT" for the purpose of the demo.

        _intsObservable = _intsObservable.share();
        // Do not do this in production! (i blame Jake Wharton for giving me this clever idea)
        _intsObservable.subscribe();
    }

    /**
     * The Worker fragment has started doing it's thing
     */
    @Override
    public void onStart() {
        super.onStart();
        _masterFrag.observeResults(_intsObservable);
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

    public interface IAmYourMaster {
        void observeResults(Observable<Integer> ints);
    }
}
