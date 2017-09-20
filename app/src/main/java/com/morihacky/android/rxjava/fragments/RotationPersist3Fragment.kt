package com.morihacky.android.rxjava.fragments

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Handler
import android.os.Looper.getMainLooper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.morihacky.android.rxjava.MyApp
import com.morihacky.android.rxjava.R
import com.morihacky.android.rxjava.ext.plus
import com.morihacky.android.rxjava.wiring.LogAdapter
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import timber.log.Timber
import java.util.concurrent.TimeUnit

class RotationPersist3Fragment : BaseFragment() {

    @BindView(R.id.list_threading_log)
    lateinit var logList: ListView
    lateinit var adapter: LogAdapter
    lateinit var sharedViewModel: SharedViewModel

    private var logs: MutableList<String> = ArrayList()
    private var disposables = CompositeDisposable()

    // -----------------------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel = ViewModelProviders.of(activity).get(SharedViewModel::class.java)
    }

    override fun onCreateView(
            inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater!!.inflate(R.layout.fragment_rotation_persist, container, false)
        ButterKnife.bind(this, layout)
        return layout
    }

    @OnClick(R.id.btn_rotate_persist)
    fun startOperationFromWorkerFrag() {
        logs = ArrayList<String>()
        adapter.clear()

        disposables +=
                sharedViewModel
                        .sourceStream()
                        .subscribe({ l ->
                            _log("Received element $l")
                        })
    }

    // -----------------------------------------------------------------------------------
    // Boilerplate
    // -----------------------------------------------------------------------------------

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        _setupLogger()
    }

    override fun onPause() {
        super.onPause()
        disposables.clear()
    }

    private fun _setupLogger() {
        logs = ArrayList<String>()
        adapter = LogAdapter(activity, ArrayList<String>())
        logList.adapter = adapter
    }

    private fun _log(logMsg: String) {
        logs.add(0, logMsg)

        // You can only do below stuff on main thread.
        Handler(getMainLooper())
                .post {
                    adapter.clear()
                    adapter.addAll(logs)
                }
    }
}

class SharedViewModel : ViewModel() {
    var disposable: Disposable? = null

    var sharedObservable: Flowable<Long> =
            Flowable.interval(1, TimeUnit.SECONDS)
                    .take(20)
                    .doOnNext { l -> Timber.tag("KG").d("onNext $l") }
                    // .replayingShare()
                    .replay(1)
                    .autoConnect(1) { t -> disposable = t }

    fun sourceStream(): Flowable<Long> {
        return sharedObservable
    }

    override fun onCleared() {
        super.onCleared()
        Timber.tag("KG").d("Clearing ViewModel")
        disposable?.dispose()
        MyApp.getRefWatcher().watch(this)
    }
}

