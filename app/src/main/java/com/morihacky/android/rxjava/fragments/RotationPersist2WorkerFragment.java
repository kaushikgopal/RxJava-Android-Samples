package com.morihacky.android.rxjava.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.morihacky.android.rxjava.MainActivity;
import io.reactivex.Flowable;
import io.reactivex.processors.PublishProcessor;
import java.util.concurrent.TimeUnit;

public class RotationPersist2WorkerFragment extends Fragment {

  public static final String TAG = RotationPersist2WorkerFragment.class.toString();

  private PublishProcessor<Integer> _intStream;
  private PublishProcessor<Boolean> _lifeCycleStream;

  private IAmYourMaster _masterFrag;

  /**
   * Since we're holding a reference to the Master a.k.a Activity/Master Frag remember to explicitly
   * remove the worker fragment or you'll have a mem leak in your hands.
   *
   * <p>See {@link MainActivity#onBackPressed()}
   */
  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    _masterFrag =
        (RotationPersist2Fragment)
            ((MainActivity) context)
                .getSupportFragmentManager()
                .findFragmentByTag(RotationPersist2Fragment.TAG);

    if (_masterFrag == null) {
      throw new ClassCastException("We did not find a master who can understand us :(");
    }
  }

  /** This method will only be called once when the retained Fragment is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    _intStream = PublishProcessor.create();
    _lifeCycleStream = PublishProcessor.create();

    // Retain this fragment across configuration changes.
    setRetainInstance(true);

    _intStream.takeUntil(_lifeCycleStream);

    Flowable.interval(1, TimeUnit.SECONDS).map(Long::intValue).take(20).subscribe(_intStream);
  }

  /** The Worker fragment has started doing it's thing */
  @Override
  public void onResume() {
    super.onResume();
    _masterFrag.setStream(_intStream);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    _lifeCycleStream.onComplete();
  }

  /** Set the callback to null so we don't accidentally leak the Activity instance. */
  @Override
  public void onDetach() {
    super.onDetach();
    _masterFrag = null;
  }

  public interface IAmYourMaster {
    void setStream(Flowable<Integer> intStream);
  }
}
