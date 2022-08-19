package com.wsdy.saasops.agapi.modules.controller;

import com.wsdy.saasops.agapi.annotation.AgentLogin;
import com.wsdy.saasops.agapi.modules.service.AgentCrPyaService;
import com.wsdy.saasops.agapi.modules.service.AgentPayInfoService;
import com.wsdy.saasops.agapi.modules.service.AgentQrCodePayService;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.pay.dto.DepositPostScript;
import com.wsdy.saasops.api.modules.pay.dto.PayParams;
import com.wsdy.saasops.api.modules.pay.dto.PayResponseDto;
import com.wsdy.saasops.api.modules.pay.service.PayInfoService;
import com.wsdy.saasops.api.utils.HttpsRequestUtil;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.utils.SnowFlake;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.system.pay.service.CryptoCurrenciesService;
import com.wsdy.saasops.modules.system.pay.service.SysQrCodeService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@Slf4j
@RequestMapping("/agapi/n2/pay")
@Api(tags = "财务中心")
public class AgentPayController {

    @Autowired
    private PayInfoService offlinePayService;
    @Autowired
    private SysQrCodeService sysQrCodeService;
    @Autowired
    private PayInfoService payInfoService;
    @Autowired
    private AgentQrCodePayService qrCodePayService;
    @Autowired
    private AgentPayInfoService agentPayInfoService;
    @Autowired
    private CryptoCurrenciesService cryptoCurrenciesService;
    @Autowired
    private AgentCrPyaService agentCrPyaService;

    @AgentLogin
    @GetMapping("bankList")
    @ApiOperation(value = "公司入款可入款的银行", notes = "公司入款可入款的银行")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R bankList(HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if (account.getAttributes() == 2 || account.getAttributes() == 3 || account.getAttributes() == 4) {
            return R.ok().put(null);
        }
        return R.ok().put(offlinePayService.findDepositList(qrCodePayService.getAccountId(account)));
    }

    @AgentLogin
    @GetMapping("fastBankList")
    @ApiOperation(value = "自动入款可入款的银行", notes = "自动入款可入款的银行")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R fastBankList(HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if (account.getAttributes() == 2 || account.getAttributes() == 3 || account.getAttributes() == 4) {
            return R.ok().put("fastBanks", null);
        }
        Integer accountId = qrCodePayService.getAccountId(account);
        if (accountId == null || accountId.intValue() == 0) {
            return R.ok().put("fastBanks", null);
        }
        return R.ok().put("fastBanks", offlinePayService.findFastPayList(accountId));
    }

    @AgentLogin
    @PostMapping("applyPay")
    @ApiOperation(value = "公司入款支付申请", notes = "存款--普通银行卡/快捷银行卡支付")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R applyPay(@RequestBody PayParams params, HttpServletRequest request) {
        // 入参校验
        Assert.isNull(params.getDepositId(), "depositId不能为空");
        Assert.isNull(params.getFee(), "入款金额不能为空");

        String dev = request.getHeader("dev");  // PC/H5
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        Integer accountId = qrCodePayService.getAccountId(account);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);

        params.setAccountId(accountId);                             // 会员id
        params.setLoginName(loginName);                             // 会员名： 打日志用
        params.setSiteCode(CommonUtil.getSiteCode());               // siteCode
        params.setIp(CommonUtil.getIpAddress(request));             // IP
        params.setFundSource(HttpsRequestUtil.getHeaderOfDev(dev)); // 客户端： PC->0, H5->3, 默认0
        // 提交申请，返回申请结果信息
        DepositPostScript info = agentPayInfoService.getDepositPostScript(params);
        return R.ok().put("info", info);
    }

    @AgentLogin
    @PostMapping("applyQrPay")
    @ApiOperation(value = "普通扫码入款支付申请", notes = "普通扫码入款支付申请")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R applyQrPay(@RequestBody PayParams params, HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        Integer accountId = qrCodePayService.getAccountId(account);
        Assert.isNull(params.getDepositId(), "入款银行不能为空");
        Assert.isNull(params.getFee(), "入款金额不能为空");
        params.setAccountId(accountId);
        params.setIp(CommonUtil.getIpAddress(request));
        String dev = request.getHeader("dev");
        params.setFundSource(HttpsRequestUtil.getHeaderOfDev(dev));
        params.setSiteCode(CommonUtil.getSiteCode());
        return R.ok().put("info", qrCodePayService.qrCodePay(params));
    }

    @AgentLogin
    @GetMapping("qrCodeList")
    @ApiOperation(value = "个人二维码入款", notes = "个人二维码入款")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R qrCodeList(HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if (account.getAttributes() == 2 || account.getAttributes() == 3 || account.getAttributes() == 4) {
            return R.ok().put("qrCodeList", null);
        }
        Integer accountId = qrCodePayService.getAccountId(account);
        return R.ok().put("qrCodeList", sysQrCodeService.findQrCodeList(accountId));
    }


    @AgentLogin
    @GetMapping("/payUrl")
    @ApiOperation(value = "线上支付，充值", notes = "线上支付，充值")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R getPayUrl(@ModelAttribute PayParams params, HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if (account.getAttributes() == 2 || account.getAttributes() == 3 || account.getAttributes() == 4) {
            return R.ok().put("res", null);
        }
        Integer accountId = qrCodePayService.getAccountId(account);
        params.setAccountId(accountId);
        String dev = request.getHeader("dev");
        params.setFundSource(HttpsRequestUtil.getHeaderOfDev(dev));
        params.setOutTradeNo(new SnowFlake().nextId());
        params.setIp(CommonUtil.getIpAddress(request));
        params.setSiteCode(CommonUtil.getSiteCode());
        payInfoService.checkoutOnlinePay(params);
        PayResponseDto responseDto = agentPayInfoService.optionPayment(params);

        if (!responseDto.getStatus()) {
            log.info("agentPayApply==!responseDto.getStatus()=={}", responseDto.getErrMsg());
            throw new R200Exception(responseDto.getErrMsg());
        }

        return R.ok().put("res", responseDto);
    }

    @AgentLogin
    @GetMapping("/getPzpayPictureUrl")
    @ApiOperation(value = "获取支付类型相对的支付方式及图片路径 ", notes = "获取支付类型相对的支付方式及图片路径")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "api/OfflinePay/crListtoken", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R getPzpayPictureUrl(@RequestParam(value = "terminal", required = false) String terminal, HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if (account.getAttributes() == 2 || account.getAttributes() == 3 || account.getAttributes() == 4) {
            return R.ok().put("res", null);
        }
        Integer accountId = qrCodePayService.getAccountId(account);
        return R.ok().put("res", payInfoService.getPzpayPictureUrl(terminal, accountId));
    }

    @GetMapping("/getExchangeRate")
    @ApiOperation(value = "获取入款参考汇率", notes = "获取入款参考汇率")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R getExchangeRate() {
        return R.ok(cryptoCurrenciesService.getExchangeRate("deposit"));
    }

    @GetMapping("/getWithdrawExchangeRate")
    @ApiOperation(value = "获取取款参考汇率", notes = "获取取款参考汇率")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R getWithdrawExchangeRate() {
        return R.ok(cryptoCurrenciesService.getExchangeRate("Withdrawal"));
    }

    @AgentLogin
    @GetMapping("/crList")
    @ApiOperation(value = "加密钱包入款列表", notes = "加密钱包入款列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R crList(HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if (account.getAttributes() == 2 || account.getAttributes() == 3 || account.getAttributes() == 4) {
            return R.ok().put("crList", null);
        }
        Integer accountId = qrCodePayService.getAccountId(account);
        return R.ok().put("crList", cryptoCurrenciesService.findCrList(accountId));
    }

    @AgentLogin
    @PostMapping("/applyCrPay")
    @ApiOperation(value = "数字货币入款支付申请", notes = "数字货币入款支付申请")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R applyCrPay(@RequestBody PayParams params, HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if (account.getAttributes() == 2 || account.getAttributes() == 3 || account.getAttributes() == 4) {
            return R.ok().put("info", null);
        }
        Assert.isNull(params.getDepositId(), "depositId不能为空");
        Assert.isNull(params.getFee(), "入款金额不能为空");
        params.setIp(CommonUtil.getIpAddress(request));
        String dev = request.getHeader("dev");
        params.setFundSource(HttpsRequestUtil.getHeaderOfDev(dev));
        params.setSiteCode(CommonUtil.getSiteCode());
        return R.ok().put("info", agentCrPyaService.qrCrPay(params, account));
    }
}
