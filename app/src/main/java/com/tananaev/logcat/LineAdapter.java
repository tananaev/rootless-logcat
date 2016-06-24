package com.tananaev.logcat;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class LineAdapter extends RecyclerView.Adapter<LineAdapter.LineViewHolder> {

    private List<Line> lines = new ArrayList<>();

    public LineAdapter() {
        lines.add(new Line("ClassLoader referenced unknown path: /data/app/com.tananaev.logcat-1/lib/x86_64"));
        lines.add(new Line("Before Android 4.1, method android.graphics.PorterDuffColorFilter android.support.graphics.drawable.VectorDrawableCompat.updateTintFilter(android.graphics.PorterDuffColorFilter"));
        lines.add(new Line("No adapter attached; skipping layout"));
        lines.add(new Line("Failed sending reply to debugger: Broken pipe"));
        lines.add(new Line("ClassLoader referenced unknown path: /data/app/com.tananaev.logcat-1/lib/x86_64"));
        lines.add(new Line("Before Android 4.1, method android.graphics.PorterDuffColorFilter android.support.graphics.drawable.VectorDrawableCompat.updateTintFilter(android.graphics.PorterDuffColorFilter"));
        lines.add(new Line("No adapter attached; skipping layout"));
        lines.add(new Line("Failed sending reply to debugger: Broken pipe"));
        lines.add(new Line("ClassLoader referenced unknown path: /data/app/com.tananaev.logcat-1/lib/x86_64"));
        lines.add(new Line("Before Android 4.1, method android.graphics.PorterDuffColorFilter android.support.graphics.drawable.VectorDrawableCompat.updateTintFilter(android.graphics.PorterDuffColorFilter"));
        lines.add(new Line("No adapter attached; skipping layout"));
        lines.add(new Line("Failed sending reply to debugger: Broken pipe"));
        lines.add(new Line("ClassLoader referenced unknown path: /data/app/com.tananaev.logcat-1/lib/x86_64"));
        lines.add(new Line("Before Android 4.1, method android.graphics.PorterDuffColorFilter android.support.graphics.drawable.VectorDrawableCompat.updateTintFilter(android.graphics.PorterDuffColorFilter"));
        lines.add(new Line("No adapter attached; skipping layout"));
        lines.add(new Line("Failed sending reply to debugger: Broken pipe"));
        lines.add(new Line("ClassLoader referenced unknown path: /data/app/com.tananaev.logcat-1/lib/x86_64"));
        lines.add(new Line("Before Android 4.1, method android.graphics.PorterDuffColorFilter android.support.graphics.drawable.VectorDrawableCompat.updateTintFilter(android.graphics.PorterDuffColorFilter"));
        lines.add(new Line("No adapter attached; skipping layout"));
        lines.add(new Line("Failed sending reply to debugger: Broken pipe"));
        lines.add(new Line("ClassLoader referenced unknown path: /data/app/com.tananaev.logcat-1/lib/x86_64"));
        lines.add(new Line("Before Android 4.1, method android.graphics.PorterDuffColorFilter android.support.graphics.drawable.VectorDrawableCompat.updateTintFilter(android.graphics.PorterDuffColorFilter"));
        lines.add(new Line("No adapter attached; skipping layout"));
        lines.add(new Line("Failed sending reply to debugger: Broken pipe"));
        lines.add(new Line("ClassLoader referenced unknown path: /data/app/com.tananaev.logcat-1/lib/x86_64"));
        lines.add(new Line("Before Android 4.1, method android.graphics.PorterDuffColorFilter android.support.graphics.drawable.VectorDrawableCompat.updateTintFilter(android.graphics.PorterDuffColorFilter"));
        lines.add(new Line("No adapter attached; skipping layout"));
        lines.add(new Line("Failed sending reply to debugger: Broken pipe"));
        lines.add(new Line("ClassLoader referenced unknown path: /data/app/com.tananaev.logcat-1/lib/x86_64"));
        lines.add(new Line("Before Android 4.1, method android.graphics.PorterDuffColorFilter android.support.graphics.drawable.VectorDrawableCompat.updateTintFilter(android.graphics.PorterDuffColorFilter"));
        lines.add(new Line("No adapter attached; skipping layout"));
        lines.add(new Line("Failed sending reply to debugger: Broken pipe"));
    }

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
        return lines.size();
    }

}
