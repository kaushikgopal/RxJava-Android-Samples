package com.morihacky.android.rxjava.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.morihacky.android.rxjava.R;
import com.morihacky.android.rxjava.retrofit.GithubApi;
import com.morihacky.android.rxjava.retrofit.GithubService;
import com.morihacky.android.rxjava.retrofit.Repository;
import com.morihacky.android.rxjava.retrofit.RepositoryResponseData;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static java.lang.String.format;

public class RetrofitFragment extends Fragment {

    @BindView(R.id.github_username1)
    EditText _username1;

    @BindView(R.id.github_username2)
    EditText _username2;

    @BindView(R.id.winner)
    TextView _winner;

    @BindView(R.id.log_list1)
    RecyclerView _resultList1;

    @BindView(R.id.log_list2)
    RecyclerView _resultList2;

    private int _stars1, _stars2;

    private CustomAdapter _adapter1, _adapter2;
    private GithubApi _githubService;
    private CompositeDisposable _disposables;
    private Unbinder _unbinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String githubToken = getResources().getString(R.string.github_oauth_token);
        _githubService = GithubService.createGithubService(githubToken);

        _disposables = new CompositeDisposable();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_retrofit, container, false);
        _unbinder = ButterKnife.bind(this, layout);

        _adapter1 = new CustomAdapter(getActivity(), R.layout.item_log, R.id.item_log, new ArrayList<>());
        _resultList1.setAdapter(_adapter1);
        _resultList1.setLayoutManager(new LinearLayoutManager(getContext()));
        _adapter2 = new CustomAdapter(getActivity(), R.layout.item_log, R.id.item_log, new ArrayList<>());
        _resultList2.setAdapter(_adapter2);
        _resultList2.setLayoutManager(new LinearLayoutManager(getContext()));

        return layout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        _disposables.dispose();
    }

    @OnClick(R.id.compare_by_stars)
    public void compareByStarsClicked() {
        _adapter1.clear();
        _adapter2.clear();

        _disposables.add(Observable.merge(getRepos(_username2.getText().toString()),
                getRepos(_username1.getText().toString()))
                .flatMap(repositoryResponseData -> Observable.fromArray(repositoryResponseData.getItems()))
                .flatMap(Observable::fromIterable)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(repository -> incrementTotalStars(repository))
                .subscribeWith(
                        new DisposableObserver<Repository>() {
                            @Override
                            public void onComplete() {
                                if (_stars1 == _stars2)
                                    _winner.setText(getString(R.string.draw));
                                else
                                    _winner.setText(
                                            format(getString(R.string.winner), _stars1 > _stars2 ?
                                                    _username1.getText().toString() : _username2.getText().toString()));
                                _stars1 = _stars2 = 0; // resetting the field
                            }

                            @Override
                            public void onError(Throwable e) {
                                Timber.e(e);
                            }

                            @Override
                            public void onNext(Repository repository) {
                                addToList(repository);
                            }
                        }));
    }

    private void incrementTotalStars(Repository repository) {
        if (repository.getOwner().getLogin().equals(_username1.getText().toString()))
            _stars1 += repository.getStargazersCount();
        else
            _stars2 += repository.getStargazersCount();
    }

    private void addToList(Repository repository) {
        if (repository.getOwner().getLogin().equals(_username1.getText().toString())) {
            _adapter1.add(format(getString(R.string.stars), repository.getName(), repository.getStargazersCount()));
            _adapter1.notifyDataSetChanged();
        } else {
            _adapter2.add(format(getString(R.string.stars), repository.getName(), repository.getStargazersCount()));
            _adapter2.notifyDataSetChanged();
        }
    }

    private Observable<RepositoryResponseData> getRepos(String user) {
        return _githubService.getGitHubRepositories("user:"+user, "stars", "desc", 100,1);
    }
}
