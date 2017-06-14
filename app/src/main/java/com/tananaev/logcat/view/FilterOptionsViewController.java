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
package com.tananaev.logcat.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;

import com.tananaev.logcat.R;
import com.tananaev.logcat.Settings;

public class FilterOptionsViewController {

    @NonNull
    private final Context context;
    @NonNull
    private final View baseView;
    @NonNull
    private final AutoCompleteTextView inputTag;
    @NonNull
    private final AutoCompleteTextView inputKeyword;
    @NonNull
    private final String[] tagHistory;
    @NonNull
    private final String[] keywordHistory;

    public FilterOptionsViewController(Context context) {
        this.context = context;
        baseView = LayoutInflater.from(context).inflate(R.layout.dialog_filter, null);
        inputTag = (AutoCompleteTextView) baseView.findViewById(R.id.tag);
        inputKeyword = (AutoCompleteTextView) baseView.findViewById(R.id.keyword);

        baseView.findViewById(R.id.clear_tag).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputTag.setText("");
            }
        });
        baseView.findViewById(R.id.clear_keyword).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputKeyword.setText("");
            }
        });

        tagHistory = Settings.getTagHistory(context);
        ViewUtils.setAutoCompleteTextViewAdapter(context, inputTag, tagHistory);
        keywordHistory = Settings.getKeywordHistory(context);
        ViewUtils.setAutoCompleteTextViewAdapter(context, inputKeyword, keywordHistory);
    }

    public View getBaseView() {
        return baseView;
    }

    public void setTag(String tag) {
        inputTag.setText(tag);
    }

    public String getTag() {
        return inputTag.getText().toString();
    }

    public void setKeyword(String keyword) {
        inputKeyword.setText(keyword);
    }

    public String getKeyword() {
        return inputKeyword.getText().toString();
    }

    public void saveTagKeyword(String tag, String keyword) {
        Settings.appendTagHistory(context, tagHistory, keywordHistory, tag, keyword);
    }
}
