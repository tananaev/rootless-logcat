package com.tananaev.logcat;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

public class LineAdapter extends RecyclerView.Adapter<LineAdapter.LineViewHolder> {

    private static long MIN_TIME = 100;

    private List<Line> lines = new LinkedList<>();

    private long updated = System.currentTimeMillis();
    private int size = 0;

    public static class LineViewHolder extends RecyclerView.ViewHolder {

        private TextView textView;

        public LineViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(android.R.id.text1);
        }

        public TextView getTextView() {
            return textView;
        }

    }

    public boolean addItem(String line) {
        lines.add(new Line(line));
        if (System.currentTimeMillis() - updated > MIN_TIME) {
            notifyItemRangeInserted(size, lines.size() - size);
            updated = System.currentTimeMillis();
            size = lines.size();
            return true;
        } else {
            updated = System.currentTimeMillis();
            return false;
        }
    }

    @Override
    public LineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.line_list_item, parent, false);
        return new LineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LineViewHolder holder, int position) {
        Line item = lines.get(position);
        holder.getTextView().setText(item.getContent());
    }

    @Override
    public int getItemCount() {
        return size;
    }

}
