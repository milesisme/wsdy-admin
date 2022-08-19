package com.wsdy.saasops.agapi.modules.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import com.wsdy.saasops.agapi.modules.dto.ReportResultDto;
import com.wsdy.saasops.common.utils.StringUtil;
import com.wsdy.saasops.modules.agent.dto.CostTotalDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wsdy.saasops.agapi.annotation.AgentLogin;
import com.wsdy.saasops.agapi.modules.dto.ReportParamsDto;
import com.wsdy.saasops.agapi.modules.service.AgentFinaceReportService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.base.controller.AbstractController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.math.BigDecimal;

@RestController
@RequestMapping("/agapi/v2")
@Api(tags = "财务中心报表")
public class AgentFinanceReportController extends AbstractController {

    @Autowired
    private AgentFinaceReportService finaceReportService;

    @AgentLogin
    @GetMapping("/agentFinanceReportList")
    @ApiOperation(value = "代理存取点数列表", notes = "存取点数列表")
    public R agentFinanceReportList(@ModelAttribute ReportParamsDto dto,
                                    @RequestParam("pageNo") @NotNull Integer pageNo,
                                    @RequestParam("pageSize") @NotNull Integer pageSize,
                                    HttpServletRequest request) {
        Assert.isBlank(dto.getStartTime(), "开始时间不能为空");
        Assert.isBlank(dto.getEndTime(), "结束时间不能为空");
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        dto.setCagencyId(account.getId());
        // 如果是分线代理
        if (account.getAttributes() == Constants.EVNumber.one) {
            dto.setCagencyId(null);
            dto.setSubCagencyId(account.getId());
        }
        dto.setAgyAccount(account.getAgyAccount());
        return R.ok().putPage(finaceReportService.agentFinanceReportList(dto, pageNo, pageSize));
    }

    @AgentLogin
    @GetMapping("/agentFinanceReportTotal")
    @ApiOperation(value = "代理存取点数统计", notes = "存取点数统计")
	public R agentFinanceReportTotal(@ModelAttribute ReportParamsDto dto, HttpServletRequest request) {
		Assert.isBlank(dto.getStartTime(), "开始时间不能为空");
		Assert.isBlank(dto.getEndTime(), "结束时间不能为空");
		AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
		dto.setCagencyId(account.getId());
		if (account.getAttributes() == Constants.EVNumber.one) {
			dto.setCagencyId(null);
			dto.setSubCagencyId(account.getId());
		}
		dto.setAgyAccount(account.getAgyAccount());
		ReportResultDto resultDto = finaceReportService.agentFinanceReportTotal(dto);
		
		BigDecimal totalProfit = resultDto.getTotalProfit();
		
		// 不是分线代理
		if (account.getAttributes() != Constants.EVNumber.one) {
			CostTotalDto cost = finaceReportService.depotCostTotalForSingle(dto);
			if (StringUtil.isEmpty(cost.getCost())) {
				cost.setCost(BigDecimal.ZERO);
			}
			// 扣除平台费，服务费
			if (null != resultDto) {
				totalProfit = totalProfit.subtract(cost.getCost()).subtract(cost.getServiceCost());
			}
		}
		resultDto.setTotalProfit(totalProfit);
		return R.ok().putPage(resultDto);
	}

    @AgentLogin
    @GetMapping("/depotCostDetailList")
    @ApiOperation(value = "平台费报表明细", notes = "平台费报表明细")
    public R depotCostDetailList(@ModelAttribute ReportParamsDto dto, @RequestParam("pageNo") @NotNull Integer pageNo,
                                 @RequestParam("pageSize") @NotNull Integer pageSize, HttpServletRequest request) {
        Assert.isBlank(dto.getStartTime(), "开始时间不能为空");
        Assert.isBlank(dto.getEndTime(), "结束时间不能为空");
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        dto.setAgyAccount(account.getAgyAccount());
        return R.ok().put(finaceReportService.depotCostDetailList(dto, pageNo, pageSize));
    }

    @AgentLogin
    @GetMapping("/depotCostTotal")
    @ApiOperation(value = "平台费报表统计", notes = "平台费报表统计")
    public R depotCostTotal(@ModelAttribute ReportParamsDto dto, HttpServletRequest request) {
        Assert.isBlank(dto.getStartTime(), "开始时间不能为空");
        Assert.isBlank(dto.getEndTime(), "结束时间不能为空");
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        // 分线代理没有平台费服务费
        if (account.getAttributes() == Constants.EVNumber.one) {
        	return R.ok();
        }
        dto.setAgyAccount(account.getAgyAccount());
        CostTotalDto depotCostTotalForSingle = finaceReportService.depotCostTotalForSingle(dto);
        boolean IsDetails = true;
        if (account.getFeeModel() == 2) {
        	IsDetails = false;
        }
        depotCostTotalForSingle.setIsDetails(IsDetails);
        return R.ok().put(depotCostTotalForSingle);
    }


}
