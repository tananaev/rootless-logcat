package com.tananaev.logcat.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.tananaev.logcat.R;
import com.tananaev.logcat.Settings;

public class FilterOptionsViewController {

    private final Context context;
    private final View baseView;
    private final AutoCompleteTextView inputTag;
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
        setAutoCompleteTextViewAdapter(inputTag, tagHistory);
        keywordHistory = Settings.getKeywordHistory(context);
        setAutoCompleteTextViewAdapter(inputKeyword, keywordHistory);
    }

    private void setAutoCompleteTextViewAdapter(final AutoCompleteTextView autoCompleteTextView, String[] history) {

        ArrayAdapter<String> tagAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, history);
        autoCompleteTextView.setThreshold(1);
        autoCompleteTextView.setAdapter(tagAdapter);

        autoCompleteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (autoCompleteTextView.length() == 0) {
                    autoCompleteTextView.showDropDown();
                }
            }
        });

        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            boolean skipFirst = true;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    if (skipFirst) {
                        skipFirst = false;
                        return;
                    }
                    autoCompleteTextView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            autoCompleteTextView.showDropDown();
                        }
                    }, 100);
                }
            }
        });
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
