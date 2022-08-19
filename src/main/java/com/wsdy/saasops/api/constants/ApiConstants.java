package com.wsdy.saasops.api.constants;

import java.math.BigDecimal;


public class ApiConstants {

    public static final BigDecimal DEAULT_ZERO_VALUE = new BigDecimal("0.00");
    public static final String USER_TOKEN_SPLIT = "_";// 用户信息token分隔符

    public static final Integer BBIN_TRANSFER_SUCCEED = 11100;// 11100 Transfer Successful 轉帳成功

    public static final Integer BBIN_TRANSFER_FAIL = 10003;// 转账失败

    public static final Integer BBIN_SYS_MAINTENANCE = 44444;// 系统维护中

    public static final Integer BBIN_GM_MAINTENANCE = 44445;// 系统维护中Game is in maintenance

    public static final Integer BBIN_KEY_ERROR = 44000;// key驗證錯誤

    public static final String KAPTCHA_REG_SESSION_KEY = "KAPTCHA_REG_SESSION_KEY";// 注册SESSION key

    public static final String KAPTCHA_PHONE_SESSION_KEY = "KAPTCHA_PHONE_SESSION_KEY";// 注册SESSION key

    public static final String KAPTCHA_LOGPHONE_SESSION_KEY = "KAPTCHA_LOGPHONE_SESSION_KEY";// 注册SESSION key

    public static final String KAPTCHA_LOGIN_SESSION_KEY = "KAPTCHA_LOGIN_SESSION_KEY";// 登陆图片SEESSION key

    public static final String KAPTCHA_RETRVPWD_SESSION_KEY = "KAPTCHA_RETRVPWD_SESSION_KEY";// 找回密码图片SEESSION key

    public static final String KAPTCHA_ADDACCOUNT_SESSION_KEY = "KAPTCHA_ADDACCOUNT_SESSION_KEY";//添加好友验证码

    public static final String KAPTCHA_AGENT_SESSION_KEY = "KAPTCHA_AGENT_SESSION_KEY";// 注册SESSION key
    public static final String KAPTCHA_AGENT_V2_SESSION_KEY = "KAPTCHA_AGENT_V2_SESSION_KEY";// 外围系统key

    public static final Integer DEAULT_GROUP_REG = new Integer("0");// 会员默认会员组

    public static final String USER_ID = "userId";// 用户Id

    public static final String DOMAIN_CODE = "domainCode";//推广code

    public static final String USER_NAME = "userName";// 用户名

    public static final String WEB_SITE_PREFIX = "webSitePrefix";// 站点前缀

    public static final String WEB_SITE_OBJECT = "webSiteObject";// 站点对象

    public static final String LOGINTIMES_KEY = "logintimes";// 登陆次数限制

    public static final String SMS_UNCODE_GBK = "GBK";

    public static final String COOKIE_JSESSIONID = "JSESSIONID";

    public static final String REDIS_CODE_KEY = "utf-8";// 字符集编码

    public static final String CURRENCY_TYPE = "CNY";// 人民币

    public static final String CURRENCY_TYPE_RMB = "RMB";// 人民币

    public static final String AGIN_BALANCE_INFO = "error";

    public static final String AGIN_BALANCE_MSG = "error:60001";

    public static final Double USER_DEFAULT_BALANCE = new Double("0.00");

    public static final String ip = "ip";// 用户Ip


    // spring 缓存KEY定义开始

    public static final String REDIS_USER_CACHE_KEY = "usercache";// 根据用户ID缓存用户
    public static final String REDIS_GAME_SITE_CACHE_KEY = "gameSiteCache";// 根据URL地址缓存 站点数据
    public static final String REDIS_GAME_SITECODE_CACHE_KEY = "gameSiteCodeCache";// 根据站点代码缓存站点信息
    public static final String REDIS_GAME_API_CACHE_KEY = "gameApiCache";// 根据用户ID缓存用户
    public static final String REDIS_GAME_COMPANY_CACHE_KEY = "gameCompanyCache";// 每一商户每一游戏缓存
    public static final String REDIS_DEPOT_ACC_KEY = "gameAccCache";// 平台账号缓存
    public static final String REDIS_DEF_GROUP_KEY = "defGroupCache";// 平台默认会员组
    public static final String REDIS_PROTOCOL_USER_KEY = "protocolUserCache";// 用户协议
    public static final String REIDS_LOGIN_PASS_KEY = "loginPassCache";// 用户登陆密码

    public static final String REDIS_DEPOT_BALANCE_ACC_KEY = "depotBalanceAcc";// 平台余额

    public static final String REIDS_AGENT_LOGIN_TOKEN_KEY = "agentLoginTokenCache";// 代理用户登入TOKEN
    
    public static final String REIDS_LOGIN_TOKEN_KEY = "loginTokenCache";// 用户登入TOKEN
    public static final String REIDS_LOGIN_TOKEN_LISTENER_KEY = "loginTokenCacheListener";
    public static final String REIDS_LOGIN_OFFLINE_KEY = "loginOfflineCache";// 用户登出标记

    public static final String REIDS_MAIL_SET_KEY = "mailsetCache";
    public static final String REIDS_SMS_SET_KEY = "SMSsetCache";
    public static final String REIDS_STATION_SET_KEY = "StationsetCache";
    /** 系统设置缓存key */
    public static final String REIDS_SYS_SETTING_KEY = "sys_setting_key:";
    public static final String REIDS_CAPTCHA_KEY = "retrvRegCaptchaCache";// 用户注册验证码,找回密码
    public static final String REIDS_LOGIN_CAPTCHA_KEY = "loginCaptchaCache";// 用户登陆验证码保存
    public static final String REIDS_VFYMAILORMOB_CODE_KEY = "vfyMailOrMobCodeCache";// 用户验证邮箱或手机CODE
    //public static final String REIDS_AGENT_MOBILE_CODE_KEY = "agentLoginMobileCodeCache";//代理验证手机号
    public static final String REIDS_SECURITY_MOB_CODE_KEY = "updateSecurityMobCode";// 用户手机安全验证code
    public static final String REIDS_LOGIN_PT2TOKEN_KEY = "loginPt2TokenCache";// pt代理账号用户登入token

    public static final String REIDS_LOGIN_NTTOKEN_KEY = "loginNtTokenCache";// Nt代理账号用户登入token

    public static final String REIDS_LOGIN_PNGTOKEN_KEY = "loginPngTokenCache";// PNG会员账号用户登入token

    public final static String REIDS_KAPTCHA_REG_CACHE = "kaptchaRegCache:";// 注册

    public static final String REIDS_KAPTCHA_LOGIN_CACHE = "kaptchaLoginCache:";// 登陆

    public static final String REIDS_KAPTCHA_RETRVPWD_CACHE = "kaptchaRetrvPwdCache:";// 找回密码

    public static final String REDIS_PROXY_CATCH = "redisProxyCache";//代理缓存

    public static final String REDIS_WINLOST_CATCH = "gameCatCache";//游戏分类缓存

    public static final Integer REIDS_TOKEN_DEFTIME_KEY = 1800;// 默认半个小时

    public static final String REIDS_FRIENDTRANS_CODE_KEY ="friendTrans";

    public static final String REIDS_QUERYTREELISTNEW_KEY ="queryTreeListNew";   // 权限树缓存

    /**
     * 奖池余额
     */
    public static final String ACCOUNT_SITE = "accountSite_";

    public static final String SUCC_CODE = "1";

    public static final String SITE_CODE = "sys:siteCode:";
    public static final String SCHEMA_NAME = "sys:schemaName:";
    public static final String DEPOTCODE_CONVERTO_DEPOTNAME_CACHE = "depotcodeconvertodepotnamecache";

    public static final String ORIGIN_PC = "PC";
    public static final String ORIGIN_APP = "APP";
    public static final String ORIGIN_H5 = "H5";

    // 平台depotCode常量
    public interface DepotCode {
        String	AB	="AB";	//	AB	平台
        String	AECP	="AECP";	//	AECP	平台
        String	AGIN	="AGIN";	//	AGIN	平台
        String	BBIN	="BBIN";	//	BBIN	平台
        String	BBIN2	="BBIN2";	//	BBIN2	平台
        String	BG	="BG";	//	BG	平台
        String	CMD	="CMD";	//	CMD	平台
        String	CQ9	="CQ9";	//	CQ9	平台
        String	EB	="EB";	//	EB	平台
        String	GDQ	="GDQ";	//	GDQ	平台
        String	IBC2	="IBC2";	//	IBC2	平台
        String	IGGF	="IGGF";	//	IGGF	平台
        String	IGHK	="IGHK";	//	IGHK	平台
        String	IGSS	="IGSS";	//	IGSS	平台
        String	IM	="IM";	//	IM	平台
        String	IM2	="IM2";	//	IM2	平台
        String	JDB	="JDB";	//	JDB	平台
        String	KY	="KY";	//	KY	平台
        String	LC	="LC";	//	LC	平台
        String	MG2	="MG2";	//	MG2	平台
        String	OG	="OG";	//	OG	平台
        String	PGS	="PGS";	//	PGS	平台
        String	PT	="PT";	//	PT	平台
        String	SG	="SG";	//	SG	平台
        String	TC	="TC";	//	TC	平台
        String	TF	="TF";	//	TF	平台
        String	XA	="XA";	//	XA	平台
        String	XJ	="XJ";	//	XJ	平台
        String  OBDY ="OBDY";       // OB电游
        String  OBQP ="OBQP";       // OB棋牌

        String  OBES ="OBES";       // OB电竞

        // 其他
        String T188 = "188";
        String PT2 = "PT2";
        String NT = "NT";
        String PNG = "PNG";
        String PB = "PB";
        String EV = "EV";
        String EG = "EG";
        String GD = "GD";
        String DS = "DS";   // 东森娱乐
        String DFW = "DFW"; // 大富翁棋牌
        String TM = "TM";   // 天美棋牌
        String MG = "MG";
        String AGST = "AGST";
        String OPUSSB = "OPUSSB";   // OPUS平台_体育
        String OPUSCA = "OPUSCA";   // OPUS平台_真人
    }

    public interface Transfer {
        String in = "IN";// 从网站账号转款到游戏账号;
        String out = "OUT";// 從遊戲账號转款到網站賬號
    }
    // 邮箱常量

    public static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
    public static final String MAIL_SMTP_TIMEOUT = "mail.smtp.timeout";
    public static final String MAIL_SMTP_SSL_ENABLE = "mail.smtp.ssl.enable";
    public static final String MAIL_SMTP_SSL_SOCKETFACTORY = "mail.smtp.ssl_socketFactory";
    public static final String MAIL_DEBUG = "mail.debug";

    public interface Terminal {
        Byte pc = 0;
        Byte mobile = 1;
    }

    public interface TransferStates {
        Integer suc = 0;// 成功
        Integer fail = 1;// 失败
        Integer progress = 2;// 挂起
    }
}
