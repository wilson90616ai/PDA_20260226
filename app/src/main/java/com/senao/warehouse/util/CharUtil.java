package com.senao.warehouse.util;

import android.text.TextUtils;

import java.util.regex.Pattern;

public class CharUtil {

    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
            //|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
            //|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
            //|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
            //|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                )
        //|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION)
        {
            return true;
        }
        return false;
    }

    // 完整的判斷中文漢字和符號
    public static boolean isChineseOrGreek(String strName) {
        if (!TextUtils.isEmpty(strName)) {
            char[] ch = strName.toCharArray();
            for (int i = 0; i < ch.length; i++) {
                char c = ch[i];
                if (isChinese(c) || isGreek(c) || isMathematicalOperators(c)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isGreek(char c) {
        int startPosition = 0, endPosition = 0, currentPosition = 0;
        char cStart = 'Α', cEnd = 'ω';
        startPosition = (int) cStart;
        endPosition = (int) cEnd;
        currentPosition = (int) c;
        if (currentPosition >= startPosition && currentPosition <= endPosition)
            return true;
        else
            return false;
    }

    public static boolean isMathematicalOperators(char c) {
        int startPosition = 0, endPosition = 0, currentPosition = 0;
        char cStart = '∀', cEnd = '⋿';
        startPosition = (int) cStart;
        endPosition = (int) cEnd;
        currentPosition = (int) c;
        if (currentPosition >= startPosition && currentPosition <= endPosition)
            return true;
        else
            return false;
    }


    // 只能判斷部分CJK字符（CJK統一漢字）
    public static boolean isChineseByREG(String str) {
        if (str == null) {
            return false;
        }
        Pattern pattern = Pattern.compile("[\\u4E00-\\u9FBF]+");
        return pattern.matcher(str.trim()).find();
    }

}
