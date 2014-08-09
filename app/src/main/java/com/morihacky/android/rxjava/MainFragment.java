package com.morihacky.android.rxjava;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.morihacky.android.rxjava.app.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainFragment
    extends Fragment {

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
                     .replace(R.id.activity_main,
                              new BufferDemoFragment(),
                              this.toString())
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

    @OnClick(R.id.btn_demo_subject_timeout)
    public void demoTimeout() {
        getActivity().getSupportFragmentManager()
                     .beginTransaction()
                     .addToBackStack(this.toString())
                     .replace(R.id.activity_main,
                              new DemoTimeoutFragment(),
                              this.toString())
                     .commit();
    }
}
