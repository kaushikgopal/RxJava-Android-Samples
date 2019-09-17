package com.morihacky.android.rxjava.fragments

import androidx.fragment.app.Fragment
import com.morihacky.android.rxjava.MyApp
import com.squareup.leakcanary.RefWatcher

open class BaseFragment : Fragment() {

    override fun onDestroy() {
        super.onDestroy()
        val refWatcher = MyApp.getRefWatcher()
        refWatcher.watch(this)
    }
}
