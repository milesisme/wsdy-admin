package com.wsdy.saasops.modules.fund.service;

import com.wsdy.saasops.api.constants.PayConstants;
import com.wsdy.saasops.api.modules.user.service.OkHttpService;
import com.wsdy.saasops.api.utils.ASCIIUtils;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.api.utils.MD5;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.common.utils.pay.PaymentPaySignUtil;
import com.wsdy.saasops.modules.fund.dto.LBTCallbackReqDto;
import com.wsdy.saasops.modules.fund.dto.PaymentPayRequestDto;
import com.wsdy.saasops.modules.fund.entity.AccWithdraw;
import com.wsdy.saasops.modules.fund.entity.FundMerchantPay;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrBankcard;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;

@Slf4j
@Service
public class LBTPayService {

    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private OkHttpService okHttpService;
    @Autowired
    private MbrAccountMapper accountMapper;

    @Value("${panzi.callback.url}")
    public String callbackUrl;     //所有回调地址是一个

    public static final String debit_payment = "Api/SetTransaction";     // 代付接口
    public static final String debit_query = "APi/QueryTransaction";     // 查询接口


    // 代付提单
    public String debitPayment(MbrBankcard bankcard, AccWithdraw withDraw, FundMerchantPay merchantPay) {
        String siteCode = CommonUtil.getSiteCode();
        // 处理请求参数
        Map<String, Object> paramsMap = new HashMap<>();
        getSign(bankcard, withDraw,merchantPay,siteCode, paramsMap);

        try{
            // 发送代付申请
            String webSite = merchantPay.getUrl();
            if(!webSite.endsWith("/")) {
                webSite+="/";
            }
            String url = webSite+ debit_payment;
            String startTime = DateUtil.getCurrentDate(FORMAT_18_DATE_TIME);
            log.info("LBT代付==outTradeNo==" + withDraw.getOrderNo() + "==下单==参数【" + jsonUtil.toJson(paramsMap) + "】");
            String result = okHttpService.postJson(okHttpService.getPayHttpsClient(), url, paramsMap, null);
            log.info("LBT代付==outTradeNo==" + withDraw.getOrderNo() + "==下单==返回数据信息【" + result + "】" +"==开始时间==" + startTime + "==结束时间==" + DateUtil.getCurrentDate(FORMAT_18_DATE_TIME));

            return result;
        }catch (Exception e){
            log.error("LBT代付==outTradeNo==" + withDraw.getOrderNo() + "==下单==异常==e==", e);
            return null;
        }
    }

    // 查询订单状态
    public String querySubmitSuccess(AccWithdraw as, FundMerchantPay merchantPay){
        // 处理请求参数
        Map<String, Object> paramsMap = getQuerySign(merchantPay,as);
        log.info("LBT代付==outTradeNo==" +as.getOrderNo() +"==查询==请求参数==" + jsonUtil.toJson(paramsMap));

        String webSite = merchantPay.getUrl();
        if(!webSite.endsWith("/")) {
            webSite+="/";
        }
        String url = webSite+ debit_query;
        try {
            log.info("LBT代付==outTradeNo==" + as.getOrderNo() + "==查询==参数【" + jsonUtil.toJson(paramsMap) + "】");
            String result = okHttpService.postJson(okHttpService.getPayHttpsClient(), url, paramsMap, null);
            log.info("LBT代付==outTradeNo==" + as.getOrderNo() + "==查询==返回数据信息【" + result + "】");
            return result;
        } catch (Exception e) {
            log.error("LBT代付==outTradeNo==" + as.getOrderNo() + "==查询==异常==e==", e);
            return null;
        }
    }

    // 查询余额
    public String balance(FundMerchantPay merchantPay) {
        return null;
    }

    /**
     * 查询计算签名
     */
    private Map<String, Object> getQuerySign(FundMerchantPay merchantPay, AccWithdraw as){
        Map<String, Object> params = new HashMap<>();
        params.put("orderno",as.getOrderNo());             // 商户订单号: v2

        String urlParams = ASCIIUtils.formatUrlMap(params, false, false);
        String beforeSign = urlParams + "&key=" + merchantPay.getMerchantKey();
        log.info("LBT代付==outTradeNo==" +as.getOrderNo() +"==查询==beforeSign==" + beforeSign);
        String sign = MD5.getMD5(beforeSign);
        log.info("LBT代付==outTradeNo==" +as.getOrderNo() +"==查询==sign==" + sign);
        params.put("sign", sign);

        return params;
    }

    /**
     * 回调校验签名。
     */
    public Boolean checkSign(LBTCallbackReqDto callbackDto,FundMerchantPay merchantPay){
        Map<String, Object> params = jsonUtil.Entity2Map(callbackDto);
        params.remove("sign");
        String urlParams = ASCIIUtils.formatUrlMap(params, false, false);
        urlParams = urlParams + "&key=" + merchantPay.getMerchantKey();
        log.info("LBT代付==outTradeNo==" +callbackDto.getOrderno() +"==回调==checkSign--urlParams==" + urlParams);
        String sign = MD5.getMD5(urlParams);
        log.info("LBT代付==outTradeNo==" +callbackDto.getOrderno() +"==回调==checkSign--sign===" + sign);
        return sign.equals(callbackDto.getSign());
    }

    /**
     *  获取LBT请求参数，包括签名
     *  签名串string to signksort parameters, md5(urldecode(http_build_query(fields))&key=key)
     * @param params
     */
    private void getSign(MbrBankcard bankcard, AccWithdraw withDraw, FundMerchantPay merchantPay,String siteCode, Map<String, Object> params){
        MbrAccount mbr = accountMapper.selectByPrimaryKey(withDraw.getAccountId());
        params.put("orderno",withDraw.getOrderNo());                                        // 商户订单号: 使用v2订单号
        params.put("type","2");                                                             // 1: Deposit 2: Withdrawal
        params.put("membercode",mbr.getLoginName());                                        // 会员名
        params.put("memberid",withDraw.getAccountId());                                     // 会员id
        params.put("fullname",bankcard.getRealName());                                      // 取款人姓名 会员真实姓名
//        params.put("userallbankaccno",payParams.getPayCardNum());                         // 存款人银行卡号 取款不送
        if ("支付宝".equals(bankcard.getBankName())) {
            params.put("tobankname","ALIPAY");                                              // 取款银行名称
        } else {
            params.put("tobankname",bankcard.getBankName());                                // 取款银行名称
        }
        params.put("tobankacc",bankcard.getCardNo());                                       // 取款银行账号
        BigDecimal fee = withDraw.getActualArrival();
        params.put("adjustamount", CommonUtil.adjustScale(new BigDecimal(0.00)));      // 调整的金额
        params.put("grossamount",  fee);                                                    // 调整前的充值的金额
        params.put("amount", fee);                                                          // 调整后的充值金额
        params.put("cryptoamount",CommonUtil.adjustScale(new BigDecimal(0.00)));       // USDT金额 人民币存款时为0

        params.put("createip",withDraw.getIp());                                            // ip
        params.put("received",  DateUtil.getCurrentDate(FORMAT_18_DATE_TIME));              // 时间
        //params.put("verifycode", withDraw.getVerifyCode()==null?"":withDraw.getVerifyCode());   // 多语言增加字段
        // 处理回调url
        String notifyUrl = "";
        if(!callbackUrl.endsWith("/")) {
            notifyUrl = callbackUrl + "/" + PayConstants.LBT_PAYURL+siteCode;
        }else{
            notifyUrl = callbackUrl  + PayConstants.LBT_PAYURL+siteCode;
        }

        params.put("notifyurl",notifyUrl);                                              // 支付结果后台回调URL
        String urlParams = ASCIIUtils.formatUrlMap(params, false, false);
        urlParams = urlParams + "&key=" + merchantPay.getMerchantKey();
        log.info("LBT代付==outTradeNo==" + withDraw.getOrderNo() +"==下单==urlParams==" + urlParams);
        String sign = MD5.getMD5(urlParams);
        log.info("LBT代付==outTradeNo==" + withDraw.getOrderNo() +"==下单==sign==" + sign);
        params.put("sign",sign);
    }
}
