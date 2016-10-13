package com.morihacky.android.rxjava.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.morihacky.android.rxjava.R;
import com.morihacky.android.rxjava.retrofit.Contributor;
import com.morihacky.android.rxjava.retrofit.GithubApi;
import com.morihacky.android.rxjava.retrofit.GithubService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class PseudoCacheConcatFragment
      extends BaseFragment {

    @Bind(R.id.log_list) ListView _resultList;

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
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick(R.id.btn_start_pseudo_cache)
    public void onDemoPseudoCacheClicked() {
        _adapter = new ArrayAdapter<>(getActivity(),
              R.layout.item_log,
              R.id.item_log,
              new ArrayList<>());

        _resultList.setAdapter(_adapter);
        _initializeCache();

        Observable.concatEager(_getCachedData(), _getFreshData())
              .subscribeOn(Schedulers.io())
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
        String githubToken = getResources().getString(R.string.github_oauth_token);
        GithubApi githubService = GithubService.createGithubService(githubToken);
        return githubService.contributors("square", "retrofit")
              .flatMap(Observable::from);
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
