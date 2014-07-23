package com.morihacky.android.rxjava;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.morihacky.android.rxjava.app.R;

import butterknife.ButterKnife;

public class DemoAccumulateEventFragment
    extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_accumulate, container, false);
        ButterKnife.inject(this, layout);
        return layout;
    }
}
