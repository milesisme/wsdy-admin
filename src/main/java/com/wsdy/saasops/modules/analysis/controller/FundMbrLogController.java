package com.wsdy.saasops.modules.analysis.controller;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.analysis.entity.RptBetRcdDayLog;
import com.wsdy.saasops.modules.analysis.service.RptBetRcdDayLogService;
import com.wsdy.saasops.modules.base.controller.AbstractController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/bkapi/analysis/mbrlog")
@Api(value = "FundMbrLogController", tags = "资金报表日志")
public class FundMbrLogController extends AbstractController {

	@Autowired
	private RptBetRcdDayLogService rptBetRcdDayLogService;

	@PostMapping("/list")
	@RequiresPermissions("analysis:mbrlog:list")
	@ApiOperation(value = "资金报表日志", notes = "资金报表日志")
	public R list(@RequestBody RptBetRcdDayLog rptBetRcdDayLog) {
		return R.ok().put("page", rptBetRcdDayLogService.list(rptBetRcdDayLog));
	}

}
