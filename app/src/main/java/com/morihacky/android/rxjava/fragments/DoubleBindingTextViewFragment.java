package com.morihacky.android.rxjava.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewTextChangeEvent;
import com.morihacky.android.rxjava.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

public class DoubleBindingTextViewFragment
      extends BaseFragment {

    @Bind(R.id.double_binding_num1) EditText _number1;
    @Bind(R.id.double_binding_num2) EditText _number2;
    @Bind(R.id.double_binding_result) TextView _result;

    Subscription _subscription;
    PublishSubject<Float> _resultEmitterSubject;
    boolean usePattern1 = false;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_double_binding_textview, container, false);
        ButterKnife.bind(this, layout);

        if (usePattern1) {
            executePattern1();
        } else {
            executePattern2();//more generic
        }
        _number2.requestFocus();

        return layout;
    }

    public void executePattern1() {
        _resultEmitterSubject = PublishSubject.create();
        _subscription = _resultEmitterSubject.asObservable().subscribe(new Action1<Float>() {
            @Override
            public void call(Float aFloat) {
                _result.setText(String.valueOf(aFloat));
            }
        });
        onNumberChanged();
    }

    public void executePattern2() {
        _subscription = Observable
              .combineLatest(_getObservable(_number1), _getObservable(_number2), new Func2<Float, Float, Float>() {
                  @Override
                  public Float call(Float aFloat, Float aFloat2) {
                      return aFloat + aFloat2;
                  }
              })
              .subscribe(new Action1<Float>() {
                  @Override
                  public void call(Float aFloat) {
                      _result.setText(String.valueOf(aFloat));
                  }
              });
    }

    private Observable<Float> _getObservable(EditText editText) {
        return RxTextView
              .textChangeEvents(editText)
//              .filter(new Func1<TextViewTextChangeEvent, Boolean>() {
//                  @Override
//                  public Boolean call(TextViewTextChangeEvent changes) {
//                      return !TextUtils.isEmpty(changes.text().toString());
//                  }
//              })
              .map(new Func1<TextViewTextChangeEvent, Float>() {
                  @Override
                  public Float call(TextViewTextChangeEvent changes) {
                      if (TextUtils.isEmpty(changes.text().toString())) {
                          return 0f;
                      } else {
                          return Float.parseFloat(changes.text().toString());
                      }
                  }
              });
    }

    @OnTextChanged({R.id.double_binding_num1, R.id.double_binding_num2})
    public void onNumberChanged() {
        if (usePattern1) {
            float num1 = 0;
            float num2 = 0;

            if (!TextUtils.isEmpty(_number1.getText().toString())) {
                num1 = Float.parseFloat(_number1.getText().toString());
            }

            if (!TextUtils.isEmpty(_number2.getText().toString())) {
                num2 = Float.parseFloat(_number2.getText().toString());
            }

            _resultEmitterSubject.onNext(num1 + num2);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _subscription.unsubscribe();
        ButterKnife.unbind(this);
    }
}
