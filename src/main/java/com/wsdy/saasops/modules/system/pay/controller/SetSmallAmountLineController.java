package com.wsdy.saasops.modules.system.pay.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.system.pay.entity.SetSmallAmountLine;
import com.wsdy.saasops.modules.system.pay.service.SetSmallAmountLineService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/bkapi/company/lineService")
@Api(tags = "小额客服线")
public class SetSmallAmountLineController extends AbstractController {

	@Autowired
	private SetSmallAmountLineService setSmallAmountLineService;

	@GetMapping("/list")
	@RequiresPermissions("setting:smallAmountLine:list")
	@ApiOperation(value = "渠道分組列表", notes = "代理分組列表")
	public R list() {
		return R.ok().put(setSmallAmountLineService.list());
	}
	
    @PostMapping("/update")
    @RequiresPermissions("setting:smallAmountLine:update")
    @ApiOperation(value = "小额客服线编辑", notes = "小额客服线编辑")
    public R update(@RequestBody SetSmallAmountLine setSmallAmountLine, HttpServletRequest request) {
    	setSmallAmountLine.setUpdateBy(getUser().getUsername());
    	setSmallAmountLineService.update(setSmallAmountLine);
        return R.ok();
    }

}
