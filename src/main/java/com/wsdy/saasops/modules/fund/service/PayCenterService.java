package com.wsdy.saasops.modules.fund.service;

import com.wsdy.saasops.api.constants.PayConstants;
import com.wsdy.saasops.api.modules.pay.dto.DPaySearchResponseDto;
import com.wsdy.saasops.api.modules.user.service.OkHttpService;
import com.wsdy.saasops.api.utils.ASCIIUtils;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.api.utils.MD5;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.modules.base.entity.BaseBank;
import com.wsdy.saasops.modules.base.service.BaseBankService;
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
public class PayCenterService {
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private OkHttpService okHttpService;
    @Autowired
    private BaseBankService baseBankService;
    @Autowired
    private MbrAccountMapper accountMapper;


    @Value("${panzi.callback.url}")
    public String callbackUrl;

    public String  debitPayment(MbrBankcard bankcard, AccWithdraw withDraw, FundMerchantPay merchantPay){
        Map<String, String> paramsMap = new HashMap<>();
        // 发送代付申请
        String webSite = merchantPay.getUrl();
        if(!webSite.endsWith("/")) {
            webSite+="/";
        }
        getSign(bankcard, withDraw,merchantPay, paramsMap);

        String url = webSite+ PayConstants.SAASOPS_DPAY_PATDO;
        String startTime = DateUtil.getCurrentDate(FORMAT_18_DATE_TIME);
        log.info("SAASOPS_PAY==支付中心代付==outTradeNo==" + withDraw.getOrderNo() + "==下单==参数【" + jsonUtil.toJson(paramsMap) + "】");
        String result = okHttpService.postForm(okHttpService.getHttpNoProxyClient(), url, paramsMap);
        log.info("SAASOPS_PAY==支付中心代付==outTradeNo==" + withDraw.getOrderNo() + "==下单==返回数据信息【" + result + "】" +"==开始时间==" + startTime + "==结束时间==" + DateUtil.getCurrentDate(FORMAT_18_DATE_TIME));

        return result;

    }

    // 查询订单状态
    public String querySubmitSuccess(AccWithdraw as, FundMerchantPay merchantPay){
        // 处理请求参数
        Map<String, Object> paramsMap = getQuerySign(merchantPay,as);
        log.info("SAASOPS_PAY==支付中心代付==outTradeNo==" +as.getOrderNo() +"==查询==请求参数==" + jsonUtil.toJson(paramsMap));

        String webSite = merchantPay.getUrl();
        if(!webSite.endsWith("/")) {
            webSite+="/";
        }
        String url = webSite+ PayConstants.SAASOPS_DPAY_QUERY;
        try {
            log.info("SAASOPS_PAY==支付中心代付==outTradeNo==" + as.getOrderNo() + "==查询==参数【" + jsonUtil.toJson(paramsMap) + "】");
            String result = okHttpService.postJson(okHttpService.getHttpNoProxyClient(), url, paramsMap, null);
            log.info("SAASOPS_PAY==支付中心代付==outTradeNo==" + as.getOrderNo() + "==查询==返回数据信息【" + result + "】");
            return result;
        } catch (Exception e) {
            log.error("SAASOPS_PAY==支付中心代付==outTradeNo==" + as.getOrderNo() + "==查询==异常==e==", e);
            return null;
        }
    }

    public boolean checkSign(DPaySearchResponseDto data, String merchantKey){
         Map<String, Object> paramsMap  = jsonUtil.Entity2Map(data);
         String sign = paramsMap.remove("sign").toString();
         paramsMap.remove("returnParams");
         String str = ASCIIUtils.getFormatUrl(paramsMap, merchantKey);
         String newSign= MD5.getMD5(str); // 签名
        return newSign.equals(sign);
    }


    /**
     * 查询计算签名
     */
    private Map<String, Object> getQuerySign(FundMerchantPay merchantPay, AccWithdraw as){
        Map<String, Object> params = new HashMap<>();
        params.put("orderno",as.getOrderNo());             // 商户订单号: v2

        String urlParams = ASCIIUtils.formatUrlMap(params, false, false);
        String beforeSign = urlParams + "&key=" + merchantPay.getMerchantKey();
        log.info("SAASOPS_PAY==支付中心代付==outTradeNo==" +as.getOrderNo() +"==查询==beforeSign==" + beforeSign);
        String sign = MD5.getMD5(beforeSign);
        log.info("SAASOPS_PAY==支付中心代付==outTradeNo==" +as.getOrderNo() +"==查询==sign==" + sign);
        params.put("sign", sign);

        return params;
    }


    private void getSign(MbrBankcard bankcard, AccWithdraw withDraw, FundMerchantPay merchantPay, Map<String, String> params){
        params.put("outTradeNo",withDraw.getOrderNo());                                        // 商户订单号: 使用v2订单号
        params.put("accountId",withDraw.getAccountId().toString());                                     // 会员id
        params.put("merchantNo", merchantPay.getMerchantNo());
        params.put("ip", withDraw.getIp());
        params.put("receiveAccount", bankcard.getCardNo());
        params.put("receiveName", bankcard.getRealName());
        params.put("bankName", bankcard.getBankName());
        params.put("bankBranch", bankcard.getAddress());
        params.put("bankProvince", bankcard.getProvince());
        params.put("bankCity", bankcard.getCity());
        params.put("returnParams", withDraw.getSiteCode());
        BigDecimal fee = withDraw.getActualArrival();
        params.put("amount", fee.toString());                                                          // 调整后的充值金额

        MbrAccount mbr = accountMapper.selectByPrimaryKey(withDraw.getAccountId());
        params.put("loginName", mbr.getLoginName());
        if(merchantPay.getMethodType() == Constants.EVNumber.zero){
            params.put("paymentCode","BANKCRAD_1");
        }else if(merchantPay.getMethodType() == Constants.EVNumber.two){
            params.put("paymentCode", "ALIPAY");
        }else if(merchantPay.getMethodType() == Constants.EVNumber.three){
            params.put("paymentCode", "BANKCRAD_1");
        }
        if(bankcard.getBankCardId() != null){
            BaseBank baseBank = baseBankService.queryObject(bankcard.getBankCardId());
            if(baseBank!=null){
                params.put("bankCode", baseBank.getBankCode());
            }
        }

        // 处理回调url
        String notifyUrl = "";
        if(!callbackUrl.endsWith("/")) {
            notifyUrl = callbackUrl + "/" + PayConstants.SAASOPS_DPAY_NOTIFY_URL;
        }else{
            notifyUrl = callbackUrl  + PayConstants.SAASOPS_DPAY_NOTIFY_URL;
        }

        params.put("callbackUrl",notifyUrl);                                              // 支付结果后台回调URL
        String urlParams = ASCIIUtils.formatUrlMap2(params, false, false);
        urlParams = urlParams + "&key=" + merchantPay.getMerchantKey();
        log.info("SAASOPS_PAY代付==outTradeNo==" + withDraw.getOrderNo() +"==下单==urlParams==" + urlParams);
        String sign = MD5.getMD5(urlParams);
        log.info("SAASOPS_PAY代付==outTradeNo==" + withDraw.getOrderNo() +"==下单==sign==" + sign);
        params.put("sign",sign);
    }


}
