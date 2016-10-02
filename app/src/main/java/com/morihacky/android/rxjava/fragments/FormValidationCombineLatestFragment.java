package com.morihacky.android.rxjava.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
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

        _emailChangeObservable = RxTextView.textChanges(_email).skip(1);
        _passwordChangeObservable = RxTextView.textChanges(_password).skip(1);
        _numberChangeObservable = RxTextView.textChanges(_number).skip(1);

        _combineLatestEvents();

        return layout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        _subscription.unsubscribe();
    }

    private void _combineLatestEvents() {
        _subscription = Observable.combineLatest(_emailChangeObservable,
              _passwordChangeObservable,
              _numberChangeObservable,
              (newEmail, newPassword, newNumber) -> {

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

              })//
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
                          _btnValidIndicator.setBackgroundColor(getResources().getColor(R.color.blue));
                      } else {
                          _btnValidIndicator.setBackgroundColor(getResources().getColor(R.color.gray));
                      }
                  }
              });
    }
}
