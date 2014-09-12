package com.morihacky.android.rxjava;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.google.common.base.Strings;
import com.morihacky.android.rxjava.app.R;
import com.morihacky.android.rxjava.retrofit.Contributor;
import com.morihacky.android.rxjava.retrofit.GithubApi;
import com.morihacky.android.rxjava.retrofit.User;
import java.util.List;
import retrofit.RestAdapter;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class RetrofitFragment
    extends Fragment {

  GithubApi api;

  @InjectView(R.id.demo_retrofit_contributors_username) EditText _username;
  @InjectView(R.id.demo_retrofit_contributors_repository) EditText _repo;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint("https://api.github.com/")
        .build();

    api = restAdapter.create(GithubApi.class);
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    View layout = inflater.inflate(R.layout.fragment_retrofit, container, false);
    ButterKnife.inject(this, layout);
    return layout;
  }

  @OnClick(R.id.btn_demo_retrofit_contributors)
  public void onListContributorsClicked() {
    api.contributors(_username.getText().toString(), _repo.getText().toString())
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

/*

   // Get Observable of List<Contributor>
    // Then from each contributor object in that List
      // print out the contributions and name

    github.contributors("netflix", "rxjava")
          .lift(flattenList())
          .forEach(c -> println(c.contributions + '\t' + c.login));


            1483 benjchritensen
            225  zxswing


    // Get Observable of List<Contributor>
    // Then from each contributor object in that List
    // emit stream of User Observables (by making retrofit call)  ---  1/2 flatmap
    // combine into a single observable of User                   ---  2/2 flatmap
    // print out the name of user

    github.contributors("netflix", "rxjava")
          .lift(flattenList())
          .flatMap(c -> gitHub.user(c.login))
          .forEach(user -> println(user.name));


            Observable.range(0, n)
                .flatMap({n -> doAsyncWorkThatReturnsObservable(n)})
                .subscribe(
                   { println(it); },                          // onNext
                   { println("Error: " + it.getMessage()); }, // onError
                   { println("Sequence complete"); }          // onCompleted
                );

            Ben Christensen
            Shixong Zu


    // Get Observable of List<Contributor>
    // Then from each contributor object in that List
    // emit stream of User Observables (by making retrofit call)  ---  1/2 flatmap
    // combine into a single observable of User                   ---  2/2 flatmap
    // filter only those users that have a name
    // print out the name of user

    github.contributors("netflix", "rxjava")
          .lift(flattenList())
          .flatMap(c -> gitHub.user(c.login))
          .filter(user -> user.name != null)
          .forEach(user -> println(user.name));

*/
}