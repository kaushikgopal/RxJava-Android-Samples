package com.morihacky.android.rxjava.rxbus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.morihacky.android.rxjava.R
import com.morihacky.android.rxjava.fragments.BaseFragment

class RxBusDemoFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_rxbus_demo, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity!!
            .supportFragmentManager
            .beginTransaction()
            .replace(R.id.demo_rxbus_frag_1, RxBusDemo_TopFragment())
            .replace(R.id.demo_rxbus_frag_2, RxBusDemo_Bottom3Fragment())
            //.replace(R.id.demo_rxbus_frag_2, new RxBusDemo_Bottom2Fragment())
            //.replace(R.id.demo_rxbus_frag_2, new RxBusDemo_Bottom1Fragment())
            .commit()
    }

    class TapEvent
}
