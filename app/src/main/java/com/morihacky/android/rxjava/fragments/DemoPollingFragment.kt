package com.morihacky.android.rxjava.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import com.morihacky.android.rxjava.R
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function
import kotlinx.android.synthetic.main.fragment_buffer.*
import timber.log.Timber
import java.util.ArrayList
import java.util.concurrent.TimeUnit

class DemoPollingFragment : BaseFragment() {

    companion object {
        private const val INITIAL_DELAY = 0
        private const val POLL_INTERVAL = 1000
        private const val POLL_COUNT = 5L
    }

    private lateinit var adapter: LogAdapter
    private lateinit var disposeBag: CompositeDisposable
    private lateinit var logs: MutableList<String>
    private lateinit var unbinder: Unbinder

    private var counter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        disposeBag = CompositeDisposable()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposeBag.clear()
        unbinder.unbind()
    }

    @OnClick(R.id.btn_start_simple_polling)
    fun onStartSimplePollingClicked() {

        val d = Observable
            .interval(INITIAL_DELAY.toLong(), POLL_INTERVAL.toLong(), TimeUnit.MILLISECONDS)
            .map {
                log("[simple polling] map $it (perform task)")
                it
            }
            .take(POLL_COUNT) // important to terminate the interval
            .doOnSubscribe { log("[simple polling] onSubscribe") }
            .subscribe { event: Long ->
                log("Executing polled task [$event] now time : [${"%02d".format(getSecondHand())}]")
            }

        disposeBag.add(d)
    }

    @OnClick(R.id.btn_start_increasingly_delayed_polling)
    fun onStartIncreasinglyDelayedPolling() {
        setupLogger()

        log("[increasingly delayed polling] now time : [${"%02d".format(getSecondHand())}]")

        // 1. simple way (where you maintain state outside)
        var repeatCount: Int = 0
        val externalStateHandling = Observable.just(1)
            .repeatWhen { o: Observable<Any> ->
                o.flatMap {
                    repeatCount += 1
                    Observable.timer(repeatCount.toLong() * POLL_INTERVAL, TimeUnit.MILLISECONDS)
                }
            }

        // 2. cleaner way (where the state is kept within the chain)
        val cleanStateHandling = Observable.just(1L)
            .repeatWhen(RepeatWithDelay(POLL_COUNT.toInt(), POLL_INTERVAL))

        disposeBag.add(
//            externalStateHandling
            cleanStateHandling
                .subscribe(
                    { event ->
                        log("Executing polled task [$event] now time : [${"%02d".format(getSecondHand())}]")
                    },
                    { e -> Timber.d(e, "arrrr. Error") }
                )
        )
    }

    private inner class RepeatWithDelay(
        private val repeatLimit: Int,
        private val pollingInterval: Int
    ) : Function<Observable<Any>, ObservableSource<Long>> {

        private var repeatCount = 0

        // this is a notification handler, all we care about is
        // the emission "type" not emission "content"
        // only onNext triggers a re-subscription

        @Throws(Exception::class)
        override fun apply(upstream: Observable<Any>): ObservableSource<Long> {
            // it is critical to use upstream observable in the chain for the result
            // ignoring it and doing your own thing will break the sequence

            return upstream.flatMap {
                if (repeatCount >= repeatLimit) {
                    // terminate the sequence cause we reached the limit
                    log("Completing sequence")
                    return@flatMap Observable.empty<Long>()
                }

                // since we don't get an input
                // we store state in this handler to tell us the point of time we're firing
                repeatCount += 1

                Observable.timer((repeatCount * pollingInterval).toLong(), TimeUnit.MILLISECONDS)
            }
        }
    }

    // -----------------------------------------------------------------------------------
    // Method that help wiring up the example (irrelevant to RxJava)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val layout = inflater.inflate(R.layout.fragment_polling, container, false)
        unbinder = ButterKnife.bind(this, layout)
        return layout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupLogger()
    }

    private fun getSecondHand(): Int {
        val millis = System.currentTimeMillis()
        return (TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(
            TimeUnit.MILLISECONDS.toMinutes(
                millis
            )
        )).toInt()
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

    private fun setupLogger() {
        logs = ArrayList()
        adapter = LogAdapter(requireContext(), ArrayList())
        list_threading_log.adapter = adapter
        counter = 0
    }

    private fun isCurrentlyOnMainThread(): Boolean {
        return Looper.myLooper() == Looper.getMainLooper()
    }

    private inner class LogAdapter(context: Context, logs: List<String>) :
        ArrayAdapter<String>(context, R.layout.item_log, R.id.item_log, logs)
}
