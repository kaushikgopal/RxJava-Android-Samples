package com.morihacky.android.rxjava.pagination;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.morihacky.android.rxjava.R;
import com.morihacky.android.rxjava.rxbus.RxBus;
import java.util.ArrayList;
import java.util.List;

class PaginationAutoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private static final int ITEM_LOG = 0;

  private final List<String> _items = new ArrayList<>();
  private final RxBus _bus;

  PaginationAutoAdapter(RxBus bus) {
    _bus = bus;
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return ItemLogViewHolder.create(parent);
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    ((ItemLogViewHolder) holder).bindContent(_items.get(position));

    boolean lastPositionReached = position == _items.size() - 1;
    if (lastPositionReached) {
      _bus.send(new PageEvent());
    }
  }

  @Override
  public int getItemViewType(int position) {
    return ITEM_LOG;
  }

  @Override
  public int getItemCount() {
    return _items.size();
  }

  void addItems(List<String> items) {
    _items.addAll(items);
  }

  private static class ItemLogViewHolder extends RecyclerView.ViewHolder {
    ItemLogViewHolder(View itemView) {
      super(itemView);
    }

    static ItemLogViewHolder create(ViewGroup parent) {
      return new ItemLogViewHolder(
          LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log, parent, false));
    }

    void bindContent(String content) {
      ((TextView) itemView).setText(content);
    }
  }

  static class PageEvent {}
}
