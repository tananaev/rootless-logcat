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
import com.tananaev.logcat.Settings.getTagHistory
import com.tananaev.logcat.view.ViewUtils.setAutoCompleteTextViewAdapter
import com.tananaev.logcat.Settings.getKeywordHistory
import com.tananaev.logcat.Settings.appendTagHistory
import android.widget.AutoCompleteTextView
import android.view.LayoutInflater
import android.view.View
import com.tananaev.logcat.R

class FilterOptionsController(private val context: Context) {

    val baseView: View = LayoutInflater.from(context).inflate(R.layout.dialog_filter, null)
    private val inputTag = baseView.findViewById<View>(R.id.tag) as AutoCompleteTextView
    private val inputKeyword = baseView.findViewById<View>(R.id.keyword) as AutoCompleteTextView
    private val tagHistory: Array<String>
    private val keywordHistory: Array<String>

    init {
        baseView.findViewById<View>(R.id.clear_tag).setOnClickListener { inputTag.setText("") }
        baseView.findViewById<View>(R.id.clear_keyword).setOnClickListener { inputKeyword.setText("") }
        tagHistory = getTagHistory(context)
        setAutoCompleteTextViewAdapter(context, inputTag, tagHistory)
        keywordHistory = getKeywordHistory(context)
        setAutoCompleteTextViewAdapter(context, inputKeyword, keywordHistory)
    }

    var tag: String?
        get() = inputTag.text.toString()
        set(tag) {
            inputTag.setText(tag)
        }

    var keyword: String?
        get() = inputKeyword.text.toString()
        set(keyword) {
            inputKeyword.setText(keyword)
        }

    fun saveTagKeyword(tag: String?, keyword: String?) {
        appendTagHistory(context, tagHistory, keywordHistory, tag!!, keyword!!)
    }

}
