package com.morihacky.android.rxjava.pagination;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.morihacky.android.rxjava.MainActivity;
import com.morihacky.android.rxjava.R;
import com.morihacky.android.rxjava.fragments.BaseFragment;
import com.morihacky.android.rxjava.rxbus.RxBus;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.processors.PublishProcessor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PaginationAutoFragment extends BaseFragment {

  @BindView(R.id.list_paging)
  RecyclerView _pagingList;

  @BindView(R.id.progress_paging)
  ProgressBar _progressBar;

  private PaginationAutoAdapter _adapter;
  private RxBus _bus;
  private CompositeDisposable _disposables;
  private PublishProcessor<Integer> _paginator;
  private boolean _requestUnderWay = false;

  @Override
  public View onCreateView(
      LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View layout = inflater.inflate(R.layout.fragment_pagination, container, false);
    ButterKnife.bind(this, layout);
    return layout;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    _bus = ((MainActivity) getActivity()).getRxBusSingleton();

    LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
    layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    _pagingList.setLayoutManager(layoutManager);

    _adapter = new PaginationAutoAdapter(_bus);
    _pagingList.setAdapter(_adapter);

    _paginator = PublishProcessor.create();
  }

  @Override
  public void onStart() {
    super.onStart();
    _disposables = new CompositeDisposable();

    Disposable d2 =
        _paginator
            .onBackpressureDrop()
            .doOnNext(
                i -> {
                  _requestUnderWay = true;
                  _progressBar.setVisibility(View.VISIBLE);
                })
            .concatMap(this::_itemsFromNetworkCall)
            .observeOn(AndroidSchedulers.mainThread())
            .map(
                items -> {
                  _adapter.addItems(items);
                  _adapter.notifyDataSetChanged();

                  return items;
                })
            .doOnNext(
                i -> {
                  _requestUnderWay = false;
                  _progressBar.setVisibility(View.INVISIBLE);
                })
            .subscribe();

    // I'm using an RxBus purely to hear from a nested button click
    // we don't really need Rx for this part. it's just easy ¯\_(ツ)_/¯

    Disposable d1 =
        _bus.asFlowable()
            .filter(o -> !_requestUnderWay)
            .subscribe(
                event -> {
                  if (event instanceof PaginationAutoAdapter.PageEvent) {

                    // trigger the paginator for the next event
                    int nextPage = _adapter.getItemCount();
                    _paginator.onNext(nextPage);
                  }
                });

    _disposables.add(d1);
    _disposables.add(d2);

    _paginator.onNext(0);
  }

  @Override
  public void onStop() {
    super.onStop();
    _disposables.clear();
  }

  /** Fake Observable that simulates a network call and then sends down a list of items */
  private Flowable<List<String>> _itemsFromNetworkCall(int pageStart) {
    return Flowable.just(true)
        .observeOn(AndroidSchedulers.mainThread())
        .delay(2, TimeUnit.SECONDS)
        .map(
            dummy -> {
              List<String> items = new ArrayList<>();
              for (int i = 0; i < 10; i++) {
                items.add("Item " + (pageStart + i));
              }
              return items;
            });
  }
}
