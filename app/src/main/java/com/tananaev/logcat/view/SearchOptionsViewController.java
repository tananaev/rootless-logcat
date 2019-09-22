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
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;

import com.tananaev.logcat.R;
import com.tananaev.logcat.Settings;

public class SearchOptionsViewController {
    @NonNull
    private final Context context;
    @NonNull
    private final View baseView;
    @NonNull
    private final AutoCompleteTextView inputSearch;
    @NonNull
    private final String[] searchHistory;

    public SearchOptionsViewController(Context context) {
        this.context = context;
        baseView = LayoutInflater.from(context).inflate(R.layout.dialog_search, null);
        inputSearch = (AutoCompleteTextView) baseView.findViewById(R.id.input_search);
        baseView.findViewById(R.id.clear_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputSearch.setText("");
            }
        });

        searchHistory = Settings.getSearchHistory(context);
        ViewUtils.setAutoCompleteTextViewAdapter(context, inputSearch, searchHistory);
    }

    public View getBaseView() {
        return baseView;
    }

    public void setSearchWord(String searchWord) {
        inputSearch.setText(searchWord);
    }

    public String getSearchWord() {
        return inputSearch.getText().toString();
    }

    public void saveSearchWord(String searchWord) {
        Settings.appendSearchHistory(context, searchHistory, searchWord);
    }
}
