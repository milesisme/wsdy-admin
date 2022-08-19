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
import com.wsdy.saasops.modules.agent.dto.AgyChannelGroupDto;
import com.wsdy.saasops.modules.agent.entity.AgyChannelGroup;
import com.wsdy.saasops.modules.agent.service.AgyChannelGroupService;
import com.wsdy.saasops.modules.base.controller.AbstractController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/bkapi/agent/channelgroup")
@Api(tags = "渠道分組")
public class AgyChannelGroupController extends AbstractController {

	@Autowired
	private AgyChannelGroupService agyChannelGroupService;

	@GetMapping("/list")
	@RequiresPermissions("agent:channelGroup:list")
	@ApiOperation(value = "渠道分組列表", notes = "代理分組列表")
	public R list(@ModelAttribute AgyChannelGroupDto agyChannelGroup, @RequestParam("pageNo") @NotNull Integer pageNo,
			@RequestParam("pageSize") @NotNull Integer pageSize) {
		return R.ok().putPage(agyChannelGroupService.list(agyChannelGroup, pageNo, pageSize));
	}

	@PostMapping("/save")
	@RequiresPermissions("agent:channelGroup:save")
	@ApiOperation(value = "渠道分組新增", notes = "渠道分組新增")
	public R agyAccountSave(@RequestBody AgyChannelGroupDto agyChannelGroupDto) {
		Assert.isBlank(agyChannelGroupDto.getName(), "渠道分組名不能为空");
		AgyChannelGroup agyChannelGroup = new AgyChannelGroup();
		BeanUtils.copyProperties(agyChannelGroupDto, agyChannelGroup);
		agyChannelGroup.setUdapyeBy(getUser().getUsername());
		agyChannelGroup.setUpdateTime(getCurrentDate(FORMAT_18_DATE_TIME));
		return R.ok(agyChannelGroupService.save(agyChannelGroup));
	}

	@PostMapping("/update")
	@RequiresPermissions("agent:channelGroup:update")
	@ApiOperation(value = "渠道分組更新", notes = "渠道分組更新")
	public R agyDomainAudit(@RequestBody AgyChannelGroup agyChannelGroup) {
		Assert.isNull(agyChannelGroup.getId(), "渠道分組id不能为空");
		Assert.isNull(agyChannelGroup.getName(), "渠道分組名不能为空");
		agyChannelGroup.setUdapyeBy(getUser().getUsername());
		agyChannelGroup.setUpdateTime(getCurrentDate(FORMAT_18_DATE_TIME));
		return R.ok(agyChannelGroupService.update(agyChannelGroup));
	}

	@PostMapping("/delete")
	@RequiresPermissions("agent:channelGroup:delete")
	@ApiOperation(value = "渠道分組删除", notes = "渠道分組删除")
	public R agyDomainDelete(@RequestBody AgyChannelGroup agyChannelGroup) {
		Assert.isNull(agyChannelGroup.getId(), "渠道分組id不能为空");
		return R.ok(agyChannelGroupService.delete(agyChannelGroup.getId()));
	}

}
