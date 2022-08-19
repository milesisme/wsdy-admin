package com.wsdy.saasops.agapi.modules.controller;

import com.wsdy.saasops.agapi.annotation.AgentLogin;
import com.wsdy.saasops.agapi.modules.service.AgentService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.common.validator.ValidRegUtils;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.service.AgentAccountService;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.system.systemsetting.entity.SysSetting;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import static com.wsdy.saasops.common.utils.CommonUtil.getIpAddress;


@RestController
@RequestMapping("/agapi/agent")
@Api(tags = "代理列表")
public class AgentModuleController extends AbstractController {

    @Autowired
    private AgentAccountService accountService;

    @Autowired
    private AgentService agentService;

    @AgentLogin
    @GetMapping("/agyAccountList")
    @ApiOperation(value = "代理列表", notes = "代理列表")
    public R agyAccountList(@ModelAttribute AgentAccount agentAccount,
                            @RequestParam("pageNo") @NotNull Integer pageNo,
                            @RequestParam("pageSize") @NotNull Integer pageSize,
                            @RequestParam(value = "orderBy", required = true) String orderBy) {
        return R.ok().putPage(accountService.agyAccountList(agentAccount, pageNo, pageSize, orderBy));
    }

    @AgentLogin
    @GetMapping("/getAgentBanner")
    @ApiOperation(value = "获取面包屑导航-代理", notes = "获取面包屑导航-代理")
    public R getAgentBanner(@ModelAttribute AgentAccount agentAccount) {
        return R.ok().put(accountService.getAgentBanner(agentAccount));
    }

    @AgentLogin
    @GetMapping("/getMbrBanner")
    @ApiOperation(value = "获取面包屑导航-会员", notes = "获取面包屑导航-会员")
    public R getMbrBanner(@ModelAttribute AgentAccount agentAccount) {
        return R.ok().put(accountService.getMbrBanner(agentAccount));
    }

    @AgentLogin
    @PostMapping("/agyAccountSave")
    @ApiOperation(value = "代理列表代理新增", notes = "代理列表代理新增")
    public R agyAccountSave(@RequestBody AgentAccount agentAccount, HttpServletRequest request) {
        Assert.isBlank(agentAccount.getAgyAccount(), "帐号不能为空");
        ValidRegUtils.validAgentName(agentAccount.getAgyAccount(), SysSetting.SysValueConst.require);
        Assert.isBlank(agentAccount.getAgyPwd(), "密码不能为空");
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        agentAccount.setIp(getIpAddress(request));
        agentService.agyAccountSave(agentAccount, account);
        return R.ok();
    }

    @AgentLogin
    @GetMapping("/agyAccountUpdate")
    @ApiOperation(value = "代理列表代理编辑", notes = "代理列表代理编辑")
    public R agyAccountUpdate(@ModelAttribute AgentAccount agentAccount) {
        Assert.isNull(agentAccount.getId(), "代理id不能为空");
        agentService.agyAccountUpdate(agentAccount);
        return R.ok();
    }

    @AgentLogin
    @GetMapping("/updateAvailable")
    @ApiOperation(value = "修改代理状态", notes = "修改代理状态")
    public R updateAvailable(@ModelAttribute AgentAccount agentAccount) {
        Assert.isNull(agentAccount.getId(), "代理id不能为空");
        Assert.isNull(agentAccount.getAvailable(), "状态不能为空");
        agentService.agyUpdateAvailable(agentAccount);
        return R.ok();
    }

    @GetMapping("/agyAccountDelete")
    @ApiOperation(value = "代理删除", notes = "代理删除")
    public R agyAccountDelete(@RequestParam("id") Integer id) {
        Assert.isNull(id, "代理id不能为空");
        accountService.agyAccountDelete(id,Constants.EVNumber.zero);
        return R.ok();
    }

    @GetMapping("/findAllSubAgency")
    @ApiOperation(value = "查找所有子代理", notes = "查找所有子代理")
    public R findAllSubAgency(@RequestParam("agyId") Integer agyId) {
        return R.ok().put("accounts", accountService.findSubAgencyByName(agyId));
    }

    @GetMapping("/queryAllDomains")
    @ApiOperation(value = "查询所有推广域名", notes = "查询所有推广域名")
    public R queryAllDomains() {
        return R.ok().put("list", agentService.queryAllDomains());
    }

    @GetMapping("/findDomainsById")
    @ApiOperation(value = "查询当前代理推广域名", notes = "查询当前代理推广域名")
    public R findDomainsById(@RequestParam("id") Integer id, @RequestParam("pageNo") @NotNull Integer pageNo,
                             @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().put("unbase", agentService.findNoBaseDomains(id, pageNo, pageSize)).put("base", agentService.findBaseDomains(id, CommonUtil.getSiteCode()));
    }

}
