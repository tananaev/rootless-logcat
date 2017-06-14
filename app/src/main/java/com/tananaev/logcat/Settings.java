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
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

public class Settings {
    private static final String SETTINGS = "settings";
    private static final int MAX_COUNT = 10;
    private static final String KEY_TAG_HISTORY = "tagHistory";
    private static final String KEY_KEYWORD_HISTORY = "keywordHistory";
    private static final String KEY_SEARCH_HISTORY = "searchHistory";

    // for filtering
    @NonNull
    public static String[] getTagHistory(Context context) {
        return getHistory(context, KEY_TAG_HISTORY);
    }

    // for filtering
    @NonNull
    public static String[] getKeywordHistory(Context context) {
        return getHistory(context, KEY_KEYWORD_HISTORY);
    }

    @NonNull
    private static String[] getHistory(Context context, String key) {
        SharedPreferences pref = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        String history = pref.getString(key, "");
        if (TextUtils.isEmpty(history)) {
            return new String[]{};
        }
        return history.split("\n");
    }

    public static void appendTagHistory(Context context, @NonNull String[] tagHistory, @NonNull String[] keywordHistory, String newTag, String newKeyword) {
        String tagHistoryData = makeHistoryData(tagHistory, newTag);
        String keywordHistoryData = makeHistoryData(keywordHistory, newKeyword);
        if (TextUtils.isEmpty(tagHistoryData) && TextUtils.isEmpty(keywordHistoryData)) {
            return;
        }

        SharedPreferences pref = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        if (!TextUtils.isEmpty(tagHistoryData)) {
            editor.putString(KEY_TAG_HISTORY, tagHistoryData);
        }
        if (!TextUtils.isEmpty(keywordHistoryData)) {
            editor.putString(KEY_KEYWORD_HISTORY, keywordHistoryData);
        }
        editor.apply();
    }

    @Nullable
    private static String makeHistoryData(String[] history, String data) {
        if (TextUtils.isEmpty(data)) {
            return null;
        }
        if (history.length > 0 && TextUtils.equals(history[0], data)) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(data);
        for (int i = 0; i < history.length && i < MAX_COUNT; i++) {
            if (TextUtils.equals(history[i], data)) {
                continue;
            }
            sb.append("\n").append(history[i]);
        }

        return sb.toString();
    }

    // for highlighting
    @NonNull
    public static String[] getSearchHistory(Context context) {
        return getHistory(context, KEY_SEARCH_HISTORY);
    }

    public static void appendSearchHistory(Context context, @NonNull String[] searchHistory, String newSearchWord) {
        String searchHistoryData = makeHistoryData(searchHistory, newSearchWord);
        if (TextUtils.isEmpty(searchHistoryData)) {
            return;
        }
        SharedPreferences pref = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(KEY_SEARCH_HISTORY, searchHistoryData);
        editor.apply();
    }
}
