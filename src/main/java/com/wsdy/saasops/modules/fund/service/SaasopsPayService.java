package com.wsdy.saasops.modules.fund.service;

import com.alibaba.fastjson.JSON;
import com.wsdy.saasops.api.constants.PayConstants;
import com.wsdy.saasops.api.modules.pay.dto.*;
import com.wsdy.saasops.api.modules.pay.dto.saaspay.BankpayTradeRequestDto;
import com.wsdy.saasops.api.modules.pay.dto.saaspay.CommonPayResponse;
import com.wsdy.saasops.api.modules.pay.dto.saaspay.PayBankResponse;
import com.wsdy.saasops.api.modules.user.service.OkHttpService;
import com.wsdy.saasops.api.utils.ASCIIUtils;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.api.utils.MD5;
import com.wsdy.saasops.api.utils.OkHttpUtils;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.modules.fund.entity.FundDeposit;
import com.wsdy.saasops.modules.system.pay.entity.SetBacicFastPay;
import com.wsdy.saasops.modules.system.pay.entity.SysDeposit;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static java.util.Objects.isNull;

@Slf4j
@Service
public class SaasopsPayService {

    @Value("${panzi.callback.url}")
    private String panziCallbackUrl;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private OkHttpService okHttpService;

    // 提单
    public DepositPostScript placeOrder(FundDeposit deposit, SysDeposit sysDeposit, String siteCode){
        // 参数处理
        BankpayTradeRequestDto requestDto =  getBankPayParam(deposit, sysDeposit, siteCode);
        // 处理url
        String url = sysDeposit.getPayUrl() + PayConstants.SAASOPS_PAY_PATDO_BANK;
        if(!sysDeposit.getPayUrl().endsWith("/")) {
            url = sysDeposit.getPayUrl() + "/" +PayConstants.SAASOPS_PAY_PATDO_BANK;
        }
        String jsonMessage;
        try {
            log.info("SaasopsPay==outTradeNo==" +requestDto.getOutTradeNo() +"==下单==请求信息==" + jsonUtil.toJson(requestDto));
            String startTime = DateUtil.getCurrentDate(FORMAT_18_DATE_TIME);
            jsonMessage = okHttpService.postForm(okHttpService.getHttpNoProxyClient(),url, jsonUtil.toStringMap(requestDto));
            log.info("SaasopsPay==outTradeNo==" +requestDto.getOutTradeNo() +"==下单==支付开始时间==" + startTime + "==结束时间==" + DateUtil.getCurrentDate(FORMAT_18_DATE_TIME));
            log.info("SaasopsPay==outTradeNo==" +requestDto.getOutTradeNo() +"==下单==返回信息==" + jsonMessage);
        } catch (Exception e) {
            log.error("SaasopsPay==outTradeNo==" +requestDto.getOutTradeNo() +"==下单==支付报错==" + e);
            throw new RRException("该支付维护中,请使用其它支付");
        }
        if (isNull(jsonMessage)) {
            log.info("SaasopsPay==outTradeNo==" +requestDto.getOutTradeNo() +"==下单==返回空");
            throw new RRException("提交支付网关支付返回空");
        }
        Type jsonType = new TypeToken<CommonPayResponse<PayBankResponse>>() {}.getType();
        CommonPayResponse<PayBankResponse> payResponse = jsonUtil.fromJson(jsonMessage, jsonType);
        if(isNull(payResponse)){
            log.info("SaasopsPay==outTradeNo==" +requestDto.getOutTradeNo() +"==下单==提交支付网关支付返回空!");
            throw new RRException("提交支付网关支付返回空!");
        }
        if(payResponse.getCode() != 200){
            log.info("SaasopsPay==outTradeNo==" +requestDto.getOutTradeNo() +"==下单==提交支付网关支付异常!");
            throw new RRException("提交支付网关支付异常");
        }
        PayBankResponse responseData = payResponse.getData();
        if (isNull(responseData)) {
            log.info("SaasopsPay==outTradeNo==" +requestDto.getOutTradeNo() +"==下单==提交支付网关支付返回空!!");
            throw new RRException("提交支付网关支付返回空!!");
        }
        if (!responseData.getSucceed()) {
            log.info("SaasopsPay==outTradeNo==" +requestDto.getOutTradeNo() +"==下单==" + responseData.getError());
            throw new RRException(responseData.getError());
        }

        if(responseData.getUrlMethod() == 0){//返回obj
            DepositPostScript script = jsonUtil.fromJson(responseData.getData(),DepositPostScript.class);
            return script;
        }else if(responseData.getUrlMethod() == 1){//返回自行转换二维码
            DepositPostScript script = new DepositPostScript();
            script.setUrl(responseData.getData());
            script.setUrlMethod(0);
            return script;
        }else if(responseData.getUrlMethod() == 2){//返回html
            DepositPostScript script = new DepositPostScript();
            script.setUrl(responseData.getData());
            script.setUrlMethod(1);
            return script;
        }else if(responseData.getUrlMethod() == 3){//返回url
            DepositPostScript script = new DepositPostScript();
            script.setUrl(responseData.getData());
            script.setUrlMethod(1);
            return script;
        }
        return null;
    }

    private BankpayTradeRequestDto getBankPayParam(FundDeposit deposit, SysDeposit sysDeposit, String siteCode){
        BankpayTradeRequestDto bankDto = new BankpayTradeRequestDto();

        bankDto.setMerchantNo(sysDeposit.getCid());             // 商户号
        bankDto.setEvbBankId(sysDeposit.getEvebBankId());       // 渠道标识
        bankDto.setBankName(sysDeposit.getBankName());          // 入款银行名称
        bankDto.setBankCode(sysDeposit.getBankCode());          // 入款银行bankCode
        bankDto.setReceiveCardNum(sysDeposit.getBankAccount()); // 入款银行的账号
        bankDto.setReceiveUserName(sysDeposit.getRealName());   // 入款银行的姓名
        bankDto.setBankBranch(sysDeposit.getBankBranch());      // 入款银行分行

        bankDto.setOutTradeNo(deposit.getOrderNo());        // 订单号
        bankDto.setAmount(deposit.getDepositAmount());      // 存款金额
        bankDto.setPayUserName(deposit.getDepositUser());   // 存款人姓名
//        bankDto.setPayCardNum(deposit.getDepositUserAcc()); // 存款人卡号
        bankDto.setComment(deposit.getDepositPostscript()); // 附言

        bankDto.setAccountId(deposit.getAccountId());   // 会员ID
        bankDto.setLoginName(deposit.getLoginName());   // 会员名
        bankDto.setTerminal(deposit.getFundSource());   // 支付终端
        bankDto.setIp(deposit.getIp());                 // IP

        bankDto.setReturnParams(siteCode);              // 站点code
        bankDto.setVerifyCode(deposit.getVerifyCode()); // 多语言使用code
        // 处理url
        String callbackUrl = panziCallbackUrl+ PayConstants.SAASOPS_PAY_NOTIFY_URL;
        if(!panziCallbackUrl.endsWith("/")) {
            callbackUrl = panziCallbackUrl + "/" +PayConstants.SAASOPS_PAY_NOTIFY_URL;
        }
        bankDto.setCallbackUrl(callbackUrl);  // 回调url
        // 签名
        String str = MD5.getMD5(ASCIIUtils.getFormatUrl(jsonUtil.Entity2Map(bankDto), sysDeposit.getPassword()));
        bankDto.setSign(str);
        return bankDto;
    }

    // 极速存款提单
    public DepositPostScript fastDepositPlaceOrder(FundDeposit deposit, SetBacicFastPay fastPay, String siteCode){
        // 参数处理
        BankpayTradeRequestDto requestDto = getFastDepositParam(deposit, fastPay, siteCode);
        // 处理url
        String url = fastPay.getPayUrl() + PayConstants.SAASOPS_FASTDEPOSIT_PATDO;
        if(!fastPay.getPayUrl().endsWith("/")) {
            url = fastPay.getPayUrl() + "/" +PayConstants.SAASOPS_FASTDEPOSIT_PATDO;
        }
        String jsonMessage;
        try {
            log.info("SaasopsFastDepositPay==outTradeNo==" +requestDto.getOutTradeNo() +"==下单==请求信息==" + jsonUtil.toJson(requestDto));
            String startTime = DateUtil.getCurrentDate(FORMAT_18_DATE_TIME);
            jsonMessage = okHttpService.postForm(okHttpService.getHttpNoProxyClient(),url, jsonUtil.toStringMap(requestDto));
            log.info("SaasopsFastDepositPay==outTradeNo==" +requestDto.getOutTradeNo() +"==下单==支付开始时间==" + startTime + "==结束时间==" + DateUtil.getCurrentDate(FORMAT_18_DATE_TIME));
            log.info("SaasopsFastDepositPay==outTradeNo==" +requestDto.getOutTradeNo() +"==下单==返回信息==" + jsonMessage);
        } catch (Exception e) {
            log.error("SaasopsFastDepositPay==outTradeNo==" +requestDto.getOutTradeNo() +"==下单==支付报错==" + e);
            throw new RRException("该支付维护中,请使用其它支付");
        }
        if (isNull(jsonMessage)) {
            log.info("SaasopsFastDepositPay==outTradeNo==" +requestDto.getOutTradeNo() +"==下单==返回空");
            throw new RRException("提交支付网关支付返回空");
        }

        DepositPostScript payResponse = JSON.parseObject(jsonMessage, DepositPostScript.class);
        if(isNull(payResponse)){
            log.info("SaasopsFastDepositPay==outTradeNo==" +requestDto.getOutTradeNo() +"==下单==提交支付网关支付返回空!");
            throw new RRException("提交支付网关支付返回空!");
        }
        if(payResponse.getSucceed()){
            log.info("SaasopsFastDepositPay==outTradeNo==" +requestDto.getOutTradeNo() +"==下单==匹配失败=={}", jsonMessage);
            throw new RRException("提交支付网关支付异常");
        }

        return payResponse;
    }

    private BankpayTradeRequestDto getFastDepositParam(FundDeposit deposit, SetBacicFastPay fastPay, String siteCode){
        BankpayTradeRequestDto bankDto = new BankpayTradeRequestDto();

        bankDto.setAmount(deposit.getDepositAmount());      // 存款金额
        //bankDto.setBankCode(sysDeposit.getBankCode());      // 入款银行bankCode
        bankDto.setReturnParams(siteCode);              // 站点code
        // 处理url
        String callbackUrl = panziCallbackUrl+ PayConstants.SAASOPS_PAY_NOTIFY_URL;
        if(!panziCallbackUrl.endsWith("/")) {
            callbackUrl = panziCallbackUrl + "/" +PayConstants.SAASOPS_PAY_NOTIFY_URL;
        }
        bankDto.setCallbackUrl(callbackUrl);  // 回调url
        bankDto.setOutTradeNo(deposit.getOrderNo());        // 订单号
        bankDto.setMerchantNo(fastPay.getCid());             // 商户号
        bankDto.setEvbBankId(fastPay.getEvebBankId());       // 渠道标识
        bankDto.setAccountId(deposit.getAccountId());   // 会员ID
        bankDto.setLoginName(deposit.getLoginName());   // 会员名
        bankDto.setTerminal(deposit.getFundSource());   // 支付终端
        bankDto.setPayUserName(deposit.getDepositUser());   // 存款人姓名

        // 签名
        String str = MD5.getMD5(ASCIIUtils.getFormatUrl(jsonUtil.Entity2Map(bankDto), fastPay.getPassword()));
        bankDto.setSign(str);
        return bankDto;
    }
}
