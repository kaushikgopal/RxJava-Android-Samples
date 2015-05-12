package com.morihacky.android.rxjava;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.morihacky.android.rxjava.R;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.widget.OnTextChangeEvent;
import rx.android.widget.WidgetObservable;
import rx.functions.Func3;
import timber.log.Timber;

import static android.util.Patterns.EMAIL_ADDRESS;
import static com.google.common.base.Strings.isNullOrEmpty;

public class FormValidationCombineLatestFragment
      extends BaseFragment {

    @InjectView(R.id.btn_demo_form_valid) TextView _btnValidIndicator;
    @InjectView(R.id.demo_combl_email) EditText _email;
    @InjectView(R.id.demo_combl_password) EditText _password;
    @InjectView(R.id.demo_combl_num) EditText _number;

    private Observable<OnTextChangeEvent> _emailChangeObservable;
    private Observable<OnTextChangeEvent> _passwordChangeObservable;
    private Observable<OnTextChangeEvent> _numberChangeObservable;

    private Subscription _subscription = null;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_form_validation_comb_latest,
              container,
              false);
        ButterKnife.inject(this, layout);

        _emailChangeObservable = WidgetObservable.text(_email);
        _passwordChangeObservable = WidgetObservable.text(_password);
        _numberChangeObservable = WidgetObservable.text(_number);

        _combineLatestEvents();

        return layout;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (_subscription != null) {
            _subscription.unsubscribe();
        }
    }

    private void _combineLatestEvents() {
        _subscription = Observable.combineLatest(_emailChangeObservable,
              _passwordChangeObservable,
              _numberChangeObservable,
              new Func3<OnTextChangeEvent, OnTextChangeEvent, OnTextChangeEvent, Boolean>() {
                  @Override
                  public Boolean call(OnTextChangeEvent onEmailChangeEvent,
                                      OnTextChangeEvent onPasswordChangeEvent,
                                      OnTextChangeEvent onNumberChangeEvent) {

                      boolean emailValid = !isNullOrEmpty(onEmailChangeEvent.text().toString()) &&
                                           EMAIL_ADDRESS.matcher(onEmailChangeEvent.text())
                                                 .matches();
                      if (!emailValid) {
                          _email.setError("Invalid Email!");
                      }

                      boolean passValid = !isNullOrEmpty(onPasswordChangeEvent.text().toString()) &&
                                          onPasswordChangeEvent.text().length() > 8;
                      if (!passValid) {
                          _password.setError("Invalid Password!");
                      }

                      boolean numValid = !isNullOrEmpty(onNumberChangeEvent.text().toString());
                      if (numValid) {
                          int num = Integer.parseInt(onNumberChangeEvent.text().toString());
                          numValid = num > 0 && num <= 100;
                      }
                      if (!numValid) {
                          _number.setError("Invalid Number!");
                      }

                      return emailValid && passValid && numValid;

                  }
              })//
              .subscribe(new Observer<Boolean>() {
                  @Override
                  public void onCompleted() {
                      Timber.d("completed");
                  }

                  @Override
                  public void onError(Throwable e) {
                      Timber.e(e, "there was an eroor");
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
