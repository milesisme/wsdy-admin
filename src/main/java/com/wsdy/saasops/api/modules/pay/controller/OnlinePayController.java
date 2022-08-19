package com.wsdy.saasops.api.modules.pay.controller;

import com.alibaba.fastjson.JSON;
import com.wsdy.saasops.api.annotation.Login;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.pay.dto.*;
import com.wsdy.saasops.api.modules.pay.service.PayInfoService;
import com.wsdy.saasops.api.modules.pay.service.PaymentService;
import com.wsdy.saasops.api.utils.HttpsRequestUtil;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.fund.entity.FundDeposit;
import com.wsdy.saasops.modules.fund.service.FundDepositService;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.service.MbrAccountService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

import static java.util.Objects.isNull;


@Slf4j
@RestController
@RequestMapping("/api/OnlinePay/pzPay")
@Api(value = "OnlinePay", tags = "在线充值，支付")
public class OnlinePayController {

    @Autowired
    private PaymentService paymentService;
    @Autowired
    private FundDepositService fundDepositService;
    @Autowired
    private PayInfoService payInfoService;
    @Autowired
    private MbrAccountService mbrAccountService;


    @Login
    @GetMapping("payUrl")
    @ApiOperation(value = "线上支付，充值", notes = "线上支付，充值")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R getPayUrl(@ModelAttribute PayParams params, HttpServletRequest request) {
        // 参数处理
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);

        params.setAccountId(accountId);
        params.setFundSource(HttpsRequestUtil.getHeaderOfDev(request.getHeader("dev")));
        params.setOutTradeNo(new SnowFlake().nextId());
        params.setIp(CommonUtil.getIpAddress(request));
        params.setSiteCode(CommonUtil.getSiteCode());
        // 提单
        log.info("payApply==loginName==" + loginName  + "==accountId==" + accountId
                + "==fee==" + params.getFee() + "==onlinePayId==" + params.getOnlinePayId() + "==start");
        PayResponseDto responseDto = paymentService.dispatchPayment(params);

        if (!responseDto.getStatus()) {
            log.info("payApply==!responseDto.getStatus()=={}", responseDto.getErrMsg());
            throw new R200Exception(responseDto.getErrMsg());
        }

        log.info("payApply==loginName==" + loginName  + "==accountId==" + accountId
                + "==fee==" + params.getFee() + "==onlinePayId==" + params.getOnlinePayId() + "==end");
        return R.ok().put("res", responseDto);
    }

    @Login
    @GetMapping("payResult")
    @ApiOperation(value = "获取充值结果，通过订单号,并更新用户钱包", notes = "获取充值结果，通过订单号,并更新用户钱包")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R getPayResult(HttpServletRequest request) {
        FundDeposit fundDeposit = new FundDeposit();
        fundDeposit.setAccountId((Integer) request.getAttribute(ApiConstants.USER_ID));
        fundDeposit.setStatus(Constants.EVNumber.two);
        List<FundDeposit> fundDeposits = fundDepositService.selectList(fundDeposit);
        if (Collections3.isNotEmpty(fundDeposits)) {
            String siteCode = CommonUtil.getSiteCode();
            fundDeposits.forEach(deposit -> paymentService.getPayResult(deposit, siteCode));
        }
        return R.ok();
    }


    @Login
    @GetMapping("/getPzpayPictureUrl")
    @ApiOperation(value = "获取支付类型相对的支付方式及图片路径 ", notes = "获取支付类型相对的支付方式及图片路径")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R getPzpayPictureUrl(@RequestParam(value = "terminal", required = false) String terminal, HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);

        MbrAccount mbr = mbrAccountService.getAccountInfo(accountId);
        log.info("getPzpayPictureUrl收到人民币充值渠道请求:{}, 会员ID:{}, 会员组:{}, SiteCode:{}, 请求域名 origin:{}, referer:{}", DateUtil.format(new Date(), DateUtil.FORMAT_18_DATE_TIME),
                accountId, mbr.getGroupId(), CommonUtil.getSiteCode(), request.getHeader("origin"), request.getHeader("referer"));
        PayChoiceListDto pclDto = payInfoService.getPzpayPictureUrl(terminal, accountId);
        log.info("getPzpayPictureUrl收到人民币充值渠道请求:{}, 会员ID:{}, 查询结果:{}", DateUtil.format(new Date(), DateUtil.FORMAT_18_DATE_TIME), accountId,
                JSON.toJSONString(pclDto));

        return R.ok().put("res", pclDto);
    }

    @Login
    @GetMapping("getFundDepositList")
    @ApiOperation(value = "会员充值的记录线上入款 ", notes = "会员充值的记录线上入款")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R getFundDepositOnLine(@ModelAttribute DepositListDto fundDeposit,
                                  @RequestParam("pageNo") @NotNull Integer pageNo,
                                  @RequestParam("pageSize") @NotNull Integer pageSize,
                                  @ApiParam("排序字段") @RequestParam(value = "orderBy", required = false) String orderBy,
                                  HttpServletRequest request) {
        Assert.isNull(fundDeposit.getMark(), "充值类型不能为空!");
        if (fundDeposit.getMark() != Constants.EVNumber.zero
                && fundDeposit.getMark() != Constants.EVNumber.one
                && fundDeposit.getMark() != Constants.EVNumber.two
                && fundDeposit.getMark() != Constants.EVNumber.three
                && fundDeposit.getMark() != Constants.EVNumber.four) {
            throw new R200Exception("充值类型选择错误!");
        }
        fundDeposit.setAccountId((Integer) request.getAttribute(ApiConstants.USER_ID));
        R r = R.ok().put("res", fundDepositService.queryListPage(fundDeposit, pageNo, pageSize));
        Double taltal = fundDepositService.findDepositSum(fundDeposit);
        r.put("totalCount", isNull(taltal) ? 0.00d : taltal);
        return r;
    }
}
