package com.morihacky.android.rxjava.fragments;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder>{

    private LayoutInflater inflater;
    private List<String> list;
    private @LayoutRes int layout;
    private @IdRes int view;

    public CustomAdapter(Context context, @LayoutRes int layout, @IdRes int view, List<String> list) {
        this.layout = layout;
        this.view = view;
        inflater = LayoutInflater.from(context);
        this.list=list;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(layout, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.serial_number.setText(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void clear() {
        list.clear();
        notifyDataSetChanged();
    }

    public void add(String item) {
        list.add(item);
        notifyItemInserted(list.size() - 1);
    }

    class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView serial_number;

        public MyViewHolder(View itemView) {
            super(itemView);
            serial_number = itemView.findViewById(view);
        }
    }
}