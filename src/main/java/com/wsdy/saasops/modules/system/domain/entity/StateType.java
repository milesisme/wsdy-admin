package com.wsdy.saasops.modules.system.domain.entity;

import lombok.Getter;

/**
 * 绑定状态
 * Created by William on 2017/11/2.
 */
@Getter
public enum StateType {
    Type_1("审批中", 1), Type_2("绑定", 2), Type_3("解绑", 3);
    // 成员变量
    private String name;
    private int index;

    // 构造方法
    private StateType(String name, int index) {
        this.name = name;
        this.index = index;
    }
    // 普通方法
    public static String getName(int index) {
        for (StateType c : StateType.values()) {
            if (c.getIndex() == index) {
                return c.name;
            }
        }
        return null;
    }
    public static Integer getIndex(String name) {
        for (StateType c : StateType.values()) {
            if (c.getName() == name) {
                return c.index;
            }
        }
        return null;
    }

}
