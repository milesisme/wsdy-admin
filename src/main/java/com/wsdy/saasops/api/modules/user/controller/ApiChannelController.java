package com.wsdy.saasops.api.modules.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.agent.dto.AgyChannelForRegisterDto;
import com.wsdy.saasops.modules.agent.service.AgyChannelLogService;
import com.wsdy.saasops.modules.agent.service.AgyChannelService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("/api/channel")
@Api(value = "agy", tags = "用户代理相关接口")
public class ApiChannelController {

	@Autowired
	private AgyChannelService agyChannelService;

	@Autowired
	private AgyChannelLogService agyChannelLogService;

//	1、落地页查询接口  传参：主渠道号   返回：对应的附渠道号和比例
	@GetMapping("/viceNum")
	@ApiOperation(value = "根据主渠道号获取副号及比例", notes = "根据主渠道号获取副号及比例")
	public R viceNum(@ApiParam("渠道号") @RequestParam("masterNum") String masterNum) {
		Assert.isBlank(masterNum, "渠道号不能为空");
		return R.ok(agyChannelService.viceNum(masterNum));
	}

	// 2、注册接口 传参：渠道号和设备号,是否虚拟机 返回：无
	@PostMapping("/logDeviceuuid")
	@ApiOperation(value = "用户端下载后第一次打开时调用，保存设备号以及对应的渠道", notes = "用户端下载后第一次打开时调用，保存设备号以及对应的渠道")
	public R logDeviceuuid(@RequestBody AgyChannelForRegisterDto agyChannelForRegisterDto) {
		return agyChannelLogService.logDeviceuuid(agyChannelForRegisterDto);
	}

}
