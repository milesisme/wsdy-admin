package com.wsdy.saasops.modules.agent.controller;

import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.agent.entity.AgyDomain;
import com.wsdy.saasops.modules.agent.service.AgentDomainService;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;


@RestController
@RequestMapping("/bkapi/agent/domain")
@Api(tags = "代理域名")
public class AgentDomainController extends AbstractController {

    @Autowired
    private AgentDomainService agyDomainService;

    @GetMapping("domainList")
    @ApiOperation(value = "查询可以分配的域名", notes = "查询可以分配的域名")
    public R domainList() {
        return R.ok().put(agyDomainService.domainList(CommonUtil.getSiteCode()));
    }

    @GetMapping("domainSubList")
    @ApiOperation(value = "查询可以分配的子域名", notes = "查询可以分配的子域名")
    public R domainSubList() {
        return R.ok().put(agyDomainService.domainSubList(CommonUtil.getSiteCode()));
    }

    @GetMapping("/agyDomainList")
    @RequiresPermissions("agent:domain:info")
    @ApiOperation(value = "代理域名列表", notes = "代理域名列表")
    public R agyAccountList(@ModelAttribute AgyDomain agyDomain,
                            @RequestParam("pageNo") @NotNull Integer pageNo,
                            @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(agyDomainService.agyDomainList(agyDomain, pageNo, pageSize));
    }

    @PostMapping("/save")
    @RequiresPermissions("agent:domain:save")
    @ApiOperation(value = "代理域名新增", notes = "代理域名新增")
    public R agyAccountSave(@RequestBody AgyDomain agyDomain, HttpServletRequest request) {
        Assert.isBlank(agyDomain.getAgyAccount(), "代理帐号不能为空");
        if (agyDomain.getDomainUrlList().size() == 0) {
            throw new R200Exception("域名不能为空");
        }
        agyDomainService.agyDomainSave(agyDomain, getUser().getUsername(), Constants.EVNumber.one);
        return R.ok();
    }


    @PostMapping("/audit")
    @RequiresPermissions("agent:domain:audit")
    @ApiOperation(value = "代理域名审核", notes = "代理域名审核")
    public R agyDomainAudit(@RequestBody AgyDomain agyDomain) {
        Assert.isNull(agyDomain.getId(), "代理域名id不能为空");
        Assert.isNull(agyDomain.getStatus(), "状态不能为空");
        agyDomainService.agyDomainAudit(agyDomain, getUser().getUsername(), CommonUtil.getSiteCode());
        return R.ok();
    }

    @PostMapping("/available")
    @RequiresPermissions("agent:domain:available")
    @ApiOperation(value = "代理域名禁用启用", notes = "代理域名禁用启用")
    public R updateAvailable(@RequestBody AgyDomain agyDomain) {
        Assert.isNull(agyDomain.getId(), "id不能为空!");
        Assert.isNull(agyDomain.getAvailable(), "状态不能为空!");
        agyDomainService.updateAvailable(agyDomain);
        return R.ok();
    }

    @PostMapping("/delete")
    @RequiresPermissions("agent:domain:delete")
    @ApiOperation(value = "代理域名删除", notes = "代理域名删除")
    public R agyDomainDelete(@RequestBody AgyDomain agyDomain) {
        if (agyDomain.getIds().size() == 0) {
            throw new R200Exception("代理域名id不能为空");
        }
        agyDomainService.agyDomainDelete(agyDomain, CommonUtil.getSiteCode());
        return R.ok();
    }

}
