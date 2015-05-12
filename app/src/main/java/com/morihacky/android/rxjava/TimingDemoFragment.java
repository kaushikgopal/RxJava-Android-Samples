package com.morihacky.android.rxjava;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.morihacky.android.rxjava.wiring.LogAdapter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Observer;
import timber.log.Timber;

import static android.os.Looper.getMainLooper;
import static android.os.Looper.myLooper;

public class TimingDemoFragment
      extends BaseFragment {

    @InjectView(R.id.list_threading_log) ListView _logsList;

    private LogAdapter _adapter;
    private List<String> _logs;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        _setupLogger();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_demo_timing, container, false);
        ButterKnife.inject(this, layout);
        return layout;
    }

    // -----------------------------------------------------------------------------------

    @OnClick(R.id.btn_demo_timing_1)
    public void onRunOnceWithDelay() {
        _log(String.format("C1 [%s] --- BTN click", _getCurrentTimestamp()));

        Observable.just(1).delay(1, TimeUnit.SECONDS).subscribe(new Observer<Integer>() {
            @Override
            public void onCompleted() {
                _log(String.format("C1 [%s] XX COMPLETE", _getCurrentTimestamp()));
            }

            @Override
            public void onError(Throwable e) {
                Timber.e(e, "something went wrong in TimingDemoFragment example");
            }

            @Override
            public void onNext(Integer integer) {
                _log(String.format("C1 [%s]     NEXT", _getCurrentTimestamp()));
            }
        });
    }

    // -----------------------------------------------------------------------------------
    // Method that help wiring up the example (irrelevant to RxJava)

    private void _setupLogger() {
        _logs = new ArrayList<>();
        _adapter = new LogAdapter(getActivity(), new ArrayList<String>());
        _logsList.setAdapter(_adapter);
    }

    private void _log(String logMsg) {
        _logs.add(0, String.format(logMsg + " [MainThread: %b]", getMainLooper() == myLooper()));

        // You can only do below stuff on main thread.
        new Handler(getMainLooper()).post(new Runnable() {

            @Override
            public void run() {
                _adapter.clear();
                _adapter.addAll(_logs);
            }
        });
    }

    private String _getCurrentTimestamp() {
        return new SimpleDateFormat("k:m:s:S a").format(new Date());
    }

}
