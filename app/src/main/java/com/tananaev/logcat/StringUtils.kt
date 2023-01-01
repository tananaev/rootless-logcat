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

object StringUtils {

    private const val INDEX_NOT_FOUND = -1

    @JvmStatic
    fun containsIgnoreCase(str: String?, searchStr: String?): Boolean {
        return indexOfIgnoreCase(str, searchStr, 0) >= 0
    }

    @JvmStatic
    fun indexOfIgnoreCase(str: String?, searchString: String?, startPosition: Int = 0): Int {
        var startIndex = startPosition
        if (str == null || searchString == null) {
            return INDEX_NOT_FOUND
        }
        if (startIndex < 0) {
            startIndex = 0
        }
        val endLimit = str.length - searchString.length + 1
        if (startIndex > endLimit) {
            return INDEX_NOT_FOUND
        }
        if (searchString.isEmpty()) {
            return startIndex
        }
        for (i in startIndex until endLimit) {
            if (str.regionMatches(i, searchString, 0, searchString.length, ignoreCase = true)) {
                return i
            }
        }
        return INDEX_NOT_FOUND
    }

}
