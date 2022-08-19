package com.wsdy.saasops.agapi.modulesV2.controller;

import com.wsdy.saasops.agapi.annotation.AgentLogin;
import com.wsdy.saasops.agapi.modulesV2.dto.AccountFundDto;
import com.wsdy.saasops.agapi.modulesV2.dto.AgentFundDto;
import com.wsdy.saasops.agapi.modulesV2.dto.BillRecordListDto;
import com.wsdy.saasops.agapi.modulesV2.service.AgentFundService;
import com.wsdy.saasops.api.modules.transfer.service.TransferService;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.member.entity.MbrBillManage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.isNull;


@RestController
@Slf4j
@RequestMapping("/agapi/v2")
@Api(tags = "c存取点")
public class AgentFundController extends AbstractController {

    @Autowired
    private AgentFundService agentFundService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private TransferService transferService;

    @AgentLogin
    @GetMapping("agentFundList")
    @ApiOperation(value = "代理存取点数列表", notes = "存取点数列表")
    public R agentFundList(@ModelAttribute AgentFundDto dto,
                           @RequestParam("pageNo") @NotNull Integer pageNo,
                           @RequestParam("pageSize") @NotNull Integer pageSize,
                           HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if (isNull(dto.getAccountId())) {
            dto.setAgentId(account.getId());
        } else {
            dto.setAgentId(dto.getAccountId());
        }
        return R.ok().putPage(agentFundService.agentFundList(dto, pageNo, pageSize));
    }

    @AgentLogin
    @PostMapping("updateAgentBalance")
    @ApiOperation(value = "代理存取点数设置", notes = "代理存取点数设置")
    public R updateAgentBalance(@RequestBody AccountFundDto dto,
                                HttpServletRequest request) {
        Assert.isNull(dto.getAccountId(), "帐号ID不能为空");
        Assert.isNull(dto.getBalance(), "金额不能为空");
        Assert.isNull(dto.getIsBalance(), "操作标识不能为空");
        if (dto.getBalance().compareTo(BigDecimal.ZERO) != 1) {
            throw new R200Exception("金额必须大于0");
        }
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        String key = RedisConstants.EG_AGENT_BALANCE + CommonUtil.getSiteCode() + dto.getAccountId();
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, account.getAgyAccount(), 30, TimeUnit.MINUTES);
        if (Boolean.TRUE.equals(isExpired)) {
            try {
                agentFundService.updateAgentBalance(account, dto);
            } finally {
                redisService.del(key);
            }
        }
        return R.ok();
    }

    @AgentLogin
    @GetMapping("accountFundList")
    @ApiOperation(value = "代理直属会员存取点数列表", notes = "代理直属会员存取点数列表")
    public R accountFundList(@ModelAttribute AgentFundDto dto,
                             @RequestParam("pageNo") @NotNull Integer pageNo,
                             @RequestParam("pageSize") @NotNull Integer pageSize,
                             HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if (isNull(dto.getAccountId())) {
            dto.setAgentId(account.getId());
        } else {
            dto.setAgentId(dto.getAccountId());
        }
        return R.ok().putPage(agentFundService.accountFundList(dto, pageNo, pageSize));
    }

    @AgentLogin
    @GetMapping("accountDepotBalance")
    @ApiOperation(value = "查询会员平台余额", notes = "查询会员平台余额")
    public R accountFundList(@ModelAttribute AgentFundDto dto) {
        return R.ok().put(agentFundService.accountDepotBalance(dto.getAccountId()));
    }

    @AgentLogin
    @PostMapping("updateAccountBalance")
    @ApiOperation(value = "代理直属会员存取点数设置", notes = "代理直属会员存取点数设置")
    public R updateAccountBalance(@RequestBody AccountFundDto dto,
                                  HttpServletRequest request) {
        Assert.isNull(dto.getAccountId(), "帐号ID不能为空");
        Assert.isNull(dto.getBalance(), "金额不能为空");
        Assert.isNull(dto.getIsBalance(), "操作标识不能为空");
        if (dto.getBalance().compareTo(BigDecimal.ZERO) != 1) {
            throw new R200Exception("金额必须大于0");
        }
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        String key = RedisConstants.EG_ACCOUNT_BALANCE + CommonUtil.getSiteCode() + dto.getAccountId();
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, account.getAgyAccount(), 30, TimeUnit.MINUTES);
        if (Boolean.TRUE.equals(isExpired)) {
            try {
                agentFundService.updateAccountBalance(account, dto);
            } finally {
                redisService.del(key);
            }
        }
        return R.ok();
    }

    @AgentLogin
    @GetMapping("billList")
    @ApiOperation(value = "转账报表查询列表", notes = "转账报表查询列表")
    public R billList(@ModelAttribute MbrBillManage mbrBillManage,
                      @RequestParam("pageNo") @NotNull Integer pageNo,
                      @RequestParam("pageSize") @NotNull Integer pageSize,
                      HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        mbrBillManage.setTagencyId(account.getId());
        return R.ok().putPage(agentFundService.billListPage(mbrBillManage, pageNo, pageSize));
    }

    @GetMapping("/apiTrfRefresh")
    @ApiOperation(value = "会员第三方接口订单状态查询", notes = "会员第三方接口订单状态查询")
    public R apiTrfRefresh(@RequestParam("orderNo") Long orderNo) {
        Assert.isNull(orderNo, "订单号不能为空!");
        return transferService.checkTransfer(orderNo, CommonUtil.getSiteCode());
    }


    @AgentLogin
    @GetMapping("billRecordList")
    @ApiOperation(value = "账变记录", notes = "账变记录")
    public R billRecordList(@ModelAttribute BillRecordListDto recordListDto,
                            @RequestParam("pageNo") @NotNull Integer pageNo,
                            @RequestParam("pageSize") @NotNull Integer pageSize,
                            HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        recordListDto.setAgentId(account.getId());
        return R.ok().putPage(agentFundService.billRecordList(recordListDto, pageNo, pageSize));
    }
}
