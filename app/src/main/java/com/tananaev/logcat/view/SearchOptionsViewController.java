package com.tananaev.logcat.view;

import android.content.Context;
import android.support.annotation.NonNull;
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
