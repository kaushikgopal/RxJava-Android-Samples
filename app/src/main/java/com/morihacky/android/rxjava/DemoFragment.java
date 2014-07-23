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

public class DemoFragment
    extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_demo, container, false);
        ButterKnife.inject(this, layout);
        return layout;
    }

    @OnClick(R.id.demo_accumulate_event)
    public void demoAccumulationOfEvents() {
        getActivity().getSupportFragmentManager()
                     .beginTransaction()
                     .addToBackStack(this.toString())
                     .replace(R.id.activity_container,
                              new DemoAccumulateEventFragment(),
                              this.toString())
                     .commit();
    }

}
