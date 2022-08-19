package com.wsdy.saasops.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VerificationUtil {
    private static Pattern PHONE_PATTERN = Pattern.compile("^(13[0-9]|15[0-9]|153|15[6-9]|180|18[23]|18[5-9])\\d{8}$");
    /**
     * 判断是否是邮箱.
     * @param str 指定的字符串
     * @return 是否是邮箱:是为true，否则false
     */
    public static Boolean isEmail(String str) {
        Boolean isEmail = false;
        String expr = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})$";
        if (str.matches(expr)) {
            isEmail = true;
        }
        return isEmail;
    }

    /**
     * 判断是否是手机号
     * @param phone
     * @return
     */
    public static boolean isPhone(String phone) {
        Pattern pattern = PHONE_PATTERN;
        Matcher matcher = pattern.matcher(phone);
        if (matcher.matches()) {
            return true;
        }
        return false;
    }
    /**
     * 判断是否包含汉字
     * @param str
     * @return
     */
    public static boolean hasChineseByRange(String str) {
        if (str == null) {
            return false;
        }
        char[] ch = str.toCharArray();
        for (char c : ch) {
            if (c >= 0x4E00 && c <= 0x9FBF) {
                return true;
            }
        }
        return false;
    }
}
