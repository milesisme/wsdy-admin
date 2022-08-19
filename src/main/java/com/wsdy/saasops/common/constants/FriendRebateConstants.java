package com.wsdy.saasops.common.constants;

import lombok.Getter;

@Getter

public enum FriendRebateConstants {

    TD("累计存款", 1),

    NUM("好友数量", 2),

    FC("首充", 3),

    VB("有效下注", 4),

    VIP("VIP", 5),

    CHARGE("充值奖励", 6),

    SELF("自身奖励", 7),

    FRIEND("好友奖励", 8),

    TY("体育", 20),

    DZ("电子", 21),

    DJ("电竞", 22),

    QP("棋牌", 23),

    CP("彩票", 24),

    ZR("真人", 25),
    ;

    private int value;

    private String name;

    FriendRebateConstants(String name, int  value){
        this.value = value;
        this.name = name;
    }

}
