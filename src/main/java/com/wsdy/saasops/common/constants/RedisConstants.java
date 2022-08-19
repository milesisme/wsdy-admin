package com.wsdy.saasops.common.constants;


public class RedisConstants {

    /**
     * 平台余额
     **/
    public static final String REDIS_DEPOT_BALANCE = "redisDepotBalance_";

    /**
     * 用户登陆
     **/
    public static final String REDIS_USER_LOGIN = "redisUserLogin_";

    /**
     * 用户登陆
     **/
    public static final String REDIS_MOBILE_LOGIN = "redisMobileLogin_";

    /**
     * 用户短信登陆
     **/
    public static final String REDIS_MOBILE_LOGIN_CODE = "redisMobileLoginCode_";

    /**
     * 用户短信注册
     **/
    public static final String REDIS_MOBILE_REGISTE_CODE = "redisMobileRegisteCode_";

    /**
     * 平台总余额
     **/
    public static final String DEPOT_TOTAL_BALANCE = "depotTotalBalance_";

    /**
     * 余额
     */
    public static final String SEESION_TRANSFERIN = "applyTransferIn_";

    /**
     * 测试成功（转出）
     */
    public static final String SEESION_COMMONTRANSFEROUTSUC = "applyCommontransferOutSuc_";

    /**
     * 测试失败（转出）
     */
    public static final String SEESION_COMMONTRANSFEROUTFAIL = "applyCommontransferOutFail_";
    /**
     * 测试成功（转入）
     */
    public static final String SEESION_COMMONTRANSFERINSUC = "applyCommontransferInSuc_";

    /**
     * 测试失败（转入）
     */
    public static final String SEESION_COMMONTRANSFERINFAIL = "applyCommontransferInFail_";


    /**
     * 稽核计算会员前缀
     */
    public static final String AUDIT_ACCOUNT = "auditAccount_";


    /**
     * 稽核计算会员前缀
     */
    public static final String AUDIT_ACCOUNT_SIGN = "auditAccountSign_";

    /**
     * 会员查询支付前缀
     */
    public static final String QUERY_ACCOUNT_PAY = "queryAccountPay_";


    /**
     * 会员申请活动前缀
     */
    public static final String APPLY_ACTIVITY = "applyActivity_";

    /**
     * 会员平台转账
     */
    public static final String ACCOUNT_DEPOT_TRANSFER = "acccountDpotTransfer_";

    /**
     * 推荐好友生成链接
     */
    public static final String PROMOTION_ACCOUNT_ISCHECK = "promotionAccountIsCheck";

    /**
     * 返点job
     */
    public static final String ACCOUNT_REBATE = "accountRebateJob_";


    /**
     * 发送消息
     */
    public static final String ACCOUNT_MESSAGE_KEY = "accountMessageKey_";
    public static final String ACCOUNT_MESSAGE_KEY_BATCH = "accountMessageKeyBatch_";

    /**
     * 会员户内转账 刷新
     */
    public static final String ACCOUNT_CHECK_TRANSFER = "acccountCheckTransfer_";

    public static final String ACCOUNT_UPDATE_TRANSFER = "acccountUpdateTransfer_";

    public static final String FRIENDS_TRANS_KEY = "friendsTrans";

    public static final String FRIENDS_TRANS_ADD_KEY = "friendsTransAdd_";

    public static final String AGENTMANAGERVERIFYCODE = "agentmanagerverifycode";

    /**
     * 批量审核活动 or单个
     */
    public static final String ACTIVITY_AUDIT_ACCOUNT = "activityAuditAccount_";
    public static final String ACTIVITY_AUDIT_ACCOUNT_WATER = "activityAuditAccountWater_";
    public static final String ACTIVITY_AUDIT_MODIFY_AMOUNT = "activityModifyAmount_";

    public static final String FUND_AUDIT_UPDATE = "fundAuditUpdate_";
    public static final String AGENT_AUDIT_UPDATE = "agentAuditUpdate_";

    public static final String EXCEL_EXPORT = "excel_export_";

    public static final String EG_EXCEL_EXPORT = "egagent_excel_export_";

    public static final String UPDATE_WITHDRAW = "update_withdraw_";
    public static final String ONE_PAY_UPDATE_WITHDRAW = "onepay_update_withdraw_";
    public static final String PAYMENT_UPDATE_WITHDRAW = "payment_update_withdraw_";
    public static final String EVELLET_UPDATE_WITHDRAW = "evellet_update_withdraw_";
    public static final String LBT_UPDATE_WITHDRAW = "lbt_update_withdraw_";
    public static final String UPDATE_WITHDRAW_LOCK = "update_withdraw_lock";
    public static final String UPDATE_WITHDRAW_LOCK_NEW = "UPDATE_WITHDRAW_LOCK_NEW";
    public static final String SAVE_BONUS_LOCK = "saveBonus_";

    public static final String AGENT_UPDATE_WITHDRAW_LOCK = "agentupdate_withdraw_lock";


    /**
     * 会员取款
     */
    public static final String ACCOUNT_WITHDRAW = "accountWithdraw_";


    /**
     * 会员取款
     */
    public static final String ACCOUNT_HUPENG_WITHDRAW = "accountHuPengWithdraw_";

    /**
     * 会员取款审核
     */
    public static final String ACCOUNT_WITHDRAW_AUDIT = "accountWithdrawAudit_";

    /**
     * 活动规则创建
     */
    public static final String ACTIVITY_RULE = "activityRule_";

    /**
     * 入款审核
     */
    public static final String ACCOUNT_DEPOSiT_AUDIT = "accountDepositAudit_";

    /**
     * 入款成功改失败
     */
    public static final String ACCOUNT_DEPOSiT_SUCTOFAIL = "accountDepositSucToFail_";


    /**
     * 返水批处理
     */
    public static final String OPRACT_WATER_BATCHINFO = "insertOprActWaterBatchInfo_";

    /**
     * 返水批处理
     */
    public static final String SETTLEMENT_WATER_BATCHINFO = "settlementWaterBatchInfo_";

    /**
     * 会员批量调级
     */
    public static final String ACCOUNT_ACT_LEVEL_UPDATE_BATCH = "batchUpdateActLevel";

    // 国际化数据
    public static final String I18N = "Saasops-biz:I18n:";

    public static final String ACCOUNT_SETTLEMENT_WATER = "settlementWater_";

    /**
     * 通用执行器
     */
    public static final String BATCH_DISPATCHERTASK = "batchdispatcherTask_";

    /**
     * 会员job返水
     */
    public static final String BATCH_WATER_ACCOUNT = "batchdWaterAccount_";

    /**
     * sms告警
     */
    public static final String BATCH_SMS_ALARM = "batchSmsAlarm_";
    /**
     * 过期消息清理
     */
    public static final String BATCH_MESSAGE_DELETE = "batchMessageDelete_";

    /**
     * 注册生成账密
     */
    public static final String MEMBER_REGISTER_LOGINNAME_ONLI = "memberRegisterLoginNameOnly";
    public static final String MEMBER_REGISTER_GENERATE_INFO = "memberRegisterGenerateInfo";

    /**
     * 活动规则创建
     */
    public static final String ACTIVITY_BLACKLIST = "actBlacklist_";

    public static final String SAN_GONG_CAST = "sanGongCast_";

    // 短信群发
    public static final String ACCOUNT_MESSAGE_MASS = "accountMessageMass_";

    // 统计点击量
    public static final String TASK_CLICK_RATE = "taskClickRate_";

    // 领取任务
    public static final String TASK_ACCOUT = "taskAccount_";

    /**
     * 代理领取点
     */
    public static final String EG_AGENT_BALANCE = "egAgentBalance_";

    /**
     * 会员领取点
     */
    public static final String EG_ACCOUNT_BALANCE = "egAccountBalance_";


    // 加密钱包回调请求
    public static final String EVELLET_CALLBACK = "evelletCallback_";


    // 代理加密钱包回调请求
    public static final String EVELLET_CALLBACK_AGENT = "evelletCallbackAgent_";

    /**
     * 发送反馈消息
     */
    public static final String ACCOUNT_OPINION_KEY = "accountOpinionKey_";

    /**
     * 会员发放红包
     */
    public static final String ACCOUNT_VIPRED = "accountVipRedKey_";

    /**
     * 会员抽奖
     */
    public static final String ACCOUNT_LOTTERY = "accountLotteryKey_";

    /**
     * 会员抽奖生成记录
     */
    public static final String ACCOUNT_ADDLOTTERY = "accountaddLotteryKey_";

    /**
     * 会员催单
     */
    public static final String ACCOUNT_REMINDER = "acccountReminder_";


    /**
     * 代理入款审核
     */
    public static final String AGENT_DEPOSiT_AUDIT = "agentDepositAudit_";

    /**
     * 代理取款审核
     */
    public static final String AGENT_WITHDRAW_AUDIT = "agentWithdrawAudit_";

    /**
     * 代理短信注册
     **/
    public static final String AGENT_REDIS_MOBILE_REGISTE_CODE = "agRedisMobileRegisteCode_";

    /**
     * 会员点击红包
     */
    public static final String ACCOUNT_REDPOCKETRAIN_CLICK = "accountRedPacketRainKey_";

    /**
     * 代理查询支付前缀
     */
    public static final String QUERY_AGENT_PAY = "queryAgetntPay_";

    /**
     * 代理安全密码修短信
     **/
    public static final String AGENT_REDIS_MOBILE_SECURE_CODE = "agRedisMobileSecurePwdCode_";

    /**
     * 代理安全密码修短信
     **/
    public static final String AGENT_REDIS_MOBILE_CARD_CODE = "bankcardCode_";

    /**
     * 稽核计算会员前缀
     */
    public static final String AGENT_COMM_CAST = "agentCommissionCast_";
    /**
     * 全民代理
     */
    public static final String MBR_REBATE_AGENT_AUDIT = "mbrRebateAgentAudit_";
    public static final String MBR_REBATE_AGENT_AUDIT_BATCH = "mbrRebateAgentAuditBatch_";
    public static final String MBR_REBATE_AGENT_CAST_DAY = "mbrRebateAgentCast_";
    public static final String MBR_REBATE_AGENT_CAST_MONTH = "mbrRebateAgentCastMonth_";

    /**
     * 统计每日会员存取款优惠
     */
    public static final String MBR_FUNDS_REPORT_COUNT_DAY = "mbrFundsReportCount_";

    /**
     * 统计会员投注比
     */
    public static final String MBR_BET_POINT_COUNT = "MBRBetPointCount_";

    /**
     * 统计每月会员存取款优惠
     */
    public static final String MBR_FUNDS_REPORT_COUNT_MONTH = "mbrFundsReportCountMonth_";

    // 风险查询结果保存redis
    public static final String MEMBER_CHECK_IP = "memberCheckIp_";

    /**
     * 站点七牛云静态资源访问域名
     */
    public static final String QINNIUYUN_URL = "qiniuDomainOfBucketV2";


    public static final String ACCOUNT_VERIFYSERCRET = "accountVerifySercret_";

    /**
     * 登录注册是否需要验证码
     */
    public static final String ACCOUNT_REGISTE_LOGIN = "accountRegisteLogin_";

    /**
     * 单一钱包操作余额
     */
    public static final String TRADE_OPERATING_BALANCE = "tradeOperatingBalance_";

    /**
     * 代理彩金
     */
    public static final String PAY_OFF_TRANSFER = "payoffTransfer_";
    
    /**
     * 	小额客服线当日点击次数统计
     */
    public static final String LINE_SERVICE_COUNT = "line_service_count_";
}
