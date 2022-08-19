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

        // 获取支付渠道信息
        SetBacicOnlinepay onlinepay = payMapper.findOnlinePayAndPay(params.getOnlinePayId());
        String code = onlinepay.getCode();
        if (PayConstants.JUHE_URL.equals(code)) {
            // 聚合支付
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

        // 校验入款配置等
        payInfoService.checkoutOnlinePay(params);

        // 获得会员
        // MbrAccount account = mbrAccountMapper.selectByPrimaryKey(params.getAccountId());
        // 校验存款计数
        // MbrDepositCount count = payInfoService.checkDepositCount(account, Constants.EVNumber.zero);

        // 获取支付渠道信息
        SetBacicOnlinepay onlinepay = payMapper.findOnlinePayAndPay(params.getOnlinePayId());

        // 效验存款锁定规则，是否应该锁定会员
       /*
        boolean lock = mbrDepositLockLogService.onlinepayUnpayLock(account, onlinepay.getPayId());
        if (lock) {
            throw new R200Exception("您已被限制存款，请联系客服.！");
        }
        */

        // 请求获取优享支付平台产品信息，返回客服聊天充值url

        // 请求支付中心往优享支付请求平台产品信息
        String url = onlinepay.getPayUrl() + PayConstants.SAASOPS_THIRDPARTY_RECHARGECHATLINK_URL;
        if (!onlinepay.getPayUrl().endsWith("/")) {
            url = onlinepay.getPayUrl() + "/" + PayConstants.SAASOPS_THIRDPARTY_RECHARGECHATLINK_URL;
        }
        String jsonMessage = null;
        try {
            log.info("UxiangPay==获取平台产品信息，获取客服聊天充值链接==");

            Long startTime = System.currentTimeMillis();
            HashMap<String, String> paramsMap = new HashMap<>();
            /*
            // app_id	商户产品ID	cfltmPEvYrRotXCs2LE4qFvJ
            paramsMap.put("app_id", onlinepay.getMerNo());
            // out_uid	商户用户ID	12345678
            paramsMap.put("out_uid", params.getAccountId()+"");
            // platform_product_ids	平台产品ID 文档 参数说明	多个用英文半角逗号隔开 1,2,3,4,5(1优享 3数字 4聚合 5快兑)
            paramsMap.put("platform_product_ids", "1");
            // user_recharge_amount	用户充值总额/用户分层 （聚合可选参数）	500.00
            paramsMap.put("user_recharge_amount", params.getFee().toString());
            // return_type	返回数据结构类型	固定值 1
            paramsMap.put("return_type", "1");
            // device	设备标识 Int 类型	1. IOS 2.Android 3.其他
            // params.getTerminal(): 0代表PC端,1代表手机
            paramsMap.put("device", (0 == params.getTerminal()) ? "3" : "1");
            // timestamp	当前时间10位时间戳	1592544604
            paramsMap.put("timestamp", System.currentTimeMillis()+"");

            // bankId
            paramsMap.put("evbBankId", onlinepay.getBankId()+"");

            // orderParams, 支付订单参数，用在后头订单创建的参数
            paramsMap.put("orderParams", JSON.toJSONString(params));
            */
            // fixme 为了对接被动下单模式的支付，这里可以将所有入参直接传递到服务pay进行处理
            paramsMap.put("orderParams", JSON.toJSONString(params));
            paramsMap.put("onlinePay", JSON.toJSONString(onlinepay));

            jsonMessage = OkHttpUtils.postGateWayJson(url, paramsMap, null);
            Long endTime = System.currentTimeMillis();
            log.info("UxiangPay==获取平台产品信息==返回值：{}，接口请求时间:{}",jsonMessage, endTime-startTime);
        } catch (Exception e) {
            log.error("UxiangPay==获取平台产品信息==异常！", e);
            throw new R200Exception("该支付维护中,请使用其它支付");
        }

        if (StringUtils.isBlank(jsonMessage)) {
            log.info("UxiangPay==获取平台产品信息返回空==");
            throw new R200Exception("获取平台产品信息返回空");
        }

        JSONObject jsonObject = JSON.parseObject(jsonMessage);
        Integer code = jsonObject.getIntValue("code");
        // 返回code值不为200都是错误
        if (!"200".equals(String.valueOf(code))) {
            log.info("UxiangPay==获取平台产品信息异常==，异常信息：{}", jsonObject.get("msg"));
            throw new R200Exception(jsonObject.getString("msg"));
        }

        // 充值聊天url
        String payUrl = jsonObject.getString("payUrl");
        result.setUrl(payUrl);
        result.setUrlMethod(0);

        return result;
    }

    public PayResponseDto optionPayment(PayParams params) {
        // 校验入款配置等
        payInfoService.checkoutOnlinePay(params);

        // 校验存款计数
        // 获得会员
        MbrAccount account = mbrAccountMapper.selectByPrimaryKey(params.getAccountId());
        MbrDepositCount count = payInfoService.checkDepositCount(account, Constants.EVNumber.zero);

        // 处理deposit对象
        FundDeposit deposit = saveFundDespoit(params);
        // 获取支付渠道信息
        SetBacicOnlinepay onlinepay = payMapper.findOnlinePayAndPay(params.getOnlinePayId());

        // 效验存款锁定规则，是否应该锁定会员
        boolean lock = mbrDepositLockLogService.onlinepayUnpayLock(account, onlinepay.getPayId());
        if (lock) {
            throw new R200Exception("您已被限制存款，请联系客服.！");
        }

        if (PayConstants.SAASOPS_PAY_CODE.equals(onlinepay.getPlatfromCode())) {
            // 提单
            if(StringUtil.isEmpty(params.getUserName())){
                params.setUserName(deposit.getDepositUser());
            }
            PayResponseDto ret = commonPay(params, onlinepay, deposit, null);
            // 不存在成功入款单，则更新计数
            mbrDepositCountMapper.updateCount(count);
            // 更新会员存款锁定状态
            if (count.getIsUpdateDepositLock()) {
                MbrAccount mbr = new MbrAccount();
                mbr.setId(params.getAccountId());
                mbr.setDepositLock(Constants.EVNumber.one);
                mbrAccountMapper.updateByPrimaryKeySelective(mbr);
            }
            return ret;
        }
        throw new R200Exception("支付不可用");
    }

    public PayResponseDto commonPay(PayParams params, SetBacicOnlinepay onlinepay, FundDeposit deposit, AgentDeposit agentDeposit) {
        PayResponseDto responseDto = new PayResponseDto();
        PayTradeRequestDto requestDto = getCommonPayParam(params, onlinepay);
        // 处理url
        String url = onlinepay.getPayUrl() + PayConstants.SAASOPS_PAY_PATDO;
        if (!onlinepay.getPayUrl().endsWith("/")) {
            url = onlinepay.getPayUrl() + "/" + PayConstants.SAASOPS_PAY_PATDO;
        }
        String jsonMessage = null;
        String errMsg = null;
        try {
            log.info("SaasopsPay==outTradeNo==" + requestDto.getOutTradeNo() + "==下单1==请求信息==" + jsonUtil.toJson(requestDto));
            String startTime = DateUtil.getCurrentDate(FORMAT_18_DATE_TIME);
            jsonMessage = OkHttpUtils.postForm(url, jsonUtil.toStringMap(requestDto));
            log.info("SaasopsPay==outTradeNo==" + requestDto.getOutTradeNo() + "==下单1==支付开始时间==" + startTime + "==结束时间==" + DateUtil.getCurrentDate(FORMAT_18_DATE_TIME));
            log.info("SaasopsPay==outTradeNo==" + requestDto.getOutTradeNo() + "==下单1==返回信息==" + jsonMessage);
        } catch (Exception e) {
            log.error("SaasopsPay==outTradeNo==" + requestDto.getOutTradeNo() + "==下单1==支付报错==" + e);
            // throw new R200Exception("该支付维护中,请使用其它支付");
            errMsg = "该支付维护中,请使用其它支付";
        }

        if (StringUtils.isEmpty(errMsg) && isNull(jsonMessage)) {
            log.info("SaasopsPay==outTradeNo==" + requestDto.getOutTradeNo() + "==下单1==提交支付网关支付返回空");
            // throw new R200Exception("提交支付网关支付返回空");
            errMsg = "提交支付网关支付返回空";

        }

        Type jsonType = new TypeToken<CommonPayResponse<PayTradeResponseDto>>() {}.getType();
        CommonPayResponse<PayTradeResponseDto> payResponse = jsonUtil.fromJson(jsonMessage, jsonType);
        if (StringUtils.isEmpty(errMsg) && payResponse.getCode() != 200) {
            log.info("SaasopsPay==outTradeNo==" + requestDto.getOutTradeNo() + "==下单1==" + payResponse.getMsg()+"==payResponse.getCode() != 200=="+ JSON.toJSONString(payResponse));
            // throw new R200Exception(payResponse.getMsg());
            errMsg = payResponse.getMsg();
        }

        PayTradeResponseDto response = payResponse.getData();
        if (StringUtils.isEmpty(errMsg) && isNull(response)) {
            log.info("SaasopsPay==outTradeNo==" + requestDto.getOutTradeNo() + "==下单1==提交支付网关支付返回空!");
            // throw new R200Exception("提交支付网关支付返回空!");
            errMsg = "提交支付网关支付返回空!";
        }

        if (StringUtils.isEmpty(errMsg) && !isNull(response) && !response.getSucceed()) {
            log.info("SaasopsPay==outTradeNo==" + requestDto.getOutTradeNo() + "==下单1==" + response.getError()+"==!response.getSucceed()=="+ JSON.toJSONString(payResponse));
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
     * 设置错误信息
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
        if (StringUtil.isNotEmpty(params.getBankCode()) && onlinepay.getPaymentType() == 4) {//网银
            requestDto.setBankCode(params.getBankCode());
        }
        requestDto.setEvbBankId(onlinepay.getBankId());
        // 处理url
        String callbackUrl = panziCallbackUrl + PayConstants.SAASOPS_PAY_NOTIFY_URL;
        if (!panziCallbackUrl.endsWith("/")) {
            callbackUrl = panziCallbackUrl + "/" + PayConstants.SAASOPS_PAY_NOTIFY_URL;
        }
        requestDto.setCallbackUrl(callbackUrl);
        requestDto.setIp(params.getIp());
        requestDto.setOutTradeNo(params.getOutTradeNo().toString());
        requestDto.setMerchantNo(onlinepay.getMerNo());
        requestDto.setReturnParams(params.getSiteCode());
        requestDto.setAccountId(params.getAccountId());     // 会员ID
        requestDto.setLoginName(params.getLoginName());     // 会员名
        requestDto.setTerminal(params.getFundSource());     // 支付终端
        requestDto.setPayUserName(params.getUserName());    // 支付者 银行卡名
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
            // 此处不会再更新(success状态下)
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
        log.info(deposit.getOrderNo() + "查询请求参数【" + jsonUtil.toJson(paramsMap) + "】");
        String payUrl = onlinepay.getPayUrl() + PayConstants.PAY_DONGDONG_QUERY;
        String result = okHttpService.postFormBTP(okHttpService.getPayHttpsClient(), payUrl, paramsMap);
        log.info(deposit.getOrderNo() + "查询返回参数【" + result + "】");
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
                deposit.setMemo("存款金额跟DONGDONG支付返回实际金额不符，DONGDONG支付支付金额" + new BigDecimal(dataDto.getMoneyReceived()));
                deposit.setStatus(Constants.EVNumber.zero);
            }
            if (Constants.EVNumber.one == dataDto.getStatus()) {
                deposit.setMemo("支付成功，DONGDONG支付");
                fundDepositService.updateDepositSucceed(deposit, true, false);
                payMessage(deposit, siteCode);
            } else if (Constants.EVNumber.two == dataDto.getStatus()) {
                deposit.setMemo("支付失败，DONGDONG支付");
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

        log.info("'outTradeNo==" + deposit.getOrderNo() + "==查询==请求参数==" + jsonUtil.toJson(paramsMap));
        // 处理url
        String payUrl = url + PayConstants.SAASOPS_PAY_QUERY;
        if (!url.endsWith("/")) {
            payUrl = url + "/" + PayConstants.SAASOPS_PAY_QUERY;
        }
//        String result = okHttpService.postFormBTP(okHttpService.getPayHttpsClient(), payUrl, paramsMap);
        String result = OkHttpUtils.postForm(payUrl, jsonUtil.toStringMap(paramsMap));
        log.info("'outTradeNo==" + deposit.getOrderNo() + "==查询==返回参数==" + result);
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
                    deposit.setMemo("支付成功，网关支付");
                } else {
                    deposit.setMemo(searchResponseDto.getMemo());
                }
                // 修改实际入款金额
                if (Objects.nonNull(searchResponseDto.getActualarrival()) && (searchResponseDto.getActualarrival().compareTo(new BigDecimal(0)) > 0)) {
                    deposit.setActualArrival(searchResponseDto.getActualarrival());
                    isActualArrival = true;
                }
                // 支付成功处理
                fundDepositService.updateDepositSucceed(deposit, true, isActualArrival);
                // 成功消息发送
                payMessage(deposit, siteCode);
                // 更新会员存款锁定状态
                mbrAccountService.unlockDepositLock(deposit.getAccountId());
                // 充值存款申请次数重置
                mbrAccountService.resetDepositLockNum(deposit.getAccountId());

                //异步处理
                syncHandler(siteCode, deposit);
            } else if (Constants.EVNumber.zero == searchResponseDto.getStatus()) {
                if (StringUtils.isEmpty(searchResponseDto.getMemo())) {
                    deposit.setMemo("支付失败，网关支付");
                } else {
                    deposit.setMemo(searchResponseDto.getMemo());
                }
                deposit.setStatus(Constants.EVNumber.zero);
            } else {
                return;
            }
            // 只更新超过12小时的订单
            fundMapper.updatePayStatus(deposit);
        }

    }

    // 充值成功异步处理
    private void syncHandler(String siteCode, FundDeposit deposit) {
        CompletableFuture.runAsync(() -> {
            ThreadLocalCache.setSiteCodeAsny(siteCode);
            try {
                mtDataService.updateAgentId(deposit.getAccountId(), deposit.getSpreadCode());
            }catch (Exception e){
                log.error("mtDataService蜜桃绑定错误" + e);
            }

            try {
                mtDataService.mtCallBack(siteCode, deposit.getAccountId(), deposit.getDepositAmount(),  deposit.getSpreadCode());
            }catch (Exception e){
                log.error("mtDataService蜜桃回调错误" + e);
            }

            try {
                firstChargeOprService.applyFirstCharge(deposit.getAccountId(), deposit.getIp(), siteCode);
            }catch (Exception e){
                log.error("firstChargeOprService充值返上级错误" + e);
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
        log.info(deposit.getOrderNo() + "查询请求参数【" + jsonUtil.toJson(paramsMap) + "】");
        String payUrl = onlinepay.getPayUrl() + PayConstants.PAY_CFB_QUERY;
        String result = okHttpService.postJson(okHttpService.getPayHttpsClient(), payUrl, paramsMap, null);
        log.info(deposit.getOrderNo() + "查询返回参数【" + result + "】");
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
                deposit.setMemo("存款金额跟CFBPAY返回实际金额不符，CFBPAY支付金额" + new BigDecimal(content.getAmount() / ONE_HUNDRED));
                deposit.setStatus(Constants.EVNumber.zero);
            }
            if (Constants.EVNumber.one == content.getPayStatus()) {
                deposit.setMemo("支付成功，财付宝支付");
                fundDepositService.updateDepositSucceed(deposit, true, false);
                payMessage(deposit, siteCode);
            } else if (Constants.EVNumber.two == content.getPayStatus()) {
                deposit.setMemo("支付失败，财付宝支付");
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
        log.info(deposit.getOrderNo() + "请求支付的返回参数【" + result + "】");
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
                deposit.setMemo("存款金额跟BTPPAY返回实际金额不对，BTPPAY支付金额" + new BigDecimal(content.getTotal_fee() / ONE_HUNDRED));
                return;
            }
            if ("FAIL".equals(content.getTrade_status())) {
                deposit.setMemo("BTPPAY:支付失败");
            } else {
                deposit.setMemo("支付成功，BTPPAY");
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
        params.setLoginName(mbrAccount.getLoginName()); // 增加会员名
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
        // 根据该会员组,会员的存款设置,获取线上支付玩家单笔存款手续费及相关设置信息
        MbrDepositCond mbrDeposit = mbrDepositCondService.getMbrDeposit(accountId);
        if (isNull(mbrDeposit) || isNull(mbrDeposit.getFeeAvailable()) || mbrDeposit.getFeeAvailable() != 1) {
            return BigDecimal.ZERO;
        }
        // 判断限免,从支付流水中取出该用户支付数据,
        FundDeposit fundDeposit = new FundDeposit();
        fundDeposit.setAccountId(accountId);
        // 根据限免周期获得start时间
        String startTime = getStartTime(mbrDeposit.getFeeHours());
        // 实际这段时间的充值次数
        Map<String, Object> mbrFreeTimes = fundMapper.querySumFeeFreeTimes(accountId, startTime);
        BigDecimal freeTime = mbrFreeTimes != null ? (BigDecimal) mbrFreeTimes.get("freeTimes") : new BigDecimal(0);
        if (freeTime.compareTo(new BigDecimal(mbrDeposit.getFeeTimes())) == -1) {
            return BigDecimal.ZERO;
        }
        BigDecimal feeScale = fee.multiply(mbrDeposit.getFeeScale().divide(new BigDecimal(100))); // 手续费 按比例收费
        return feeScale.compareTo(mbrDeposit.getFeeTop()) == 1 ? mbrDeposit.getFeeTop() : feeScale; // 手续费
    }

    // 根据日/周/月获取时间：1日 2周 3月
    private String getStartTime(Integer rule) {
        String startTime = DateUtil.getTodayStart(DateUtil.FORMAT_10_DATE);
        if (Constants.EVNumber.one == rule) {
            startTime = DateUtil.getTodayStart(DateUtil.FORMAT_10_DATE);
        } else if (Constants.EVNumber.two == rule) {
            startTime = DateUtil.getMonday(DateUtil.FORMAT_18_DATE_TIME, 0, 0);//本周第一天
        } else if (Constants.EVNumber.three == rule) {
            startTime = DateUtil.getFirstOfMonth(DateUtil.FORMAT_18_DATE_TIME, 0, 0);//本月第一天
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
