package com.opss.movibus.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class Adapter<T> extends RecyclerView.Adapter implements View.OnClickListener, View.OnLongClickListener {

    protected List<T> itensList;
    protected Context context;
    protected Actions onClick;
    protected Actions onLongClick;

    public Adapter(List<T> itensList, Context context, Actions onClick, Actions onLongClick) {
        this.itensList = itensList;
        this.context = context;
        this.onClick = onClick;
        this.onLongClick = onLongClick;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return itensList == null ? 0 : itensList.size();
    }

    @Override
    public void onClick(View view) {
        if (onClick == null)
            return;

        this.onClick.onClick(view);
    }

    @Override
    public boolean onLongClick(View view) {
        if (onLongClick == null)
            return false;

        this.onLongClick.onLongClick(view);
        return false;
    }

    public void addItem(T item) {
        this.itensList.add(item);
        this.notifyDataSetChanged();
    }

    public void removeItem(T item) {
        this.itensList.remove(item);
        this.notifyDataSetChanged();
    }

    public T getItem(int position) {
        return itensList == null || itensList.isEmpty() ? null : itensList.get(position);
    }

    public void update(T item) {
    }

    public interface Actions {
        void onClick(View view);

        void onLongClick(View view);
    }

}
