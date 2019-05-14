package com.morihacky.android.rxjava.fragments

import android.os.Bundle
import android.text.TextUtils.isEmpty
import android.util.Patterns.EMAIL_ADDRESS
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import butterknife.ButterKnife
import butterknife.Unbinder
import com.jakewharton.rxbinding2.widget.RxTextView
import com.morihacky.android.rxjava.R
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function3
import kotlinx.android.synthetic.main.fragment_form_validation_comb_latest.*
import timber.log.Timber

class FormValidationCombineLatestFragment : BaseFragment() {

    private lateinit var disposable: Disposable
    private lateinit var unbinder: Unbinder

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val layout = inflater.inflate(R.layout.fragment_form_validation_comb_latest, container, false)
        unbinder = ButterKnife.bind(this, layout)
        return layout
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
        unbinder.unbind()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupFormValidationWithCombineLatest()
    }

    private fun setupFormValidationWithCombineLatest() {
        val emailChanges: Observable<CharSequence> = RxTextView.textChanges(demo_combl_email)
            .skip(1)
        val passwordChanges: Observable<CharSequence> = RxTextView.textChanges(demo_combl_password).skip(1)
        val numberChanges: Observable<CharSequence> = RxTextView.textChanges(demo_combl_num).skip(1)

        disposable = Observable.combineLatest<CharSequence, CharSequence, CharSequence, Boolean>(
            emailChanges,
            passwordChanges,
            numberChanges,
            Function3 { newEmail: CharSequence, newPassword: CharSequence, newNumber: CharSequence ->
                val emailValid = !isEmpty(newEmail) && EMAIL_ADDRESS.matcher(newEmail).matches()
                if (!emailValid) {
                    demo_combl_email.error = "Invalid Email!"
                }

                val passValid = !isEmpty(newPassword) && newPassword.length > 8
                if (!passValid) {
                    demo_combl_password.error = "Invalid Password!"
                }

                var numValid = !isEmpty(newNumber)
                if (numValid) {
                    val num = Integer.parseInt(newNumber.toString())
                    numValid = num in 1..100
                }
                if (!numValid) {
                    demo_combl_num.error = "Invalid Number!"
                }

                emailValid && passValid && numValid
            })
            .subscribe(
                { formValid ->
                    if (formValid) {
                        btn_demo_form_valid.setBackgroundColor(
                            ContextCompat.getColor(context!!, R.color.blue)
                        )
                    } else {
                        btn_demo_form_valid.setBackgroundColor(
                            ContextCompat.getColor(context!!, R.color.gray)
                        )
                    }
                },
                { Timber.e(it, "there was an error") }
            )
    }

    // -----------------------------------------------------------------------------------
    // Method that help wiring up the example (irrelevant to RxJava)

}
