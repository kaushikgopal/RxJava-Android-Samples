package com.morihacky.android.rxjava.fragments;

import static android.text.TextUtils.isEmpty;
import static android.util.Patterns.EMAIL_ADDRESS;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.morihacky.android.rxjava.R;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.subscribers.DisposableSubscriber;
import timber.log.Timber;

public class FormValidationCombineLatestFragment extends BaseFragment {

  @BindView(R.id.btn_demo_form_valid)
  TextView _btnValidIndicator;

  @BindView(R.id.demo_combl_email)
  EditText _email;

  @BindView(R.id.demo_combl_password)
  EditText _password;

  @BindView(R.id.demo_combl_num)
  EditText _number;

  private DisposableSubscriber<Boolean> _disposableObserver = null;
  private Flowable<CharSequence> _emailChangeObservable;
  private Flowable<CharSequence> _numberChangeObservable;
  private Flowable<CharSequence> _passwordChangeObservable;
  private Unbinder unbinder;

  @Override
  public View onCreateView(
      LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View layout = inflater.inflate(R.layout.fragment_form_validation_comb_latest, container, false);
    unbinder = ButterKnife.bind(this, layout);

    _emailChangeObservable =
        RxTextView.textChanges(_email).skip(1).toFlowable(BackpressureStrategy.LATEST);
    _passwordChangeObservable =
        RxTextView.textChanges(_password).skip(1).toFlowable(BackpressureStrategy.LATEST);
    _numberChangeObservable =
        RxTextView.textChanges(_number).skip(1).toFlowable(BackpressureStrategy.LATEST);

    _combineLatestEvents();

    return layout;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
    _disposableObserver.dispose();
  }

  private void _combineLatestEvents() {

    _disposableObserver =
        new DisposableSubscriber<Boolean>() {
          @Override
          public void onNext(Boolean formValid) {
            if (formValid) {
              _btnValidIndicator.setBackgroundColor(
                  ContextCompat.getColor(getContext(), R.color.blue));
            } else {
              _btnValidIndicator.setBackgroundColor(
                  ContextCompat.getColor(getContext(), R.color.gray));
            }
          }

          @Override
          public void onError(Throwable e) {
            Timber.e(e, "there was an error");
          }

          @Override
          public void onComplete() {
            Timber.d("completed");
          }
        };

    Flowable.combineLatest(
            _emailChangeObservable,
            _passwordChangeObservable,
            _numberChangeObservable,
            (newEmail, newPassword, newNumber) -> {
              boolean emailValid = !isEmpty(newEmail) && EMAIL_ADDRESS.matcher(newEmail).matches();
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
            })
        .subscribe(_disposableObserver);
  }
}
