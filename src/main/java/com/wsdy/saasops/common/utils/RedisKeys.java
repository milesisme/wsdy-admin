package com.wsdy.saasops.common.utils;

/**
 * Redis所有Keys
 */
public class RedisKeys {

    public static String getSysConfigKey(String siteCode,Long userId){
        return "sys:userId:" + siteCode+":"+userId;
    }
    public static String getSysToken(String siteCode,String token){
        return "sys:token:" + siteCode+":"+token;
    }
}
