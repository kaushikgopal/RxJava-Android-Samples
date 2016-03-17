package com.morihacky.android.rxjava.fragments;

import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.morihacky.android.rxjava.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.functions.Func0;
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

    private Subscription _subscription = null;
    private Observable<CharSequence> _numberChangeObservable;
    private Observable<CharSequence> _passwordChangeObservable;
    private Observable<CharSequence> _emailChangeObservable;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_form_validation_comb_latest,
              container,
              false);
        ButterKnife.bind(this, layout);

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();
        _emailChangeObservable = RxTextView.textChanges(_email);
        _passwordChangeObservable = RxTextView.textChanges(_password);
        _numberChangeObservable = RxTextView.textChanges(_number);
        _combineLatestEvents();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        _subscription.unsubscribe();
    }

    private void _combineLatestEvents() {
        final Observable<Boolean> isDirtyObservable = Observable.combineLatest(
              _emailChangeObservable,
              _numberChangeObservable,
              _passwordChangeObservable,
              new Func3<CharSequence, CharSequence, CharSequence, Boolean>() {
                  @Override
                  public Boolean call(CharSequence email, CharSequence number, CharSequence password) {
                      return !isEmpty(email) && !isEmpty(number) && !isEmpty(password);
                  }
              }
        ).filter(new Func1<Boolean, Boolean>() {
            @Override
            public Boolean call(Boolean isDirty) {
                return isDirty;
            }
        });

        _subscription = Observable
              .defer(new Func0<Observable<Boolean>>() {
                  @Override
                  public Observable<Boolean> call() {
                      return isDirtyObservable;
                  }
              }).flatMap(new Func1<Boolean, Observable<Boolean>>() {
                  @Override
                  public Observable<Boolean> call(Boolean aBoolean) {
                      return Observable.combineLatest(
                            _emailChangeObservable,
                            _passwordChangeObservable,
                            _numberChangeObservable,
                            new Func3<CharSequence, CharSequence, CharSequence, Boolean>() {
                                @Override
                                public Boolean call(CharSequence newEmail,
                                                    CharSequence newPassword,
                                                    CharSequence newNumber) {

                                    boolean emailValid = !isEmpty(newEmail) &&
                                                         EMAIL_ADDRESS.matcher(newEmail).matches();
                                    if (!emailValid) {
                                        _email.setError("Invalid Email!");
                                    }

                                    boolean passValid = !isEmpty(newPassword) && newPassword.length() > 8;
                                    if (!passValid) {
                                        _password.setError("Invalid Password!");
                                    }

                                    boolean numValid = !isEmpty(newNumber);
                                    if (numValid) {
                                        int num = Integer.parseInt(newNumber.toString());
                                        numValid = num > 0 && num <= 100;
                                    }
                                    if (!numValid) {
                                        _number.setError("Invalid Number!");
                                    }

                                    return emailValid && passValid && numValid;

                                }
                            });
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
                      _flipButton(formValid);
                  }
              });
    }

    private void _flipButton(final Boolean formValid) {
        @ColorInt
        final int colorId = ContextCompat.getColor(this.getActivity(), formValid ? R.color.blue : R.color.gray);
        _btnValidIndicator.setBackgroundColor(colorId);
    }
}
