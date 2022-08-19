package com.wsdy.saasops.api.modules.pay.controller;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.alibaba.fastjson.JSON;
import com.wsdy.saasops.api.annotation.Login;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.pay.dto.BankResponseDto;
import com.wsdy.saasops.api.modules.pay.dto.DepositPostScript;
import com.wsdy.saasops.api.modules.pay.dto.PayParams;
import com.wsdy.saasops.api.modules.pay.service.PayInfoService;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.api.utils.HttpsRequestUtil;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.fund.entity.FundDeposit;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.service.MbrAccountService;
import com.wsdy.saasops.modules.system.pay.service.CryptoCurrenciesService;
import com.wsdy.saasops.modules.system.pay.service.SysQrCodeService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/OfflinePay")
@Api(value = "OfflinePay", tags = "线下支付")
public class OfflinePayController {

    @Autowired
    private PayInfoService offlinePayService;
    @Autowired
    private SysQrCodeService sysQrCodeService;
    @Autowired
    private CryptoCurrenciesService cryptoCurrenciesService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private MbrAccountService mbrAccountService;
    @Autowired
    private PayInfoService payInfoService;

    @Login
    @GetMapping("/bankList")
    @ApiOperation(value = "公司入款可入款的银行", notes = "公司入款可入款的银行")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R bankList(HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);

        MbrAccount mbr = mbrAccountService.getAccountInfo(accountId);
        log.info("bankList收到人民币充值渠道请求:{}, 会员ID:{}, 会员组:{}, SiteCode:{}, 请求域名 origin:{}, referer:{}", DateUtil.format(new Date(), DateUtil.FORMAT_18_DATE_TIME),
                accountId, mbr.getGroupId(), CommonUtil.getSiteCode(), request.getHeader("origin"), request.getHeader("referer"));
        BankResponseDto brDto = offlinePayService.findDepositList(accountId);
        log.info("bankList收到人民币充值渠道请求:{}, 会员ID:{}, 查询结果:{}", DateUtil.format(new Date(), DateUtil.FORMAT_18_DATE_TIME), accountId,
                JSON.toJSONString(brDto));

        // 查询会员组是否开启存款姓名
        /*MbrDepositCond cond = mbrDepositCondService.getMbrDeposit(mbr.getId());
        Integer depositName = Constants.EVNumber.zero;
        if (Objects.nonNull(cond)) {
            depositName = cond.getDepositName();
        }*/

        R r = R.ok().put(brDto);
        //r.put("depositName", depositName);
        return r;
    }

    @Login
    @GetMapping("/fastDepositWithdrawList")
    @ApiOperation(value = "极速存取款列表", notes = "极速存取款列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R fastDepositWithdrawList(HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);

        MbrAccount mbr = mbrAccountService.getAccountInfo(accountId);
        log.info("fastDepositWithdrawList收到获取极速存取款信息请求:{}, 会员ID:{}, 会员组:{}, SiteCode:{}, 请求域名 origin:{}, referer:{}", DateUtil.format(new Date(), DateUtil.FORMAT_18_DATE_TIME),
                accountId, mbr.getGroupId(), CommonUtil.getSiteCode(), request.getHeader("origin"), request.getHeader("referer"));
        List<BankResponseDto> brDtoList = offlinePayService.findFastDepositWithdrawList(accountId);
        log.info("fastDepositWithdrawList收到获取极速存取款信息请求:{}, 会员ID:{}, 查询结果:{}", DateUtil.format(new Date(), DateUtil.FORMAT_18_DATE_TIME), accountId,
                JSON.toJSONString(brDtoList));

        return R.ok().put("fastDW", brDtoList);
    }

    @Login
    @GetMapping("/fastBankList")
    @ApiOperation(value = "自动入款可入款的银行", notes = "自动入款可入款的银行")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R fastBankList(HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);

        MbrAccount mbr = mbrAccountService.getAccountInfo(accountId);
        log.info("fastBankList收到人民币充值渠道请求:{}, 会员ID:{}, 会员组:{}, SiteCode:{}, 请求域名 origin:{}, referer:{}", DateUtil.format(new Date(), DateUtil.FORMAT_18_DATE_TIME),
                accountId, mbr.getGroupId(), CommonUtil.getSiteCode(), request.getHeader("origin"), request.getHeader("referer"));
        List<BankResponseDto> brDtoList = offlinePayService.findFastPayList(accountId);
        log.info("fastBankList收到人民币充值渠道请求:{}, 会员ID:{}, 查询结果:{}", DateUtil.format(new Date(), DateUtil.FORMAT_18_DATE_TIME), accountId,
                JSON.toJSONString(brDtoList));
        return R.ok().put("fastBanks", brDtoList);
    }

    @Login
    @PostMapping("/fastDepositPay")
    @ApiOperation(value = "极速存款申请", notes = "存款-极速存款申请")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R fastDepositPay(@RequestBody PayParams params, HttpServletRequest request) {
        // 入参校验
        Assert.isNull(params.getDepositId(), "depositId不能为空");
        Assert.isNull(params.getFee(), "入款金额不能为空");

        String dev = request.getHeader("dev");  // PC/H5
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);

        params.setAccountId(accountId);                             // 会员id
        params.setLoginName(loginName);                             // 会员名： 打日志用
        params.setSiteCode(CommonUtil.getSiteCode());               // siteCode
        params.setIp(CommonUtil.getIpAddress(request));             // IP
        params.setFundSource(HttpsRequestUtil.getHeaderOfDev(dev)); // 客户端： PC->0, H5->3, 默认0

        // 提交申请，返回申请结果信息
        log.info("fastDepositPay==loginName==" + loginName  + "==accountId==" + accountId
                + "==fee==" + params.getFee() + "==depositId==" + params.getDepositId()+ "==start");
        DepositPostScript info = offlinePayService.getFastDepositPostScript(params);
        log.info("fastDepositPay==loginName==" + loginName  + "==accountId==" + accountId
                + "==fee==" + params.getFee() + "==depositId==" + params.getDepositId()+ "==end" );
        return R.ok().put("info", info);
    }

    @Login
    @PostMapping("/fastDepositPayUpload")
    @ApiOperation(value = "极速存款确认上传", notes = "存款-极速存款确认上传")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R fastDepositPayUpload(@RequestBody PayParams params, HttpServletRequest request) {
        // 入参校验
        Assert.isNull(params.getPictureList(), "必须上传存款凭证");
        Assert.isNull(params.getOrderId(), "订单号不能为空");

        String dev = request.getHeader("dev");  // PC/H5
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);

        params.setAccountId(accountId);                             // 会员id
        params.setLoginName(loginName);                             // 会员名： 打日志用
        params.setSiteCode(CommonUtil.getSiteCode());               // siteCode
        params.setIp(CommonUtil.getIpAddress(request));             // IP
        params.setFundSource(HttpsRequestUtil.getHeaderOfDev(dev)); // 客户端： PC->0, H5->3, 默认0

        // 保存存款凭证
        log.info("fastDepositPay==loginName==" + loginName  + "==accountId==" + accountId + "==保存凭证地址{}", params.getPictureList());
        offlinePayService.uploadFastDepositCertificate(params);
        return R.ok();
    }

    @Login
    @PostMapping("/applyPay")
    @ApiOperation(value = "公司入款支付申请", notes = "存款--普通银行卡/快捷银行卡支付")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R applyPay(@RequestBody PayParams params, HttpServletRequest request) {
        // 入参校验
        Assert.isNull(params.getDepositId(), "depositId不能为空");
        Assert.isNull(params.getFee(), "入款金额不能为空");

        String dev = request.getHeader("dev");  // PC/H5
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);

        params.setAccountId(accountId);                             // 会员id
        params.setLoginName(loginName);                             // 会员名： 打日志用
        params.setSiteCode(CommonUtil.getSiteCode());               // siteCode
        params.setIp(CommonUtil.getIpAddress(request));             // IP
        params.setFundSource(HttpsRequestUtil.getHeaderOfDev(dev)); // 客户端： PC->0, H5->3, 默认0

        // 提交申请，返回申请结果信息
        log.info("payApply==loginName==" + loginName  + "==accountId==" + accountId
                + "==fee==" + params.getFee() + "==depositId==" + params.getDepositId()+ "==start");
        DepositPostScript info = offlinePayService.getDepositPostScript(params);
        log.info("payApply==loginName==" + loginName  + "==accountId==" + accountId
                + "==fee==" + params.getFee() + "==depositId==" + params.getDepositId()+ "==end" );
        return R.ok().put("info", info);
    }

    @Login
    @PostMapping("applyQrPay")
    @ApiOperation(value = "普通扫码入款支付申请", notes = "普通扫码入款支付申请")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R applyQrPay(@RequestBody PayParams params, HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        Assert.isNull(params.getDepositId(), "入款银行不能为空");
        Assert.isNull(params.getFee(), "入款金额不能为空");
        params.setAccountId(accountId);
        params.setIp(CommonUtil.getIpAddress(request));
        String dev = request.getHeader("dev");
        params.setFundSource(HttpsRequestUtil.getHeaderOfDev(dev));
        params.setSiteCode(CommonUtil.getSiteCode());
        return R.ok().put("info", sysQrCodeService.qrCodePay(params));
    }

    @Login
    @PostMapping("/applyCrPay")
    @ApiOperation(value = "数字货币入款支付申请", notes = "数字货币入款支付申请")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R applyCrPay(@RequestBody PayParams params, HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        Assert.isNull(params.getDepositId(), "depositId不能为空");
        Assert.isNull(params.getFee(), "入款金额不能为空");
        params.setAccountId(accountId);
        params.setIp(CommonUtil.getIpAddress(request));
        String dev = request.getHeader("dev");
        params.setFundSource(HttpsRequestUtil.getHeaderOfDev(dev));
        params.setSiteCode(CommonUtil.getSiteCode());
        return R.ok().put("info", cryptoCurrenciesService.qrCrPay(params));
    }

    @GetMapping("/getExchangeRate")
    @ApiOperation(value = "获取入款参考汇率", notes = "获取入款参考汇率")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R getExchangeRate() {
        return R.ok(cryptoCurrenciesService.getExchangeRate("deposit"));
    }

    @Login
    @GetMapping("/memberReminder")
    @ApiOperation(value = "USDT催单", notes = "USDT催单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R memberReminder(HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        FundDeposit deposit = new FundDeposit();
        deposit.setAccountId(accountId);
        deposit.setLoginName(loginName);

        // 催单
        String key = RedisConstants.ACCOUNT_REMINDER + CommonUtil.getSiteCode() + accountId;
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, accountId, 10, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(isExpired)) {
            try {
                cryptoCurrenciesService.memberReminder(deposit);
            } finally {
                redisService.del(key);
            }
        } else {
            throw new R200Exception("正在处理中，请误重复提交...");
        }
        return R.ok();
    }

    @Login
    @GetMapping("qrCodeList")
    @ApiOperation(value = "个人二维码入款", notes = "个人二维码入款")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R qrCodeList(HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put("qrCodeList",sysQrCodeService.findQrCodeList(accountId));
    }

    @Login
    @GetMapping("/getJDepositActivityRules")
    @ApiOperation(value = "获取会员匹配的首条存就送规则", notes = "获取会员匹配的首条存就送规则")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R getJDepositActivityRules(@RequestParam(value = "terminal", required = false) String terminal, HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put("jDepositActivityRules", payInfoService.getJDepositActivityRules(accountId, terminal));
    }

    @Login
    @GetMapping("/crList")
    @ApiOperation(value = "加密钱包入款列表", notes = "加密钱包入款列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R crList(HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put("crList",cryptoCurrenciesService.findCrList(accountId));
    }
    
    @Login
    @GetMapping("resentPayBank")
    @ApiOperation(value = "查询用户最近使用的银行卡支付", notes = "查询用户最近使用的银行卡支付")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R resentPayBank(HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put(offlinePayService.getRecentBankPay(accountId));
    }

    @Login
    @GetMapping("resentPayChannel")
    @ApiOperation(value = "查询用户最近使用的银行卡支付", notes = "查询用户最近使用的银行卡支付")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R resentPayChannel(HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put(offlinePayService.getResentPayChannel(accountId));
    }

    @GetMapping("/getCrLogo")
    @ApiOperation(value = "获取数字货币钱包/平台logo", notes = "获取数字货币钱包/平台logo")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R getCrLogo(HttpServletRequest request) {
        return R.ok().put(cryptoCurrenciesService.getCrLogo());
    }
}
