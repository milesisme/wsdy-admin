package com.wsdy.saasops.modules.member.controller;

import java.math.BigDecimal;
import java.util.Optional;

import com.wsdy.saasops.common.annotation.SysLog;
import com.wsdy.saasops.common.exception.R200Exception;
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
import com.wsdy.saasops.modules.member.entity.MbrWithdrawalCond;
import com.wsdy.saasops.modules.member.entity.MbrWithdrawalCond.FeeWayVal;
import com.wsdy.saasops.modules.member.service.MbrGroupService;
import com.wsdy.saasops.modules.member.service.MbrWithdrawalCondService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/bkapi/member/mbrwithdrawalcond")
@Api(value = "MbrWithdrawalCond", tags = "会员取款条件设置")
public class MbrWithdrawalCondController extends AbstractController {
	@Autowired
	private MbrWithdrawalCondService mbrWithdrawalCondService;
	@Autowired
	private MbrGroupService mbrGroupService;

	@Autowired
	private MbrAccountLogService mbrAccountLogService;

	/**
	 * 信息
	 */
	@GetMapping("/info/{groupId}")
	@RequiresPermissions("member:mbrwithdrawalcond:info")
	@ApiOperation(value = "会员取款条件设置", notes = "列表信息")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header") })
	public R info(@PathVariable("groupId") Integer groupId) {
		MbrWithdrawalCond condition = new MbrWithdrawalCond();
		condition.setGroupId(groupId);
		MbrWithdrawalCond withdrawalCond = mbrWithdrawalCondService.queryObjectCond(condition);
		if (null!=withdrawalCond){
			if (StringUtils.isEmpty(withdrawalCond.getLowUsdt())&&null!=withdrawalCond.getLowQuota()){
				withdrawalCond.setLowUsdt(withdrawalCond.getLowQuota());
			}
			if (StringUtils.isEmpty(withdrawalCond.getTopUsdt())&&null!=withdrawalCond.getTopQuota()){
				withdrawalCond.setTopUsdt(withdrawalCond.getTopQuota());
			}
		}
		return R.ok().put("mbrWithdrawalCond", Optional.ofNullable(withdrawalCond).orElse(new MbrWithdrawalCond()));
	}

	/**
	 * 保存
	 */
	@PostMapping("/save")
	@RequiresPermissions("member:mbrwithdrawalcond:save")
	@ApiOperation(value = "会员取款条件设置", notes = "保存信息")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header") })
	@SysLog(module = "会员取款条件",methodText = "会员取款条件保存")
	public R save(@RequestBody MbrWithdrawalCond mbrWithdrawalCond) {
		Assert.isNull(mbrWithdrawalCond.getGroupId(), "会员组ID不能为空!");
		if (StringUtils.isEmpty(mbrWithdrawalCond.getFeeAvailable())) {
            mbrWithdrawalCond.setFeeAvailable(Available.disable);
        }
		// 初始化的时候该字段为null
		if (StringUtils.isEmpty(mbrWithdrawalCond.getChargeFeeAvailable())) {
			mbrWithdrawalCond.setChargeFeeAvailable(Available.disable);
		}
		verifyWithdrawalCond(mbrWithdrawalCond);
		mbrWithdrawalCondService.save(mbrWithdrawalCond);
		mbrGroupService.updateGroupAvil(mbrWithdrawalCond.getGroupId(), Available.enable);
		return R.ok();
	}

	/**
	 * 修改
	 */
	@PostMapping("/update")
	@RequiresPermissions("member:mbrwithdrawalcond:update")
	@ApiOperation(value = "会员取款条件设置", notes = "更新信息")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header") })
	@SysLog(module = "会员取款条件",methodText = "会员取款条件更新")
	public R update(@RequestBody MbrWithdrawalCond mbrWithdrawalCond, HttpServletRequest request) {
		Assert.isNull(mbrWithdrawalCond.getId(), "会员取款条件id不能为空!");
		//mbrWithdrawalCond.setGroupId(null);
		verifyWithdrawalCond(mbrWithdrawalCond);
		mbrWithdrawalCondService.update(mbrWithdrawalCond);
		mbrAccountLogService.updateGroupWisdrawConfig(mbrWithdrawalCond, getUser().getUsername(), CommonUtil.getIpAddress(request));
		return R.ok();
	}

	private void verifyWithdrawalCond(MbrWithdrawalCond mbrWithdrawalCond) {
		Assert.isNumeric(mbrWithdrawalCond.getLowQuota(), "最低限额只能为数字,并且最大长度为12位!", 12);
		Assert.isNumeric(mbrWithdrawalCond.getTopQuota(), "最高限额只能为数字,并且最大长度为12位!", 12);
		Assert.isNumeric(mbrWithdrawalCond.getLowAlipayQuota(), "最低限额只能为数字,并且最大长度为12位!", 12);
		Assert.isNumeric(mbrWithdrawalCond.getTopAlipayQuota(), "最高限额只能为数字,并且最大长度为12位!", 12);
		Assert.isNumeric(mbrWithdrawalCond.getLowUsdt(), "最低限额只能为数字,并且最大长度为12位!", 12);
		Assert.isNumeric(mbrWithdrawalCond.getTopUsdt(), "最高限额只能为数字,并且最大长度为12位!", 12);
		Assert.isMax(mbrWithdrawalCond.getLowQuota(), mbrWithdrawalCond.getTopQuota(), "最低限额不能大于最高限额!");
		Assert.isMax(mbrWithdrawalCond.getLowAlipayQuota(), mbrWithdrawalCond.getTopAlipayQuota(), "最低限额不能大于最高限额!");
		Assert.isMax(mbrWithdrawalCond.getLowUsdt(), mbrWithdrawalCond.getTopUsdt(), "最低限额不能大于最高限额!");

		// 每日取款限制
		if (!StringUtils.isEmpty(mbrWithdrawalCond.getFeeAvailable())
				&& mbrWithdrawalCond.getFeeAvailable() == Available.enable) {
			Assert.isNumeric(mbrWithdrawalCond.getWithDrawalTimes(), "每日充许取款次数为数字,并且最大长充为12位!", 12);
			Assert.isNumeric(mbrWithdrawalCond.getWithDrawalQuota(), "每日取款限额为数字,并且最大长充为12位!", 12);
		}

		// 取款手续费开关
		if (!StringUtils.isEmpty(mbrWithdrawalCond.getChargeFeeAvailable())
				&& mbrWithdrawalCond.getChargeFeeAvailable() == Available.enable) {
			Assert.isNumeric(mbrWithdrawalCond.getFeeHours(), "手续费时限填写错误!", 1);
			Assert.isNumeric(mbrWithdrawalCond.getFeeTimes(), "手续费 - 限免次数为数字,并且最大长充为12位!", 12);
			Assert.isNumeric(mbrWithdrawalCond.getFeeTop(), "手续费上限金额为数字,并且最大长充为12位!", 12);
			Assert.isNull(mbrWithdrawalCond.getFeeWay(), "收费方式不能为空!");
			if (mbrWithdrawalCond.getFeeWay() == FeeWayVal.scale) {
				Assert.isMaxNum(mbrWithdrawalCond.getFeeScale(), "手续费比例只能为数字,并且最大仅为100!", new BigDecimal("100"));
			} else {
				mbrWithdrawalCond.setFeeWay(FeeWayVal.fixed);
				Assert.isNumeric(mbrWithdrawalCond.getFeeFixed(), "固定收费为数字,并且最大长充为12位!", 12);
				if (mbrWithdrawalCond.getFeeFixed().compareTo(mbrWithdrawalCond.getFeeTop()) == 1) {
					throw new R200Exception("固定收费不能大于手续上限金额CNY!");
				}
			}
		}

		Assert.isNumeric(mbrWithdrawalCond.getWithDrawalAudit(), "存款稽核为数字,并且最大长充为11位!", 11);
		Assert.isMaxNum(new BigDecimal(mbrWithdrawalCond.getManageFee()), "管理费、行政费为数字,并且最大仅为100!",
				new BigDecimal("100"));
		Assert.isNumeric(mbrWithdrawalCond.getOverFee(), "放宽额度为数字,并且最大长充为12位!", 12);
	}
}
