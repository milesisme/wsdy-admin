package com.wsdy.saasops.api.modules.pay.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wsdy.saasops.api.constants.PayConstants;
import com.wsdy.saasops.api.modules.pay.dto.*;
import com.wsdy.saasops.api.modules.pay.dto.saaspay.*;
import com.wsdy.saasops.api.modules.user.service.ApiUserService;
import com.wsdy.saasops.api.modules.user.service.OkHttpService;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.api.utils.ASCIIUtils;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.api.utils.MD5;
import com.wsdy.saasops.api.utils.OkHttpUtils;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.listener.BizEvent;
import com.wsdy.saasops.listener.BizEventType;
import com.wsdy.saasops.modules.activity.service.FirstChargeOprService;
import com.wsdy.saasops.modules.agent.dao.AgentDepositMapper;
import com.wsdy.saasops.modules.agent.entity.AgentDeposit;
import com.wsdy.saasops.modules.fund.dao.FundDepositMapper;
import com.wsdy.saasops.modules.fund.entity.FundDeposit;
import com.wsdy.saasops.modules.fund.entity.FundDeposit.Mark;
import com.wsdy.saasops.modules.fund.entity.FundDeposit.Status;
import com.wsdy.saasops.modules.fund.mapper.FundMapper;
import com.wsdy.saasops.modules.fund.service.FundDepositService;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.dao.MbrDepositCountMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrDepositCond;
import com.wsdy.saasops.modules.member.entity.MbrDepositCount;
import com.wsdy.saasops.modules.member.service.MbrAccountService;
import com.wsdy.saasops.modules.member.service.MbrDepositCondService;
import com.wsdy.saasops.modules.member.service.MbrDepositLockLogService;
import com.wsdy.saasops.modules.member.service.MbrVerifyService;
import com.wsdy.saasops.modules.system.pay.entity.SetBacicOnlinepay;
import com.wsdy.saasops.modules.system.pay.entity.SysDeposit;
import com.wsdy.saasops.modules.system.pay.mapper.PayMapper;
import com.google.gson.reflect.TypeToken;
import com.wsdy.saasops.mt.service.MTDataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.wsdy.saasops.common.constants.Constants.ONE_HUNDRED;
import static com.wsdy.saasops.common.constants.Constants.SYSTEM_USER;
import static com.wsdy.saasops.common.utils.DateUtil.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;


@Slf4j
@Service
@Transactional
public class PaymentService {

    @Autowired
    private OkHttpService okHttpService;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private FundDepositMapper fundDepositMapper;
    @Autowired
    private FundMapper fundMapper;
    @Autowired
    private MbrAccountMapper mbrAccountMapper;
    @Autowired
    private MbrDepositCondService mbrDepositCondService;
    @Autowired
    private FundDepositService fundDepositService;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private PayMapper payMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private PayInfoService payInfoService;
    @Autowired
    private MbrDepositCountMapper mbrDepositCountMapper;
    @Autowired
    private MbrAccountService mbrAccountService;
    @Value("${panzi.callback.url}")
    private String panziCallbackUrl;
    @Autowired
    private AgentDepositMapper agentDepositMapper;
    @Autowired
    private MbrVerifyService verifyService;
    @Autowired
    private MbrDepositLockLogService mbrDepositLockLogService;
    @Autowired
    private MTDataService mtDataService;
    @Autowired
    private FirstChargeOprService firstChargeOprService;
    @Autowired
    private ApiUserService apiUserService;


    public PayResponseDto dispatchPayment(PayParams params) {

        // ????????????????????????
        SetBacicOnlinepay onlinepay = payMapper.findOnlinePayAndPay(params.getOnlinePayId());
        String code = onlinepay.getCode();
        if (PayConstants.JUHE_URL.equals(code)) {
            // ????????????
            PayResponseDto result = null;
            try {
                result = getThirdPartyRechargeChatLink(params);
            }catch (Exception e) {
                result.setStatus(Boolean.FALSE);
                result.setErrMsg(e.getMessage());
            }
            return result;
        }else {
            return optionPayment(params);
        }

    }

    private PayResponseDto getThirdPartyRechargeChatLink(PayParams params) {
        PayResponseDto result = new PayResponseDto();

        // ?????????????????????
        payInfoService.checkoutOnlinePay(params);

        // ????????????
        // MbrAccount account = mbrAccountMapper.selectByPrimaryKey(params.getAccountId());
        // ??????????????????
        // MbrDepositCount count = payInfoService.checkDepositCount(account, Constants.EVNumber.zero);

        // ????????????????????????
        SetBacicOnlinepay onlinepay = payMapper.findOnlinePayAndPay(params.getOnlinePayId());

        // ???????????????????????????????????????????????????
       /*
        boolean lock = mbrDepositLockLogService.onlinepayUnpayLock(account, onlinepay.getPayId());
        if (lock) {
            throw new R200Exception("???????????????????????????????????????.???");
        }
        */

        // ?????????????????????????????????????????????????????????????????????url

        // ?????????????????????????????????????????????????????????
        String url = onlinepay.getPayUrl() + PayConstants.SAASOPS_THIRDPARTY_RECHARGECHATLINK_URL;
        if (!onlinepay.getPayUrl().endsWith("/")) {
            url = onlinepay.getPayUrl() + "/" + PayConstants.SAASOPS_THIRDPARTY_RECHARGECHATLINK_URL;
        }
        String jsonMessage = null;
        try {
            log.info("UxiangPay==?????????????????????????????????????????????????????????==");

            Long startTime = System.currentTimeMillis();
            HashMap<String, String> paramsMap = new HashMap<>();
            /*
            // app_id	????????????ID	cfltmPEvYrRotXCs2LE4qFvJ
            paramsMap.put("app_id", onlinepay.getMerNo());
            // out_uid	????????????ID	12345678
            paramsMap.put("out_uid", params.getAccountId()+"");
            // platform_product_ids	????????????ID ?????? ????????????	????????????????????????????????? 1,2,3,4,5(1?????? 3?????? 4?????? 5??????)
            paramsMap.put("platform_product_ids", "1");
            // user_recharge_amount	??????????????????/???????????? ????????????????????????	500.00
            paramsMap.put("user_recharge_amount", params.getFee().toString());
            // return_type	????????????????????????	????????? 1
            paramsMap.put("return_type", "1");
            // device	???????????? Int ??????	1. IOS 2.Android 3.??????
            // params.getTerminal(): 0??????PC???,1????????????
            paramsMap.put("device", (0 == params.getTerminal()) ? "3" : "1");
            // timestamp	????????????10????????????	1592544604
            paramsMap.put("timestamp", System.currentTimeMillis()+"");

            // bankId
            paramsMap.put("evbBankId", onlinepay.getBankId()+"");

            // orderParams, ??????????????????????????????????????????????????????
            paramsMap.put("orderParams", JSON.toJSONString(params));
            */
            // fixme ??????????????????????????????????????????????????????????????????????????????????????????pay????????????
            paramsMap.put("orderParams", JSON.toJSONString(params));
            paramsMap.put("onlinePay", JSON.toJSONString(onlinepay));

            jsonMessage = OkHttpUtils.postGateWayJson(url, paramsMap, null);
            Long endTime = System.currentTimeMillis();
            log.info("UxiangPay==????????????????????????==????????????{}?????????????????????:{}",jsonMessage, endTime-startTime);
        } catch (Exception e) {
            log.error("UxiangPay==????????????????????????==?????????", e);
            throw new R200Exception("??????????????????,?????????????????????");
        }

        if (StringUtils.isBlank(jsonMessage)) {
            log.info("UxiangPay==?????????????????????????????????==");
            throw new R200Exception("?????????????????????????????????");
        }

        JSONObject jsonObject = JSON.parseObject(jsonMessage);
        Integer code = jsonObject.getIntValue("code");
        // ??????code?????????200????????????
        if (!"200".equals(String.valueOf(code))) {
            log.info("UxiangPay==??????????????????????????????==??????????????????{}", jsonObject.get("msg"));
            throw new R200Exception(jsonObject.getString("msg"));
        }

        // ????????????url
        String payUrl = jsonObject.getString("payUrl");
        result.setUrl(payUrl);
        result.setUrlMethod(0);

        return result;
    }

    public PayResponseDto optionPayment(PayParams params) {
        // ?????????????????????
        payInfoService.checkoutOnlinePay(params);

        // ??????????????????
        // ????????????
        MbrAccount account = mbrAccountMapper.selectByPrimaryKey(params.getAccountId());
        MbrDepositCount count = payInfoService.checkDepositCount(account, Constants.EVNumber.zero);

        // ??????deposit??????
        FundDeposit deposit = saveFundDespoit(params);
        // ????????????????????????
        SetBacicOnlinepay onlinepay = payMapper.findOnlinePayAndPay(params.getOnlinePayId());

        // ???????????????????????????????????????????????????
        boolean lock = mbrDepositLockLogService.onlinepayUnpayLock(account, onlinepay.getPayId());
        if (lock) {
            throw new R200Exception("???????????????????????????????????????.???");
        }

        if (PayConstants.SAASOPS_PAY_CODE.equals(onlinepay.getPlatfromCode())) {
            // ??????
            if(StringUtil.isEmpty(params.getUserName())){
                params.setUserName(deposit.getDepositUser());
            }
            PayResponseDto ret = commonPay(params, onlinepay, deposit, null);
            // ??????????????????????????????????????????
            mbrDepositCountMapper.updateCount(count);
            // ??????????????????????????????
            if (count.getIsUpdateDepositLock()) {
                MbrAccount mbr = new MbrAccount();
                mbr.setId(params.getAccountId());
                mbr.setDepositLock(Constants.EVNumber.one);
                mbrAccountMapper.updateByPrimaryKeySelective(mbr);
            }
            return ret;
        }
        throw new R200Exception("???????????????");
    }

    public PayResponseDto commonPay(PayParams params, SetBacicOnlinepay onlinepay, FundDeposit deposit, AgentDeposit agentDeposit) {
        PayResponseDto responseDto = new PayResponseDto();
        PayTradeRequestDto requestDto = getCommonPayParam(params, onlinepay);
        // ??????url
        String url = onlinepay.getPayUrl() + PayConstants.SAASOPS_PAY_PATDO;
        if (!onlinepay.getPayUrl().endsWith("/")) {
            url = onlinepay.getPayUrl() + "/" + PayConstants.SAASOPS_PAY_PATDO;
        }
        String jsonMessage = null;
        String errMsg = null;
        try {
            log.info("SaasopsPay==outTradeNo==" + requestDto.getOutTradeNo() + "==??????1==????????????==" + jsonUtil.toJson(requestDto));
            String startTime = DateUtil.getCurrentDate(FORMAT_18_DATE_TIME);
            jsonMessage = OkHttpUtils.postForm(url, jsonUtil.toStringMap(requestDto));
            log.info("SaasopsPay==outTradeNo==" + requestDto.getOutTradeNo() + "==??????1==??????????????????==" + startTime + "==????????????==" + DateUtil.getCurrentDate(FORMAT_18_DATE_TIME));
            log.info("SaasopsPay==outTradeNo==" + requestDto.getOutTradeNo() + "==??????1==????????????==" + jsonMessage);
        } catch (Exception e) {
            log.error("SaasopsPay==outTradeNo==" + requestDto.getOutTradeNo() + "==??????1==????????????==" + e);
            // throw new R200Exception("??????????????????,?????????????????????");
            errMsg = "??????????????????,?????????????????????";
        }

        if (StringUtils.isEmpty(errMsg) && isNull(jsonMessage)) {
            log.info("SaasopsPay==outTradeNo==" + requestDto.getOutTradeNo() + "==??????1==?????????????????????????????????");
            // throw new R200Exception("?????????????????????????????????");
            errMsg = "?????????????????????????????????";

        }

        Type jsonType = new TypeToken<CommonPayResponse<PayTradeResponseDto>>() {}.getType();
        CommonPayResponse<PayTradeResponseDto> payResponse = jsonUtil.fromJson(jsonMessage, jsonType);
        if (StringUtils.isEmpty(errMsg) && payResponse.getCode() != 200) {
            log.info("SaasopsPay==outTradeNo==" + requestDto.getOutTradeNo() + "==??????1==" + payResponse.getMsg()+"==payResponse.getCode() != 200=="+ JSON.toJSONString(payResponse));
            // throw new R200Exception(payResponse.getMsg());
            errMsg = payResponse.getMsg();
        }

        PayTradeResponseDto response = payResponse.getData();
        if (StringUtils.isEmpty(errMsg) && isNull(response)) {
            log.info("SaasopsPay==outTradeNo==" + requestDto.getOutTradeNo() + "==??????1==?????????????????????????????????!");
            // throw new R200Exception("?????????????????????????????????!");
            errMsg = "?????????????????????????????????!";
        }

        if (StringUtils.isEmpty(errMsg) && !isNull(response) && !response.getSucceed()) {
            log.info("SaasopsPay==outTradeNo==" + requestDto.getOutTradeNo() + "==??????1==" + response.getError()+"==!response.getSucceed()=="+ JSON.toJSONString(payResponse));
            // throw new R200Exception(response.getError());
            errMsg = response.getError();
        }

        if (StringUtils.isNotEmpty(errMsg)){
            setErrMsg(responseDto, errMsg, deposit, agentDeposit);
        } else if (!isNull(response)) {
            responseDto.setUrl(response.getUrl());
            responseDto.setUrlMethod(response.getUrlMethod());
        }

        if (nonNull(deposit)) {
            fundDepositMapper.insert(deposit);
            verifyService.addMbrVerifyDeposit(deposit, params.getSiteCode());

        }
        if (nonNull(agentDeposit)) {
            agentDepositMapper.insert(agentDeposit);
        }
        return responseDto;
    }

    /**
     * ??????????????????
     * @param responseDto
     * @param errMsg
     * @param deposit
     */
    private void setErrMsg(PayResponseDto responseDto, String errMsg, FundDeposit deposit, AgentDeposit agentDeposit) {
        responseDto.setStatus(false);
        responseDto.setErrMsg(errMsg);

        if (!isNull(deposit)) {
            deposit.setStatus(Constants.EVNumber.zero);
            deposit.setMemo(errMsg);
        }

        if (!isNull(agentDeposit)){
            deposit.setStatus(Constants.EVNumber.zero);
            deposit.setMemo(errMsg);
        }
    }

    private PayTradeRequestDto getCommonPayParam(PayParams params, SetBacicOnlinepay onlinepay) {
        PayTradeRequestDto requestDto = new PayTradeRequestDto();
        requestDto.setAmount(params.getFee());
        if (StringUtil.isNotEmpty(params.getBankCode()) && onlinepay.getPaymentType() == 4) {//??????
            requestDto.setBankCode(params.getBankCode());
        }
        requestDto.setEvbBankId(onlinepay.getBankId());
        // ??????url
        String callbackUrl = panziCallbackUrl + PayConstants.SAASOPS_PAY_NOTIFY_URL;
        if (!panziCallbackUrl.endsWith("/")) {
            callbackUrl = panziCallbackUrl + "/" + PayConstants.SAASOPS_PAY_NOTIFY_URL;
        }
        requestDto.setCallbackUrl(callbackUrl);
        requestDto.setIp(params.getIp());
        requestDto.setOutTradeNo(params.getOutTradeNo().toString());
        requestDto.setMerchantNo(onlinepay.getMerNo());
        requestDto.setReturnParams(params.getSiteCode());
        requestDto.setAccountId(params.getAccountId());     // ??????ID
        requestDto.setLoginName(params.getLoginName());     // ?????????
        requestDto.setTerminal(params.getFundSource());     // ????????????
        requestDto.setPayUserName(params.getUserName());    // ????????? ????????????
        Map<String, Object> param = jsonUtil.Entity2Map(requestDto);
        param.remove("sign");
        param.remove("returnParams");
        String str = ASCIIUtils.getFormatUrl(param, onlinepay.getPassword());
        String sign = MD5.getMD5(str);
        requestDto.setSign(sign);
        return requestDto;
    }

    @Async("getPayResultExecutor")
    public void getPayResult(FundDeposit deposit, String siteCode) {
        ThreadLocalCache.setSiteCodeAsny(siteCode);
        if (deposit.getMark() == Constants.EVNumber.zero || StringUtils.isNotEmpty(deposit.getPayOrderNo())) {
            String key = RedisConstants.QUERY_ACCOUNT_PAY + siteCode + deposit.getId();
            Boolean isExpired = redisService.setRedisExpiredTimeBo(key, deposit.getId(), 200, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(isExpired)) {
                try {
                    FundDeposit fundDeposit = fundDepositMapper.selectByPrimaryKey(deposit.getId());
                    if (fundDeposit.getStatus() == Constants.EVNumber.two) {
                        if (StringUtils.isNotEmpty(fundDeposit.getPayOrderNo())) {
                            updateDepositFastpay(fundDeposit, siteCode);
                        } else {
                            updateDepositOnlinepay(fundDeposit, siteCode);
                        }
                    }
                } finally {
                    redisService.del(key);
                }
            }
        }
    }

    private void updateDepositOnlinepay(FundDeposit fundDeposit, String siteCode) {
        if (nonNull(fundDeposit.getOnlinePayId())) {
            SetBacicOnlinepay onlinepay = payMapper.findOnlinePayAndPay(fundDeposit.getOnlinePayId());
            if (nonNull(onlinepay)) {
                setFundDeposit(fundDeposit);
                if (PayConstants.SAASOPS_PAY_CODE.equals(onlinepay.getPlatfromCode())
                    || PayConstants.JUHE_PAY_CODE.equals(onlinepay.getPlatfromCode())) {
                    updateSaasopsPay(onlinepay.getMerNo(), onlinepay.getPassword(), onlinepay.getPayUrl(), siteCode, fundDeposit);
                }
            }
        }
    }

    private void updateDepositFastpay(FundDeposit deposit, String siteCode) {
        if (nonNull(deposit.getCompanyPayId())) {
            SysDeposit sysDeposit = payMapper.findSysDepositById(deposit.getCompanyPayId());
            setFundDeposit(deposit);
            String platFormCode = payMapper.findPayId(sysDeposit.getFastPayId());
            if (PayConstants.SAASOPS_PAY_CODE.equals(platFormCode)) {
                updateSaasopsPay(sysDeposit.getCid(), sysDeposit.getPassword(), sysDeposit.getPayUrl(), siteCode, deposit);
                return;
            }
            // ?????????????????????(success?????????)
            fundMapper.updatePayStatus(deposit);
        }
    }

    public void setFundDeposit(FundDeposit fundDeposit) {
        fundDeposit.setModifyUser(SYSTEM_USER);
        fundDeposit.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        fundDeposit.setAuditUser(SYSTEM_USER);
        fundDeposit.setAuditTime(fundDeposit.getModifyTime());
        fundDeposit.setStatus(Constants.EVNumber.zero);
        fundDeposit.setIsPayment(Boolean.FALSE);
    }

    public void updateDongdongPay(SetBacicOnlinepay onlinepay, FundDeposit deposit, String siteCode) {
        Map<String, Object> paramsMap = new HashMap<>(8);
        paramsMap.put("MerchantCode", onlinepay.getMerNo());
        paramsMap.put("OrderId", deposit.getOrderNo());
        paramsMap.put("Time", System.currentTimeMillis());
        String urlParams = ASCIIUtils.formatUrlMap(paramsMap, false, false);
        String beforeSign = urlParams + "&Key=" + onlinepay.getPassword();
        String sign = MD5.getMD5(beforeSign).toLowerCase();
        paramsMap.put("Sign", sign);
        log.info(deposit.getOrderNo() + "?????????????????????" + jsonUtil.toJson(paramsMap) + "???");
        String payUrl = onlinepay.getPayUrl() + PayConstants.PAY_DONGDONG_QUERY;
        String result = okHttpService.postFormBTP(okHttpService.getPayHttpsClient(), payUrl, paramsMap);
        log.info(deposit.getOrderNo() + "?????????????????????" + result + "???");
        if (StringUtils.isEmpty(result)) {
            return;
        }
        DongDongResponseDto response = jsonUtil.fromJson(result, DongDongResponseDto.class);
        if (isNull(response) || isNull(response.getSuccess()) || isNull(response.getData())) {
            return;
        }
        Map<String, Object> dataMap = response.getData();
        DongDongQueryDataDto dataDto = jsonUtil.fromJson(jsonUtil.toJson(dataMap.get("data")), DongDongQueryDataDto.class);
        if (nonNull(dataDto)) {
            if ((isNull(dataDto.getMoneyReceived()) ||
                    new BigDecimal(dataDto.getMoneyReceived())
                            .compareTo(deposit.getDepositAmount()) != 0)
                    && Constants.EVNumber.one == dataDto.getStatus()) {
                deposit.setMemo("???????????????DONGDONG?????????????????????????????????DONGDONG??????????????????" + new BigDecimal(dataDto.getMoneyReceived()));
                deposit.setStatus(Constants.EVNumber.zero);
            }
            if (Constants.EVNumber.one == dataDto.getStatus()) {
                deposit.setMemo("???????????????DONGDONG??????");
                fundDepositService.updateDepositSucceed(deposit, true, false);
                payMessage(deposit, siteCode);
            } else if (Constants.EVNumber.two == dataDto.getStatus()) {
                deposit.setMemo("???????????????DONGDONG??????");
                deposit.setStatus(Constants.EVNumber.zero);
            }
            fundMapper.updatePayStatus(deposit);
        }

    }

    public void updateSaasopsPay(String merchantNo, String merchantKey, String url, String siteCode, FundDeposit deposit) {
        Map<String, Object> paramsMap = new HashMap<>(4);
        paramsMap.put("outTradeNo", deposit.getOrderNo());
        paramsMap.put("merchantNo", merchantNo);
        String urlParams = ASCIIUtils.getFormatUrl(paramsMap, merchantKey);
        String sign = MD5.getMD5(urlParams);
        paramsMap.put("sign", sign);

        log.info("'outTradeNo==" + deposit.getOrderNo() + "==??????==????????????==" + jsonUtil.toJson(paramsMap));
        // ??????url
        String payUrl = url + PayConstants.SAASOPS_PAY_QUERY;
        if (!url.endsWith("/")) {
            payUrl = url + "/" + PayConstants.SAASOPS_PAY_QUERY;
        }
//        String result = okHttpService.postFormBTP(okHttpService.getPayHttpsClient(), payUrl, paramsMap);
        String result = OkHttpUtils.postForm(payUrl, jsonUtil.toStringMap(paramsMap));
        log.info("'outTradeNo==" + deposit.getOrderNo() + "==??????==????????????==" + result);
        if (StringUtils.isEmpty(result)) {
            return;
        }
        CommonPayQueryResp response = jsonUtil.fromJson(result, CommonPayQueryResp.class);
        if (isNull(response) || response.getCode() != 200 || isNull(response.getData())) {
            return;
        }
        PaySearchResponseDto searchResponseDto = response.getData();
        if (nonNull(searchResponseDto)) {
            if (Constants.EVNumber.one == searchResponseDto.getStatus()) {
                boolean isActualArrival = false;
                if (StringUtils.isEmpty(searchResponseDto.getMemo())) {
                    deposit.setMemo("???????????????????????????");
                } else {
                    deposit.setMemo(searchResponseDto.getMemo());
                }
                // ????????????????????????
                if (Objects.nonNull(searchResponseDto.getActualarrival()) && (searchResponseDto.getActualarrival().compareTo(new BigDecimal(0)) > 0)) {
                    deposit.setActualArrival(searchResponseDto.getActualarrival());
                    isActualArrival = true;
                }
                // ??????????????????
                fundDepositService.updateDepositSucceed(deposit, true, isActualArrival);
                // ??????????????????
                payMessage(deposit, siteCode);
                // ??????????????????????????????
                mbrAccountService.unlockDepositLock(deposit.getAccountId());
                // ??????????????????????????????
                mbrAccountService.resetDepositLockNum(deposit.getAccountId());

                //????????????
                syncHandler(siteCode, deposit);
            } else if (Constants.EVNumber.zero == searchResponseDto.getStatus()) {
                if (StringUtils.isEmpty(searchResponseDto.getMemo())) {
                    deposit.setMemo("???????????????????????????");
                } else {
                    deposit.setMemo(searchResponseDto.getMemo());
                }
                deposit.setStatus(Constants.EVNumber.zero);
            } else {
                return;
            }
            // ???????????????12???????????????
            fundMapper.updatePayStatus(deposit);
        }

    }

    // ????????????????????????
    private void syncHandler(String siteCode, FundDeposit deposit) {
        CompletableFuture.runAsync(() -> {
            ThreadLocalCache.setSiteCodeAsny(siteCode);
            try {
                mtDataService.updateAgentId(deposit.getAccountId(), deposit.getSpreadCode());
            }catch (Exception e){
                log.error("mtDataService??????????????????" + e);
            }

            try {
                mtDataService.mtCallBack(siteCode, deposit.getAccountId(), deposit.getDepositAmount(),  deposit.getSpreadCode());
            }catch (Exception e){
                log.error("mtDataService??????????????????" + e);
            }

            try {
                firstChargeOprService.applyFirstCharge(deposit.getAccountId(), deposit.getIp(), siteCode);
            }catch (Exception e){
                log.error("firstChargeOprService?????????????????????" + e);
            }
        });
    }

    public void updateCfbPay(SetBacicOnlinepay onlinepay, FundDeposit deposit, String siteCode) {
        Map<String, Object> paramsMap = new HashMap<>(4);
        paramsMap.put("mcnNum", onlinepay.getMerNo());
        paramsMap.put("orderId", deposit.getOrderNo());
        paramsMap.put("secreyKey", onlinepay.getPassword());
        String signStr = PayConstants.CFB_QUERY_SIGN_STR.replace("$mcnNum", paramsMap.get("mcnNum").toString())
                .replace("$orderId", paramsMap.get("orderId").toString())
                .replace("$secreyKey", paramsMap.get("secreyKey").toString());

        paramsMap.put("sign", MD5.getMD5(signStr).toUpperCase());
        log.info(deposit.getOrderNo() + "?????????????????????" + jsonUtil.toJson(paramsMap) + "???");
        String payUrl = onlinepay.getPayUrl() + PayConstants.PAY_CFB_QUERY;
        String result = okHttpService.postJson(okHttpService.getPayHttpsClient(), payUrl, paramsMap, null);
        log.info(deposit.getOrderNo() + "?????????????????????" + result + "???");
        if (StringUtils.isEmpty(result)) {
            return;
        }
        CFBpayReponseDto response = jsonUtil.fromJson(result, CFBpayReponseDto.class);
        if (isNull(response) || isNull(response.getStatus()) || isNull(response.getContent())) {
            return;
        }
        CFBpayQueryReponse content = response.getContent();
        if (nonNull(content)) {
            if ((isNull(content.getAmount()) ||
                    new BigDecimal(content.getAmount() / ONE_HUNDRED)
                            .compareTo(deposit.getDepositAmount()) != 0)
                    && Constants.EVNumber.zero == content.getPayStatus()) {
                deposit.setMemo("???????????????CFBPAY???????????????????????????CFBPAY????????????" + new BigDecimal(content.getAmount() / ONE_HUNDRED));
                deposit.setStatus(Constants.EVNumber.zero);
            }
            if (Constants.EVNumber.one == content.getPayStatus()) {
                deposit.setMemo("??????????????????????????????");
                fundDepositService.updateDepositSucceed(deposit, true, false);
                payMessage(deposit, siteCode);
            } else if (Constants.EVNumber.two == content.getPayStatus()) {
                deposit.setMemo("??????????????????????????????");
                deposit.setStatus(Constants.EVNumber.zero);
            }
            fundMapper.updatePayStatus(deposit);
        }

    }

    public void updateBtpPay(SetBacicOnlinepay onlinepay, FundDeposit deposit, String siteCode) {
        Map<String, Object> paramsMap = new HashMap<>(4);
        paramsMap.put("partner_id", onlinepay.getMerNo());
        paramsMap.put("out_trade_no", deposit.getOrderNo());
        paramsMap.put("created_time", deposit.getCreateTime().replace("-", "").replace(":", "").replace(" ", ""));
        paramsMap.put("sign", getBtpPaySignParams(paramsMap, onlinepay.getPassword()));

        String payUrl = onlinepay.getPayUrl() + PayConstants.PAY_BTP_QUERY;
        String result = okHttpService.postFormBTP(okHttpService.getPayHttpsClient(), payUrl, paramsMap);
        log.info(deposit.getOrderNo() + "??????????????????????????????" + result + "???");
        if (StringUtils.isEmpty(result)) {
            return;
        }
        PZQueryResponse response = jsonUtil.fromJson(result, PZQueryResponse.class);
        if (isNull(response) || !Boolean.TRUE.equals(response.getSuccess()) || isNull(response.getContent())) {
            return;
        }
        BtppayContent content = jsonUtil.fromJson(jsonUtil.toJson(response.getContent()), BtppayContent.class);
        if (nonNull(content)) {
            if ((isNull(content.getTotal_fee()) ||
                    new BigDecimal(content.getTotal_fee() / ONE_HUNDRED)
                            .compareTo(deposit.getDepositAmount()) != 0)
                    && "SUCCESS".equals(content.getTrade_status())) {
                deposit.setMemo("???????????????BTPPAY???????????????????????????BTPPAY????????????" + new BigDecimal(content.getTotal_fee() / ONE_HUNDRED));
                return;
            }
            if ("FAIL".equals(content.getTrade_status())) {
                deposit.setMemo("BTPPAY:????????????");
            } else {
                deposit.setMemo("???????????????BTPPAY");
                fundDepositService.updateDepositSucceed(deposit, true, false);
                payMessage(deposit, siteCode);
            }
        }
        fundMapper.updatePayStatus(deposit);
    }

    private void payMessage(FundDeposit deposit, String siteCode) {
        applicationEventPublisher.publishEvent(
                new BizEvent(this, siteCode, deposit.getAccountId(), BizEventType.ONLINE_PAY_SUCCESS,
                        deposit.getActualArrival(), deposit.getOrderPrefix() + deposit.getOrderNo()));
    }

    public FundDeposit saveFundDespoit(PayParams params) {
        FundDeposit deposit = new FundDeposit();
        deposit.setOrderNo(params.getOutTradeNo().toString());
        deposit.setMark(Mark.onlinePay);
        deposit.setStatus(Status.apply);
        deposit.setIsPayment(Boolean.FALSE);
        deposit.setOnlinePayId(params.getOnlinePayId());
        deposit.setDepositAmount(params.getFee());
        BigDecimal feeScale = getFeeScale(params.getFee(), params.getAccountId());
        deposit.setHandlingCharge(feeScale);
        deposit.setActualArrival(deposit.getDepositAmount().subtract(feeScale));
        deposit.setHandingback(Constants.Available.enable);
        deposit.setIp(params.getIp());
        deposit.setOrderPrefix(OrderConstants.FUND_ORDER_ONLINEDEPOSIT);
        MbrAccount mbrAccount = getMbr(params.getAccountId());
        params.setLoginName(mbrAccount.getLoginName()); // ???????????????
        deposit.setDepositUser(mbrAccount.getRealName());
        deposit.setCreateUser(mbrAccount.getLoginName());
        deposit.setAccountId(params.getAccountId());
        deposit.setCreateTime(DateUtil.format(new Date(), DateUtil.FORMAT_25_DATE_TIME));
        deposit.setModifyTime(DateUtil.format(new Date(), DateUtil.FORMAT_25_DATE_TIME));
        deposit.setFundSource(params.getFundSource());
        deposit.setSpreadCode(params.getSpreadCode());
        return deposit;
    }


    public BigDecimal getFeeScale(BigDecimal fee, Integer accountId) {
        // ??????????????????,?????????????????????,??????????????????????????????????????????????????????????????????
        MbrDepositCond mbrDeposit = mbrDepositCondService.getMbrDeposit(accountId);
        if (isNull(mbrDeposit) || isNull(mbrDeposit.getFeeAvailable()) || mbrDeposit.getFeeAvailable() != 1) {
            return BigDecimal.ZERO;
        }
        // ????????????,?????????????????????????????????????????????,
        FundDeposit fundDeposit = new FundDeposit();
        fundDeposit.setAccountId(accountId);
        // ????????????????????????start??????
        String startTime = getStartTime(mbrDeposit.getFeeHours());
        // ?????????????????????????????????
        Map<String, Object> mbrFreeTimes = fundMapper.querySumFeeFreeTimes(accountId, startTime);
        BigDecimal freeTime = mbrFreeTimes != null ? (BigDecimal) mbrFreeTimes.get("freeTimes") : new BigDecimal(0);
        if (freeTime.compareTo(new BigDecimal(mbrDeposit.getFeeTimes())) == -1) {
            return BigDecimal.ZERO;
        }
        BigDecimal feeScale = fee.multiply(mbrDeposit.getFeeScale().divide(new BigDecimal(100))); // ????????? ???????????????
        return feeScale.compareTo(mbrDeposit.getFeeTop()) == 1 ? mbrDeposit.getFeeTop() : feeScale; // ?????????
    }

    // ?????????/???/??????????????????1??? 2??? 3???
    private String getStartTime(Integer rule) {
        String startTime = DateUtil.getTodayStart(DateUtil.FORMAT_10_DATE);
        if (Constants.EVNumber.one == rule) {
            startTime = DateUtil.getTodayStart(DateUtil.FORMAT_10_DATE);
        } else if (Constants.EVNumber.two == rule) {
            startTime = DateUtil.getMonday(DateUtil.FORMAT_18_DATE_TIME, 0, 0);//???????????????
        } else if (Constants.EVNumber.three == rule) {
            startTime = DateUtil.getFirstOfMonth(DateUtil.FORMAT_18_DATE_TIME, 0, 0);//???????????????
        }
        return startTime;
    }

    private MbrAccount getMbr(Integer accountId) {
        return mbrAccountMapper.selectByPrimaryKey(accountId);
    }

    private String getBtpPaySignParams(Map<String, Object> params, String key) {
        String urlParams = ASCIIUtils.formatUrlMap(params, false, false);
        String sign = MD5.getMD5(urlParams + "&key=" + key);
        return sign;
    }

    public void payCallback(String orderNo, String siteCode) {
        FundDeposit fundDeposit = new FundDeposit();
        fundDeposit.setOrderNo(orderNo);
        fundDeposit.setStatus(Constants.EVNumber.two);
        FundDeposit deposit = fundDepositMapper.selectOne(fundDeposit);
        if (nonNull(deposit)) {
            getPayResult(deposit, siteCode);
        }
    }
}
