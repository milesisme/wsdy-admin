package com.wsdy.saasops.sysapi.controller;

import com.alibaba.fastjson.JSON;
import com.wsdy.saasops.agapi.modules.service.AgentCrPyaService;
import com.wsdy.saasops.agapi.modules.service.AgentPaymentService;
import com.wsdy.saasops.api.modules.apisys.service.TCpSiteService;
import com.wsdy.saasops.api.modules.pay.dto.DPaySearchResponseDto;
import com.wsdy.saasops.api.modules.pay.dto.evellet.CommonEvelletResponse;
import com.wsdy.saasops.api.modules.pay.dto.evellet.EvelletPayPushCallbackDto;
import com.wsdy.saasops.api.modules.pay.dto.evellet.EvelletPayTransferCallbackDto;
import com.wsdy.saasops.api.modules.pay.dto.saaspay.PaySearchResponseDto;
import com.wsdy.saasops.api.modules.pay.service.PaymentService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.utils.pay.OnePayUtil;
import com.wsdy.saasops.modules.fund.dto.LBTCallbackReqDto;
import com.wsdy.saasops.modules.fund.dto.OnePayResponseDto;
import com.wsdy.saasops.modules.fund.dto.PaymentPayResponseDto;
import com.wsdy.saasops.modules.fund.entity.FundMerchantPay;
import com.wsdy.saasops.modules.fund.service.FundWithdrawService;
import com.wsdy.saasops.modules.fund.service.MerchantPayService;
import com.wsdy.saasops.modules.system.pay.service.CryptoCurrenciesService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

import static com.wsdy.saasops.api.constants.PayConstants.PAY_SUCCESS;
import static java.util.Objects.isNull;

@Slf4j
@RestController
@RequestMapping("/api/callback")
@Api(value = "提供给第三方的回调服务", tags = "提供给第三方的回调服务")
public class CallbackController {

    @Autowired
    private TCpSiteService tCpSiteService;
    @Autowired
    private FundWithdrawService fundWithdrawService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private MerchantPayService merchantPayService;
    @Autowired
    private CryptoCurrenciesService cryptoCurrenciesService;
    @Autowired
    private AgentPaymentService agentPaymentService;
    @Autowired
    private AgentCrPyaService agentCrPyaService;


    @PostMapping(value = "/onePayCallback/{siteCode}")
    public String callback(@ModelAttribute OnePayResponseDto callBackDto, @PathVariable String siteCode, HttpServletRequest request) {

        if (callBackDto == null || siteCode == null || callBackDto.getMer_ordersid() == null) {
            log.info("one pay回调错误:" + siteCode + JSON.toJSONString(callBackDto));
            return "FAIL";
        }
        log.info("one pay回调数据信息【" + JSON.toJSONString(callBackDto) + "】");
//        String siteCodeAndOrderNo = callBackDto.getMer_ordersid();
//        String siteCode = siteCodeAndOrderNo.substring(0, 3);
        request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(siteCode));
        if (callBackDto != null) {
            FundMerchantPay fundMerchantPay = getMerchantPay(callBackDto);
            if (OnePayUtil.checkSign(callBackDto, fundMerchantPay.getMerchantKey())) {
                fundWithdrawService.dealOnePayCallback(callBackDto, siteCode);
                return PAY_SUCCESS;
            } else {
                return "验签失败";
            }
        }
        return "failure";
    }

    @PostMapping(value = "/paymentCallback/{siteCode}")
    public String paymentCallback(@RequestBody PaymentPayResponseDto callBackDto, @PathVariable String siteCode, HttpServletRequest request) {

        if (callBackDto == null || siteCode == null || callBackDto.getMerchantOrderNumber() == null || callBackDto.getPayStatus() == null) {
            log.info("Payment代付==回调错误==" + siteCode + JSON.toJSONString(callBackDto));
            return "SUCCESS";
        }
        log.info("Payment代付==outTradeNo==" + callBackDto.getMerchantOrderNumber() + "==回调==数据信息【" + JSON.toJSONString(callBackDto) + "】");
        request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(siteCode));
        if (callBackDto != null) {
            fundWithdrawService.dealPaymentPayCallback(callBackDto, siteCode);
        }
        return "SUCCESS";
    }

    private FundMerchantPay getMerchantPay(OnePayResponseDto callBackDto) {
        FundMerchantPay queryParam = new FundMerchantPay();
        queryParam.setMerchantNo(callBackDto.getMer_id());
        FundMerchantPay fundMerchantPay = merchantPayService.getMerchantPay(queryParam).get(0);
        return fundMerchantPay;
    }

    @PostMapping("/saasopsPayCallback")
    public R commonCallback(@RequestBody PaySearchResponseDto paySearchResponseDto, HttpServletRequest request) {
        // saasopsPay只有成功才回调
        if (isNull(paySearchResponseDto) || isNull(paySearchResponseDto.getOutTradeNo()) || isNull(paySearchResponseDto.getStatus())
                || StringUtils.isEmpty(paySearchResponseDto.getReturnParams())) {
            log.info("统一支付回调错误:" + JSON.toJSONString(paySearchResponseDto));
            return R.error("回调数据错误");
        }
        String[] params = paySearchResponseDto.getReturnParams().split("_");
        if (params.length == 1) {
            request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(paySearchResponseDto.getReturnParams()));
            log.info("统一支付回调返回数据信息【" + JSON.toJSONString(paySearchResponseDto) + "】");
            paymentService.payCallback(paySearchResponseDto.getOutTradeNo(), paySearchResponseDto.getReturnParams());
        }
        if (params.length == 2) {
            request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(params[0]));
            log.info("代理统一支付回调返回数据信息【" + JSON.toJSONString(paySearchResponseDto) + "】");
            agentPaymentService.payCallback(paySearchResponseDto.getOutTradeNo(), params[0]);
        }
        return R.ok();
    }

    @PostMapping(value = "/evelletCallback/{siteCode}")
    public String evelletCallback(@RequestBody CommonEvelletResponse<EvelletPayPushCallbackDto> callBackDto, @PathVariable String siteCode, HttpServletRequest request) {
        if (Objects.isNull(callBackDto)) {
            log.info("evelletCallback==siteCode==" + siteCode + "==回调错误1==" + siteCode + JSON.toJSONString(callBackDto));
            return "FAIL";
        }
        if (callBackDto.getCode() != 200) {
            log.info("evelletCallback==siteCode==" + siteCode + "==回调错误2==" + siteCode + "MSG==" + callBackDto.getMsg());
            return "FAIL";
        }
        EvelletPayPushCallbackDto data = callBackDto.getData();
        if (Objects.isNull(data) || Objects.isNull(siteCode)
                || StringUtils.isEmpty(data.getHash())) {
            log.info("evelletCallback==siteCode==" + siteCode + "==回调错误3==" + siteCode + JSON.toJSONString(callBackDto));
            return "FAIL";
        }
        request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(siteCode));
        log.info("evelletCallback==siteCode==" + siteCode + "==loginName==" + data.getLoginName() + "==回调==数据信息【" + JSON.toJSONString(callBackDto) + "】");

        if (Constants.AGENT.equals(data.getUserType())) {
            return agentCrPyaService.evelletCallback(data, siteCode);
        }
        return cryptoCurrenciesService.evelletCallback(data, siteCode);
    }

    @PostMapping(value = "/evelletTransferCallback/{siteCode}")
    public String evelletTransferCallback(@RequestBody CommonEvelletResponse<EvelletPayTransferCallbackDto> callBackDto, @PathVariable String siteCode, HttpServletRequest request) {
        if (Objects.isNull(callBackDto)) {
            log.info("evelletTransferCallback==siteCode==" + siteCode + "==回调错误1==" + siteCode + JSON.toJSONString(callBackDto));
            return "FAIL";
        }
        if (callBackDto.getCode() != 200) {
            log.info("evelletTransferCallback==siteCode==" + siteCode + "==回调错误2==" + siteCode + "MSG==" + callBackDto.getMsg());
            return "FAIL";
        }
        EvelletPayTransferCallbackDto data = callBackDto.getData();
        if (Objects.isNull(data) || Objects.isNull(siteCode)) {
            log.info("evelletTransferCallback==siteCode==" + siteCode + "==回调错误3==" + siteCode + JSON.toJSONString(callBackDto));
            return "FAIL";
        }
        log.info("evelletTransferCallback==siteCode==" + siteCode + "==loginName==" + data.getLoginName() + "==回调==数据信息【" + JSON.toJSONString(callBackDto) + "】");
        request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(siteCode));
        if (Constants.AGENT.equals(data.getUserType())) {
            agentCrPyaService.dealEvelletTransferCallback(data, siteCode);
        } else {
            fundWithdrawService.dealEvelletTransferCallback(data, siteCode);
        }
        return "SUCCESS";
    }

    @PostMapping(value = "/lbtCallback/{siteCode}")
    public R lbtCallback(@RequestBody LBTCallbackReqDto callbackReqDto, @PathVariable String siteCode, HttpServletRequest request) {

        if (Objects.isNull(callbackReqDto) || StringUtils.isEmpty(callbackReqDto.getStatus())
                || StringUtils.isEmpty(callbackReqDto.getOrderno()) || StringUtils.isEmpty(callbackReqDto.getSign())) {
            log.info("LBT代付==outTradeNo==回调==返回值缺失==" + JSON.toJSONString(callbackReqDto));
            throw new RRException("LBTCallback回调返回值缺失！");
        }

        log.info("LBT代付==outTradeNo==" + callbackReqDto.getOrderno() + "==回调==数据信息【" + JSON.toJSONString(callbackReqDto) + "】");
        request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(siteCode));

        fundWithdrawService.dealLBTCallback(callbackReqDto, siteCode);
        return R.ok();
    }

    @PostMapping(value = "/saasopsDPayCallback")
    public R saasopsDPayCallback(@RequestBody DPaySearchResponseDto paySearchResponseDto, HttpServletRequest request) {

        if (isNull(paySearchResponseDto) || isNull(paySearchResponseDto.getOutTradeNo()) || isNull(paySearchResponseDto.getStatus())
                || StringUtils.isEmpty(paySearchResponseDto.getReturnParams())) {
            log.info("SAASOPS_PAY==支付中心代付==回调错误:" + JSON.toJSONString(paySearchResponseDto));
            return R.error("回调数据错误");
        }

        log.info("SAASOPS_PAY==支付中心代付==outTradeNo==" + paySearchResponseDto.getOutTradeNo() + "==回调==数据信息【" + JSON.toJSONString(paySearchResponseDto) + "】");
        request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(paySearchResponseDto.getReturnParams()));

        fundWithdrawService.dealPayCenterCallback(paySearchResponseDto, paySearchResponseDto.getReturnParams());
        return R.ok();
    }
}
