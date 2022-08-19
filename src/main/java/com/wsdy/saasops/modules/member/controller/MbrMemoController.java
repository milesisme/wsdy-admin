package com.wsdy.saasops.modules.member.controller;

import com.wsdy.saasops.common.annotation.SysLog;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.member.entity.MbrMemo;
import com.wsdy.saasops.modules.member.service.MbrMemoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/bkapi/member/mbrmemo")
@Api(value = "MbrMemo", tags = "会员信息备注信息")
public class MbrMemoController extends AbstractController {

	@Autowired
	private MbrMemoService mbrMemoService;


	@GetMapping("/list")
//	@RequiresPermissions("member:mbrmemo:list")
	@ApiOperation(value = "会员信息备注信息只查最近3条", notes = "会员信息备注信息只查最近3条")
	public R list(@ModelAttribute MbrMemo mbrMemo) {
		return R.ok().put("page", mbrMemoService.queryListPage(mbrMemo));
	}

	@GetMapping("/listAll")
//	@RequiresPermissions("member:mbrmemo:list")
	@ApiOperation(value = "会员信息备注信息查询", notes = "会员信息备注信息查询")
	public R listAll(@ModelAttribute MbrMemo mbrMemo,
					 @RequestParam("pageNo") @NotNull Integer pageNo,
					 @RequestParam("pageSize") @NotNull Integer pageSize) {
		return R.ok().put("page", mbrMemoService.queryListPageAll(mbrMemo,pageNo,pageSize));
	}

	@GetMapping("/sortList")
//	@RequiresPermissions("member:mbrmemo:list")
	@ApiOperation(value = "会员信息备注分类", notes = "会员信息备注分类")
	public R sortList(@RequestParam("accountId") Integer accountId,
					  @RequestParam("pageNo") @NotNull Integer pageNo,
					  @RequestParam("pageSize") @NotNull Integer pageSize,
					  @RequestParam(value = "roleId", required = false) Integer roleId) {
		return R.ok().putPage(mbrMemoService.sortList(accountId, roleId, pageNo, pageSize));
	}

	@GetMapping("/info/{memberId}/{id}")
//	@RequiresPermissions("member:mbrmemo:info")
	@ApiOperation(value = "会员信息备注信息", notes = "根据Id显示会员备注信息明细")
	public R info(@PathVariable("id") Integer id,
				  @PathVariable("loginName") String loginName) {
		MbrMemo mbrMemo = mbrMemoService.queryObject(id);
		return R.ok().put("memo", mbrMemo).put("loginName", loginName);
	}

	@PostMapping("/save")
//	@RequiresPermissions("member:mbrmemo:save")
	@ApiOperation(value = "会员信息备注信息", notes = "保存会员备注信息明细")
	@SysLog(module = "会员备注模块",methodText = "保存会员备注信息")
	public R save(@RequestBody MbrMemo mbrMemo, HttpServletRequest request) {
		mbrMemoService.saveMbrMemo(mbrMemo,getUser(), CommonUtil.getIpAddress(request));
		return R.ok();
	}

	@PostMapping("/update")
	@ApiOperation(value = "会员信息备注信息", notes = "更新会员备注信息明细")
//	@RequiresPermissions("member:mbrmemo:update")
	@SysLog(module = "会员备注模块",methodText = "更新会员备注信息")
	public R update(@RequestBody MbrMemo mbrMemo) {
		Assert.isNull(mbrMemo.getId(), "记录ID不能为空!");
		mbrMemoService.updateMbrMemo(mbrMemo);
		return R.ok();
	}

	@PostMapping("/delete")
//	@RequiresPermissions("member:mbrmemo:delete")
	@SysLog(module = "会员备注模块",methodText = "删除会员备注信息")
	public R delete(@RequestBody MbrMemo mbrMemo) {
		mbrMemoService.deleteBatch(mbrMemo.getIds());
		return R.ok();
	}
}
