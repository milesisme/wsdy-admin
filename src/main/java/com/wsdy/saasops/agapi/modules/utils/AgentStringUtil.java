package com.wsdy.saasops.agapi.modules.utils;


import org.apache.commons.lang3.StringUtils;

public class AgentStringUtil {

    public static String replaceCardNo(String cardNo) {
        if (StringUtils.isEmpty(cardNo)) {
            return StringUtils.EMPTY;
        }
        return cardNo.replaceAll(cardNo.substring(4, cardNo.length()-4), getAesKey(cardNo.length()-8));
    }

    public static String replaceRealName(String realName) {
        if (StringUtils.isEmpty(realName)) {
            return StringUtils.EMPTY;
        }
        int i = realName.length() < 2 ? realName.length() : realName.length()-1;
        return realName.replaceAll(realName.substring(0, i), getAesKey(i));
    }

    public static String replaceWeChat(String weChat) {
        if (StringUtils.isEmpty(weChat)) {
            return StringUtils.EMPTY;
        }
        int i = weChat.length() < 3 ? weChat.length() : weChat.length()-2;
        return weChat.replaceAll(weChat.substring(0, i), getAesKey(i));
    }

    public static String replaceQq(String qq) {
        if (StringUtils.isEmpty(qq)) {
            return StringUtils.EMPTY;
        }
        int i = qq.length() < 5 ? 0 : 4;
        return qq.replaceAll(qq.substring(i, qq.length()), getAesKey(qq.length() - i));
    }

    public static String replacePhone(String phone) {
        if (StringUtils.isEmpty(phone)) {
            return StringUtils.EMPTY;
        }
        return phone.replaceAll("(\\d{3})\\d{6}(\\d{2})", "$1******$2");
    }

    public static String replaceEmail(String email) {
        if (StringUtils.isEmpty(email)) {
            return StringUtils.EMPTY;
        }
        int a = email.indexOf("@");
        int i = a < 5 ? 0 : 4;
        return email.replaceAll(email.substring(i, email.lastIndexOf("@")), getAesKey(a - i));
    }

    private static String getAesKey(int j) {
        String key = StringUtils.EMPTY;
        String strPool = "*";
        int max = strPool.length() - 1;
        for (int i = 0; i < j; i++) {
            key += strPool.charAt((int) (Math.random() * max));
        }
        return StringUtils.isEmpty(key) ? "****" : key;
    }
}