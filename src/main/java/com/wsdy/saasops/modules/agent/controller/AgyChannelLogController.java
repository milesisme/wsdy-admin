package com.wsdy.saasops.modules.agent.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.agent.dto.AgyChannelLogDto;
import com.wsdy.saasops.modules.agent.service.AgyChannelLogService;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/bkapi/agent/channelog")
@Api(tags = "渠道相关数据统计")
public class AgyChannelLogController extends AbstractController {

	private final String agyChannelLogModule = "agyChannelLogModule";

	@Value("${agent.channel.excel.path}")
	private String excelPath;

	@Autowired
	private AgyChannelLogService agyChannelLogService;

	@GetMapping("/list")
	@RequiresPermissions("agent:channelLog:list")
	@ApiOperation(value = "渠道统计列表", notes = "渠道统计列表")
	public R list(@ModelAttribute AgyChannelLogDto agyChannelLogDto, @RequestParam("pageNo") @NotNull Integer pageNo,
			@RequestParam("pageSize") @NotNull Integer pageSize) {
		return R.ok().putPage(agyChannelLogService.list(agyChannelLogDto, pageNo, pageSize));
	}

	@GetMapping("/exportList")
	@RequiresPermissions("agent:channelLog:exportExcel")
	@ApiOperation(value = "导出渠道统计列表", notes = "导出渠道统计列表")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header") })
	public R exportBetDetails(AgyChannelLogDto agyChannelLogDto, HttpServletRequest request,
			HttpServletResponse response) {
		log.info("导出渠道统计列表Path----- {}", excelPath);
		
		// 处理catCodes
		SysFileExportRecord record = agyChannelLogService.exportChannelLog(agyChannelLogDto, agyChannelLogModule,
				excelPath);
		if (record == null) {
			throw new R200Exception("正在处理中!");
		}
		return R.ok();
	}

}
