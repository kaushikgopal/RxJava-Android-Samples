package com.morihacky.android.rxjava.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.jakewharton.rx.replayingShare
import com.morihacky.android.rxjava.R
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

class MulticastPlaygroundFragment : BaseFragment() {

    @BindView(R.id.list_threading_log) lateinit var logList: ListView
    @BindView(R.id.dropdown) lateinit var pickOperatorDD: Spinner
    @BindView(R.id.msg_text) lateinit var messageText: TextView

    private lateinit var sharedObservable: Observable<Long>
    private lateinit var adapter: LogAdapter

    private var logs: MutableList<String> = ArrayList()
    private var disposable1: Disposable? = null
    private var disposable2: Disposable? = null

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val layout = inflater!!.inflate(R.layout.fragment_multicast_playground, container, false)
        ButterKnife.bind(this, layout)

        _setupLogger()
        _setupDropdown()

        return layout
    }

    @OnClick(R.id.btn_1)
    fun onBtn1Click() {

        disposable1?.let {
            it.dispose()
            _log("subscriber 1 disposed")
            disposable1 = null
            return
        }

        disposable1 =
                sharedObservable
                        .doOnSubscribe { _log("subscriber 1 (subscribed)") }
                        .subscribe({ long -> _log("subscriber 1: onNext $long") })

    }

    @OnClick(R.id.btn_2)
    fun onBtn2Click() {
        disposable2?.let {
            it.dispose()
            _log("subscriber 2 disposed")
            disposable2 = null
            return
        }

        disposable2 =
                sharedObservable
                        .doOnSubscribe { _log("subscriber 2 (subscribed)") }
                        .subscribe({ long -> _log("subscriber 2: onNext $long") })
    }

    @OnClick(R.id.btn_3)
    fun onBtn3Click() {
        logs = ArrayList<String>()
        adapter.clear()
    }

    // -----------------------------------------------------------------------------------
    // Method that help wiring up the example (irrelevant to RxJava)

    private fun _log(logMsg: String) {

        if (_isCurrentlyOnMainThread()) {
            logs.add(0, logMsg + " (main thread) ")
            adapter.clear()
            adapter.addAll(logs)
        } else {
            logs.add(0, logMsg + " (NOT main thread) ")

            // You can only do below stuff on main thread.
            Handler(Looper.getMainLooper()).post {
                adapter.clear()
                adapter.addAll(logs)
            }
        }
    }

    private fun _setupLogger() {
        logs = ArrayList<String>()
        adapter = LogAdapter(activity, ArrayList<String>())
        logList.adapter = adapter
    }

    private fun _setupDropdown() {
        pickOperatorDD.adapter = ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_dropdown_item,
                arrayOf(".publish().refCount()",
                        ".publish().autoConnect(2)",
                        ".replay(1).autoConnect(2)",
                        ".replay(1).refCount()",
                        ".replayingShare()"))


        pickOperatorDD.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, index: Int, p3: Long) {

                val sourceObservable = Observable.interval(0L, 3, TimeUnit.SECONDS)
                        .doOnSubscribe { _log("observer (subscribed)") }
                        .doOnDispose { _log("observer (disposed)") }
                        .doOnTerminate { _log("observer (terminated)") }

                sharedObservable =
                        when (index) {
                            0 -> {
                                messageText.setText(R.string.msg_demo_multicast_publishRefCount)
                                sourceObservable.publish().refCount()
                            }
                            1 -> {
                                messageText.setText(R.string.msg_demo_multicast_publishAutoConnect)
                                sourceObservable.publish().autoConnect(2)
                            }
                            2 -> {
                                messageText.setText(R.string.msg_demo_multicast_replayAutoConnect)
                                sourceObservable.replay(1).autoConnect(2)
                            }
                            3 -> {
                                messageText.setText(R.string.msg_demo_multicast_replayRefCount)
                                sourceObservable.replay(1).refCount()
                            }
                            4 -> {
                                messageText.setText(R.string.msg_demo_multicast_replayingShare)
                                sourceObservable.replayingShare()
                            }
                            else -> throw RuntimeException("got to pick an op yo!")
                        }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun _isCurrentlyOnMainThread(): Boolean {
        return Looper.myLooper() == Looper.getMainLooper()
    }

    private inner class LogAdapter(context: Context, logs: List<String>) :
            ArrayAdapter<String>(context, R.layout.item_log, R.id.item_log, logs)

}

