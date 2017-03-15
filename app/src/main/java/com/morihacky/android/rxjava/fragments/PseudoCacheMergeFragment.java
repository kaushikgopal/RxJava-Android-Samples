package com.morihacky.android.rxjava.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.morihacky.android.rxjava.R;
import com.morihacky.android.rxjava.retrofit.Contributor;
import com.morihacky.android.rxjava.retrofit.GithubApi;
import com.morihacky.android.rxjava.retrofit.GithubService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class PseudoCacheMergeFragment extends BaseFragment {

  @BindView(R.id.log_list)
  ListView _resultList;

  private ArrayAdapter<String> _adapter;
  private HashMap<String, Long> _contributionMap = null;
  private HashMap<Contributor, Long> _resultAgeMap = new HashMap<>();
  private Unbinder unbinder;

  @Override
  public View onCreateView(
      LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View layout = inflater.inflate(R.layout.fragment_pseudo_cache_concat, container, false);
    unbinder = ButterKnife.bind(this, layout);
    _initializeCache();
    return layout;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
  }

  @OnClick(R.id.btn_start_pseudo_cache)
  public void onDemoPseudoCacheClicked() {
    _adapter =
        new ArrayAdapter<>(getActivity(), R.layout.item_log, R.id.item_log, new ArrayList<>());

    _resultList.setAdapter(_adapter);
    _initializeCache();

    Observable.merge(_getCachedData(), _getFreshData())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            new DisposableObserver<Pair<Contributor, Long>>() {
              @Override
              public void onComplete() {
                Timber.d("done loading all data");
              }

              @Override
              public void onError(Throwable e) {
                Timber.e(e, "arr something went wrong");
              }

              @Override
              public void onNext(Pair<Contributor, Long> contributorAgePair) {
                Contributor contributor = contributorAgePair.first;

                if (_resultAgeMap.containsKey(contributor)
                    && _resultAgeMap.get(contributor) > contributorAgePair.second) {
                  return;
                }

                _contributionMap.put(contributor.login, contributor.contributions);
                _resultAgeMap.put(contributor, contributorAgePair.second);

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

  private Observable<Pair<Contributor, Long>> _getCachedData() {

    List<Pair<Contributor, Long>> list = new ArrayList<>();

    Pair<Contributor, Long> dataWithAgePair;

    for (String username : _contributionMap.keySet()) {
      Contributor c = new Contributor();
      c.login = username;
      c.contributions = _contributionMap.get(username);

      dataWithAgePair = new Pair<>(c, System.currentTimeMillis());
      list.add(dataWithAgePair);
    }

    return Observable.fromIterable(list);
  }

  private Observable<Pair<Contributor, Long>> _getFreshData() {
    String githubToken = getResources().getString(R.string.github_oauth_token);
    GithubApi githubService = GithubService.createGithubService(githubToken);

    return githubService
        .contributors("square", "retrofit")
        .flatMap(Observable::fromIterable)
        .map(contributor -> new Pair<>(contributor, System.currentTimeMillis()));
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
