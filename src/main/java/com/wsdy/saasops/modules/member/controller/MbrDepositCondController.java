package com.wsdy.saasops.modules.member.controller;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

import com.wsdy.saasops.common.annotation.SysLog;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.member.service.MbrAccountLogService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wsdy.saasops.common.constants.Constants.Available;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.member.entity.MbrDepositCond;
import com.wsdy.saasops.modules.member.service.MbrDepositCondService;
import com.wsdy.saasops.modules.member.service.MbrGroupService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/bkapi/member/mbrdepositcond")
@Api(value = "MbrDepositCond", tags = "会员组存款条件设置")
public class MbrDepositCondController extends AbstractController {

	@Autowired
	private MbrDepositCondService mbrDepositCondService;
	@Autowired
	private MbrGroupService mbrGroupService;
	@Autowired
	private MbrAccountLogService mbrAccountLogService;

	/**
	 * 信息
	 */
	@GetMapping("/info/{groupId}")
	@RequiresPermissions("member:mbrdepositcond:info")
	@ApiOperation(value = "会员组存款条件设置", notes = "根据groupId查看会员存款条件")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header") })
	public R info(@PathVariable("groupId") Integer groupId) {
		MbrDepositCond condition = new MbrDepositCond();
		condition.setGroupId(groupId);
		return R.ok().put("mbrDepositCond", Optional.ofNullable(mbrDepositCondService.queryObjectCond(condition)).orElse(new MbrDepositCond()));
	}

	/**
	 * 保存
	 */
	@PostMapping("/save")
	@RequiresPermissions("member:mbrdepositcond:save")
	@ApiOperation(value = "会员组存款条件设置", notes = "保存条件明细设置")
	@SysLog(module = "会员组-存款条件",methodText = "保存存款条件")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header") })
	public R save(@RequestBody MbrDepositCond mbrDepositCond) {
		Assert.isNull(mbrDepositCond.getGroupId(), "会员组ID不能为空!");
		verifyDepositCond(mbrDepositCond);
		mbrDepositCondService.save(mbrDepositCond);
		mbrGroupService.updateGroupAvil(mbrDepositCond.getGroupId(), Available.enable);
		return R.ok().put("groupId", mbrDepositCond.getGroupId());
	}

	/**
	 * 修改
	 */
	@PostMapping("/update")
	@RequiresPermissions("member:mbrdepositcond:update")
	@ApiOperation(value = "会员组存款条件设置", notes = "修改条件明细设置")
	@SysLog(module = "会员组-存款条件",methodText = "更新存款条件")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header") })
	public R update(@RequestBody MbrDepositCond mbrDepositCond, HttpServletRequest request) {
		Assert.isNull(mbrDepositCond.getId(), "ID不能为空!");
		//mbrDepositCond.setGroupId(null);
		verifyDepositCond(mbrDepositCond);
		mbrDepositCondService.update(mbrDepositCond);
		mbrAccountLogService.updateGroupDepositConfig(mbrDepositCond, getUser().getUsername(), CommonUtil.getIpAddress(request));
		return R.ok();
	}

	private void verifyDepositCond(MbrDepositCond mbrDepositCond) {
		
		//Assert.isNumeric(mbrDepositCond.getLowQuota(), "最低限额只能为数字,并且最大长充为12位!", 12);
		//Assert.isNumeric(mbrDepositCond.getTopQuota(), "最高限额只能为数字,并且最大长充为12位!", 12);
		//Assert.isMax(mbrDepositCond.getLowQuota(), mbrDepositCond.getTopQuota(), "最低限额不能大于最高限额!");
		if (StringUtils.isEmpty(mbrDepositCond.getFeeAvailable())) {
            mbrDepositCond.setFeeAvailable(Available.disable);
        }
		if (StringUtils.isEmpty(mbrDepositCond.getFeeEnable())) {
            mbrDepositCond.setFeeEnable(Available.disable);
        }
		if (mbrDepositCond.getFeeAvailable() == Available.enable) {
			Assert.isNumeric(mbrDepositCond.getFeeHours(), "手续费时限填写错误!!", 1);
			Assert.isNumeric(mbrDepositCond.getFeeTimes(), "限免次数只能为数字,并且最大长度为12位!", 12);
			Assert.isMaxNum(mbrDepositCond.getFeeScale(), "手续费比例只能为数字,并且最大仅为100!", new BigDecimal("100"));
			Assert.isNumeric(mbrDepositCond.getFeeTop(), "手续费上限金额只能为数字,并且最大长度为12位!", 12);
		}
	}
}
