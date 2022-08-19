package com.wsdy.saasops.api.utils;

/***
 * gameType游戏分类
 */
public enum GameTypeEnum {

    // es查询参数小写
    ENUM_THREE(3, "live"),  //真人
    ENUM_EIGHT(8, "hunter"),   //捕鱼
    ENUM_FIVE(5, "slot"),   //电子
    ENUM_TWELVE(12, "lottery"),  //彩票
    ENUM_ONE(1, "sport"),    //体育
    ENUM_ONES(1, "sports"),    //体育
    ENUM_SIX(6, "chess"),     // 棋牌
    ENUM_NINE(9,"esport"),     // 电竞—查询
    ENUM_NINE_2(9,"esports"),     // 电竞-跳转
    ENUM_FORTY_NINE(49,"fight"),     // 斗鸡
    ENUM_TIPS(100,"tip");     // 小费打赏

    private Integer key;
    private String value;

    GameTypeEnum(Integer key, String value) {
        this.key = key;
        this.value = value;
    }

    GameTypeEnum() {
    }

    public Integer getKey() {
        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
