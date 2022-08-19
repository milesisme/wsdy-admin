package com.wsdy.saasops.agapi.modules.controller;

import com.wsdy.saasops.agapi.annotation.AgentLogin;
import com.wsdy.saasops.agapi.modules.dto.DirectMemberParamDto;
import com.wsdy.saasops.agapi.modules.service.AgentTeamService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.entity.AgentAccountMemo;
import com.wsdy.saasops.modules.agent.service.AgentAccountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

@RestController
@Slf4j
@RequestMapping("/agapi/n2")
@Api(tags = "团队管理")
public class AgentTeamController {

    @Autowired
    private AgentTeamService teamService;
    @Autowired
    private AgentAccountService accountService;

    @AgentLogin
    @GetMapping("/directMember")
    @ApiOperation(value = "直属会员", notes = "直属会员")
    public R directMember(@ModelAttribute DirectMemberParamDto paramDto,
                          @RequestParam("pageNo") @NotNull Integer pageNo,
                          @RequestParam("pageSize") @NotNull Integer pageSize,
                          HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        return R.ok().putPage(teamService.directMember(account, paramDto, pageNo, pageSize));
    }
    
    @AgentLogin
    @GetMapping("/directMemberHead")
    @ApiOperation(value = "直属会员头部", notes = "直属会员头部")
    public R directMemberHead(@ModelAttribute DirectMemberParamDto paramDto,
                          HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        return R.ok().put(teamService.directMemberHead(account, paramDto));
    }
    @AgentLogin
    @GetMapping("/accountTransfer")
    @ApiOperation(value = "直属会员-转账", notes = "直属会员-转账")
    public R updateAccountMemo(@ModelAttribute DirectMemberParamDto paramDto,
                               HttpServletRequest request) {
        Assert.isNull(paramDto.getAccountId(), "会员ID不能为空");
        Assert.isNull(paramDto.getAmount(), "转账金额不能为空");
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if (account.getAttributes() == Constants.EVNumber.four) {
            throw new R200Exception("无此权限");
        }
        teamService.accountTransfer(account, paramDto);
        return R.ok();
    }

    @AgentLogin
    @GetMapping("/updateAccountMemo")
    @ApiOperation(value = "直属会员-编辑会员备注", notes = "直属会员-详情")
    public R updateAccountMemo(@ModelAttribute AgentAccountMemo accountMemo) {
        Assert.isNull(accountMemo.getAccountId(), "会员ID不能为空");
        Assert.isNull(accountMemo.getNumbering(), "编号不能为空");
        Assert.isNull(accountMemo.getMemoType(), "备注类型不能为空");
        teamService.updateAccountMemo(accountMemo);
        return R.ok();
    }

    @AgentLogin
    @GetMapping("/memberDetails")
    @ApiOperation(value = "直属会员-详情-上", notes = "直属会员-详情-上")
    public R memberDetails(@ModelAttribute DirectMemberParamDto paramDto,
                           HttpServletRequest request) {
        Assert.isNull(paramDto.getLoginName(), "会员名不能为空");
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        return R.ok().put(teamService.memberDetails(account, paramDto.getLoginName()));
    }

    @AgentLogin
    @GetMapping("/memberBillRecordList")
    @ApiOperation(value = "直属会员-详情-下资金流水", notes = "直属会员-详情-下资金流水")
    public R memberBillRecordList(@ModelAttribute DirectMemberParamDto paramDto,
                                  @RequestParam("pageNo") @NotNull Integer pageNo,
                                  @RequestParam("pageSize") @NotNull Integer pageSize,
                                  HttpServletRequest request) {
        Assert.isNull(paramDto.getLoginName(), "会员名不能为空");
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        return R.ok().putPage(teamService.memberBillRecordList(paramDto, pageNo, pageSize));
    }

    @AgentLogin
    @GetMapping("/subAgentList")
    @ApiOperation(value = "直属会员-下级代理", notes = "直属会员-下级代理")
    public R subAgentList(@ModelAttribute DirectMemberParamDto paramDto,
                          @RequestParam("pageNo") @NotNull Integer pageNo,
                          @RequestParam("pageSize") @NotNull Integer pageSize,
                          HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if (account.getAttributes() == Constants.EVNumber.four) {
            throw new R200Exception("无此权限");
        }
        paramDto.setSubAgentId(account.getId());
        return R.ok().putPage(teamService.subAgentList(paramDto, pageNo, pageSize));
    }

    @AgentLogin
    @GetMapping("/subAgentHead")
    @ApiOperation(value = "直属会员-下级代理头部", notes = "直属会员-下级代理头部")
    public R subAgentHead(@ModelAttribute DirectMemberParamDto paramDto,
                          HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if (account.getAttributes() == Constants.EVNumber.four) {
            throw new R200Exception("无此权限");
        }
        paramDto.setSubAgentId(account.getId());
        return R.ok().put(teamService.subAgentHead(paramDto));
    }

    @AgentLogin
    @GetMapping("/aentAccountList")
    @ApiOperation(value = "下级代理-查询直属会员", notes = "下级代理-查询直属会员")
    public R aentAccountList(@ModelAttribute DirectMemberParamDto paramDto,
                             @RequestParam("pageNo") @NotNull Integer pageNo,
                             @RequestParam("pageSize") @NotNull Integer pageSize) {
        Assert.isNull(paramDto.getAgyAccount(), "代理名不能为空");
        return R.ok().putPage(teamService.aentAccountList(paramDto, pageNo, pageSize));
    }

    @AgentLogin
    @GetMapping("/superiorCloneList")
    @ApiOperation(value = "分线代理", notes = "分线代理")
    public R superiorCloneList(@ModelAttribute DirectMemberParamDto paramDto,
                               @RequestParam("pageNo") @NotNull Integer pageNo,
                               @RequestParam("pageSize") @NotNull Integer pageSize,
                               HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if (account.getAttributes() == Constants.EVNumber.four) {
            throw new R200Exception("无此权限");
        }
        paramDto.setCagencyId(account.getId());
        return R.ok().putPage(teamService.superiorCloneList(paramDto, pageNo, pageSize));
    }

    @AgentLogin
    @GetMapping("/superiorCloneHead")
    @ApiOperation(value = "分线代理头部", notes = "分线代理头部")
    public R superiorCloneHead(@ModelAttribute DirectMemberParamDto paramDto,
                               HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if (account.getAttributes() == Constants.EVNumber.four) {
            throw new R200Exception("无此权限");
        }
        paramDto.setCagencyId(account.getId());
        return R.ok().put(teamService.superiorCloneHead(paramDto));
    }
    @AgentLogin
    @GetMapping("/superiorCloneAccountList")
    @ApiOperation(value = "分线代理-直属会员", notes = "分线代理-直属会员")
    public R superiorCloneAccountList(@ModelAttribute DirectMemberParamDto paramDto,
                                      @RequestParam("pageNo") @NotNull Integer pageNo,
                                      @RequestParam("pageSize") @NotNull Integer pageSize) {
        Assert.isNull(paramDto.getAgyAccount(), "代理名不能为空");
        return R.ok().putPage(teamService.superiorCloneAccountList(paramDto, pageNo, pageSize));
    }

    @GetMapping("/getAgentBanner")
    @ApiOperation(value = "获取面包屑导航-代理", notes = "获取面包屑导航-代理")
    public R getAgentBanner(@ModelAttribute AgentAccount agentAccount) {
        return R.ok().put(accountService.getAgentBanner(agentAccount));
    }

    @AgentLogin
    @GetMapping("/accountDepositList")
    @ApiOperation(value = "直属会员-详情-存款记录", notes = "直属会员-详情-存款记录")
    public R accountDepositList(@ModelAttribute DirectMemberParamDto paramDto,
                                @RequestParam("pageNo") @NotNull Integer pageNo,
                                @RequestParam("pageSize") @NotNull Integer pageSize) {
        Assert.isNull(paramDto.getLoginName(), "会员名不能为空");
        return R.ok().putPage(teamService.accountDepositList(paramDto, pageNo, pageSize));
    }

    @AgentLogin
    @GetMapping("/accountWithdrawList")
    @ApiOperation(value = "直属会员-详情-提款记录", notes = "直属会员-详情-提款记录")
    public R accountWithdrawList(@ModelAttribute DirectMemberParamDto paramDto,
                                @RequestParam("pageNo") @NotNull Integer pageNo,
                                @RequestParam("pageSize") @NotNull Integer pageSize) {
        Assert.isNull(paramDto.getLoginName(), "会员名不能为空");
        return R.ok().putPage(teamService.accountWithdrawList(paramDto, pageNo, pageSize));
    }

}
