package com.wsdy.saasops.modules.agent.controller;

import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.agent.dto.DepotCostDto;
import com.wsdy.saasops.modules.agent.service.AgentWaterCostService;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;


@RestController
@RequestMapping("/bkapi/agent/cost")
@Api(tags = "平台费率")
public class AgentWaterCostController extends AbstractController {

    @Autowired
    private AgentWaterCostService waterCostService;

    @GetMapping("/findCostReportViewAgent")
    @RequiresPermissions("agent:commission:depotCost")
    @ApiOperation(value = "视图--直属代理", notes = "视图--直属代理")
    public R findWinLostReportViewAgent(@ModelAttribute DepotCostDto dto,
                                        @RequestParam("pageNo") @NotNull Integer pageNo,
                                        @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(waterCostService.findCostReportViewAgent(dto, pageNo, pageSize));
    }

    @GetMapping("/findCostLostReportView")
    @RequiresPermissions("agent:commission:depotCost")
    @ApiOperation(value = "代理的视图切换表头", notes = "代理的视图切换表头")
    public R findCostLostReportView(@ModelAttribute DepotCostDto dto) {
        return R.ok().putPage(waterCostService.findCostLostReportView(dto));
    }

    @GetMapping("/findCostAccountListLevel")
    @RequiresPermissions("agent:commission:depotCost")
    @ApiOperation(value = "直属会员列表", notes = "直属会员")
    public R findCostListLevel(@ModelAttribute DepotCostDto dto,
                                  @RequestParam("pageNo") @NotNull Integer pageNo,
                                  @RequestParam("pageSize") @NotNull Integer pageSize) {
        Assert.isBlank(dto.getAgyAccount(), "代理账号不能为空");
        return R.ok().putPage(waterCostService.findCostListLevel(dto, pageNo, pageSize));
    }

    @GetMapping("/findCostAccountDetails")
    @RequiresPermissions("agent:commission:depotCost")
    @ApiOperation(value = "直属会员详情", notes = "直属会员详情")
    public R findCostAccountDetails(@ModelAttribute DepotCostDto dto) {
        Assert.isBlank(dto.getLoginName(), "会员账号不能为空");
        return R.ok().putPage(waterCostService.findCostAccountDetails(dto));
    }
}
