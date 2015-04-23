package com.morihacky.android.rxjava;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.google.common.base.Strings;
import com.morihacky.android.rxjava.app.R;
import com.morihacky.android.rxjava.retrofit.Contributor;
import com.morihacky.android.rxjava.retrofit.GithubApi;
import com.morihacky.android.rxjava.retrofit.User;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class RetrofitFragment extends Fragment {

  private ArrayAdapter<String> _adapter;

  GithubApi api;

  @InjectView(R.id.log_list) ListView _resultsListView;
  @InjectView(R.id.demo_retrofit_contributors_username) EditText _username;
  @InjectView(R.id.demo_retrofit_contributors_repository) EditText _repo;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    final String githubUsernamePassword = getActivity().getString(R.string.github_username_password);

    RestAdapter.Builder builder = new RestAdapter.Builder()
        .setEndpoint("https://api.github.com/")
        .setLogLevel(RestAdapter.LogLevel.FULL);

    if (!TextUtils.isEmpty(githubUsernamePassword)) {
      builder.setRequestInterceptor(new RequestInterceptor() {
        @Override
        public void intercept(RequestFacade request) {
          String string = "Basic " + Base64.encodeToString(githubUsernamePassword.getBytes(), Base64.NO_WRAP);
          request.addHeader("Accept", "application/json");
          request.addHeader("Authorization", string);
        }
      });
    }

    RestAdapter restAdapter = builder.build();

    api = restAdapter.create(GithubApi.class);
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    View layout = inflater.inflate(R.layout.fragment_retrofit, container, false);
    ButterKnife.inject(this, layout);



    _adapter = new ArrayAdapter<>(getActivity(), R.layout.item_log, R.id.item_log, new ArrayList<String>());
    _adapter.setNotifyOnChange(true);
    _resultsListView.setAdapter(_adapter);

    return layout;
  }

  @OnClick(R.id.btn_demo_retrofit_contributors)
  public void onListContributorsClicked() {
    _adapter.clear();
    api.contributors(_username.getText().toString(), _repo.getText().toString())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<List<Contributor>>() {
          @Override
          public void onCompleted() {
            Timber.d("Retrofit call 1 completed");
          }

          @Override
          public void onError(Throwable e) {
            Timber.e(e, "woops we got an error while getting the list of contributors");
          }

          @Override
          public void onNext(List<Contributor> contributors) {
            for (Contributor c : contributors) {
              _adapter.add(String.format("%s has made %d contributions to %s",
                  c.login,
                  c.contributions,
                  _repo.getText().toString()));
              Timber.d("%s has made %d contributions to %s",
                  c.login,
                  c.contributions,
                  _repo.getText().toString());
            }
          }
        });
  }

  @OnClick(R.id.btn_demo_retrofit_contributors_with_user_info)
  public void onListContributorsWithFullUserInfoClicked() {
    _adapter.clear();
    api.contributors(_username.getText().toString(), _repo.getText().toString())
        .flatMap(new Func1<List<Contributor>, Observable<Contributor>>() {
          @Override
          public Observable<Contributor> call(List<Contributor> contributors) {
            return Observable.from(contributors);
          }
        })
        .flatMap(new Func1<Contributor, Observable<?>>() {
          @Override
          public Observable<?> call(Contributor contributor) {
            Observable.zip(Observable.just(contributor),
                api.user(contributor.login).filter(new Func1<User, Boolean>() {
                  @Override
                  public Boolean call(User user) {
                    return !Strings.isNullOrEmpty(user.name) && !Strings.isNullOrEmpty(user.email);
                  }
                }),
                new Func2<Contributor, User, Object>() {
                  @Override
                  public Object call(Contributor contributor, User user) {
                    _adapter.add(String.format("%s(%s) has made %d contributions to %s",
                        user.name,
                        user.email,
                        contributor.contributions,
                        _repo.getText().toString()));
                    _adapter.notifyDataSetChanged();
                    Timber.d("%s(%s) has made %d contributions to %s",
                        user.name,
                        user.email,
                        contributor.contributions,
                        _repo.getText().toString());

                    return Observable.empty();
                  }
                }).subscribe();
            return Observable.empty();
          }
        })
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<Object>() {
          @Override
          public void onCompleted() {
            Timber.d("Retrofit call 2 completed ");
          }

          @Override
          public void onError(Throwable e) {
            Timber.e(e,
                "woops we got an error while getting the list of contributors along with full names");
          }

          @Override
          public void onNext(Object o) {
            Timber.d("hi! onNext");
          }
        });
  }
}
