package com.morihacky.android.rxjava.fragments;

import com.morihacky.android.rxjava.R;
import com.morihacky.android.rxjava.RxUtils;
import com.morihacky.android.rxjava.retrofit.Contributor;
import com.morihacky.android.rxjava.retrofit.GithubApi;
import com.morihacky.android.rxjava.retrofit.User;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;
import static java.lang.String.format;

public class RetrofitFragment
      extends Fragment {

    @Bind(R.id.demo_retrofit_contributors_username) EditText _username;
    @Bind(R.id.demo_retrofit_contributors_repository) EditText _repo;
    @Bind(R.id.log_list) ListView _resultList;

    private GithubApi _api;
    private ArrayAdapter<String> _adapter;
    private CompositeSubscription _subscriptions = new CompositeSubscription();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _api = _createGithubApi();
    }

    @Override
    public void onResume() {
        super.onResume();
        _subscriptions = RxUtils.getNewCompositeSubIfUnsubscribed(_subscriptions);
    }

    @Override
    public void onPause() {
        super.onPause();

        RxUtils.unsubscribeIfNotNull(_subscriptions);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_retrofit, container, false);
        ButterKnife.bind(this, layout);

        _adapter = new ArrayAdapter<>(getActivity(),
              R.layout.item_log,
              R.id.item_log,
              new ArrayList<String>());
        //_adapter.setNotifyOnChange(true);
        _resultList.setAdapter(_adapter);

        return layout;
    }

    @OnClick(R.id.btn_demo_retrofit_contributors)
    public void onListContributorsClicked() {
        _adapter.clear();

        _subscriptions.add(//
              _api.contributors(_username.getText().toString(), _repo.getText().toString())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<List<Contributor>>() {
                        @Override
                        public void onCompleted() {
                            Timber.d("Retrofit call 1 completed");
                        }

                        @Override
                        public void onError(Throwable e) {
                            Timber.e(e,
                                  "woops we got an error while getting the list of contributors");
                        }

                        @Override
                        public void onNext(List<Contributor> contributors) {
                            for (Contributor c : contributors) {
                                _adapter.add(format("%s has made %d contributions to %s",
                                      c.login,
                                      c.contributions,
                                      _repo.getText().toString()));

                                Timber.d("%s has made %d contributions to %s",
                                      c.login,
                                      c.contributions,
                                      _repo.getText().toString());
                            }
                        }
                    }));
    }

    @OnClick(R.id.btn_demo_retrofit_contributors_with_user_info)
    public void onListContributorsWithFullUserInfoClicked() {
        _adapter.clear();

        _subscriptions.add(_api.contributors(_username.getText().toString(),
              _repo.getText().toString())
              .flatMap(new Func1<List<Contributor>, Observable<Contributor>>() {
                  @Override
                  public Observable<Contributor> call(List<Contributor> contributors) {
                      return Observable.from(contributors);
                  }
              })
              .flatMap(new Func1<Contributor, Observable<Pair<User, Contributor>>>() {
                  @Override
                  public Observable<Pair<User, Contributor>> call(Contributor contributor) {
                      Observable<User> _userObservable = _api.user(contributor.login)
                            .filter(new Func1<User, Boolean>() {
                                @Override
                                public Boolean call(User user) {
                                    return !isEmpty(user.name) && !isEmpty(user.email);
                                }
                            });

                      return Observable.zip(_userObservable,
                            Observable.just(contributor),
                            new Func2<User, Contributor, Pair<User, Contributor>>() {
                                @Override
                                public Pair<User, Contributor> call(User user,
                                                                    Contributor contributor) {
                                    return new Pair<>(user, contributor);
                                }
                            });
                  }
              })
              .subscribeOn(Schedulers.newThread())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(new Observer<Pair<User, Contributor>>() {
                  @Override
                  public void onCompleted() {
                      Timber.d("Retrofit call 2 completed ");
                  }

                  @Override
                  public void onError(Throwable e) {
                      Timber.e(e,
                            "error while getting the list of contributors along with full names");
                  }

                  @Override
                  public void onNext(Pair<User, Contributor> pair) {
                      User user = pair.first;
                      Contributor contributor = pair.second;

                      _adapter.add(format("%s(%s) has made %d contributions to %s",
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
                  }
              }));
    }

    // -----------------------------------------------------------------------------------

    private GithubApi _createGithubApi() {
        Retrofit.Builder builder = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.github.com");

        final String githubToken = getResources().getString(R.string.github_oauth_token);

        if (!TextUtils.isEmpty(githubToken)) {
            OkHttpClient client = new OkHttpClient();
            client.interceptors().add(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request request = chain.request();
                    Request newReq = request.newBuilder()
                            .addHeader("Authorization", format("token %s", githubToken))
                            .build();
                    return chain.proceed(newReq);
                }
            });
            builder.client(client);
        }

        return builder.build().create(GithubApi.class);
    }
}
