package com.wsdy.saasops.api.constants;


public class PayConstants {

    public static final String TEST_PANZI = "test";

    //盘子支付code
    public static final String PAY_PANZI = "PANZI";

    //盘子支付code
    public static final String PAY_ZHIFU = "ZHIFU";

    public static final String PAY_PANZI_PATDO = "recharge/pay.do";
    public static final String PAY_PANZI_QUERY = "recharge/query.do";

    public static final String PAY_PANZI_TYPE_QR = "QR";
    public static final String PAY_PANZI_TYPE_HTML = "HTML";

    public static final String PAY_SUCCESS = "success";
    public static final String PANZI_NOTIFY_URL = "/api/OnlinePay/pzPay/paiZiCallback";

    public static final String DSDFPAY_CODE = "DSDFPAY";
    public static final String TONGLUEYUN_CODE = "TONGLUEYUN";
    public static final String MAXPAY_CODE = "MAXPAY";

    public static final String MAXPAY_PATDO = "pay";
    public static final String MAXPAY_QUERY = "pay_search";
    public static final String MAXPAY_NOTIFY_URL = "/api/OnlinePay/pzPay/maxpayCallback";

    public static final String TONGLUEYUN_PAYURL = "dsdf/customer_pay/init_din";
    public static final String TONGLUEYUN_LISTURL = "dsdf/api/query_order";

    public static final String ONEPAY_PAYURL = "/api/callback/onePayCallback/";
//    public static final String TONGLUEYUN_LISTURL = "dsdf/api/query_order";
    // Payment代付
    public static final String PAYMENT_PAYURL = "api/callback/paymentCallback/";
    // LBT
    public static final String LBT_PAYURL = "api/callback/lbtCallback/";




    //BTP支付code
    public static final String BTPPAY_CODE = "BTPPAY";
    public static final String PAY_BTP_PATDO = "recharge/transfer.do";  //pay.do
    public static final String PAY_BTP_PAYDO = "recharge/pay.do";  //pay.do
    public static final String PAY_BTP_QUERY = "recharge/query.do";
    public static final String PAY_BTP_NOTIFY_URL = "/api/callback/btppayCallback";

    //财付宝支付
    public static final String CFBPAY_CODE = "CAIFUBAO";
    public static final String PAY_CFB_PATDO = "api/v1/pay_qrcode.api";  //pay.do
    public static final String PAY_CFB_QUERY = "api/v1/query_record.api";
    public static final String PAY_CFB_NOTIFY_URL = "api/callback/cfbpayCallback";
    public static final String CFB_SIGN_STR = "mcnNum=$mcnNum&orderId=$orderId&backUrl=$backUrl&payType=$payType&amount=$amount&secreyKey=$secreyKey";
    public static final String CFB_QUERY_SIGN_STR = "mcnNum=$mcnNum&orderId=$orderId&secreyKey=$secreyKey";

    //东东支付
    public static final String DONGDONGPAY_CODE = "DONGDONG";
    public static final String PAY_DONGDONG_PATDO = "api/pay";  //pay.do
    public static final String PAY_DONGDONG_QUERY = "api/orderquery";
    public static final String PAY_DONGDONG_NOTIFY_URL = "api/callback/dongdongPayCallback";

    //网关支付代收
    public static final String SAASOPS_PAY_CODE = "SAASOPS_PAY";
    public static final String JUHE_PAY_CODE = "JUHE_PAY";
    public static final String SAASOPS_PAY_PATDO = "poapi/pay/trade_order";  //pay.do
    public static final String SAASOPS_PAY_PATDO_BANK = "poapi/pay/trade_order_bank";
    public static final String SAASOPS_PAY_QUERY = "poapi/pay/pay_search";
    public static final String SAASOPS_PAY_NOTIFY_URL = "api/callback/saasopsPayCallback";
    public static final String SAASOPS_THIRDPARTY_RECHARGECHATLINK_URL = "poapi/pay/getThirdPartyRechargeChatLink";
    public static final String JUHE_URL = "JUHE_PAY";
    public static final String UXIANG_PAY_CODE = "UXIANG_PAY";


    //极速存款下单地址
    public static final String SAASOPS_FASTDEPOSIT_PATDO = "poapi/pay/fast_trade_order";

    //网关代付
    public static final String SAASOPS_DPAY_CODE = "SAASOPS_PAY";
    public static final String SAASOPS_DPAY_PATDO = "poapi/dpay/trade_order";
    public static final String SAASOPS_DPAY_QUERY = "poapi/dpay/pay_search";
    public static final String SAASOPS_DPAY_NOTIFY_URL = "api/callback/saasopsDPayCallback";
    //极速取款下单地址
    public static final String SAASOPS_FASTWITHDRAW_DPAY_PATDO = "poapi/dpay/fast_trade_order";

    // 加密货币 evellet 入款
    public static final String SAASOPS_PAY_EVELLET = "ellevet";
    public static final String SAASOPS_PAY_EVELLET_EXCHANGE_RATE = "api/exchange_rate";
    public static final String SAASOPS_PAY_EVELLET_PATDO  = "api/trade_order";
    public static final String SAASOPS_PAY_EVELLET_QUERY = "";
    public static final String SAASOPS_PAY_EVELLET_REMINDER = "api/memberReminder";
    public static final String SAASOPS_PAY_EVELLET_QUERY_ADDRESS = "api/queryAddress";
    // 加密货币 evellet 出款
    public static final String SAASOPS_PAY_EVELLET_TRANSFER = "api/transfer";
    public static final String SAASOPS_PAY_EVELLET_NOTIFY_URL = "api/callback/evelletTransferCallback";
    public static final String SAASOPS_PAY_EVELLET_TRANSFER_QUERY= "api/transfer_search";

    // SPTV
    public static final String SPTV_CREATE_USER = "splive/app/user/createUser";

}
