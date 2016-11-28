package com.morihacky.android.rxjava.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.morihacky.android.rxjava.R;
import com.morihacky.android.rxjava.wiring.LogAdapter;
import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subscribers.DisposableSubscriber;
import java.util.ArrayList;
import java.util.List;
import timber.log.Timber;


import static android.os.Looper.getMainLooper;

public class RotationPersist1Fragment
      extends BaseFragment
      implements RotationPersist1WorkerFragment.IAmYourMaster {

    public static final String FRAG_TAG = RotationPersist1WorkerFragment.class.getName();

    @Bind(R.id.list_threading_log) ListView _logList;

    private LogAdapter _adapter;
    private List<String> _logs;

    private CompositeDisposable _disposables = new CompositeDisposable();

    // -----------------------------------------------------------------------------------

    @OnClick(R.id.btn_rotate_persist)
    public void startOperationFromWorkerFrag() {
        _logs = new ArrayList<>();
        _adapter.clear();

        FragmentManager fm = getActivity().getSupportFragmentManager();
        RotationPersist1WorkerFragment frag =
              (RotationPersist1WorkerFragment) fm.findFragmentByTag(FRAG_TAG);

        if (frag == null) {
            frag = new RotationPersist1WorkerFragment();
            fm.beginTransaction().add(frag, FRAG_TAG).commit();
        } else {
            Timber.d("Worker frag already spawned");
        }
    }

    @Override
    public void observeResults(Flowable<Integer> intsFlowable) {

        DisposableSubscriber<Integer> d = new DisposableSubscriber<Integer>() {
            @Override
            public void onNext(Integer integer) {
                _log(String.format("Worker frag spits out - %d", integer));
            }

            @Override
            public void onError(Throwable e) {
                Timber.e(e, "Error in worker demo frag observable");
                _log("Dang! something went wrong.");
            }

            @Override
            public void onComplete() {
                _log("Observable is complete");
            }
        };

        intsFlowable
              .doOnSubscribe(subscription -> {
                  _log("Subscribing to intsObservable");
              })
              .subscribe(d);

        _disposables.add(d);
    }

    // -----------------------------------------------------------------------------------
    // Boilerplate
    // -----------------------------------------------------------------------------------

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
        ButterKnife.bind(this, layout);
        return layout;
    }

    @Override
    public void onPause() {
        super.onPause();
        _disposables.clear();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    private void _setupLogger() {
        _logs = new ArrayList<>();
        _adapter = new LogAdapter(getActivity(), new ArrayList<>());
        _logList.setAdapter(_adapter);
    }

    private void _log(String logMsg) {
        _logs.add(0, logMsg);

        // You can only do below stuff on main thread.
        new Handler(getMainLooper()).post(() -> {
            _adapter.clear();
            _adapter.addAll(_logs);
        });
    }

}