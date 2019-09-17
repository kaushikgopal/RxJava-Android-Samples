package com.morihacky.android.rxjava.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder

import com.morihacky.android.rxjava.R
import com.morihacky.android.rxjava.pagination.PaginationAutoFragment
import com.morihacky.android.rxjava.rxbus.RxBusDemoFragment
import com.morihacky.android.rxjava.volley.VolleyDemoFragment

class MainFragment : BaseFragment() {

    private lateinit var unbinder: Unbinder

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val layout = inflater.inflate(R.layout.fragment_main, container, false)
        unbinder = ButterKnife.bind(this, layout)
        return layout
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unbinder.unbind()
    }

    @OnClick(R.id.btn_demo_schedulers)
    fun demoConcurrencyWithSchedulers() {
        clickedOn(DemoSchedulersFragment())
    }

    @OnClick(R.id.btn_demo_polling)
    fun demoPolling() {
        clickedOn(DemoPollingFragment())
    }

    @OnClick(R.id.btn_demo_buffer)
    fun demoBuffer() {
        clickedOn(BufferDemoFragment())
    }

    @OnClick(R.id.btn_demo_debounce)
    fun demoThrottling() {
        clickedOn(DebounceSearchEmitterFragment())
    }

    @OnClick(R.id.btn_demo_retrofit)
    fun demoRetrofitCalls() {
        clickedOn(RetrofitFragment())
    }

    @OnClick(R.id.btn_demo_double_binding_textview)
    fun demoDoubleBindingWithPublishSubject() {
        clickedOn(DoubleBindingTextViewFragment())
    }

    @OnClick(R.id.btn_demo_rxbus)
    fun demoRxBus() {
        clickedOn(RxBusDemoFragment())
    }

    @OnClick(R.id.btn_demo_form_validation_combinel)
    fun formValidation() {
        clickedOn(FormValidationCombineLatestFragment())
    }

    @OnClick(R.id.btn_demo_pseudo_cache)
    fun pseudoCacheDemo() {
        clickedOn(PseudoCacheFragment())
    }

    @OnClick(R.id.btn_demo_timing)
    fun demoTimerIntervalDelays() {
        clickedOn(TimingDemoFragment())
    }

    @OnClick(R.id.btn_demo_timeout)
    fun demoTimeout() {
        clickedOn(TimeoutDemoFragment())
    }

    @OnClick(R.id.btn_demo_exponential_backoff)
    fun demoExponentialBackoff() {
        clickedOn(ExponentialBackoffFragment())
    }

    @OnClick(R.id.btn_demo_rotation_persist)
    fun demoRotationPersist() {
        clickedOn(RotationPersist3Fragment())
        // clickedOn(new RotationPersist2Fragment());
        // clickedOn(new RotationPersist1Fragment());
    }

    @OnClick(R.id.btn_demo_pagination)
    fun demoPaging() {
        clickedOn(PaginationAutoFragment())
        //clickedOn(new PaginationFragment());
    }

    @OnClick(R.id.btn_demo_volley)
    fun demoVolleyRequest() {
        clickedOn(VolleyDemoFragment())
    }

    @OnClick(R.id.btn_demo_networkDetector)
    fun demoNetworkDetector() {
        clickedOn(NetworkDetectorFragment())
    }

    @OnClick(R.id.btn_demo_using)
    fun demoUsing() {
        clickedOn(UsingFragment())
    }

    @OnClick(R.id.btn_demo_multicastPlayground)
    fun demoMulticastPlayground() {
        clickedOn(MulticastPlaygroundFragment())
    }

    private fun clickedOn(fragment: Fragment) {
        val tag = fragment.javaClass.toString()
        activity!!
            .supportFragmentManager
            .beginTransaction()
            .addToBackStack(tag)
            .replace(android.R.id.content, fragment, tag)
            .commit()
    }
}
