package com.wsdy.saasops.modules.analysis.controller;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wsdy.saasops.api.modules.user.service.SendSmsSevice;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.system.systemsetting.entity.SmsLog;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/bkapi/analysis/smslog")
@Api(value = "SmsLogController", tags = "短信记录")
public class SmsLogController extends AbstractController {

	@Autowired
	private SendSmsSevice sendSmsSevice;

	@PostMapping("/list")
	@RequiresPermissions("analysis:smslog:list")
	@ApiOperation(value = "短信记录", notes = "短信记录")
	public R list(@RequestBody SmsLog smsLog) {
		return R.ok().put("page", sendSmsSevice.list(smsLog));
	}

}
