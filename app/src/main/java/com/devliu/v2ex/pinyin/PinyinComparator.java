package com.devliu.v2ex.pinyin;

import java.util.Comparator;
import net.sourceforge.pinyin4j.PinyinHelper;
import org.json.JSONObject;

/**
 * Created by yw on 2015/4/22.
 */
public class PinyinComparator implements Comparator<JSONObject> {
    public int compare(JSONObject obj1, JSONObject obj2) {
        String ostr1 = obj1.optString("title");
        String ostr2 = obj2.optString("title");
        for (int i = 0; i < ostr1.length() && i < ostr2.length(); i++) {
            int codePoint1 = ostr1.charAt(i);
            int codePoint2 = ostr2.charAt(i);
            if (Character.isSupplementaryCodePoint(codePoint1)
                    || Character.isSupplementaryCodePoint(codePoint2)) {
                i++;
            }
            if (codePoint1 != codePoint2) {
                if (Character.isSupplementaryCodePoint(codePoint1)
                        || Character.isSupplementaryCodePoint(codePoint2)) {
                    return codePoint1 - codePoint2;
                }
                String pinyin1 = pinyin((char) codePoint1);
                String pinyin2 = pinyin((char) codePoint2);

                if (pinyin1 != null && pinyin2 != null) { // 两个字符都是汉字
                    if (!pinyin1.equals(pinyin2)) {
                        return pinyin1.compareTo(pinyin2);
                    }
                } else {
                    return codePoint1 - codePoint2;
                }
            }
        }
        return ostr1.length() - ostr2.length();
    }

    // 获得汉字拼音的首字符
    private String pinyin(char c) {
        String[] pinyins = PinyinHelper.toHanyuPinyinStringArray(c);
        if (pinyins == null) {
            return null;
        }
        return pinyins[0];
    }
}
