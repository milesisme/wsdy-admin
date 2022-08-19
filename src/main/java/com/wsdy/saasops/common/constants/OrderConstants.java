package com.wsdy.saasops.common.constants;

import io.swagger.annotations.ApiModel;

@ApiModel(value = "OrderConstants", description = "财务收入与支出类别代码")
public class OrderConstants {
    public static final String FUND_ORDER_ONLINEDEPOSIT = "DP"; // 线上入款
    public static final String FUND_ORDER_COMPANYDEPOSIT = "CP";// 公司入款
    public static final String FUND_ORDER_ACCWITHDRAW = "WD";   // 会员提款
    public static final String FUND_ORDER_ACCWITHDRAW_CG = "WDCG";   // 会员提款成功改失败
    public static final String FUND_ORDER_AGYWITHDRAW = "PW";   // 代理提款
    public static final String FUND_ORDER_TRIN = "BI";          // 转账报表转入
    public static final String FUND_ORDER_TROUT = "BO";         // 转账报表转出
    public static final String FUND_ORDER_AUDIT = "AM";         // 调整报表
    public static final String FUND_ORDER_CODE_AM = "AM";       // 人工减少  支出
    public static final String FUND_ORDER_CODE_AA = "AA";       // 人工增加
    public static final String FUND_ORDER_FHQB = "FHQB";        // 转账掉单返回钱包
    public static final String FUND_ORDER_HUPENG_ACCWITHDRAW_BANK = "HPWD";   // 呼朋银行提款
    public static final String FUND_ORDER_HUPENG_ACCWITHDRAW_CENTER = "HPWC";   // 呼朋提款到中心钱包
    public static final String FUND_ORDER_HUPENG = "HP";   // 呼朋入款

    public static final String FUND_ORDER_HUPENG_ADD = "HA";   // 添加入款

    // 活动
    public static final String ACTIVITY_PREFERENTIAL = "SC";    // 首存送
    public static final String ACTIVITY_BETTINGGIFT = "TS";     // 投就送
    public static final String ACTIVITY_AFF = "AFFS";           // aff代理优惠
    public static final String ACTIVITY_RESCUE = "JY";          // 救援金
    public static final String ACTIVITY_REGISTER = "ZC";        // 注册送
    public static final String ACTIVITY_MEMDAY = "HY";          // 会员日
    public static final String ACTIVITY_APPDOWNLOAD = "APPD";          // APP下载
    public static final String ACTIVITY_DEPOSITSENT = "CS";     // 存就送
    public static final String ACTIVITY_WATERBONUS = "FS";      // 返水优惠
    public static final String ACTIVITY_AC = "AC";              // 优惠活动
    public static final String ACTIVITY_QT = "QT";              // 会员日
    public static final String ACTIVITY_SJL = "SJL";            // 升级礼金
    public static final String ACTIVITY_SRLJ = "SRLJ";          // 生日礼金
    public static final String ACTIVITY_MYHB = "MYHB";          // 每月红包
    public static final String ACTIVITY_CJL = "CJL";            // 抽奖
    public static final String ACTIVITY_RESTS = "RS";           // 除了返水优惠 其他优惠
    public static final String ACTIVITY_VALIADBONUS = "YX";     // 有效投注
    public static final String ACTIVITY_NOTYPE = "WL";          // 无类型
    public static final String ACTIVITY_HBY = "HBY";             // 红包雨
    public static final String ACTIVITY_QMDL = "QMDL";           // 全民代理
    public static final String ACTIVITY_HH = "HH";               // 混合活动
    public static final String ACTIVITY_BPYH = "BPYH";           // 包赔优惠
    public static final String ACTIVITY_SCUP = "SCUP";           // 首存返上级

    // 其他
    public static final String AGENT_COMMISSION_YJ = "YJ";      // 佣金结算
    public static final String AGENT_COMMISSION_JF = "JF";      // 佣金结算净负盈利
    public static final String ACCOUNT_REBATE_FA = "FA";        // 好友返利增加
    public static final String FRIENDTRANS_FT = "FT";           // 好友转账

    // 任务
    public static final String QD_TASK_ACTIVITY = "QD";         // 签到
    public static final String XS_TASK_ACTIVITY = "XS";         // 限时活动
    public static final String SJ_TASK_ACTIVITY = "SJ";         // 等级晋升
    public static final String ZL_TASK_ACTIVITY = "ZL";         // 完善资料
    public static final String DS_TASK_ACTIVITY = "DS";         // 定时
    public static final String HR_TASK_ACTIVITY = "HR";         // 活跃奖励
    public static final String HLZS_TASK_ACTIVITY = "HLZS";         // 豪礼赠送
    public static final String HDGZ_TASK_ACTIVITY = "HDGZ";         // 活动规则

    // 外围系统
    public static final String WCK_EG = "WCK";                  // 存点
    public static final String WQK_EG = "WQK";                  // 取点

    // 代理
    public static final String AGENT_CZ = "ACZ";                // 代理充值
    public static final String AGENT_ADC = "ADZ";               // 代理代充
    public static final String AGENT_ASF = "ASF";               // 代理上分
    public static final String AGENT_MSF = "MSF";               // 会员上分
    public static final String AGENT_ORDER_CODE_AM = "GM";      // 代理人工减少  支出
    public static final String AGENT_ORDER_CODE_AA = "GA";      // 代理人工增加
    public static final String AGENT_ORDER_CODE_ATK = "ATK";      // 代理佣金提款
    public static final String AGENT_ORDER_CODE_AYS = "AYS";      // 转入游戏钱包
    public static final String AGENT_ORDER_CODE_ADC = "ADC";      // 转入代充钱包
    public static final String AGENT_ORDER_CODE_ACK = "ACK";      // 存款
    public static final String AGENT_ORDER_CODE_AYH = "AYH";      // 会员佣金
    public static final String AGENT_ORDER_CODE_AXJ = "AXJ";      // 下级佣金
    public static final String AGENT_ORDER_CODE_ATH = "ATH";      // 提款拒绝退回
    public static final String AGENT_ORDER_CODE_AYJ = "AYJ";      // 佣金钱包转入

    public static final String TYJ_PAYOUT_FINANCIAL_CODE = "TY";    //体验金派彩
}