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
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_concurrency_schedulers.*
import timber.log.Timber
import java.util.ArrayList

class DemoSchedulersFragment : BaseFragment() {

    private lateinit var adapter: LogAdapter
    private lateinit var logs: MutableList<String>
    private lateinit var unbinder: Unbinder
    private val disposeBag = CompositeDisposable()

    override fun onDestroy() {
        super.onDestroy()
        unbinder.unbind()
        disposeBag.clear()
    }

    @OnClick(R.id.btn_start_operation)
    fun startLongOperation() {
        progress_operation_running.visibility = View.VISIBLE
        log("Button Clicked")

        val disposable: Disposable =
            Observable.just(true)
                .doOnSubscribe { log("doOnSubscribe") }
                .map {
                    log("before sleep")
                    someLongOperationThatBlocksCurrentThread()
                    it
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                // Subscriber or listener
                .subscribe(
                    { log("onNext with return value $it") },
                    { e ->
                        Timber.e(e, "Error in RxJava Demo concurrency")
                        log(String.format("Boo! Error %s", e.message))
                        progress_operation_running.visibility = View.INVISIBLE
                    },
                    {
                        log("On complete")
                        progress_operation_running.visibility = View.INVISIBLE
                    }
                )

        disposeBag.add(disposable)
    }

    // -----------------------------------------------------------------------------------
    // Method that help wiring up the example (irrelevant to RxJava)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupLogger()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val layout = inflater.inflate(R.layout.fragment_concurrency_schedulers, container, false)
        unbinder = ButterKnife.bind(this, layout)
        return layout
    }

    private fun someLongOperationThatBlocksCurrentThread() {
        log("performing long operation")

        try {
            Thread.sleep(3000)
        } catch (e: InterruptedException) {
            Timber.d("Operation was interrupted")
        }
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
    }

    private fun isCurrentlyOnMainThread(): Boolean {
        return Looper.myLooper() == Looper.getMainLooper()
    }

    private inner class LogAdapter(context: Context, logs: List<String>) :
        ArrayAdapter<String>(context, R.layout.item_log, R.id.item_log, logs)
}
