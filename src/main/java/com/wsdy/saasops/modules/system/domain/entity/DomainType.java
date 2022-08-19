package com.wsdy.saasops.modules.system.domain.entity;

import lombok.Getter;

/**
 * 域名类型
 * Created by William on 2017/11/1.
 */

@Getter
public enum DomainType {

    Type_1("管理后台", 1), Type_2("网站主页", 2), Type_3("支付域名", 3), Type_4("代理后台", 4);
    // 成员变量
    private String name;
    private int index;

    // 构造方法
     DomainType(String name, int index) {
        this.name = name;
        this.index = index;
    }
    // 普通方法
    public static String getName(int index) {
        for (DomainType c : DomainType.values()) {
            if (c.getIndex() == index) {
                return c.name;
            }
        }
        return null;
    }
    public static Integer getIndex(String name) {
        for (DomainType c : DomainType.values()) {
            if (c.getName() == name) {
                return c.index;
            }
        }
        return null;
    }

}

