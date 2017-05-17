package com.morihacky.android.rxjava.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.morihacky.android.rxjava.R;
import com.morihacky.android.rxjava.pagination.PaginationAutoFragment;
import com.morihacky.android.rxjava.rxbus.RxBusDemoFragment;
import com.morihacky.android.rxjava.volley.VolleyDemoFragment;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class MainFragment extends BaseFragment {

    private Unbinder unbinder;

    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_main, container, false);
        unbinder = ButterKnife.bind(this, layout);
        return layout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    /**
     * 后台工作 (schedulers &amp; concurrency)
     */
    @OnClick(R.id.btn_demo_schedulers)
    void demoConcurrencyWithSchedulers() {
        clickedOn(new ConcurrencyWithSchedulersDemoFragment());
    }

    /**
     * 累积请求 (buffer)
     */
    @OnClick(R.id.btn_demo_buffer)
    void demoBuffer() {
        clickedOn(new BufferDemoFragment());
    }

    /**
     * 搜索内容监听 (debounce)
     */
    @OnClick(R.id.btn_demo_debounce)
    void demoThrottling() {
        clickedOn(new DebounceSearchEmitterFragment());
    }

    /**
     * Retrofit + RxJava
     */
    @OnClick(R.id.btn_demo_retrofit)
    void demoRetrofitCalls() {
        clickedOn(new RetrofitFragment());
    }

    /**
     * 双向绑定 (PublishSubject)
     */
    @OnClick(R.id.btn_demo_double_binding_textview)
    void demoDoubleBindingWithPublishSubject() {
        clickedOn(new DoubleBindingTextViewFragment());
    }

    /**
     * RxJava 轮询
     */
    @OnClick(R.id.btn_demo_polling)
    void demoPolling() {
        clickedOn(new PollingFragment());
    }

    /**
     * Event Bus with RxJava
     */
    @OnClick(R.id.btn_demo_rxbus)
    void demoRxBus() {
        clickedOn(new RxBusDemoFragment());
    }

    /**
     * 使用 combineLatest 进行表单验证
     */
    @OnClick(R.id.btn_demo_form_validation_combinel)
    void formValidation() {
        clickedOn(new FormValidationCombineLatestFragment());
    }

    @OnClick(R.id.btn_demo_pseudo_cache)
    void pseudoCacheDemo() {
        clickedOn(new PseudoCacheFragment());
    }

    /**
     * timing/intervals/delays
     */
    @OnClick(R.id.btn_demo_timing)
    void demoTimerIntervalDelays() {
        clickedOn(new TimingDemoFragment());
    }

    /**
     * 超时运行
     */
    @OnClick(R.id.btn_demo_timeout)
    void demoTimeout() {
        clickedOn(new TimeoutDemoFragment());
    }

    /**
     * backoff 策略
     */
    @OnClick(R.id.btn_demo_exponential_backoff)
    void demoExponentialBackoff() {
        clickedOn(new ExponentialBackoffFragment());
    }

    /**
     * 不停旋转手机
     */
    @OnClick(R.id.btn_demo_rotation_persist)
    void demoRotationPersist() {
        clickedOn(new RotationPersist2Fragment());
        //clickedOn(new RotationPersist1Fragment());
    }

    /**
     * 分页示例
     */
    @OnClick(R.id.btn_demo_pagination)
    void demoPaging() {
        clickedOn(new PaginationAutoFragment());
//        clickedOn(new PaginationFragment());
    }

    /**
     * Volley 请求 Demo
     */
    @OnClick(R.id.btn_demo_volley)
    void demoVolleyRequest() {
        clickedOn(new VolleyDemoFragment());
    }

    /**
     * 网络状态检测器
     */
    @OnClick(R.id.btn_demo_networkDetector)
    void demoNetworkDetector() {
        clickedOn(new NetworkDetectorFragment());
    }

    private void clickedOn(@NonNull Fragment fragment) {
        final String tag = fragment.getClass().toString();
        getActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(tag)
                .replace(android.R.id.content, fragment, tag)
                .commit();
    }
}
