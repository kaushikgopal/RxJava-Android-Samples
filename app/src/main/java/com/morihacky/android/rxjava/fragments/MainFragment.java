package com.morihacky.android.rxjava.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import com.morihacky.android.rxjava.R;
import com.morihacky.android.rxjava.pagination.PaginationAutoFragment;
import com.morihacky.android.rxjava.rxbus.RxBusDemoFragment;
import com.morihacky.android.rxjava.volley.VolleyDemoFragment;

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

  @OnClick(R.id.btn_demo_schedulers)
  void demoConcurrencyWithSchedulers() {
    clickedOn(new ConcurrencyWithSchedulersDemoFragment());
  }

  @OnClick(R.id.btn_demo_buffer)
  void demoBuffer() {
    clickedOn(new BufferDemoFragment());
  }

  @OnClick(R.id.btn_demo_debounce)
  void demoThrottling() {
    clickedOn(new DebounceSearchEmitterFragment());
  }

  @OnClick(R.id.btn_demo_retrofit)
  void demoRetrofitCalls() {
    clickedOn(new RetrofitFragment());
  }

  @OnClick(R.id.btn_demo_polling)
  void demoPolling() {
    clickedOn(new PollingFragment());
  }

  @OnClick(R.id.btn_demo_double_binding_textview)
  void demoDoubleBindingWithPublishSubject() {
    clickedOn(new DoubleBindingTextViewFragment());
  }

  @OnClick(R.id.btn_demo_rxbus)
  void demoRxBus() {
    clickedOn(new RxBusDemoFragment());
  }

  @OnClick(R.id.btn_demo_form_validation_combinel)
  void formValidation() {
    clickedOn(new FormValidationCombineLatestFragment());
  }

  @OnClick(R.id.btn_demo_pseudo_cache)
  void pseudoCacheDemo() {
    clickedOn(new PseudoCacheFragment());
  }

  @OnClick(R.id.btn_demo_timing)
  void demoTimerIntervalDelays() {
    clickedOn(new TimingDemoFragment());
  }

  @OnClick(R.id.btn_demo_timeout)
  void demoTimeout() {
    clickedOn(new TimeoutDemoFragment());
  }

  @OnClick(R.id.btn_demo_exponential_backoff)
  void demoExponentialBackoff() {
    clickedOn(new ExponentialBackoffFragment());
  }

  @OnClick(R.id.btn_demo_rotation_persist)
  void demoRotationPersist() {
    clickedOn(new RotationPersist3Fragment());
    // clickedOn(new RotationPersist2Fragment());
    // clickedOn(new RotationPersist1Fragment());
  }

  @OnClick(R.id.btn_demo_pagination)
  void demoPaging() {
    clickedOn(new PaginationAutoFragment());
    //clickedOn(new PaginationFragment());
  }

  @OnClick(R.id.btn_demo_volley)
  void demoVolleyRequest() {
    clickedOn(new VolleyDemoFragment());
  }

  @OnClick(R.id.btn_demo_networkDetector)
  void demoNetworkDetector() {
    clickedOn(new NetworkDetectorFragment());
  }

  @OnClick(R.id.btn_demo_using)
  void demoUsing() {
    clickedOn(new UsingFragment());
  }

  @OnClick(R.id.btn_demo_multicastPlayground)
  void demoMulticastPlayground() {
    clickedOn(new MulticastPlaygroundFragment());
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
