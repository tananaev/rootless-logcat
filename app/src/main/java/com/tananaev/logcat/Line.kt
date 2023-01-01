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

import java.util.regex.Pattern

class Line(val content: String) {

    var level = 'D'
    var tag: String? = null

    init {
        val matcher = linePattern.matcher(
            content
        )
        if (matcher.matches()) {
            level = matcher.group(1)?.get(0) ?: level
            tag = matcher.group(2)?.trim { it <= ' ' }
        }
    }

    companion object {
        private val linePattern = Pattern.compile("\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d\\.\\d\\d\\d (\\w)/(\\w+).*")
    }

}
