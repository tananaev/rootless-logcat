/*
 * Copyright 2016 Anton Tananaev (anton.tananaev@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tananaev.logcat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

public class LineAdapter extends RecyclerView.Adapter<LineAdapter.LineViewHolder> {

    private List<Line> lines = new LinkedList<>();

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

    public List<Line> getLines() {
        return lines;
    }

    public void clear() {
        lines.clear();
        notifyDataSetChanged();
    }

    public void addItems(List<String> lines) {
        for (String line : lines) {
            this.lines.add(new Line(line));
        }
        notifyItemRangeInserted(this.lines.size() - lines.size(), lines.size());
    }

    @Override
    public LineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.line_list_item, parent, false);
        return new LineViewHolder(view);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBindViewHolder(LineViewHolder holder, int position) {
        Line item = lines.get(position);
        holder.getTextView().setText(item.getContent());
        Context context = holder.getTextView().getContext();
        switch (item.getContent().charAt(0)) {
            case 'V':
            case 'D':
            case 'I':
                holder.getTextView().setTextColor(context.getResources().getColor(R.color.colorNormal));
                break;
            case 'W':
                holder.getTextView().setTextColor(context.getResources().getColor(R.color.colorWarning));
                break;
            case 'E':
            case 'A':
                holder.getTextView().setTextColor(context.getResources().getColor(R.color.colorError));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return lines.size();
    }

}
