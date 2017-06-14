package com.tananaev.logcat.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

public abstract class ViewUtils {

    private static final int AUTOCOMPLETE_DROPDOWN_DELAY = 100;

    public static void setAutoCompleteTextViewAdapter(Context context, final AutoCompleteTextView autoCompleteTextView, String[] history) {

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
                    }, AUTOCOMPLETE_DROPDOWN_DELAY);
                }
            }
        });
    }
}
