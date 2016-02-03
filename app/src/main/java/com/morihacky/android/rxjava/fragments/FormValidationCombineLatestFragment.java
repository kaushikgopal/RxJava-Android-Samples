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
                new Func3<CharSequence, CharSequence, CharSequence, FormValidator>() {
                    @Override
                    public FormValidator call(CharSequence newEmail,
                                              CharSequence newPassword,
                                              CharSequence newNumber) {

                        return new FormValidator(newEmail, newPassword, newNumber);
                    }
                })
                .skipWhile(new FormIsClean())
                .map(new Func1<FormValidator, Boolean>() {
                    @Override
                    public Boolean call(FormValidator form) {
                        if (!form.emailValid) {
                            _email.setError("Invalid Email!");
                        }
                        if (!form.passwordValid) {
                            _password.setError("Invalid Password!");
                        }
                        if (!form.numberValid) {
                            _number.setError("Invalid Number!");
                        }

                        return form.valid;
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
                    public void onNext(Boolean formValid) {
                        if (formValid) {
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

    private static class FormIsClean implements Func1<FormValidator, Boolean> {

        private boolean isDirty = false;

        @Override
        public Boolean call(FormValidator formValidator) {
            if (!isDirty) {
                isDirty = formValidator.allFieldsDirty;
            }
            return !isDirty;
        }
    }

    private static class FormValidator {
        private final boolean emailValid;
        private final boolean passwordValid;
        private final boolean numberValid;
        private final boolean valid;
        private final boolean allFieldsDirty;

        public FormValidator(CharSequence newEmail, CharSequence newPassword, CharSequence newNumber) {
            this.emailValid = !isEmpty(newEmail) && EMAIL_ADDRESS.matcher(newEmail).matches();
            this.passwordValid = !isEmpty(newPassword) && newPassword.length() > 8;
            this.numberValid = isValidPhoneNumber(newNumber);
            valid = emailValid && passwordValid && numberValid;
            allFieldsDirty = !isEmpty(newEmail) && !isEmpty(newPassword) && !isEmpty(newNumber);
        }

        private static boolean isValidPhoneNumber(CharSequence newNumber) {
            boolean numValid = !isEmpty(newNumber);
            if (numValid) {
                int num = Integer.parseInt(newNumber.toString());
                numValid = num > 0 && num <= 100;
            }
            return numValid;
        }
    }
}
