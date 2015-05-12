package com.morihacky.android.rxjava;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.morihacky.android.rxjava.R;
import com.morihacky.android.rxjava.rxbus.RxBusDemoFragment;

public class MainFragment
      extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.inject(this, layout);
        return layout;
    }

    @OnClick(R.id.btn_demo_schedulers)
    public void demoConcurrencyWithSchedulers() {
        getActivity().getSupportFragmentManager()
              .beginTransaction()
              .addToBackStack(this.toString())
              .replace(R.id.activity_main,
                    new ConcurrencyWithSchedulersDemoFragment(),
                    this.toString())
              .commit();
    }

    @OnClick(R.id.btn_demo_buffer)
    public void demoBuffer() {
        getActivity().getSupportFragmentManager()
              .beginTransaction()
              .addToBackStack(this.toString())
              .replace(R.id.activity_main, new BufferDemoFragment(), this.toString())
              .commit();
    }

    @OnClick(R.id.btn_demo_subject_debounce)
    public void demoThrottling() {
        getActivity().getSupportFragmentManager()
              .beginTransaction()
              .addToBackStack(this.toString())
              .replace(R.id.activity_main,
                    new SubjectDebounceSearchEmitterFragment(),
                    this.toString())
              .commit();
    }

    @OnClick(R.id.btn_demo_retrofit)
    public void demoRetrofitCalls() {
        getActivity().getSupportFragmentManager()
              .beginTransaction()
              .addToBackStack(this.toString())
              .replace(R.id.activity_main, new RetrofitFragment(), this.toString())
              .commit();
    }

    @OnClick(R.id.btn_demo_double_binding_textview)
    public void demoDoubleBindingWithPublishSubject() {
        getActivity().getSupportFragmentManager()
              .beginTransaction()
              .addToBackStack(this.toString())
              .replace(R.id.activity_main, new DoubleBindingTextViewFragment(), this.toString())
              .commit();
    }

    @OnClick(R.id.btn_demo_polling)
    public void demoPolling() {
        getActivity().getSupportFragmentManager()
              .beginTransaction()
              .addToBackStack(this.toString())
              .replace(R.id.activity_main, new PollingFragment(), this.toString())
              .commit();
    }

    @OnClick(R.id.btn_demo_rxbus)
    public void demoRxBus() {
        getActivity().getSupportFragmentManager()
              .beginTransaction()
              .addToBackStack(this.toString())
              .replace(R.id.activity_main, new RxBusDemoFragment(), this.toString())
              .commit();
    }

    //@OnClick(R.id.btn_demo_subject_timeout)
    public void demoTimeout() {
        getActivity().getSupportFragmentManager()
              .beginTransaction()
              .addToBackStack(this.toString())
              .replace(R.id.activity_main, new DemoTimeoutFragment(), this.toString())
              .commit();
    }

    @OnClick(R.id.btn_demo_form_validation_combinel)
    public void formValidation() {
        getActivity().getSupportFragmentManager()
              .beginTransaction()
              .addToBackStack(this.toString())
              .replace(R.id.activity_main,
                    new FormValidationCombineLatestFragment(),
                    this.toString())
              .commit();
    }

    @OnClick(R.id.btn_demo_pseudo_cache)
    public void pseudoCacheDemo() {
        getActivity().getSupportFragmentManager()
              .beginTransaction()
              .addToBackStack(this.toString())
              .replace(R.id.activity_main, new PseudoCacheConcatFragment(), this.toString())
              .commit();
    }
}
