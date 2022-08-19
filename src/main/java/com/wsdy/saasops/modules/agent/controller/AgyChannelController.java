package com.wsdy.saasops.modules.agent.controller;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;

import javax.validation.constraints.NotNull;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.agent.dto.AgyChannelDto;
import com.wsdy.saasops.modules.agent.entity.AgyChannel;
import com.wsdy.saasops.modules.agent.service.AgyChannelService;
import com.wsdy.saasops.modules.base.controller.AbstractController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/bkapi/agent/channel")
@Api(tags = "渠道列表")
public class AgyChannelController extends AbstractController {

	@Autowired
	private AgyChannelService agyChannelService;

	@GetMapping("/list")
	@RequiresPermissions("agent:channel:list")
	@ApiOperation(value = "渠道列表", notes = "渠道列表")
	public R list(@ModelAttribute AgyChannelDto agyChannel, @RequestParam("pageNo") @NotNull Integer pageNo,
			@RequestParam("pageSize") @NotNull Integer pageSize) {
		return R.ok().putPage(agyChannelService.list(agyChannel, pageNo, pageSize));
	}

	@PostMapping("/save")
	@RequiresPermissions("agent:channel:save")
	@ApiOperation(value = "渠道新增", notes = "渠道新增")
	public R agyAccountSave(@RequestBody AgyChannelDto agyChannelDto) {
		Assert.isBlank(agyChannelDto.getName(), "渠道名不能为空");
		AgyChannel agyChannel = new AgyChannel();
		BeanUtils.copyProperties(agyChannelDto, agyChannel);
		agyChannel.setUdapyeBy(getUser().getUsername());
		agyChannel.setUpdateTime(getCurrentDate(FORMAT_18_DATE_TIME));
		return agyChannelService.saveChannel(agyChannel);
	}

	@PostMapping("/update")
	@RequiresPermissions("agent:channel:update")
	@ApiOperation(value = "渠道更新", notes = "渠道更新")
	public R agyDomainAudit(@RequestBody AgyChannel agyChannel) {
		Assert.isNull(agyChannel.getId(), "渠道id不能为空");
		agyChannel.setUdapyeBy(getUser().getUsername());
		agyChannel.setUpdateTime(getCurrentDate(FORMAT_18_DATE_TIME));
		return agyChannelService.updateChannel(agyChannel);
	}

	@PostMapping("/delete")
	@RequiresPermissions("agent:channel:delete")
	@ApiOperation(value = "渠道删除", notes = "渠道删除")
	public R agyDomainDelete(@RequestBody AgyChannel agyChannel) {
		Assert.isNull(agyChannel.getId(), "渠道id不能为空");
		return R.ok(agyChannelService.deleteById(agyChannel.getId()));
	}

}
