package com.wsdy.saasops.common.constants;

//系统设置常量
public class SystemConstants {

    // 网站数据默认查询天数
    public static final String DEFAULT_QUERY_DAYS = "defaultQueryDays";
    // 会员数据查询天数
    public static final String MEMBER_QUERY_DAYS = "memberQueryDays";
    // 管理员密码过期天数
    public static final String PASSWORD_EXPIRE_DAYS = "passwordExpireDays";
    // 站点logo图片
    public static final String LOGO_PATH = "logoPath";
    // 页面Title图片
    public static final String TITLE_PATH = "titlePath";
    // 网站统计代码(PC版)
    public static final String WEBSITE_CODE_PC = "websiteCodePc";
    // 网站统计代码(移动版)
    public static final String WEBSITE_CODE_MB = "websiteCodeMb";
    // 网站Title
    public static final String WEBSITE_TITLE = "websiteTitle";
    // 网站关键字
    public static final String WEBSITE_KEYWORDS = "websiteKeywords";
    // 网站描述
    public static final String WEBSITE_DESCRIPTION = "websiteDescription";
    // 客服配置代码(PC版)
    public static final String CONFIG_CODE_PC = "configCodePc";
    // 客服配置代码(PC版)
    public static final String CONFIG_CODE_MB = "configCodeMb";
    // 客服配置代码1(PC版)
    public static final String CONFIG_CODE_PC1 = "configCodePc1";
    // 客服配置代码1(手机版)
    public static final String CONFIG_CODE_MB1 = "configCodeMb1";
    // 合营部TELEGRAM
    public static final String CONFIG_TELEGRAM = "configTelegram";
    // 合营部SKYPE
    public static final String CONFIG_SKYPE = "configSkype";
    // 合营部FLYGRAM
    public static final String CONFIG_FLYGRAM = "configFlygram";
    // PC首页展示域名
    public static final String SHOW_WEBSITE = "showWebSite";
    
    /** USDT购买超链接 */
    public static final String USDT_BUY_URL = "usdtBuyUrl";

    //自动删除已读站内信天数
    public static final String AUTO_DELETE_DAYS = "autoDeleteDays";
    //代理域名解析地址配置
    public static final String AGENT_DOMAIN_ANALYSIS_SITE = "agentDomainAnalysisSite";
    // 邮件发送服务器
    public static final String MAIL_SEND_SERVER = "mailSendServer";
    // 邮件发送端口
    public static final String MAIL_SEND_PORT = "mailSendPort";
    // 邮件发送账号
    public static final String MAIL_SEND_ACCOUNT = "mailSendAccount";
    // 账号密码
    public static final String MAIL_PASSWORD = "mailPassword";
    // 是否使用SSL
    public static final String WETHER_SSL = "wetherSsl";
    // 字符集
    public static final String CHARACTER_SET = "characterSet";

    // 短信平台
    public static final String SMS_PLATFORM = "smsPlatform";    // 1.启瑞云 2.互亿无线
    // 短信网关地址
    public static final String SMS_GETWAY_ADDRESS = "smsGetwayAddress";
    // 短信接口用户名
    public static final String SMS_INTERFACE_NAME = "smsInterfaceName";
    // 短信接口密码
    public static final String SMS_INTERFACE_PASSWORD = "smsInterfacePassword";
    // 短信发送方名称
    public static final String SMS_SEND_NAME = "smsSendName";
    // 短信模板
    public static final String SMS_TEMPLATE = "smsTemplate";
    // 手机号强制绑定
    public static final String SMS_MOBILE_COMPEL_BIND = "smsMobileCompelBind";
    // 会员账号        0：无，1：默认，2：必填、默认
    public static final String MEMBER_ACCOUNT = "loginName";
    // 会员登录密码     0：无，1：默认，2：必填、默认
    public static final String MEMBER_LOGIN_PASSWORD = "loginPwd";
    // 会员重复密码    0：无，1：默认，2：必填、默认
    public static final String MEMBER_REPEATED_PASSWORD = "reLoginPwd";
    // 会员验证码      0：无，1：默认，2：必填、默认
    public static final String MEMBER_VERIFICATION_CODE = "captchareg";
    // 会员真实姓名    0：无，1：默认，2：必填、默认
    public static final String MEMBER_REAL_NAME = "realName";

    //是否允许会员名称重复    0：关闭，1 开启
    public static final String MEMBER_REAL_NAME_REPEAT = "realNameRepeat";
    // 会员手机    0：无，1：默认，2：必填、默认
    public static final String MEMBER_TELPHONE = "mobile";
    // 会员手机验证码      0：无，1：默认，2：必填、默认
    public static final String MEMBER_TELPHONE_CODE = "mobileCaptchareg";
    // 会员ip
    public static final String MEMBER_IP = "loginIp";
    // 会员设备号
    public static final String MEMBER_DEVICE = "deviceUuid";
    // 会员邮箱    0：无，1：默认，2：必填、默认
    public static final String MEMBER_EMAIL = "email";
    // 会员QQ     0：无，1：默认，2：必填、默认
    public static final String MEMBER_QQ = "qq";
    // 会员微信     0：无，1：默认，2：必填、默认
    public static final String MEMBER_WECHAT = "weChat";

    // 会员地址     0：无，1：默认，2：必填、默认
    public static final String MEMBER_ADDRESS = "address";
    // 推广码
    public static final String MEMBER_PROMOTION = "promotion";

    // 代理账号  0：无，1：默认，2：必填、默认
    public static final String AGENT_ACCOUNT = "agentLoginName";
    // 代理登录密码  0：无，1：默认，2：必填、默认
    public static final String AGENT_LOGIN_PASSWORD = "agentLoginPwd";
    // 代理重复密码   0：无，1：默认，2：必填、默认
    public static final String AGENT_REPEATED_PASSWORD = "agentReLoginPwd";
    // 代理验证码    0：无，1：默认，2：必填、默认
    public static final String AGENT_VERIFICATION_CODE = "agentCaptchareg";
    // 代理真实姓名    0：无，1：默认，2：必填、默认
    public static final String AGENT_REAL_NAME = "agentRealName";
    // 代理手机   0：无，1：默认，2：必填、默认
    public static final String AGENT_TELPHONE = "agentMobile";
    // 代理手机验证码      0：无，1：默认，2：必填、默认
    public static final String AGENT_TELPHONE_CODE = "agentMobileCaptchareg";
    // 代理邮箱   0：无，1：默认，2：必填、默认
    public static final String AGENT_EMAIL = "agentEmail";
    // 代理QQ    0：无，1：默认，2：必填、默认
    public static final String AGENT_QQ = "agentQQ";
    // 代理微信      0：无，1：默认，2：必填、默认
    public static final String AGENT_WECHAT = "agentWechat";
    // 代理地址      0：无，1：默认，2：必填、默认
    public static final String AGENT_ADDRESS = "agentAddress";
    // 用户注册是否强制显示网站服务条款    0：否  ，1 是
    public static final String MEMBER_REGISTER_DISPLAY_TERMS_OF_WEBSITE = "memberDisplayTermsOfWebsite";
    // 代理注册是否强制显示网站服务条款    0：否  ，1 是
    public static final String AGENT_REGISTER_DISPLAY_TERMS_OF_WEBSITE = "agentDisplayTermsOfWebsite";
    //用户注册网站服务条款
    public static final String MEMBER_SERVICE_TERMS_OF_WEBSITE = "memberServiceTermsOfWebsite";
    //代理网站服务条款
    public static final String AGENT_SERVICE_TERMS_OF_WEBSITE = "agentServiceTermsOfWebsite";

    public static final String KEY_ISVISIBLE = "isVisible";
    public static final String KEY_ISREQUIRE = "isRequire";

    // 安卓下载链接
    public static final String APP_DOWNLOAD_ANDORID_URL = "androidDownloadUrl";
    // ios下载链接
    public static final String APP_DOWNLOAD_IOS_URL = "iosDownloadUrl";
    
    /** 轮播模块是否启用 */
    public static final String IS_OPEN_CAROUSEL = "isOpenCarousel";
    
    /**  公告模块是否启用 */
    public static final String IS_OPEN_NOTICE = "isOpenNotice";

    // 存款锁定配置
    public static final String DEPOSIT_AUTO_LOCK = "depositAutoLock";
    
   	/** 是否未通过稽核出款 */
    public static final String isMultipleOpen = "isMultipleOpen";
    
    /** 合营计划图 */
    public static final String VENTURE_PLAN_PIC = "venturePlanPic";
    
    /** 定义行业图 */
    public static final String DEFINE_INDUSTRY_PIC = "defineIndustryPic";

    // 是否启用极速出款
    public static final String FAST_WITHDRAW_ENABLE = "fastWithdrawEnable";

    // 是否启用自动出款
    public static final String PAY_AUTOMATIC = "payAutomatic";
    // 是否启用支付宝出款
    public static final String ALIPAY_ENABLE = "alipayEnable";
    // 自动出款单笔最高限额(元)
    public static final String PAY_MONEY = "payMoney";
    //入款完整条件设置 1真实姓名，2手机号
    public static final String DEPOSIT_CONDITION = "depositCondition";
    //入款完整条件设置 1真实姓名，2手机号
    public static final String WITHDRAW_CONDITION = "withdrawCondition";


    // 是否开启限时提款
    public static final String IS_WITHDRAW_LIMIT_TIME_OPEN = "isWithdrawLimitTimeOpen";

    public static final String WITHDRAW_LIMIT_TIME_LIST = "withdrawLimitTimeDtoList";


    // 是否启用不同名银行卡
    public static final String BANK_DIFFERENT_NAME_ENABLE = "bankDifferentNameEnable";
    public static final String BANK_DIFFERENT_NAME_NUMBER = "bankDifferentNameNumber";

    // 是否启用免转
    public static final String FREE_WALLETSWITCH = "freeWalletSwitch";

    // 是否启用EG三公标志
    public static final String EG_SANGONG_FLG = "egSanGongFlg";

    // 三公代理
    public static final String EG_SANGONG_AGENT = "egSanGongAgent";

    public static final String FRIEND_TRANS_AUTOMATIC="friendTransAutomatic";

    public static final String FRIEND_TRANS_MAX_AMOUNT="friendTransMaxAmount";

    public static final String AI_RECOMMEND="aiRecommend";

    //Stoken
    public static final String STOKEN = "SToken";
    //schemaName
    public static final String SCHEMA_NAME = "schemaName";
    public static final String T_CP_SITE_SERVICE = "TCpSiteService";


    //代理注册审核 0 禁用 1启用
    public static final String AGENT_REGISTER = "agentRgister";
    //代理后台注册下线 0 禁用 1启用
    public static final String AGENT_SYS_REGISTER = "agentSysRgister";
    //代理后台新增会员 0 禁用 1启用
    public static final String AGENT_ADD_ACCOUNT = "agentAddAccount";
    //代理后台新增子代理 0 禁用 1启用
    public static final String AGENT_ADD_SUB = "agentAddSub";

    //推广域名会员设置
    public static final String ACCOUNT_PROMOTION = "accountPromotion";
    public static final String AGENT_PROMOTION = "agentPromotion";
    /** Channel 渠道的推广域名*/
    public static final String CHANNEL_PROMOTION = "channelPromotion";

    //是否开启自动晋升 0否 1是
    public static final String AUTOMATIC_PROMOTION = "automaticPromotion";
    //是否开启自动降级 0否 1是
    public static final String DOWNGRADA_PROMOTION = "downgradePromotion";
    //降级计算周期：最近多少天
    public static final String DOWNGRADA_DAY = "downgradePromotionDay";
    /** 等级恢复计算周期：最近多少天 */
    public static final String RECOVER_PROMOTION_DAY = "recoverPromotionDay";

    //活动等级周期统计规则 0无限期，1按日，2按周，3按月
    public static final String ACT_LEVEL_STATICS_RULE = "actLevelStaticsRule";
    //活动等级周期统计规则说明
    public static final String ACT_LEVEL_STATICS_RULE_DESCRIPTION = "actLevelStaticsRuleDescription";

    // 前台是否注册  0不允许 1允许
    public static final String MEMBER_WEB_REGISTER = "accWebRegister";

    //会员返利计算层级深度
    public static final String REBATE_CAST_DEPTH = "rebateCastDepth";
    // 注册方式   0 普通注册 1 普通注册+快捷模式 2 快捷模式
    public static final String MEMBER_WEB_REGISTER_METHOD = "registerMethod";
    // 语音线路        1:blink  2 rowave
    public static final String OUTCALL_PLATFORM = "outCallPlatform";
    // 短信群发标志  0 不支持 1 支持
    public static final String MASS_TEXT_FLAG = "massTextFlag";

    //是否开启数据加密 1开启 0关闭
    public static final String SYS_ENCRYPT = "sysencrypt";


    //抽奖配置
    public static final String LOTTERY_CONFIG = "lotteryConfig";

    // 全民代理计算层级深度
    public static final String REBATE_CAST_DEPTH_AGENT = "rebateCastDepthAgent";
    // 是否校验SPTV用户创建
    public static final String SPTV_CREATE_USER_FLG = "sptvCreateUserFlg";

    /** 轮播模块是否启用 */
    public static final String SYS_CUIDAN = "cuiDan";


    /**
     * 是否播放新存款订单生成广播:0,不播放广播；1,播放广播
     */
    public static final String IS_BROADCASTED_NEW_DEPOSIT_ORDER_GENERATION = "isBroadcastNewDepositOrder";
    /**
     * 是否播放新取款订单生成广播:0,不播放广播；1,播放广播
     */
    public static final String IS_BROADCASTED_NEW_WITHDRAW_ORDER_GENERATION = "isBroadcastNewWithdrawOrder";
    /**
     * 是否播放新红利生成广播:0,不播放广播；1,播放广播
     */
    public static final String IS_BROADCASTED_NEW_BONUS_GENERATION = "isBroadcastNewBonus";

}
