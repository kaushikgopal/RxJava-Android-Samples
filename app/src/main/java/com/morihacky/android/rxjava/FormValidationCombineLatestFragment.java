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
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func3;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

import static android.util.Patterns.EMAIL_ADDRESS;
import static com.google.common.base.Strings.isNullOrEmpty;

public class FormValidationCombineLatestFragment
        extends BaseFragment {

    @InjectView(R.id.btn_demo_form_valid) TextView _btnValidIndicator;
    @InjectView(R.id.demo_combl_email) EditText _email;
    @InjectView(R.id.demo_combl_password) EditText _password;
    @InjectView(R.id.demo_combl_num) EditText _number;

    private CompositeSubscription _subscriptions = null;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_form_validation_comb_latest,
                container,
                false);
        ButterKnife.inject(this, layout);

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();

        Observable<Boolean> emailValidObservable = WidgetObservable.text(_email).map(new Func1<OnTextChangeEvent, Boolean>() {
            @Override
            public Boolean call(OnTextChangeEvent onTextChangeEvent) {
                CharSequence text = onTextChangeEvent.text();
                return !isNullOrEmpty(text.toString()) &&
                        EMAIL_ADDRESS.matcher(text)
                                .matches();
            }
        });

        Observable<Boolean> passwordValidObservable = WidgetObservable.text(_password).map(new Func1<OnTextChangeEvent, Boolean>() {
            @Override
            public Boolean call(OnTextChangeEvent onTextChangeEvent) {
                CharSequence text = onTextChangeEvent.text();
                return !isNullOrEmpty(text.toString()) && text.length() > 8;
            }
        });

        Observable<Boolean> numberValidObservable = WidgetObservable.text(_number).map(new Func1<OnTextChangeEvent, Boolean>() {
            @Override
            public Boolean call(OnTextChangeEvent onTextChangeEvent) {
                String text = onTextChangeEvent.text().toString();
                if (!isNullOrEmpty(text)) {
                    int num = Integer.parseInt(text);
                    return num > 0 && num <= 100;
                } else {
                    return false;
                }
            }
        });

        Subscription emailValidSubscription = emailValidObservable.subscribe(new Action1<Boolean>() {

            @Override
            public void call(Boolean isValid) {
                _email.setError(isValid ? null : "Invalid Email!");
            }
        });

        Subscription passwordValidSubscription = passwordValidObservable.subscribe(new Action1<Boolean>() {

            @Override
            public void call(Boolean isValid) {
                _password.setError(isValid ? null : "Invalid Password!");
            }
        });

        Subscription numberValidSubscription = numberValidObservable.subscribe(new Action1<Boolean>() {

            @Override
            public void call(Boolean isValid) {
                _number.setError(isValid ? null : "Invalid Number!");
            }
        });

        Subscription submitEnabledSubscription = Observable.combineLatest(emailValidObservable,
                passwordValidObservable,
                numberValidObservable,
                new Func3<Boolean, Boolean, Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean emailValid,
                                        Boolean passwordValid,
                                        Boolean numberValid) {

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
                    public void onNext(Boolean formValid) {
                        if (formValid) {
                            _btnValidIndicator.setBackgroundColor(getResources().getColor(R.color.blue));
                        } else {
                            _btnValidIndicator.setBackgroundColor(getResources().getColor(R.color.gray));
                        }
                    }
                });

        _subscriptions = Subscriptions.from(emailValidSubscription, passwordValidSubscription, numberValidSubscription, submitEnabledSubscription);
    }

    @Override
    public void onPause() {
        super.onPause();
        _subscriptions.unsubscribe();
    }
}
