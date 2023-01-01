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
package com.tananaev.logcat

import android.content.Context
import android.text.TextUtils
import java.lang.StringBuilder

object Settings {

    private const val SETTINGS = "settings"
    private const val MAX_COUNT = 10
    private const val KEY_TAG_HISTORY = "tagHistory"
    private const val KEY_KEYWORD_HISTORY = "keywordHistory"
    private const val KEY_SEARCH_HISTORY = "searchHistory"

    // for filtering
    @JvmStatic
    fun getTagHistory(context: Context): Array<String> {
        return getHistory(context, KEY_TAG_HISTORY)
    }

    // for filtering
    @JvmStatic
    fun getKeywordHistory(context: Context): Array<String> {
        return getHistory(context, KEY_KEYWORD_HISTORY)
    }

    private fun getHistory(context: Context, key: String): Array<String> {
        val pref = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE)
        val history = pref.getString(key, "")
        return if (TextUtils.isEmpty(history)) {
            arrayOf()
        } else history!!.split("\n").toTypedArray()
    }

    @JvmStatic
    fun appendTagHistory(
        context: Context,
        tagHistory: Array<String>,
        keywordHistory: Array<String>,
        newTag: String,
        newKeyword: String,
    ) {
        val tagHistoryData = makeHistoryData(tagHistory, newTag)
        val keywordHistoryData = makeHistoryData(keywordHistory, newKeyword)
        if (TextUtils.isEmpty(tagHistoryData) && TextUtils.isEmpty(keywordHistoryData)) {
            return
        }
        val pref = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE)
        val editor = pref.edit()
        if (!TextUtils.isEmpty(tagHistoryData)) {
            editor.putString(KEY_TAG_HISTORY, tagHistoryData)
        }
        if (!TextUtils.isEmpty(keywordHistoryData)) {
            editor.putString(KEY_KEYWORD_HISTORY, keywordHistoryData)
        }
        editor.apply()
    }

    private fun makeHistoryData(history: Array<String>, data: String): String? {
        if (TextUtils.isEmpty(data)) {
            return null
        }
        if (history.isNotEmpty() && TextUtils.equals(history[0], data)) {
            return null
        }
        val sb = StringBuilder()
        sb.append(data)
        var i = 0
        while (i < history.size && i < MAX_COUNT) {
            if (TextUtils.equals(history[i], data)) {
                i++
                continue
            }
            sb.append("\n").append(history[i])
            i++
        }
        return sb.toString()
    }

    // for highlighting
    @JvmStatic
    fun getSearchHistory(context: Context): Array<String> {
        return getHistory(context, KEY_SEARCH_HISTORY)
    }

    @JvmStatic
    fun appendSearchHistory(context: Context, searchHistory: Array<String>, newSearchWord: String) {
        val searchHistoryData = makeHistoryData(searchHistory, newSearchWord)
        if (TextUtils.isEmpty(searchHistoryData)) {
            return
        }
        val pref = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString(KEY_SEARCH_HISTORY, searchHistoryData)
        editor.apply()
    }

}
