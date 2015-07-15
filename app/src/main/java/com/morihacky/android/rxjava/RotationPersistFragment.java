package com.morihacky.android.rxjava;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
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
import rx.Observer;
import rx.functions.Action0;
import rx.observables.ConnectableObservable;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static android.os.Looper.getMainLooper;

public class RotationPersistFragment
      extends BaseFragment
      implements RotationPersistWorkerFragment.IAmYourMaster {

    public static final String FRAG_TAG = RotationPersistWorkerFragment.class.getName();

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
        View layout = inflater.inflate(R.layout.fragment_rotation_persist, container, false);
        ButterKnife.inject(this, layout);
        return layout;
    }

    @Override
    public void onPause() {
        super.onPause();
        RxUtils.unsubscribeIfNotNull(_subscriptions);
    }

    // -----------------------------------------------------------------------------------

    @OnClick(R.id.btn_rotate_persist)
    public void startOperationFromWorkerFrag() {
        _logs = new ArrayList<>();
        _adapter.clear();

        FragmentManager fm = getActivity().getSupportFragmentManager();
        RotationPersistWorkerFragment frag =//
              (RotationPersistWorkerFragment) fm.findFragmentByTag(FRAG_TAG);

        if (frag == null) {
            frag = new RotationPersistWorkerFragment();
            fm.beginTransaction().add(frag, FRAG_TAG).commit();
        } else {
            Timber.d("Worker frag already spawned");
        }
    }

    @Override
    public void observeResults(ConnectableObservable<Integer> intsObservable) {

        _subscriptions.add(//
              intsObservable.doOnSubscribe(new Action0() {
                  @Override
                  public void call() {
                      _log("Subscribing to intsObservable");
                  }
              }).subscribe(new Observer<Integer>() {
                  @Override
                  public void onCompleted() {
                      _log("Observable is complete");
                  }

                  @Override
                  public void onError(Throwable e) {
                      Timber.e(e, "Error in worker demo frag observable");
                      _log("Dang! something went wrong.");
                  }

                  @Override
                  public void onNext(Integer integer) {
                      _log(String.format("Worker frag spits out - %d", integer));
                  }
              }));

    }

    // -----------------------------------------------------------------------------------

    private void _setupLogger() {
        _logs = new ArrayList<>();
        _adapter = new LogAdapter(getActivity(), new ArrayList<String>());
        _logList.setAdapter(_adapter);
    }

    private void _log(String logMsg) {
        _logs.add(0, logMsg);

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