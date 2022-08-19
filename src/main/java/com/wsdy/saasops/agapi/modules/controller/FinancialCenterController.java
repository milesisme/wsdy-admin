package com.wsdy.saasops.agapi.modules.controller;

import com.wsdy.saasops.agapi.annotation.AgentLogin;
import com.wsdy.saasops.agapi.modules.dto.RechargeTransferParamDto;
import com.wsdy.saasops.agapi.modules.dto.WalletFlowParamDto;
import com.wsdy.saasops.agapi.modules.service.FinancialCenterService;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.entity.AgyWithdraw;
import com.wsdy.saasops.modules.system.pay.service.CryptoCurrenciesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@RequestMapping("/agapi/n2/finance")
@Api(tags = "财务中心")
public class FinancialCenterController {

    @Autowired
    private FinancialCenterService financialCenterService;
    @Autowired
    private CryptoCurrenciesService cryptoCurrenciesService;
    @Autowired
    private RedisService redisService;
    @AgentLogin
    @GetMapping("agentWallet")
    @ApiOperation(value = "代理钱包", notes = "代理钱包")
    public R agentWallet(HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if (account.getAttributes() == Constants.EVNumber.four) {
            financialCenterService.financialCenterMenu(account);
        }
        return R.ok().put(financialCenterService.agentWallet(account));
    }

    @AgentLogin
    @GetMapping("walletFlow")
    @ApiOperation(value = "钱包流水-佣金钱包", notes = "钱包流水-佣金钱包")
    public R walletFlow(WalletFlowParamDto paramDto,
                        @RequestParam("pageNo") @NotNull Integer pageNo,
                        @RequestParam("pageSize") @NotNull Integer pageSize,
                        HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        return R.ok().putPage(financialCenterService.walletFlow(paramDto, account, pageNo, pageSize));
    }

    @AgentLogin
    @GetMapping("rechargeWalletFlow")
    @ApiOperation(value = "代充流水", notes = "代充流水")
    public R rechargeWalletFlow(WalletFlowParamDto paramDto,
                                @RequestParam("pageNo") @NotNull Integer pageNo,
                                @RequestParam("pageSize") @NotNull Integer pageSize,
                                HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        return R.ok().putPage(financialCenterService.rechargeWalletFlow(paramDto, account, pageNo, pageSize));
    }

    @AgentLogin
    @GetMapping("payoffWalletFlow")
    @ApiOperation(value = "彩金钱包流水", notes = "彩金钱包流水")
    public R payoffWalletFlow(WalletFlowParamDto paramDto,
                                @RequestParam("pageNo") @NotNull Integer pageNo,
                                @RequestParam("pageSize") @NotNull Integer pageSize,
                                HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        return R.ok().putPage(financialCenterService.payoffWalletFlow(paramDto, account, pageNo, pageSize));
    }

    @AgentLogin
    @PostMapping("withdrawal")
    @ApiOperation(value = "代理提款", notes = "代理提款")
    public R withdrawal(@RequestBody AgyWithdraw withdraw, HttpServletRequest request) {
        Assert.isNull(withdraw.getDrawingAmount(), "提款金额不为空");
        Assert.isBlank(withdraw.getSecurepwd(), "支付密码不能为空");
        if (Objects.nonNull(withdraw.getMethodType()) && Constants.EVNumber.one == withdraw.getMethodType().intValue()) {  // 加密货币
            Assert.isNull(withdraw.getCryptoCurrenciesId(), "取款钱包不能为空!");
        } else {
            Assert.isNull(withdraw.getBankCardId(), "取款银行不能为空!");
        }
        if (Objects.nonNull(withdraw.getMethodType()) && Constants.EVNumber.one == withdraw.getMethodType().intValue()) {  // 加密货币
            Assert.isNull(withdraw.getCryptoCurrenciesId(), "cryptoCurrenciesId不为null!");
            String rate = cryptoCurrenciesService.getExchangeRate("Withdrawal");
            withdraw.setExchangeRate(new BigDecimal(rate));
        } else if (Objects.nonNull(withdraw.getMethodType()) && Constants.EVNumber.zero == withdraw.getMethodType().intValue()){  // 银行卡
            Assert.isNull(withdraw.getBankCardId(), "bankcardId不为null!");
            withdraw.setMethodType(Constants.EVNumber.zero);
        } else {
        	withdraw.setMethodType(Constants.EVNumber.zero);
        }
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if (account.getAttributes() == Constants.EVNumber.four) {
            financialCenterService.financialCenterMenu(account);
        }
        String ip = CommonUtil.getIpAddress(request);
        withdraw.setWithdrawSource((byte) 0);
        financialCenterService.checkoutSecurePwd(account, withdraw.getSecurepwd());
        financialCenterService.withdrawal(withdraw, account, ip);
        return R.ok();
    }

    @AgentLogin
    @PostMapping("rechargeTransfer")
    @ApiOperation(value = "代充钱包转账", notes = "代充钱包转账")
    public R rechargeTransfer(@RequestBody RechargeTransferParamDto paramDto, HttpServletRequest request) {
        Assert.isNull(paramDto.getAmount(), "转账金额不为空");
        if (paramDto.getAmount().compareTo(BigDecimal.ZERO) != 1) {
            throw new R200Exception("金额必须大于0");
        }
        Assert.isBlank(paramDto.getLoginName(), "会员名不能为空");
        Assert.isBlank(paramDto.getSecurepwd(), "支付密码不能为空");
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if (account.getAttributes() == Constants.EVNumber.four) {
            financialCenterService.financialCenterMenu(account);
        }
        financialCenterService.checkoutSecurePwd(account, paramDto.getSecurepwd());
        financialCenterService.rechargeTransfer(paramDto, account);
        return R.ok();
    }

    @AgentLogin
    @PostMapping("payoffTransfer")
    @ApiOperation(value = "彩金钱包上分", notes = "彩金钱包上分")
    public R payoffTransfer(@RequestBody RechargeTransferParamDto paramDto, HttpServletRequest request) {
        Assert.isNull(paramDto.getAmount(), "上分金额不为空");
        if (paramDto.getAmount().compareTo(BigDecimal.ZERO) != 1) {
            throw new R200Exception("金额必须大于0");
        }

        Assert.isBlank(paramDto.getLoginName(), "会员名不能为空");
        Assert.isBlank(paramDto.getSecurepwd(), "支付密码不能为空");
        Assert.isBlank(paramDto.getAuditMultiple().toString(), "稽核倍数不能为空");
        if (paramDto.getAuditMultiple()<1){
            throw new R200Exception("稽核倍数必须大于0");
        }
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if (account.getAttributes() == Constants.EVNumber.four) {
            financialCenterService.financialCenterMenu(account);
        }
        String key = RedisConstants.PAY_OFF_TRANSFER + account.getId() + CommonUtil.getSiteCode();
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, account.getId(), 200, TimeUnit.SECONDS);
        if(isExpired) {
            try {
                financialCenterService.checkoutSecurePwd(account, paramDto.getSecurepwd());
                financialCenterService.payoffTransfer(paramDto, account);
            } finally {
                redisService.del(key);
            }
        }else{
            throw new R200Exception("正在处理中，请勿重复提交...");
        }
        return R.ok();
    }
    @AgentLogin
    @PostMapping("commissionTransfer")
    @ApiOperation(value = "佣金钱包转账", notes = "佣金钱包转账")
    public R commissionTransfer(@RequestBody RechargeTransferParamDto paramDto, HttpServletRequest request) {
        Assert.isNull(paramDto.getAmount(), "转账金额不为空");
        if (paramDto.getAmount().compareTo(BigDecimal.ZERO) != 1) {
            throw new R200Exception("金额必须大于0");
        }
        Assert.isNull(paramDto.getType(), "转账类型不能为空");
        Assert.isBlank(paramDto.getSecurepwd(), "支付密码不能为空");
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if (account.getAttributes() == Constants.EVNumber.four) {
            financialCenterService.financialCenterMenu(account);
        }
        financialCenterService.checkoutSecurePwd(account, paramDto.getSecurepwd());
        financialCenterService.commissionTransfer(paramDto, account);
        return R.ok();
    }
}
