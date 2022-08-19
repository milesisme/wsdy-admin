package com.wsdy.saasops.modules.agent.controller;

import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.agent.dto.AgentChargeMDto;
import com.wsdy.saasops.modules.agent.service.AgentServiceChargService;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;


@RestController
@RequestMapping("/bkapi/agent/charge")
@Api(tags = "平台服务费报表")
public class AgentServiceChargeController extends AbstractController {


    @Autowired
    private AgentServiceChargService serviceChargService;

    @GetMapping("/findServiceChargAgent")
    @RequiresPermissions("agent:commission:servicecharg")
    @ApiOperation(value = "代理服务费", notes = "代理服务费")
    public R findServiceChargAgent(@ModelAttribute AgentChargeMDto dto,
                                   @RequestParam("pageNo") @NotNull Integer pageNo,
                                   @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(serviceChargService.findServiceChargAgent(dto, pageNo, pageSize));
    }

    @GetMapping("/findServiceChargAccount")
    @RequiresPermissions("agent:commission:servicecharg")
    @ApiOperation(value = "代理服务费-会员", notes = "代理服务费-会员")
    public R findServiceChargAccount(@ModelAttribute AgentChargeMDto dto,
                                     @RequestParam("pageNo") @NotNull Integer pageNo,
                                     @RequestParam("pageSize") @NotNull Integer pageSize) {
        Assert.isBlank(dto.getAgyAccount(), "代理名称不能为空");
        return R.ok().putPage(serviceChargService.findServiceChargAccount(dto, pageNo, pageSize));
    }

}
