package com.morihacky.android.rxjava.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import com.morihacky.android.rxjava.R

class PlaygroundFragment : BaseFragment() {

    private var _logsList: ListView? = null
    private var _adapter: LogAdapter? = null

    private var _logs: MutableList<String> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_concurrency_schedulers, container, false)

        _logsList = view?.findViewById(R.id.list_threading_log) as ListView
        _setupLogger()

        view.findViewById(R.id.btn_start_operation).setOnClickListener { _ ->
            _log("Button clicked")
        }

        return view
    }

    // -----------------------------------------------------------------------------------
    // Method that help wiring up the example (irrelevant to RxJava)

    private fun _log(logMsg: String) {

        if (_isCurrentlyOnMainThread()) {
            _logs.add(0, logMsg + " (main thread) ")
            _adapter?.clear()
            _adapter?.addAll(_logs)
        } else {
            _logs.add(0, logMsg + " (NOT main thread) ")

            // You can only do below stuff on main thread.
            Handler(Looper.getMainLooper()).post {
                _adapter?.clear()
                _adapter?.addAll(_logs)
            }
        }
    }

    private fun _setupLogger() {
        _logs = ArrayList<String>()
        _adapter = LogAdapter(activity, ArrayList<String>())
        _logsList?.adapter = _adapter
    }

    private fun _isCurrentlyOnMainThread(): Boolean {
        return Looper.myLooper() == Looper.getMainLooper()
    }

    private inner class LogAdapter(context: Context, logs: List<String>) : ArrayAdapter<String>(context, R.layout.item_log, R.id.item_log, logs)
}