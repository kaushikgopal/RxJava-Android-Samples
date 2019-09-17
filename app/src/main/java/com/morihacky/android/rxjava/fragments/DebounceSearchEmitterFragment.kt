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
import com.jakewharton.rxbinding3.widget.textChangeEvents
import com.morihacky.android.rxjava.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_debounce.*
import timber.log.Timber
import java.lang.String.format
import java.util.ArrayList
import java.util.concurrent.TimeUnit

class DebounceSearchEmitterFragment : BaseFragment() {

    private lateinit var adapter: LogAdapter
    private lateinit var logs: MutableList<String>
    private lateinit var disposable: Disposable
    private lateinit var unbinder: Unbinder

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
        unbinder.unbind()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {

        super.onActivityCreated(savedInstanceState)
        setupLogger()

        disposable = input_txt_debounce.textChangeEvents()
            .debounce(400, TimeUnit.MILLISECONDS) // default Scheduler is Computation
            .filter { changes -> changes.text.isNotEmpty() }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { textChangeEvent ->
                    log(format("Searching for %s", textChangeEvent.text.toString()))
                },
                { e ->
                    Timber.e(e, "--------- Woops on error!")
                    log("Dang error. check your logs")
                }
            )
    }

    // -----------------------------------------------------------------------------------
    // Method that help wiring up the example (irrelevant to RxJava)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val layout = inflater.inflate(R.layout.fragment_debounce, container, false)
        unbinder = ButterKnife.bind(this, layout)
        return layout
    }

    private fun setupLogger() {
        logs = ArrayList()
        adapter = LogAdapter(requireContext(), ArrayList())
        list_threading_log.adapter = adapter
    }

    @OnClick(R.id.clr_debounce)
    fun onClearLog() {
        logs = ArrayList()
        adapter.clear()
    }

    private fun log(logMsg: String) {

        if (_isCurrentlyOnMainThread()) {
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

    private fun _isCurrentlyOnMainThread(): Boolean {
        return Looper.myLooper() == Looper.getMainLooper()
    }

    private inner class LogAdapter(context: Context, logs: List<String>) :
        ArrayAdapter<String>(context, R.layout.item_log, R.id.item_log, logs)
}
