package com.wsdy.saasops.common.constants;


import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class Constants {

    //项目名称
    public static final String PROJECT_NAME = "SaasopsV2";
    //redis 名称空间分隔符
    public static final String REDIS_SPACE_SPACING = ":";
    //redis 失效
    public static final String REDIS_CMD_EXPIRED = "expired";

    public static final BigDecimal DEAULT_ZERO_VALUE = BigDecimal.ZERO;
    //总代上级默认Id
    public static final int TOP_AGENT_PARENT_ID = 0;
    //暂无
    public static final String SYSTEM_NONE = "NONE";
    //本系统平台depotId
    public static final int SYS_DEPOT_ID = 0;
    
    /**
     * batch update or insert to db once num
     */
    public static final int BATCH_ONCE_COUNT = 700;


    public static final String SYSTEM_USER = "系统审核";
    public static final String SYSTEM_WATER_USER = "自助洗码";
    public static final String SYSTEM_PASSUSER = "系统出款";
    public static final String LBT_REFUSE = "LBT拒绝";

    public static final String SYSTEM_DEPOT_NAME = "钱包中心";
    public static final String SYSTEM_MESSAGE = "请自行配置";

    public static final int ONE_HUNDRED = 100;

    public static final String ACCOUNT_CONTACT = "member:mbraccount:contact";
    public static final String ACCOUNT_CURRENCY = "member:cryptocurrencies";


    public static final String AGENT_COOUNT_ID = "agentAccount";// 代理帐号
    
    public static final String AGENT_ID = "agentId";// 代理id
    
    public static final String AGENT = "AGENT";


    public static final int DEPOSIT_LOCK_NUM = 5;       // 存款锁定日次数
    public static final int DEPOSIT_LOCK_NUM_EXT = 4;   // 存款锁定日次数-1

    public static final int DEPOSIT_TIPS_NUM = 4;       // 已存款弹窗日次数
    public static final int DEPOSIT_TIPS_NUM_EXT = 3;   // 已存款弹窗日次数-1

    public interface EVNumber {
        int zero = 0;
        int one = 1;
        int two = 2;
        int three = 3;
        int four = 4;
        int five = 5;
        int six = 6;
        int seven = 7;
        int eight = 8;
        int nine = 9;
        int ten = 10;
        int eleven = 11;
        int twelve = 12;
        int thirteen = 13;
        int fourteen = 14;
        int sixteen = 16;
        int seventeen = 17;
        int eighteen = 18;
        int nineteen = 19;
    }

    /**
     * 所有记录的的状态
     */
    public interface Available {
        byte disable = 0;
        byte enable = 1;
        byte disableTwo = 2;
        byte disableThr = 3;
    }
    /**
     * 记录是否删除
     */
    public interface Isdelete {
        byte disable = 1;
        byte enable = 0;

    }
    public interface TransferType {
        Integer out = 0;//钱包转平台
        Integer into = 1;//平台转钱包
    }


    public interface ChineseStatus {
        String succeed = "成功";
        String defeated = "失败";
        String pending = "待处理";
    }

    public interface IsStatus {
        Integer defeated = 0;//失败 or 拒绝
        Integer succeed = 1;//成功 or 通过
        Integer pending = 2;//待处理
        Integer outMoney = 3;//出款中
        Integer four = 4;//待处理
    }

    public interface manageStatus {
        Integer freeze = 0;//冻结
        Integer succeed = 1;//成功
        Integer defeated = 2;//失败
    }

    public interface sourceType {
        String admin = "admin";
        String web = "web";
    }

    public interface status {
        Integer able = 1;
        Integer disable = 0;
    }

    //完整度
    public interface userInfoMeasure {
        Byte userInfoConut = 7;
        Byte zero = 0;
        Byte full = 100;
    }

    public static final Map<Integer, String> depotCatMap = new HashMap<Integer, String>() {{
        put(1, "Sport"); //体育
        put(3, "Live"); //真人
        put(5, "Slot");//电子
        put(8, "Hunter");//捕鱼
        put(12, "Lottery");//彩票
        put(6, "Chess");//棋牌
        put(9, "Esport");   // 电竞
//        put(15, "3dslot");
        put(99, "tip");
        put(100, "activity");
        put(200, "unknown");
    }};
    
    public static final Map<String, Integer> depotCatGameTypeMap = new HashMap<String, Integer>() {{
    	 put("Sport", 1 ); //体育
         put("Live", 3 ); //真人
         put("Slot", 5);//电子
         put("Hunter",8);//捕鱼
         put("Lottery",12);//彩票
         put("Chess",6);//棋牌
         put("Esport",9);   // 电竞
         put( "tip",99);
         put( "activity",100);
         put( "unknown",200);
    }};

    public static final Map<String, String> depotCatStrMap = new HashMap<String, String>() {{
        put("Sport","体育");      // 体育
        put("Live", "真人");      // 真人
        put("Slot","电子");       // 电子
        put("Hunter","捕鱼");     // 捕鱼
        put("Lottery","彩票" );   // 彩票
        put("Chess","棋牌" );     // 棋牌
        put("Esport","电竞");     // 电竞
//        put("3dslot", "");
        put("Tip", "小费");
        put("Activity", "活动");
        put("Unknown", "未知");
    }};

    public static final Map<String, String> activityStaticsRule = new HashMap<String, String>() {{
        // 活动等级周期统计规则 0无限期，1按日，2按周，3按月
        put("0", "无限期");
        put("1", "按日统计");
        put("2", "按周统计");
        put("3", "按月统计");
    }};

    public static final Map<Integer, String> bonusActivityAudit = new HashMap<Integer, String>() {{
       // 0 拒绝 or 未通过 1成功 or 已使用 2待处理 3 可使用 4已失效
        put(0, "拒绝");
        put(1, "通过");
        put(2, "待处理");
        put(3, "可使用");
        put(4, "已失效");
    }};

    public static final Map<Integer, String> accWebRegister = new HashMap<Integer, String>() {{
        // 0 拒绝 or 未通过 1成功 or 已使用 2待处理 3 可使用 4已失效
        put(0, "禁用");
        put(1, "启用");
    }};

    public interface dayMonthYear {
        String year = "%tY";
        String month = "%tm";
        String day = "%td";
    }

    public interface i18nCode {
        String ZH = "ZH";   // 汉字
        String EN = "EN";   // 英语
        String ID = "ID";   // 印尼语
    }

    public final static String TYPE_ERC20 = "ERC20";
    public final static String TYPE_TRC20 = "TRC20";

    public final static String TYPE_ERC = "ERC";
    public final static String TYPE_TRC = "TRC";

    public final static String TYPE_AGENT = "AGENT";
    public final static String TYPE_ACCOUNT = "ACCOUNT";

    public final static String CHECK_IP_FAIL = "10";

    public static final String SITECODE_GOC = "goc";

    public static final String SITECODE_FBD = "fbd";

    // 多语言站点
    public static final String LANGUAGE_VI = "vi";
}