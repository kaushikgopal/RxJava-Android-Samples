package com.morihacky.android.rxjava.rxbus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.morihacky.android.rxjava.MainActivity
import com.morihacky.android.rxjava.R
import com.morihacky.android.rxjava.fragments.BaseFragment
import kotlinx.android.synthetic.main.fragment_rxbus_top.*

class RxBusDemo_TopFragment : BaseFragment() {

    private lateinit var rxBus: RxBus

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_rxbus_top, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        rxBus = (activity as MainActivity).rxBusSingleton
        btn_demo_rxbus_tap.setOnClickListener {
            if (rxBus.hasObservers()) {
                rxBus.send(RxBusDemoFragment.TapEvent())
            }
        }
    }
}
