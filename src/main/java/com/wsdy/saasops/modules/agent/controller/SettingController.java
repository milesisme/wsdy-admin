package com.wsdy.saasops.modules.agent.controller;

import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.agent.dto.SettingAgentDto;
import com.wsdy.saasops.modules.agent.entity.AgyDomain;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.agent.service.CommissionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;


@RestController
@RequestMapping("/bkapi/agent/setting")
@Api(tags = "佣金方案设置 代理设置")
public class SettingController extends AbstractController {

    @Autowired
    private CommissionService commissionService;

    @GetMapping("commDepotList")
    @ApiOperation(value = "佣金设置，查询所有平台", notes = "佣金设置，查询所有平台")
    public R commDepotList() {
        return R.ok().put(commissionService.findDpotList(CommonUtil.getSiteCode()));
    }


    @GetMapping("agentInfo")
    @RequiresPermissions(value = {"setting:agent:info", "setting:agent:info2"}, logical = Logical.OR)
    @ApiOperation(value = "代理设置查看", notes = "代理设置查看")
    public R agentInfo() {
        return R.ok().put(commissionService.agentInfo());
    }

    @PostMapping("agentRegister")
    @RequiresPermissions("setting:agent:register")
    @ApiOperation(value = "代理注册设置", notes = "代理注册设置")
    public R agentRegister(@RequestBody SettingAgentDto settingAgentDto, HttpServletRequest request) {
        Assert.isNull(settingAgentDto, "数据不能为空");
        commissionService.agentRegister(settingAgentDto, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @GetMapping("domain/list")
    @RequiresPermissions("agent:domain:list")
    @ApiOperation(value = "代理域名查询", notes = "代理域名查询")
    public R agentDomainList(@ModelAttribute AgyDomain agyDomain,
                             @RequestParam("pageNo") @NotNull Integer pageNo,
                             @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(commissionService.agentDomainList(agyDomain, pageNo, pageSize));
    }

    @GetMapping("domain/delete")
    @RequiresPermissions("agent:domain:delete")
    @ApiOperation(value = "代理域名删除", notes = "代理域名删除")
    public R agentDomainDelete(@RequestParam("id") Integer id) {
        Assert.isNull(id, "id不能为空");
        commissionService.agentDomainDelete(id, getUser().getUsername());
        return R.ok();
    }

    @PostMapping("domain/updateAvailable")
    @RequiresPermissions("agent:domain:update")
    @ApiOperation(value = "代理域名修改状态", notes = "代理域名修改状态")
    public R updateDomainAvailable(@RequestBody AgyDomain agyDomain, HttpServletRequest request) {
        Assert.isNull(agyDomain.getId(), "id不能为空");
        Assert.isNull(agyDomain.getAvailable(), "状态不能为空");
        commissionService.updateDomainAvailable(agyDomain, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @PostMapping("domain/updateAudit")
    @RequiresPermissions("agent:domain:audit")
    @ApiOperation(value = "代理域名审核", notes = "代理域名审核")
    public R updateDomainAudit(@RequestBody AgyDomain agyDomain) {
        Assert.isNull(agyDomain.getId(), "id不能为空");
        Assert.isNull(agyDomain.getStatus(), "状态不能为空");
        commissionService.updateDomainAudit(agyDomain, getUser().getUsername());
        return R.ok();
    }

    @PostMapping("domain/save")
    @RequiresPermissions("agent:domain:save")
    @ApiOperation(value = "代理域名新增", notes = "代理域名新增")
    public R domainSave(@RequestBody AgyDomain agyDomain) {
        Assert.isNull(agyDomain.getDomainUrl(), "域名不能为空");
        Assert.isNull(agyDomain.getAccountId(), "代理id能为空");
        commissionService.domainSave(agyDomain, getUser().getUsername());
        return R.ok();
    }

    @PostMapping("domain/update")
    @RequiresPermissions("agent:domain:update")
    @ApiOperation(value = "代理域名update", notes = "代理域名update")
    public R domainUpdate(@RequestBody AgyDomain agyDomain) {
        Assert.isNull(agyDomain.getId(), "id不能为空");
        commissionService.domainUpdate(agyDomain, getUser().getUsername());
        return R.ok();
    }
}
