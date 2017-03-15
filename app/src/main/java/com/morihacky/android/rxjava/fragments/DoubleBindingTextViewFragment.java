package com.morihacky.android.rxjava.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;
import com.morihacky.android.rxjava.R;

import butterknife.Unbinder;
import io.reactivex.disposables.Disposable;
import io.reactivex.processors.PublishProcessor;

import static android.text.TextUtils.isEmpty;

public class DoubleBindingTextViewFragment extends BaseFragment {

  @BindView(R.id.double_binding_num1)
  EditText _number1;

  @BindView(R.id.double_binding_num2)
  EditText _number2;

  @BindView(R.id.double_binding_result)
  TextView _result;

  Disposable _disposable;
  PublishProcessor<Float> _resultEmitterSubject;
  private Unbinder unbinder;

  @Override
  public View onCreateView(
      LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View layout = inflater.inflate(R.layout.fragment_double_binding_textview, container, false);
    unbinder = ButterKnife.bind(this, layout);

    _resultEmitterSubject = PublishProcessor.create();

    _disposable =
        _resultEmitterSubject.subscribe(
            aFloat -> {
              _result.setText(String.valueOf(aFloat));
            });

    onNumberChanged();
    _number2.requestFocus();

    return layout;
  }

  @OnTextChanged({R.id.double_binding_num1, R.id.double_binding_num2})
  public void onNumberChanged() {
    float num1 = 0;
    float num2 = 0;

    if (!isEmpty(_number1.getText().toString())) {
      num1 = Float.parseFloat(_number1.getText().toString());
    }

    if (!isEmpty(_number2.getText().toString())) {
      num2 = Float.parseFloat(_number2.getText().toString());
    }

    _resultEmitterSubject.onNext(num1 + num2);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    _disposable.dispose();
    unbinder.unbind();
  }
}
