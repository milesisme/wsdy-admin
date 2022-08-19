package com.wsdy.saasops.modules.agent.controller;

import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.agent.dto.DepotCostDto;
import com.wsdy.saasops.modules.agent.entity.AgyBillDetail;
import com.wsdy.saasops.modules.agent.entity.AgyCommission;
import com.wsdy.saasops.modules.agent.service.AgentReportService;
import com.wsdy.saasops.modules.agent.service.CommissionReportService;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;


@RestController
@RequestMapping("/bkapi/agent/repot")
@Api(tags = "代理提款列表")
public class AgentReportController extends AbstractController {

    @Autowired
    private AgentReportService reportService;
    @Autowired
    private CommissionReportService commissionReportService;


    @GetMapping("upperScoreRecord")
    @RequiresPermissions("agent:repot:upperScoreRecord")
    @ApiOperation(value = "代充记录", notes = "代充记录")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R upperScoreRecord(@ModelAttribute AgyBillDetail billDetail,
                              @RequestParam("pageNo") @NotNull Integer pageNo,
                              @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(reportService.upperScoreRecord(billDetail, pageNo, pageSize));
    }

    @GetMapping("agentAccountChange")
    @RequiresPermissions("agent:repot:agentChange")
    @ApiOperation(value = "账变流水", notes = "账变流水")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R agentAccountChange(@ModelAttribute AgyBillDetail billDetail,
                                @RequestParam("pageNo") @NotNull Integer pageNo,
                                @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(reportService.agentAccountChange(billDetail, pageNo, pageSize));
    }


    @GetMapping("commissionReviewList")
    @RequiresPermissions("agent:commission:reviewList")
    @ApiOperation(value = "代理佣金初审核列表", notes = "代理佣金初审核列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R commissionReviewList(@ModelAttribute AgyCommission commission,
                                  @RequestParam("pageNo") @NotNull Integer pageNo,
                                  @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(commissionReportService.commissionReviewList(commission, pageNo, pageSize));
    }

    @GetMapping("commissionDetails")
    @ApiOperation(value = "代理佣金详情", notes = "代理佣金详情")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R commissionDetails(@ModelAttribute AgyCommission commission) {
        return R.ok().put(commissionReportService.commissionDetails(commission));
    }

    @GetMapping("updateReviewStatus")
    @RequiresPermissions("agent:commission:updateReviewStatus")
    @ApiOperation(value = "代理佣金初审核变更状态", notes = "代理佣金初审核变更状态")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R updateReviewStatus(@ModelAttribute AgyCommission commission) {
        commissionReportService.updateReviewStatus(commission, getUser().getUsername());
        return R.ok();
    }

    @GetMapping("commissionFreedList")
    @RequiresPermissions("agent:commission:freedList")
    @ApiOperation(value = "代理佣金发放列表", notes = "代理佣金发放列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R commissionFreedList(@ModelAttribute AgyCommission commission,
                                 @RequestParam("pageNo") @NotNull Integer pageNo,
                                 @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(commissionReportService.commissionFreedList(commission, pageNo, pageSize));
    }
    
    @GetMapping("commissionAllSubList")
    @RequiresPermissions("agent:commission:allSubList")
    @ApiOperation(value = "下级代理佣金", notes = "下级代理佣金")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
    , @ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R commissionAllSubList(@ModelAttribute AgyCommission commission,
    		@RequestParam("pageNo") @NotNull Integer pageNo,
    		@RequestParam("pageSize") @NotNull Integer pageSize) {
    	return R.ok().putPage(commissionReportService.commissionAllSubList(commission, pageNo, pageSize));
    }

    @GetMapping("updateFreedStatus")
    @RequiresPermissions("agent:commission:updateFreedStatus")
    @ApiOperation(value = "代理佣金初审核变更状态", notes = "代理佣金初审核变更状态")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R updateFreedStatus(@ModelAttribute AgyCommission commission) {
        commissionReportService.updateFreedStatus(commission, getUser().getUsername());
        return R.ok();
    }


    @GetMapping("commissionReport")
    @RequiresPermissions("agent:commission:commissionReport")
    @ApiOperation(value = "代理佣金报表", notes = "代理佣金报表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R commissionReport(@ModelAttribute AgyCommission commission,
                              @RequestParam("pageNo") @NotNull Integer pageNo,
                              @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(commissionReportService.commissionReport(commission, pageNo, pageSize));
    }

    @Deprecated
    @GetMapping("depotCostList")
    @RequiresPermissions("agent:commission:depotCost")
    @ApiOperation(value = "平台费报表", notes = "平台费报表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R depotCostList(@ModelAttribute DepotCostDto dto,
                              @RequestParam("pageNo") @NotNull Integer pageNo,
                              @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(commissionReportService.depotCostList(dto, pageNo, pageSize));
    }

    @Deprecated
    @GetMapping("sumDepotCost")
    @RequiresPermissions("agent:commission:depotCost")
    @ApiOperation(value = "平台费报表总计", notes = "平台费报表总计")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R sumDepotCost(@ModelAttribute DepotCostDto dto) {
        return R.ok().put(commissionReportService.sumDepotCost(dto));
    }

    @Deprecated
    @GetMapping("depotCostDetail")
    @RequiresPermissions("agent:commission:depotCost")
    @ApiOperation(value = "平台费报表明细", notes = "平台费报表明细")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R depotCostDetail(@ModelAttribute DepotCostDto dto) {
        return R.ok().put(commissionReportService.depotCostDetail(dto));
    }
}
