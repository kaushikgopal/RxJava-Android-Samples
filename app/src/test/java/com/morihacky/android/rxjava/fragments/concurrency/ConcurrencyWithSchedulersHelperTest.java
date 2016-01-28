package com.morihacky.android.rxjava.fragments.concurrency;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import rx.observers.TestSubscriber;

/**
 * Single Responsibility:
 *
 * Tests for {@link ConcurrencyWithSchedulersHelper}
 */
@RunWith(MockitoJUnitRunner.class)
public class ConcurrencyWithSchedulersHelperTest {

	@Mock ConcurrencyWithSchedulersDemoFragment fragment;

	ConcurrencyWithSchedulersHelper helper;
	TestSubscriber<Boolean> subscriber = new TestSubscriber<>();

	@Before
	public void setup() {
		helper = new ConcurrencyWithSchedulersHelper(fragment);
	}

	@Test
	public void when_getObservableCalled_then_subscriberReceivedExpectedEmissions() {
		//WHEN
		helper.getObservable().subscribe(subscriber);

		//THEN
		List<Boolean> expected = new ArrayList<>();
		expected.add(true);
		subscriber.assertReceivedOnNext(expected);
	}

}

