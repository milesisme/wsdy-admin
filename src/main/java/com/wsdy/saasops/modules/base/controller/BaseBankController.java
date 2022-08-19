package com.wsdy.saasops.modules.base.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.base.service.BaseBankService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/bkapi/base/baseBank")
@Api(value = "BaseBank", tags = "银行基本信息记录")
public class BaseBankController {
	@Autowired
	private BaseBankService baseBankService;
	
    @RequestMapping("/list")
    @ApiOperation(value="银行信息-列表", notes="显示所有银行基本信息")
    public R list() {
        return R.ok().put("banks",baseBankService.selectAll());
    }

    @GetMapping("payBankList")
    @ApiOperation(value="银行信息-列表", notes="银行信息-列表")
    public R payBankList(@RequestParam("payId") Integer payId) {
        return R.ok().put(baseBankService.payBankList(payId));
    }

    @RequestMapping("/qrTypeList")
    @ApiOperation(value="二维码-列表", notes="所有支持二维码支付的方式")
    public R qrTypeList() {
        return R.ok().put(baseBankService.qrList());
    }
}
