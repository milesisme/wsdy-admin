package com.wsdy.saasops.modules.operate.controller;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;

import javax.validation.constraints.NotNull;

import org.apache.shiro.authz.annotation.RequiresPermissions;
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
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.operate.entity.SetGameCategory;
import com.wsdy.saasops.modules.operate.service.SetGameCategoryService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/bkapi/operate/gameCategory")
@Api(value = "SetGameCategory", tags = "")
public class SetGameCategoryController extends AbstractController {

	@Autowired
	private SetGameCategoryService setGameCategoryService;

	@GetMapping("/list")
	@RequiresPermissions("operate:gameCategory:list")
	@ApiOperation(value = "游戏分类列表", notes = "游戏分类列表")
	public R list(@ModelAttribute SetGameCategory setGameCategory, @RequestParam("pageNo") @NotNull Integer pageNo,
			@RequestParam("pageSize") @NotNull Integer pageSize) {
		return R.ok().putPage(setGameCategoryService.list(setGameCategory, pageNo, pageSize));
	}

	@PostMapping("/save")
	@RequiresPermissions("operate:gameCategory:save")
	@ApiOperation(value = "游戏分类新增", notes = "游戏分类新增")
	public R agyAccountSave(@RequestBody SetGameCategory setGameCategory) {
		Assert.isBlank(setGameCategory.getName(), "游戏分类名不能为空");
		setGameCategory.setUpdateBy(getUser().getUsername());
		setGameCategory.setUpdateTime(getCurrentDate(FORMAT_18_DATE_TIME));
		setGameCategoryService.saveCategor(setGameCategory);
		return R.ok();
	}

	@PostMapping("/update")
	@RequiresPermissions("operate:gameCategory:update")
	@ApiOperation(value = "游戏分类更新", notes = "游戏分类更新")
	public R agyDomainAudit(@RequestBody SetGameCategory setGameCategory) {
		Assert.isNull(setGameCategory.getId(), "游戏分类id不能为空");
		Assert.isBlank(setGameCategory.getName(), "游戏分类名不能为空");
		setGameCategory.setUpdateBy(getUser().getUsername());
		setGameCategory.setUpdateTime(getCurrentDate(FORMAT_18_DATE_TIME));
		setGameCategoryService.updateCategor(setGameCategory);
		return R.ok();
	}

	@PostMapping("/delete")
	@RequiresPermissions("operate:gameCategory:delete")
	@ApiOperation(value = "游戏分类删除", notes = "游戏分类删除")
	public R agyDomainDelete(@RequestBody SetGameCategory setGameCategory) {
		Assert.isNull(setGameCategory.getId(), "游戏分类id不能为空");
		// 删除set_game_category_relation里对应的数据
		setGameCategoryService.deleteCategor(setGameCategory);
		return R.ok();
	}

}
