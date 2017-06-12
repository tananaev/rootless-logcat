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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Line {

    private static Pattern linePattern = Pattern.compile("\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d\\.\\d\\d\\d (\\w)/(\\w+).*");

    private final char level;
    @NonNull
    private final String lowerTag;
    @NonNull
    private final String content;
    @Nullable
    private String lowerContent;

    public Line(@NonNull String content) {
        this.content = content;
        Matcher matcher = linePattern.matcher(content);
        if (matcher.matches()) {
            level = matcher.group(1).charAt(0);
            lowerTag = matcher.group(2).toString().trim().toLowerCase();
        } else {
            level = 'D';
            lowerTag = "";
        }
    }

    public char getLevel() {
        return level;
    }

    @NonNull
    public String getLowerTag() {
        return lowerTag;
    }

    @NonNull
    public String getContent() {
        return content;
    }

    @NonNull
    public String getLowerContent() {
        if (lowerContent == null) {
            lowerContent = content.toLowerCase();
        }
        return lowerContent;
    }

}
