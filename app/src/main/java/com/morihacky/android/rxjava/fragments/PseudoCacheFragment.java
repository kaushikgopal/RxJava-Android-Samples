package com.morihacky.android.rxjava.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
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
import java.util.Map;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class PseudoCacheFragment
      extends BaseFragment {

    @Bind(R.id.info_pseudoCache_demo) TextView infoText;
    @Bind(R.id.info_pseudoCache_listSubscription) ListView listSubscriptionInfo;
    @Bind(R.id.info_pseudoCache_listDtl) ListView listDetail;

    private ArrayAdapter<String> adapterDetail, adapterSubscriptionInfo;
    private HashMap<String, Long> contributionMap = null;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_pseudo_cache, container, false);
        ButterKnife.bind(this, layout);
        return layout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick(R.id.btn_pseudoCache_concat)
    public void onConcatBtnClicked() {
        infoText.setText(R.string.msg_pseudoCache_demoInfo_concat);
        wireupDemo();

        Observable.concat(getSlowCachedDiskData(), getFreshNetworkData())
              .subscribeOn(Schedulers.io()) // we want to add a list item at time of subscription
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
                      contributionMap.put(contributor.login, contributor.contributions);
                      adapterDetail.clear();
                      adapterDetail.addAll(mapAsList(contributionMap));
                  }
              });
    }

    @OnClick(R.id.btn_pseudoCache_concatEager)
    public void onConcatEagerBtnClicked() {
        infoText.setText(R.string.msg_pseudoCache_demoInfo_concatEager);
        wireupDemo();

        Observable.concatEager(getSlowCachedDiskData(), getFreshNetworkData())
              .subscribeOn(Schedulers.io()) // we want to add a list item at time of subscription
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
                      contributionMap.put(contributor.login, contributor.contributions);
                      adapterDetail.clear();
                      adapterDetail.addAll(mapAsList(contributionMap));
                  }
              });

    }

    @OnClick(R.id.btn_pseudoCache_merge)
    public void onMergeBtnClicked() {
        infoText.setText(R.string.msg_pseudoCache_demoInfo_merge);
        wireupDemo();

        Observable.merge(getCachedDiskData(), getFreshNetworkData())
              .subscribeOn(Schedulers.io()) // we want to add a list item at time of subscription
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
                      contributionMap.put(contributor.login, contributor.contributions);
                      adapterDetail.clear();
                      adapterDetail.addAll(mapAsList(contributionMap));
                  }
              });
    }

    @OnClick(R.id.btn_pseudoCache_mergeSlowDisk)
    public void onMergeSlowBtnClicked() {
        infoText.setText(R.string.msg_pseudoCache_demoInfo_mergeSlowDisk);
        wireupDemo();

        Observable.merge(getSlowCachedDiskData(), getFreshNetworkData())
              .subscribeOn(Schedulers.io()) // we want to add a list item at time of subscription
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
                      contributionMap.put(contributor.login, contributor.contributions);
                      adapterDetail.clear();
                      adapterDetail.addAll(mapAsList(contributionMap));
                  }
              });
    }

    @OnClick(R.id.btn_pseudoCache_mergeOptimized)
    public void onMergeOptimizedBtnClicked() {
        infoText.setText(R.string.msg_pseudoCache_demoInfo_mergeOptimized);
        wireupDemo();

        getFreshNetworkData()//
              .publish(network ->//
                             Observable.merge(network,//
                                              getCachedDiskData().takeUntil(network)))
              .subscribeOn(Schedulers.io()) // we want to add a list item at time of subscription
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
                      contributionMap.put(contributor.login, contributor.contributions);
                      adapterDetail.clear();
                      adapterDetail.addAll(mapAsList(contributionMap));
                  }
              });
    }

    @OnClick(R.id.btn_pseudoCache_mergeOptimizedSlowDisk)
    public void onMergeOptimizedWithSlowDiskBtnClicked() {
        infoText.setText(R.string.msg_pseudoCache_demoInfo_mergeOptimizedSlowDisk);
        wireupDemo();

        getFreshNetworkData()//
              .publish(network ->//
                             Observable.merge(network,//
                                              getSlowCachedDiskData().takeUntil(network)))
              .subscribeOn(Schedulers.io()) // we want to add a list item at time of subscription
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
                      contributionMap.put(contributor.login, contributor.contributions);
                      adapterDetail.clear();
                      adapterDetail.addAll(mapAsList(contributionMap));
                  }
              });
    }

    // -----------------------------------------------------------------------------------
    // WIRING for example

    private void wireupDemo() {
        contributionMap = new HashMap<>();

        adapterDetail = new ArrayAdapter<>(getActivity(), R.layout.item_log_white, R.id.item_log, new ArrayList<>());
        listDetail.setAdapter(adapterDetail);

        adapterSubscriptionInfo = new ArrayAdapter<>(getActivity(),
                                                     R.layout.item_log_white,
                                                     R.id.item_log,
                                                     new ArrayList<>());
        listSubscriptionInfo.setAdapter(adapterSubscriptionInfo);
    }

    private Observable<Contributor> getSlowCachedDiskData() {
        return Observable.timer(1, TimeUnit.SECONDS).flatMap(dummy -> getCachedDiskData());
    }

    private Observable<Contributor> getCachedDiskData() {
        List<Contributor> list = new ArrayList<>();
        Map<String, Long> map = dummyDiskData();

        for (String username : map.keySet()) {
            Contributor c = new Contributor();
            c.login = username;
            c.contributions = map.get(username);
            list.add(c);
        }

        return Observable.from(list)//
              .doOnSubscribe(() -> new Handler(Looper.getMainLooper())//
                    .post(() -> adapterSubscriptionInfo.add("(disk) cache subscribed")))//
              .doOnCompleted(() -> new Handler(Looper.getMainLooper())//
                    .post(() -> adapterSubscriptionInfo.add("(disk) cache completed")));
    }

    private Observable<Contributor> getFreshNetworkData() {
        String githubToken = getResources().getString(R.string.github_oauth_token);
        GithubApi githubService = GithubService.createGithubService(githubToken);

        return githubService.contributors("square", "retrofit")
              .flatMap(Observable::from)
              .doOnSubscribe(() -> new Handler(Looper.getMainLooper())//
                    .post(() -> adapterSubscriptionInfo.add("(network) subscribed")))//
              .doOnCompleted(() -> new Handler(Looper.getMainLooper())//
                    .post(() -> adapterSubscriptionInfo.add("(network) completed")));
    }

    private List<String> mapAsList(HashMap<String, Long> map) {
        List<String> list = new ArrayList<>();

        for (String username : map.keySet()) {
            String rowLog = String.format("%s [%d]", username, contributionMap.get(username));
            list.add(rowLog);
        }

        return list;
    }

    private Map<String, Long> dummyDiskData() {
        Map<String, Long> map = new HashMap<>();
        map.put("JakeWharton", 0L);
        map.put("pforhan", 0L);
        map.put("edenman", 0L);
        map.put("swankjesse", 0L);
        map.put("bruceLee", 0L);
        return map;
    }
}
