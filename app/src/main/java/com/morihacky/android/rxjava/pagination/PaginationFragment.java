package com.morihacky.android.rxjava.pagination;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.morihacky.android.rxjava.MainActivity;
import com.morihacky.android.rxjava.R;
import com.morihacky.android.rxjava.fragments.BaseFragment;
import com.morihacky.android.rxjava.rxbus.RxBus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class PaginationFragment extends BaseFragment {

    @Bind(R.id.list_paging) RecyclerView _pagingList;
    @Bind(R.id.progress_paging) ProgressBar _progressBar;

    private CompositeSubscription _subscriptions;
    private PaginationAdapter _adapter;
    private RxBus _bus;
    private PublishSubject<Integer> _paginator;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        _bus = ((MainActivity) getActivity()).getRxBusSingleton();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        _pagingList.setLayoutManager(layoutManager);

        _adapter = new PaginationAdapter(_bus);
        _pagingList.setAdapter(_adapter);

        _paginator = PublishSubject.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        _subscriptions = new CompositeSubscription();

        Subscription s2 =//
              _paginator//
                    .onBackpressureDrop()//
                    .concatMap(nextPage -> _itemsFromNetworkCall(nextPage + 1, 10))//
                    .observeOn(AndroidSchedulers.mainThread()).map(items -> {
                  int start = _adapter.getItemCount() - 1;

                  _adapter.addItems(items);
                  _adapter.notifyItemRangeInserted(start, 10);

                  _progressBar.setVisibility(View.INVISIBLE);
                  return null;
              })//
                    .subscribe();

        // I'm using an Rxbus purely to hear from a nested button click
        // we don't really need Rx for this part. it's just easy ¯\_(ツ)_/¯
        Subscription s1 = _bus.asObservable().subscribe(event -> {
            if (event instanceof PaginationAdapter.ItemBtnViewHolder.PageEvent) {

                // trigger the paginator for the next event
                int nextPage = _adapter.getItemCount() - 1;
                _paginator.onNext(nextPage);

            }
        });

        _subscriptions.add(s1);
        _subscriptions.add(s2);
    }

    @Override
    public void onStop() {
        super.onStop();
        _subscriptions.clear();
    }

    /**
     * Fake Observable that simulates a network call and then sends down a list of items
     */
    private Observable<List<String>> _itemsFromNetworkCall(int start, int count) {
        return Observable.just(true)
              .observeOn(AndroidSchedulers.mainThread())
              .doOnNext(dummy -> _progressBar.setVisibility(View.VISIBLE))
              .delay(2, TimeUnit.SECONDS)
              .map(dummy -> {
                  List<String> items = new ArrayList<>();
                  for (int i = 0; i < count; i++) {
                      items.add("Item " + (start + i));
                  }
                  return items;
              });
    }


    // -----------------------------------------------------------------------------------
    // WIRING up the views required for this example

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_pagination, container, false);
        ButterKnife.bind(this, layout);
        return layout;
    }


}
