package com.morihacky.android.rxjava.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.morihacky.android.rxjava.R;
import com.morihacky.android.rxjava.wiring.LogAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Flowable;
import io.reactivex.subscribers.DefaultSubscriber;
import io.reactivex.subscribers.DisposableSubscriber;
import timber.log.Timber;

import static android.os.Looper.getMainLooper;
import static android.os.Looper.myLooper;

public class TimingDemoFragment extends BaseFragment {
    @BindView(R.id.list_threading_log)
    ListView _logsList;

    private LogAdapter _adapter;
    private List<String> _logs;

    private DisposableSubscriber<Long> _subscriber1;
    private DisposableSubscriber<Long> _subscriber2;
    private Unbinder unbinder;

    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_demo_timing, container, false);
        unbinder = ButterKnife.bind(this, layout);
        return layout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        _setupLogger();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
    // -----------------------------------------------------------------------------------

    @OnClick(R.id.btn_demo_timing_1)
    public void btn1_RunSingleTaskAfter2s() {
        _log(String.format("A1 [%s] --- BTN click", _getCurrentTimestamp()));

        Flowable.timer(2, TimeUnit.SECONDS) //
                .subscribe(
                        new DefaultSubscriber<Long>() {
                            @Override
                            public void onNext(Long number) {
                                _log(String.format("onNext: A1 [%s]", _getCurrentTimestamp()));
                            }

                            @Override
                            public void onError(Throwable e) {
                                Timber.e(e, "出错啦");
                            }

                            @Override
                            public void onComplete() {
                                _log(String.format("onComplete: A1 [%s]", _getCurrentTimestamp()));
                            }
                        });
    }

    @OnClick(R.id.btn_demo_timing_2)
    public void btn2_RunTask_IntervalOf1s() {
        if (_subscriber1 != null && !_subscriber1.isDisposed()) {
            _subscriber1.dispose();
            _log(String.format("B2 [%s] XXX BTN KILLED", _getCurrentTimestamp()));

            return;
        }

        _log(String.format("B2 [%s] --- BTN click", _getCurrentTimestamp()));

        _subscriber1 =
                new DisposableSubscriber<Long>() {
                    @Override
                    public void onComplete() {
                        _log(String.format("onComplete: B2 [%s]", _getCurrentTimestamp()));
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "出错啦");
                    }

                    @Override
                    public void onNext(Long number) {
                        _log(String.format("onNext: B2 [%s]", _getCurrentTimestamp()));
                    }
                };

        Flowable.interval(1, TimeUnit.SECONDS).subscribe(_subscriber1);
    }

    @OnClick(R.id.btn_demo_timing_3)
    public void btn3_RunTask_IntervalOf1s_StartImmediately() {
        if (_subscriber2 != null && !_subscriber2.isDisposed()) {
            _subscriber2.dispose();
            _log(String.format("C3 [%s] XXX BTN KILLED", _getCurrentTimestamp()));
            return;
        }

        _log(String.format("C3 [%s] --- BTN click", _getCurrentTimestamp()));

        _subscriber2 =
                new DisposableSubscriber<Long>() {
                    @Override
                    public void onNext(Long number) {
                        _log(String.format("onNext: C3 [%s]", _getCurrentTimestamp()));
                    }

                    @Override
                    public void onComplete() {
                        _log(String.format("onComplete: C3 [%s]", _getCurrentTimestamp()));
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "出错啦");
                    }
                };

        Flowable.interval(0, 1, TimeUnit.SECONDS).subscribe(_subscriber2);
    }

    @OnClick(R.id.btn_demo_timing_4)
    public void btn4_RunTask5Times_IntervalOf3s() {
        _log(String.format("D4 [%s] --- BTN click", _getCurrentTimestamp()));

        Flowable.interval(3, TimeUnit.SECONDS)
                .take(5)
                .subscribe(
                        new DefaultSubscriber<Long>() {
                            @Override
                            public void onNext(Long number) {
                                _log(String.format("onNext: D4 [%s]", _getCurrentTimestamp()));
                            }

                            @Override
                            public void onError(Throwable e) {
                                Timber.e(e, "出错啦");
                            }

                            @Override
                            public void onComplete() {
                                _log(String.format("onComplete: D4 [%s]", _getCurrentTimestamp()));
                            }
                        });
    }

    @OnClick(R.id.btn_demo_timing_5)
    public void btn5_RunTask5Times_IntervalOf3s() {
        _log(String.format("D5 [%s] --- BTN click", _getCurrentTimestamp()));

        Flowable.just("立即执行 A 任务")
                .doOnNext(input -> _log(String.format("D5 %s [%s]", input, _getCurrentTimestamp())))
                .delay(1, TimeUnit.SECONDS)
                .doOnNext(
                        oldInput ->
                                _log(String.format(
                                        "D5 %s [%s]", "已经延迟了一段时间，现在执行任务 B",
                                        _getCurrentTimestamp())))
                .subscribe(
                        new DefaultSubscriber<String>() {
                            @Override
                            public void onComplete() {
                                _log(String.format("onComplete: D5 [%s]", _getCurrentTimestamp()));
                            }

                            @Override
                            public void onError(Throwable e) {
                                Timber.e(e, "出错啦");
                            }

                            @Override
                            public void onNext(String number) {
                                _log(String.format("onNext: D5 [%s]", _getCurrentTimestamp()));
                            }
                        });
    }

    // -----------------------------------------------------------------------------------
    // Method that help wiring up the example (irrelevant to RxJava)

    @OnClick(R.id.btn_clr)
    public void OnClearLog() {
        _logs = new ArrayList<>();
        _adapter.clear();
    }

    private void _setupLogger() {
        _logs = new ArrayList<>();
        _adapter = new LogAdapter(getActivity(), new ArrayList<>());
        _logsList.setAdapter(_adapter);
    }

    private void _log(String logMsg) {
        _logs.add(0, getMainLooper() == myLooper() ? logMsg + "  主线程" : logMsg + "  NOT 主线程");

        new Handler(getMainLooper())
                .post(
                        () -> {
                            _adapter.clear();
                            _adapter.addAll(_logs);
                        });
    }

    private String _getCurrentTimestamp() {
        return new SimpleDateFormat("m:s", Locale.getDefault()).format(new Date());
    }
}
