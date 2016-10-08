package com.morihacky.android.rxjava.pagination;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

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
    private TestAdapter _adapter;
    private RxBus _bus;
    private PublishSubject<Integer> _paginator;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        _bus = ((MainActivity) getActivity()).getRxBusSingleton();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        _pagingList.setLayoutManager(layoutManager);

        _adapter = new TestAdapter(_bus);
        _pagingList.setAdapter(_adapter);

        _paginator = PublishSubject.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        _subscriptions = new CompositeSubscription();

        Subscription s2 =//
              _paginator//
                    .flatMap(nextPage -> _itemsFromNetworkCall(nextPage + 1, 10))//
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(items -> {
                        int start = _adapter.getItemCount() - 1;

                        _adapter.addItems(items);
                        _adapter.notifyItemRangeInserted(start, 10);

                        _progressBar.setVisibility(View.INVISIBLE);
                        return null;
                    })//
                    .subscribe();

        // I'm using an Rxbus purely to hear from a nested button click
        // we don't really need Rx for this part. it's just easy ¯\_(ツ)_/¯
        Subscription s1 = _bus.toObserverable().subscribe(event -> {
            if (event instanceof ItemBtnViewHolder.PageEvent) {

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


    private static class TestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        static final int ITEM_LOG = 0;
        static final int ITEM_BTN = 1;

        final List<String> _items = new ArrayList<>();
        final RxBus _bus;

        TestAdapter(RxBus bus) {
            _bus = bus;
        }

        void addItems(List<String> items) {
            _items.addAll(items);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == _items.size()) {
                return ITEM_BTN;
            }

            return ITEM_LOG;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case ITEM_BTN:
                    return ItemBtnViewHolder.create(parent);
                default:
                    return ItemLogViewHolder.create(parent);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (getItemViewType(position)) {
                case ITEM_LOG:
                    ((ItemLogViewHolder) holder).bindContent(_items.get(position));
                    return;
                case ITEM_BTN:
                    ((ItemBtnViewHolder) holder).bindContent(_bus);
            }
        }

        @Override
        public int getItemCount() {
            return _items.size() + 1; // add 1 for paging button
        }
    }

    private static class ItemLogViewHolder extends RecyclerView.ViewHolder {
        ItemLogViewHolder(View itemView) {
            super(itemView);
        }

        static ItemLogViewHolder create(ViewGroup parent) {
            return new ItemLogViewHolder(LayoutInflater.from(parent.getContext())
                  .inflate(R.layout.item_log, parent, false));
        }

        void bindContent(String content) {
            ((TextView) itemView).setText(content);
        }
    }

    private static class ItemBtnViewHolder extends RecyclerView.ViewHolder {
        ItemBtnViewHolder(View itemView) {
            super(itemView);
        }

        static ItemBtnViewHolder create(ViewGroup parent) {
            return new ItemBtnViewHolder(LayoutInflater.from(parent.getContext())
                  .inflate(R.layout.item_btn, parent, false));
        }

        void bindContent(RxBus bus) {
            ((Button) itemView).setText(R.string.btn_demo_pagination_more);
            itemView.setOnClickListener(v -> bus.send(new PageEvent()));
        }

        static class PageEvent {
        }
    }
}
