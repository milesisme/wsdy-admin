package com.wsdy.saasops.common.constants;

import java.util.HashMap;
import java.util.Map;

public class MerchantPayConstants {

    // 代付平台platformCode
    public static final String PAY_PANZI = "PANZI";
    public static final String ONE_PAY = "ONEPAY";
    public static final String PAYMENT_PAY = "PAYMENT";
    public static final String EVELLET_PAY = "evellet";
    public static final String LBT_PAY = "LBT";
    public static final String PAY_CENTER = "SAASOPS_PAY";

    public static final Map<String, String> htMerchantPayMap = new HashMap<String, String>() {{
        put("中国农业银行", "ABC");
        put("中国银行", "BOC");
        put("交通银行", "BOCOM");
        put("中国建设银行", "CCB");
        put("中国工商银行", "ICBC");
        put("中国邮政储蓄银行", "PSBC");
        put("招商银行", "CMBC");
        put("浦发银行", "SPDB");
        put("中国光大银行", "CEBBANK");
        put("中信银行", "ECITIC");
        put("平安银行", "PINGAN");
        put("中国民生银行", "CMBCS");
        put("华夏银行", "HXB");
        put("广发银行", "CGB");
        put("北京银行", "BCCB");
        put("上海银行", "BOS");
        put("兴业银行", "CIB");
    }};

    public static final Map<String, String> pzMerchantPayMap = new HashMap<String, String>() {{
        put("中国工商银行", "ABC");
        put("中国银行", "BOC");
        put("中国建设银行", "CCB");
        put("中国农业银行", "ABC");
        put("交通银行", "BOCOM");
        put("招商银行", "CMB");
        put("民生银行", "CMBC");
        put("平安银行", "PAB");
        put("华夏银行", "HXBC");
        put("中国邮政储蓄银行", "PSBC");
        put("兴业银行", "CIB");
        put("中国光大银行", "CEB");
        put("中信银行", "CITIC");
        put("浦发银行", "SPDB");
    }};

    public static final Map<String,Integer > aoYoMerchantPayMap = new HashMap<String,Integer>(){{
        put("中国银行",4);
        put("中国农业银行",3);
        put("中国工商银行",1);
        put("民生银行",2);
        put("招商银行",12);
        put("兴业银行",13);
        put("交通银行",6);
        put("中信银行",7);
        put("中国光大银行",8);
        put("华夏银行",9);
        put("广发银行",10);
        put("平安银行",11);
        put("渤海银行",87);
        put("北京银行",15);
        put("中国建设银行",5);
        put("南京银行",38);
        put("中国邮政储蓄银行",93);
        put("上海银行",90);
        put("浦发银行",14);
    }};

    public static final Map<String, String> paymentMerchantPayMap = new HashMap<String, String>() {{
        put("中国邮政储蓄银行", "PSBC");
    }};

    public interface paymentStatusCode {
        int zero = 0;   // 未知错误
        int one = 1;    // 成功
        int two = 2;    // 参数错误
        int three = 3;  // 商户号存在
        int four = 4;   // 商户订单号已存在
        int five = 5;   // 验签失败
        int six = 6;    // 订单还未处理
        int seven = 7;  // 支付失败
        int eight = 8;  // 处理中
        int nine = 9;   // 已触发风控，请在商户后台审核后代付
        int ten = 10;   // 余额不足
        int eleven = 11;// 商户已冻结
        int twelve = 12;// 支付通道已关闭
        int thirteen = 13;// 未知异常，请联系客服
        int fourteen = 14;// 请输入正确金额，代付金额100-50000
        int fiveteen = 15;// 短期内请勿重复提交
        int sixteen = 16;   // 商户费率还未设置，请联系客服
        int seventeen = 17; // 商户还未开启
        int eighteen = 18;  // 订单号不存在
        int nineteen = 19;  // ip没在白名单
        int twentyone = 21; // 卡号格式错误
    }
}
