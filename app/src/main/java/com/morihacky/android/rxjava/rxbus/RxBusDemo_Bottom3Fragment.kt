package com.morihacky.android.rxjava.rxbus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import com.morihacky.android.rxjava.MainActivity
import com.morihacky.android.rxjava.R
import com.morihacky.android.rxjava.fragments.BaseFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_rxbus_bottom.*
import java.util.concurrent.TimeUnit

class RxBusDemo_Bottom3Fragment : BaseFragment() {

    private lateinit var rxBus: RxBus
    private lateinit var disposables: CompositeDisposable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_rxbus_bottom, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        rxBus = (activity as MainActivity).rxBusSingleton
    }

    override fun onStart() {
        super.onStart()
        disposables = CompositeDisposable()

        val tapEventEmitter = rxBus.asObservable().publish()

        disposables
            .add(
                tapEventEmitter.subscribe { event ->
                    if (event is RxBusDemoFragment.TapEvent) {
                        showTapText()
                    }
                })

        disposables.add(
            tapEventEmitter
                .publish { stream ->
                    stream.buffer(
                        stream.debounce(1, TimeUnit.SECONDS)
                    )
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { taps -> showTapCount(taps.size) })

        disposables.add(tapEventEmitter.connect())
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }

    // -----------------------------------------------------------------------------------
    // Helper to show the text via an animation

    private fun showTapText() {
        demo_rxbus_tap_txt.visibility = View.VISIBLE
        demo_rxbus_tap_txt.alpha = 1f
        ViewCompat.animate(demo_rxbus_tap_txt).alphaBy(-1f).duration = 400
    }

    private fun showTapCount(size: Int) {
        demo_rxbus_tap_count.text = size.toString()
        demo_rxbus_tap_count.visibility = View.VISIBLE
        demo_rxbus_tap_count.scaleX = 1f
        demo_rxbus_tap_count.scaleY = 1f
        ViewCompat.animate(demo_rxbus_tap_count)
            .scaleXBy(-1f)
            .scaleYBy(-1f)
            .setDuration(800).startDelay = 100
    }
}
