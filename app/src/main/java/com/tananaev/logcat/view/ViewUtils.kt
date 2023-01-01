/*
 * Copyright 2016 - 2022 Anton Tananaev (anton.tananaev@gmail.com)
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
package com.tananaev.logcat.view

import android.content.Context
import android.widget.AutoCompleteTextView
import android.widget.ArrayAdapter
import android.text.TextWatcher
import android.text.Editable

object ViewUtils {

    private const val AUTOCOMPLETE_DROPDOWN_DELAY = 100

    @JvmStatic
    fun setAutoCompleteTextViewAdapter(
        context: Context?,
        autoCompleteTextView: AutoCompleteTextView,
        history: Array<String>,
    ) {
        val tagAdapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, history)
        autoCompleteTextView.threshold = 1
        autoCompleteTextView.setAdapter(tagAdapter)
        autoCompleteTextView.setOnClickListener {
            if (autoCompleteTextView.length() == 0) {
                autoCompleteTextView.showDropDown()
            }
        }
        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            var skipFirst = true
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.isEmpty()) {
                    if (skipFirst) {
                        skipFirst = false
                        return
                    }
                    autoCompleteTextView.postDelayed(
                        { autoCompleteTextView.showDropDown() },
                        AUTOCOMPLETE_DROPDOWN_DELAY.toLong(),
                    )
                }
            }
        })
    }

}
