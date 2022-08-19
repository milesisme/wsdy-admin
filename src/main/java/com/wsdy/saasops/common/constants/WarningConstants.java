package com.wsdy.saasops.common.constants;

import java.math.BigDecimal;

public class WarningConstants {



    public static BigDecimal WARNING_DEPOSIT = new BigDecimal(10000);


    public static BigDecimal WARNING_BONUS = new BigDecimal(1000);

    public static Integer  WARNING_DEPOSIT_COUNT = 5;

    public static BigDecimal WARING_FUND_DEPOSIT = new BigDecimal(1000);


    public static BigDecimal FUND_AUDIT = new BigDecimal(1000);


    public static Integer REGISTER_DAY = new Integer(30);


    /**
     * IP异常
     */
    public static String LOGIN_WARNING_TMP = "登录IP异常%s";

    /**
     * 资金调整
     */
    public static String FUND_AUDIT_WARNING_TMP = "人工调整的额度%s%.2f元";


    /**
     * 存款
     */
    public static String FUND_DEPOSIT_WARNING_TMP = "大额存款订单预警金额%.2f元";


    /**
     * 彩金
     */
    public static String BONUS_TMP = "人工添加彩金红利%.2f元";


    /**
     * 存款手动通过
     */
    public static String FUND_DEPOSIT_PASS_TMP = "存款订单手动通过%.2f元";

    /**
     * 绑定钱包和卡
     */
    public static String BIND_TMP = "注册时间超过%d天，绑定%s";
}
