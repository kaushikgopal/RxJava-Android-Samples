package com.morihacky.android.rxjava.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import butterknife.ButterKnife;
import butterknife.Bind;
import butterknife.OnClick;
import com.morihacky.android.rxjava.R;
import com.morihacky.android.rxjava.retrofit.Contributor;
import com.morihacky.android.rxjava.retrofit.GithubApi;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import timber.log.Timber;

import static java.lang.String.format;

public class PseudoCacheConcatFragment
      extends BaseFragment {

    @Bind(R.id.log_list) ListView _resultList;

    private Subscription _subscription = null;
    private HashMap<String, Long> _contributionMap = null;
    private ArrayAdapter<String> _adapter;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_pseudo_cache_concat, container, false);
        ButterKnife.bind(this, layout);
        _initializeCache();
        return layout;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (_subscription != null) {
            _subscription.unsubscribe();
        }
    }

    @OnClick(R.id.btn_start_pseudo_cache)
    public void onDemoPseudoCacheClicked() {
        _adapter = new ArrayAdapter<>(getActivity(),
              R.layout.item_log,
              R.id.item_log,
              new ArrayList<String>());

        _resultList.setAdapter(_adapter);
        _initializeCache();

        Observable.concat(_getCachedData(), _getFreshData())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(new Subscriber<Contributor>() {
                  @Override
                  public void onCompleted() {
                      Timber.d("done loading all data");
                  }

                  @Override
                  public void onError(Throwable e) {
                      Timber.e(e, "arr something went wrong");
                  }

                  @Override
                  public void onNext(Contributor contributor) {
                      _contributionMap.put(contributor.login, contributor.contributions);
                      _adapter.clear();
                      _adapter.addAll(getListStringFromMap());
                  }
              });
    }

    private List<String> getListStringFromMap() {
        List<String> list = new ArrayList<>();

        for (String username : _contributionMap.keySet()) {
            String rowLog = String.format("%s [%d]", username, _contributionMap.get(username));
            list.add(rowLog);
        }

        return list;
    }

    private Observable<Contributor> _getCachedData() {

        List<Contributor> list = new ArrayList<>();

        for (String username : _contributionMap.keySet()) {
            Contributor c = new Contributor();
            c.login = username;
            c.contributions = _contributionMap.get(username);
            list.add(c);
        }

        return Observable.from(list);
    }

    private Observable<Contributor> _getFreshData() {
        return _createGithubApi().contributors("square", "retrofit")
              .flatMap(new Func1<List<Contributor>, Observable<Contributor>>() {
                  @Override
                  public Observable<Contributor> call(List<Contributor> contributors) {
                      return Observable.from(contributors);
                  }
              });
    }

    private GithubApi _createGithubApi() {

        RestAdapter.Builder builder = new RestAdapter.Builder().setEndpoint(
              "https://api.github.com/");
        //.setLogLevel(RestAdapter.LogLevel.FULL);

        final String githubToken = getResources().getString(R.string.github_oauth_token);
        if (!TextUtils.isEmpty(githubToken)) {
            builder.setRequestInterceptor(new RequestInterceptor() {
                @Override
                public void intercept(RequestFacade request) {
                    request.addHeader("Authorization", format("token %s", githubToken));
                }
            });
        }

        return builder.build().create(GithubApi.class);
    }

    private void _initializeCache() {
        _contributionMap = new HashMap<>();
        _contributionMap.put("JakeWharton", 0l);
        _contributionMap.put("pforhan", 0l);
        _contributionMap.put("edenman", 0l);
        _contributionMap.put("swankjesse", 0l);
        _contributionMap.put("bruceLee", 0l);
    }
}
