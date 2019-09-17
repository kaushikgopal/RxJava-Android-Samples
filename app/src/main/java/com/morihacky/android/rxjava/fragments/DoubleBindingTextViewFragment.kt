package com.morihacky.android.rxjava.fragments

import android.os.Bundle
import android.text.TextUtils.isEmpty
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.ButterKnife
import butterknife.OnTextChanged
import butterknife.Unbinder
import com.morihacky.android.rxjava.R
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import kotlinx.android.synthetic.main.fragment_double_binding_textview.*
import java.lang.Float.parseFloat

class DoubleBindingTextViewFragment : BaseFragment() {

    private lateinit var disposable: Disposable
    private lateinit var unbinder: Unbinder

    private lateinit var resultEmitterSubject: PublishProcessor<Float>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val layout = inflater.inflate(R.layout.fragment_double_binding_textview, container, false)
        unbinder = ButterKnife.bind(this, layout)

        resultEmitterSubject = PublishProcessor.create()

        disposable = resultEmitterSubject
            .subscribe { double_binding_result.text = it.toString() }

        onNumberChanged()
        double_binding_num2.requestFocus()

        return layout
    }

    @OnTextChanged(R.id.double_binding_num1, R.id.double_binding_num2)
    fun onNumberChanged() {
        var num1 = 0f
        var num2 = 0f

        if (!isEmpty(double_binding_num1.text.toString())) {
            num1 = parseFloat(double_binding_num1.text.toString())
        }

        if (!isEmpty(double_binding_num2.text.toString())) {
            num2 = parseFloat(double_binding_num2.text.toString())
        }

        resultEmitterSubject.onNext(num1 + num2)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposable.dispose()
        unbinder.unbind()
    }
}
