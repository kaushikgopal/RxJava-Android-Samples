package com.morihacky.android.rxjava.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.morihacky.android.rxjava.MainActivity;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

public class RotationPersist2WorkerFragment
      extends Fragment {

    private IAmYourMaster _masterFrag;
    private Subscription _storedIntsSubscription;
    private Subject<Integer, Integer> _intStream = PublishSubject.create();

    /**
     * Since we're holding a reference to the Master a.k.a Activity/Master Frag
     * remember to explicitly remove the worker fragment or you'll have a mem leak in your hands.
     * <p>
     * See {@link MainActivity#onBackPressed()}
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

        _storedIntsSubscription =
              Observable.interval(1, TimeUnit.SECONDS)
                    .map(Long::intValue)
                    .take(20)
                    .subscribe(_intStream);
    }

    /**
     * The Worker fragment has started doing it's thing
     */
    @Override
    public void onResume() {
        super.onResume();
        _masterFrag.setStream(_intStream.asObservable());
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
        void setStream(Observable<Integer> intStream);
    }
}
