package com.tananaev.logcat;

import android.support.annotation.Nullable;

public abstract class StringUtils {

    private static final int INDEX_NOT_FOUND = -1;

    public static int indexOfIgnoreCase(@Nullable String str, @Nullable String searchStr) {
        return indexOfIgnoreCase(str, searchStr, 0);
    }

    public static boolean containsIgnoreCase(@Nullable String str, @Nullable String searchStr) {
        return indexOfIgnoreCase(str, searchStr, 0) >= 0;
    }

    public static int indexOfIgnoreCase(@Nullable String str, @Nullable String searchStr, int startPos) {
        if (str == null || searchStr == null) {
            return INDEX_NOT_FOUND;
        }
        if (startPos < 0) {
            startPos = 0;
        }
        final int endLimit = str.length() - searchStr.length() + 1;
        if (startPos > endLimit) {
            return INDEX_NOT_FOUND;
        }
        if (searchStr.length() == 0) {
            return startPos;
        }
        for (int i = startPos; i < endLimit; i++) {
            if (str.regionMatches(true, i, searchStr, 0, searchStr.length())) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }
}
