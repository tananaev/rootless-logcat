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
import com.tananaev.logcat.Settings.getSearchHistory
import com.tananaev.logcat.view.ViewUtils.setAutoCompleteTextViewAdapter
import com.tananaev.logcat.Settings.appendSearchHistory
import android.widget.AutoCompleteTextView
import android.view.LayoutInflater
import android.view.View
import com.tananaev.logcat.R

class SearchOptionsController(private val context: Context) {

    val baseView: View = LayoutInflater.from(context).inflate(R.layout.dialog_search, null)
    private val inputSearch = baseView.findViewById<View>(R.id.input_search) as AutoCompleteTextView
    private val searchHistory: Array<String>

    init {
        baseView.findViewById<View>(R.id.clear_search).setOnClickListener { inputSearch.setText("") }
        searchHistory = getSearchHistory(context)
        setAutoCompleteTextViewAdapter(context, inputSearch, searchHistory)
    }

    var searchWord: String?
        get() = inputSearch.text.toString()
        set(searchWord) {
            inputSearch.setText(searchWord)
        }

    fun saveSearchWord(searchWord: String?) {
        appendSearchHistory(context, searchHistory, searchWord!!)
    }

}
