package com.wsdy.saasops.modules.fund.service;

import com.wsdy.saasops.api.constants.PayConstants;
import com.wsdy.saasops.api.modules.user.service.OkHttpService;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.utils.pay.PaymentPaySignUtil;
import com.wsdy.saasops.modules.fund.dto.PaymentPayRequestDto;
import com.wsdy.saasops.modules.fund.entity.FundMerchantPay;
import com.wsdy.saasops.modules.member.entity.MbrBankcard;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;

@Slf4j
@Service
public class PaymentPayService {

    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private OkHttpService okHttpService;


    @Value("${panzi.callback.url}")
    public String callbackUrl;     //所有回调地址是一个

    public static final String debit_payment = "merchantApi/execute";     // 代付接口
    public static final String debit_query = "merchantApi/query";           // 查询接口
    public static final String debit_balance = "merchantApi/queryBalance"; // 余额查询


    // 代付提单
    public String debitPayment(String orderNo, MbrBankcard bankcard,
                                          BigDecimal totalFee, FundMerchantPay merchantPay,String siteCode) {
        PaymentPayRequestDto paymentPayRequestDto = new PaymentPayRequestDto();
        paymentPayRequestDto.setMerchantNumber(merchantPay.getMerchantNo());    // 商户号
        paymentPayRequestDto.setMerchantOrderNumber(orderNo);                   // 订单号
        paymentPayRequestDto.setReceiveName(bankcard.getRealName());            // 帐户名
        paymentPayRequestDto.setReceiveCard(bankcard.getCardNo());              // 银行卡号
        paymentPayRequestDto.setAmount(totalFee);                               // 转账金额

        if(!callbackUrl.endsWith("/")) {
            callbackUrl+="/";
        }
        paymentPayRequestDto.setCallBackUrl(callbackUrl+ PayConstants.PAYMENT_PAYURL+siteCode); // 回调地址

        try{
            Map<String, String>  paramsMap = jsonUtil.toStringMap(paymentPayRequestDto);
            paramsMap.put("sign", PaymentPaySignUtil.sign(merchantPay.getMerchantKey().getBytes("UTF-8"), PaymentPaySignUtil.createLinkString(paramsMap)));
            // 发送代付申请
            String webSite = merchantPay.getUrl();
            if(!webSite.endsWith("/")) {
                webSite+="/";
            }
            String url = webSite+ debit_payment;
            log.info("Payment代付==outTradeNo==" + orderNo + "==提单==参数【" + jsonUtil.toJson(paramsMap) + "】");
            String result = okHttpService.postJson(okHttpService.getPayHttpsClient(), url, paramsMap, null);
            log.info("Payment代付==outTradeNo==" + orderNo + "==提单==返回数据信息【" + result + "】");

            return result;
        }catch (Exception e){
            log.error("Payment代付==outTradeNo==" + orderNo + "==提单异常==e==", e);
            return null;
        }
    }

    // 查询订单状态
    public String querySubmitSuccess(String orderNo, FundMerchantPay merchantPay){
        String webSite = merchantPay.getUrl();
        if(!webSite.endsWith("/")) {
            webSite+="/";
        }
        String url = webSite+ debit_query;
        try {
            Map<String,String> paramsMap = new HashMap<>();
            paramsMap.put("merchantNumber",merchantPay.getMerchantNo() );   // 商户号
            paramsMap.put("merchantOrderNumber",orderNo );                  // 商户订单号
            String sign = PaymentPaySignUtil.sign(merchantPay.getMerchantKey().getBytes("UTF-8"), PaymentPaySignUtil.createLinkString(paramsMap));
            paramsMap.put("sign",sign );
            log.info("Payment代付==outTradeNo==" + orderNo + "==订单查询==参数【" + jsonUtil.toJson(paramsMap) + "】");
            String result = okHttpService.postJson(okHttpService.getPayHttpsClient(), url, paramsMap, null);
            log.info("Payment代付==outTradeNo==" + orderNo + "==订单查询==返回数据信息【" + result + "】");
            return result;
        } catch (Exception e) {
            log.error("Payment代付==outTradeNo==" + orderNo + "==订单查询异常==e==", e);
            return null;
        }
    }

    // 查询余额
    public String balance(FundMerchantPay merchantPay) {
        if (isNull(merchantPay)) {
            return null;
        }
        String webSite = merchantPay.getUrl();
        if(!webSite.endsWith("/")) {
            webSite+="/";
        }
        String url = webSite+ debit_balance;
        try {
            Map<String,String> paramsMap = new HashMap<>();
            paramsMap.put("merchantNumber",merchantPay.getMerchantNo() );  // 商户号
            String sign = PaymentPaySignUtil.sign(merchantPay.getMerchantKey().getBytes("UTF-8"), PaymentPaySignUtil.createLinkString(paramsMap));
            paramsMap.put("sign",sign );
            log.info("Payment代付==merchantNumber==" + merchantPay.getMerchantNo() + "==余额查询==参数【" + jsonUtil.toJson(paramsMap) + "】");
            String result = okHttpService.postJson(okHttpService.getPayHttpsClient(), url, paramsMap, null);
            log.info("Payment代付==merchantNumber==" + merchantPay.getMerchantNo() + "==余额查询==返回数据信息【" + result + "】");
            return result;
        } catch (Exception e) {
            log.error("Payment代付==merchantNumber==" + merchantPay.getMerchantNo() + "==余额查询==异常==e==", e);
            return null;
        }
    }
}
