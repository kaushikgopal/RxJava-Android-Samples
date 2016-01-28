package com.morihacky.android.rxjava.fragments.concurrency;

import rx.Observable;
import rx.functions.Func1;
import timber.log.Timber;

/**
 * A partial implementation of Presenter for {@link ConcurrencyWithSchedulersDemoFragment}
 * Its makes testing RxJava much easier.
 */
class ConcurrencyWithSchedulersHelper {

	private final ConcurrencyWithSchedulersDemoFragment fragment;

	public ConcurrencyWithSchedulersHelper(ConcurrencyWithSchedulersDemoFragment fragment) {
		this.fragment = fragment;
	}


	Observable<Boolean> getObservable() {
		return Observable.just(true).map(new Func1<Boolean, Boolean>() {
			@Override
			public Boolean call(Boolean aBoolean) {
				fragment._log("Within Observable");
				_doSomeLongOperation_thatBlocksCurrentThread();
				return aBoolean;
			}
		});
	}

	// -----------------------------------------------------------------------------------
	// Methods that help wiring up the example (irrelevant to RxJava)

	private void _doSomeLongOperation_thatBlocksCurrentThread() {
		fragment._log("performing long operation");

		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			Timber.d("Operation was interrupted");
		}
	}

}