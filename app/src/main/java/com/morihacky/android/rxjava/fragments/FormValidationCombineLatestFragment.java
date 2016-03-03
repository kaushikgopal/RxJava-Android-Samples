package com.morihacky.android.rxjava.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.morihacky.android.rxjava.R;
import com.morihacky.android.rxjava.RxUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.functions.Func1;
import rx.functions.Func3;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;
import static android.util.Patterns.EMAIL_ADDRESS;

public class FormValidationCombineLatestFragment
      extends BaseFragment {

    @Bind(R.id.btn_demo_form_valid) TextView _btnValidIndicator;
    @Bind(R.id.demo_combl_email) EditText _email;
    @Bind(R.id.demo_combl_password) EditText _password;
    @Bind(R.id.demo_combl_num) EditText _number;

    private Observable<CharSequence> _emailChangeObservable;
    private Observable<CharSequence> _passwordChangeObservable;
    private Observable<CharSequence> _numberChangeObservable;

    private Subscription _subscription = null;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_form_validation_comb_latest,
              container,
              false);
        ButterKnife.bind(this, layout);

        _emailChangeObservable = RxTextView.textChanges(_email);
        _passwordChangeObservable = RxTextView.textChanges(_password);
        _numberChangeObservable = RxTextView.textChanges(_number);

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();
        _combineLatestEvents();
    }

    @Override
    public void onStop() {
        super.onStop();
        RxUtils.unsubscribeIfNotNull(_subscription);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    private void _combineLatestEvents() {
        _subscription = Observable.combineLatest(
                _emailChangeObservable,
                _passwordChangeObservable,
                _numberChangeObservable,
                new Func3<CharSequence, CharSequence, CharSequence, Form>() {
                    @Override
                    public Form call(CharSequence newEmail,
                                              CharSequence newPassword,
                                              CharSequence newNumber) {

                        return new Form(newEmail, newPassword, newNumber);
                    }
                })
                .skipWhile(new FormIsClean())
                .map(new Func1<Form, Boolean>() {
                    @Override
                    public Boolean call(Form form) {
                        boolean emailValid = !isEmpty(form.email) && EMAIL_ADDRESS.matcher(form.email).matches();
                        boolean passwordValid = !isEmpty(form.password) && form.password.length() > 8;
                        boolean numberValid = !isEmpty(form.number);
                        if (numberValid) {
                            int num = Integer.parseInt(form.number.toString());
                            numberValid = num > 0 && num <= 100;
                        }

                        if (!emailValid) {
                            _email.setError("Invalid Email!");
                        }
                        if (!passwordValid) {
                            _password.setError("Invalid Password!");
                        }
                        if (!numberValid) {
                            _number.setError("Invalid Number!");
                        }

                        return emailValid && passwordValid && numberValid;
                    }
                })
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {
                        Timber.d("completed");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "there was an error");
                    }

                    @Override
                    public void onNext(Boolean valid) {
                        if (valid) {
                            _btnValidIndicator.setBackgroundColor(
                                    ContextCompat.getColor(getActivity(), R.color.blue)
                            );
                        } else {
                            _btnValidIndicator.setBackgroundColor(
                                    ContextCompat.getColor(getActivity(), R.color.gray)
                            );
                        }
                    }
                });
    }

    private static class FormIsClean implements Func1<Form, Boolean> {

        private boolean isDirty = false;

        @Override
        public Boolean call(Form form) {
            if (!isDirty) {
                isDirty = !isEmpty(form.email) && !isEmpty(form.password) && !isEmpty(form.number);
            }
            return !isDirty;
        }
    }

    private static class Form {
        private final CharSequence email;
        private final CharSequence password;
        private final CharSequence number;

        public Form(CharSequence newEmail, CharSequence newPassword, CharSequence newNumber) {
            this.email = newEmail;
            this.password = newPassword;
            this.number = newNumber;
        }
    }
}
