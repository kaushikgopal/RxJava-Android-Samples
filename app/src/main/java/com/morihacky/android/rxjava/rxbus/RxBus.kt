package com.morihacky.android.rxjava.rxbus

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable

/** courtesy: https://gist.github.com/benjchristensen/04eef9ca0851f3a5d7bf  */
class RxBus {

    private val bus = PublishRelay.create<Any>().toSerialized()

    fun send(o: Any) {
        bus.accept(o)
    }

    fun asObservable(): Observable<Any> {
        return bus.cast(Any::class.java)
    }

    fun hasObservers(): Boolean {
        return bus.hasObservers()
    }
}
