package com.wsdy.saasops.config;


public class ThreadLocalCache {

    public static ThreadLocal<SiteCodeThreadLocal> siteCodeThreadLocal = new ThreadLocal<>();

    public static void  setSiteCodeAsny(String siteCode){
        SiteCodeThreadLocal siteCodeThreadLocal = new SiteCodeThreadLocal();
        siteCodeThreadLocal.setSiteCode(siteCode);
        ThreadLocalCache.siteCodeThreadLocal.set(siteCodeThreadLocal);
    }
}
