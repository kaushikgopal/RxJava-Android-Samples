package com.morihacky.android.rxjava.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.morihacky.android.rxjava.R;
import com.morihacky.android.rxjava.retrofit.GithubApi;
import com.morihacky.android.rxjava.retrofit.GithubService;
import com.morihacky.android.rxjava.retrofit.User;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static java.lang.String.format;

public class RetrofitAsyncTaskDeathFragment extends Fragment {

  @BindView(R.id.btn_demo_retrofit_async_death_username)
  EditText _username;

  @BindView(R.id.log_list)
  ListView _resultList;

  private GithubApi _githubService;
  private ArrayAdapter<String> _adapter;
  private Unbinder unbinder;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    String githubToken = getResources().getString(R.string.github_oauth_token);
    _githubService = GithubService.createGithubService(githubToken);
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

    View layout = inflater.inflate(R.layout.fragment_retrofit_async_task_death, container, false);
    unbinder = ButterKnife.bind(this, layout);

    _adapter =
        new ArrayAdapter<>(getActivity(), R.layout.item_log, R.id.item_log, new ArrayList<>());
    //_adapter.setNotifyOnChange(true);
    _resultList.setAdapter(_adapter);

    return layout;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
  }

  @OnClick(R.id.btn_demo_retrofit_async_death)
  public void onGetGithubUserClicked() {
    _adapter.clear();

    /*new AsyncTask<String, Void, User>() {
        @Override
        protected User doInBackground(String... params) {
            return _githubService.getUser(params[0]);
        }

        @Override
        protected void onPostExecute(User user) {
            _adapter.add(format("%s  = [%s: %s]", _username.getText(), user.name, user.email));
        }
    }.execute(_username.getText().toString());*/

    _githubService
        .user(_username.getText().toString())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            new DisposableObserver<User>() {
              @Override
              public void onComplete() {}

              @Override
              public void onError(Throwable e) {}

              @Override
              public void onNext(User user) {
                _adapter.add(format("%s  = [%s: %s]", _username.getText(), user.name, user.email));
              }
            });
  }

  // -----------------------------------------------------------------------------------

  private class GetGithubUser extends AsyncTask<String, Void, User> {

    @Override
    protected User doInBackground(String... params) {
      return _githubService.getUser(params[0]);
    }

    @Override
    protected void onPostExecute(User user) {
      _adapter.add(format("%s  = [%s: %s]", _username.getText(), user.name, user.email));
    }
  }
}
