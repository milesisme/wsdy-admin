package com.wsdy.saasops.common.utils;


import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * 返回数据
 */
public class R extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;
    private static final String data = "data";
    private static final String page = "page";

    public R() {
        put("code", 0);
        put("msg", "success");
    }

    public static R error() {
        return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "未知异常，请联系管理员");
    }

    public static R error(String msg) {
        return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, msg);
    }

    public static R halfError(String msg, String isSign) {
        return errorRecoverBalance(HttpStatus.SC_NOT_IMPLEMENTED, msg, isSign);
    }

    public static R halfErrorList(String msg, String isSign,String error) {
        return errorListRecoverBalance(HttpStatus.SC_NOT_IMPLEMENTED, msg, isSign,error);
    }

    public static R errorRecoverBalance(int code, String msg, String isSign) {
        R r = new R();
        r.put("code", code);
        r.put("msg", msg);
        r.put("isSign", isSign);
        return r;
    }

    public static R errorListRecoverBalance(int code, String msg, String isSign,String error) {
        R r = new R();
        r.put("code", code);
        r.put("msg", msg);
        r.put("isSign", isSign);
        r.put("error",error);
        return r;
    }

    public static R error(int code, String msg) {
        R r = new R();
        r.put("code", code);
        r.put("msg", msg);
        return r;
    }

    public static R ok(String msg) {
        R r = new R();
        r.put("msg", msg);
        return r;
    }

    public static R ok(Boolean  code, Object  message) {
        R r = new R();
        r.put("code", code);
        r.put("message", message);
        return r;
    }

    public static R ok(int code, Object obj) {
        R r = new R();
        r.put("code", code);
        r.put("user", obj);
        return r;
    }

    public static R ok(Object msg) {
        R r = new R();
        r.put("msg", msg);
        return r;
    }

    public static R ok(Map<String, Object> map) {
        R r = new R();
        r.putAll(map);
        return r;
    }

    public static R ok() {
        return new R();
    }

    @Override
    public R put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    public R putPage(Object value) {
        super.put(page, value);
        return this;
    }

    public R put(Object value) {
        super.put(data, value);
        return this;
    }
}