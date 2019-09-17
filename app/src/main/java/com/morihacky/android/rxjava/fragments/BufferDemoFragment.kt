package com.morihacky.android.rxjava.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.ButterKnife
import butterknife.Unbinder
import com.jakewharton.rxbinding2.view.RxView
import com.morihacky.android.rxjava.R
import com.morihacky.android.rxjava.wiring.LogAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_buffer.*
import timber.log.Timber
import java.util.ArrayList
import java.util.concurrent.TimeUnit

/**
 * This is a demonstration of the `buffer` Observable.
 *
 *
 * The buffer observable allows taps to be collected only within a time span. So taps outside the
 * 2s limit imposed by buffer will get accumulated in the next log statement.
 *
 *
 * If you're looking for a more foolproof solution that accumulates "continuous" taps vs a more
 * dumb solution as show below (i.e. number of taps within a timespan) look at [ ] where a combo of `publish` and
 * `buffer` is used.
 *
 *
 * Also https://blog.kaush.co/2015/01/05/debouncedbuffer-with-rxjava/
 * if you're looking for words instead of code
 */
class BufferDemoFragment : BaseFragment() {

    private lateinit var adapter: LogAdapter
    private lateinit var disposable: Disposable
    private lateinit var logs: MutableList<String>
    private lateinit var unbinder: Unbinder

    override fun onResume() {
        super.onResume()
        disposable = startBufferingTaps()
    }

    override fun onPause() {
        super.onPause()
        disposable.dispose()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unbinder.unbind()
    }

    // -----------------------------------------------------------------------------------
    // Main Rx entities

    private fun startBufferingTaps(): Disposable {
        return RxView.clicks(btn_start_operation)
            .map { clickEvent ->
                log("GOT A TAP")
                1
            }
            .buffer(2, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { tapEventList: List<Int> -> log("${tapEventList.size} taps") },
                { e ->
                    Timber.e(e, "--------- Woops on error!")
                    log("Dang error! check your logs")
                }
            )
    }

    // -----------------------------------------------------------------------------------
    // Methods that help wiring up the example (irrelevant to RxJava)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupLogger()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val layout = inflater.inflate(R.layout.fragment_buffer, container, false)
        unbinder = ButterKnife.bind(this, layout)
        return layout
    }

    private fun setupLogger() {
        logs = ArrayList()
        adapter = LogAdapter(activity, ArrayList())
        list_threading_log.adapter = adapter
    }

    private fun log(logMsg: String) {

        if (isCurrentlyOnMainThread()) {
            logs.add(0, "$logMsg (main thread) ")
            adapter.clear()
            adapter.addAll(logs)
        } else {
            logs.add(0, "$logMsg (NOT main thread) ")

            // You can only do below stuff on main thread.
            Handler(Looper.getMainLooper())
                .post {
                    adapter.clear()
                    adapter.addAll(logs)
                }
        }
    }

    private fun isCurrentlyOnMainThread(): Boolean {
        return Looper.myLooper() == Looper.getMainLooper()
    }
}
