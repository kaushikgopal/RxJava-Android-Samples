package com.morihacky.android.rxjava.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.morihacky.android.rxjava.MainActivity;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.flowables.ConnectableFlowable;
import java.util.concurrent.TimeUnit;

public class RotationPersist1WorkerFragment extends Fragment {

  public static final String TAG = RotationPersist1WorkerFragment.class.toString();

  private IAmYourMaster _masterFrag;
  private ConnectableFlowable<Integer> _storedIntsFlowable;
  private Disposable _storedIntsDisposable;

  /**
   * Hold a reference to the activity -> caller fragment this way when the worker frag kicks off we
   * can talk back to the master and send results
   */
  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    _masterFrag =
        (RotationPersist1Fragment)
            ((MainActivity) context)
                .getSupportFragmentManager()
                .findFragmentByTag(RotationPersist1Fragment.TAG);

    if (_masterFrag == null) {
      throw new ClassCastException("We did not find a master who can understand us :(");
    }
  }

  /** This method will only be called once when the retained Fragment is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Retain this fragment across configuration changes.
    setRetainInstance(true);

    if (_storedIntsFlowable != null) {
      return;
    }

    Flowable<Integer> intsObservable =
        Flowable.interval(1, TimeUnit.SECONDS).map(Long::intValue).take(20);

    _storedIntsFlowable = intsObservable.publish();
    _storedIntsDisposable = _storedIntsFlowable.connect();
  }

  /** The Worker fragment has started doing it's thing */
  @Override
  public void onResume() {
    super.onResume();
    _masterFrag.observeResults(_storedIntsFlowable);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    _storedIntsDisposable.dispose();
  }

  /** Set the callback to null so we don't accidentally leak the Activity instance. */
  @Override
  public void onDetach() {
    super.onDetach();
    _masterFrag = null;
  }

  public interface IAmYourMaster {
    void observeResults(Flowable<Integer> intsObservable);
  }
}
