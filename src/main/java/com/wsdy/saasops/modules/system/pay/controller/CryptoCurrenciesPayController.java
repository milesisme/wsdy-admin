package com.wsdy.saasops.modules.system.pay.controller;

import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.system.pay.dto.AllotDto;
import com.wsdy.saasops.modules.system.pay.entity.SetBasicSysCryptoCurrencies;
import com.wsdy.saasops.modules.system.pay.service.CryptoCurrenciesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/bkapi/company/cryptocurrencies")
@Api(value = "公司入款-加密货币", tags = "公司入款-加密货币")
public class CryptoCurrenciesPayController extends AbstractController {
    @Autowired
    private CryptoCurrenciesService cryptoCurrenciesService;

    @RequestMapping("/crTypeList")
    @ApiOperation(value="加密货币-列表", notes="加密货币-列表")
    public R crTypeList() {
        return R.ok().put(cryptoCurrenciesService.crTypeList());
    }

    @PostMapping("/crSave")
    @RequiresPermissions("setting:cryptocurrencies:save")
    @ApiOperation(value = "加密货币入款新增", notes = "加密货币入款新增")
    public R crSave(@RequestBody SetBasicSysCryptoCurrencies setBasicSysCryptoCurrencies, HttpServletRequest request) {
        // 入参校验
        checkoutCr(setBasicSysCryptoCurrencies);

        cryptoCurrenciesService.crSave(setBasicSysCryptoCurrencies, getUser().getUsername());
        return R.ok();
    }

    @GetMapping("/crPayList")
    @RequiresPermissions("setting:cryptocurrencies:list")
    @ApiOperation(value = "加密货币支付列表", notes = "加密货币支付列表")
    public R crPayList(@ModelAttribute SetBasicSysCryptoCurrencies setBasicSysCryptoCurrencies) {
        return R.ok().put(cryptoCurrenciesService.crPayList(setBasicSysCryptoCurrencies));
    }

    @PostMapping("/crUpdate")
    @RequiresPermissions("setting:cryptocurrencies:update")
    @ApiOperation(value = "加密货币入款修改", notes = "个人二维码入款修改")
    public R crUpdate(@RequestBody SetBasicSysCryptoCurrencies setBasicSysCryptoCurrencies, HttpServletRequest request) {
        // 入参校验
        Assert.isNull(setBasicSysCryptoCurrencies.getId(),"id不能为空");
        checkoutCr(setBasicSysCryptoCurrencies);

        cryptoCurrenciesService.crUpdate(setBasicSysCryptoCurrencies, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @GetMapping("/crDelete")
    @RequiresPermissions("setting:cryptocurrencies:delete")
    @ApiOperation(value = "加密货币入款删除", notes = "加密货币入款删除")
    public R crDelete(@ModelAttribute SetBasicSysCryptoCurrencies setBasicSysCryptoCurrencies, HttpServletRequest request) {
        Assert.isNull(setBasicSysCryptoCurrencies.getId(), "id不能为空");
        cryptoCurrenciesService.crDelete(setBasicSysCryptoCurrencies.getId(), getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @PostMapping("/crAvailable")
    @RequiresPermissions("setting:cryptocurrencies:available")
    @ApiOperation(value = "加密货币入款禁用启用", notes = "加密货币入款禁用启用")
    public R qrCodeAvailable(@RequestBody SetBasicSysCryptoCurrencies setBasicSysCryptoCurrencies, HttpServletRequest request) {
        Assert.isNull(setBasicSysCryptoCurrencies.getId(), "id不能为空");
        Assert.isNull(setBasicSysCryptoCurrencies.getAvailable(), "状态不能为空");

        cryptoCurrenciesService.crUpdateStatus(setBasicSysCryptoCurrencies, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @GetMapping("/crPayInfo")
    @RequiresPermissions("setting:cryptocurrencies:list")
    @ApiOperation(value = "加密货币查看单个", notes = "加密货币支付查看单个")
    public R crPayInfo(@RequestParam("id") Integer id) {
        Assert.isNull(id, "id不能为空");
        return R.ok().put(cryptoCurrenciesService.crPayInfo(id));
    }

    @PostMapping("/updateCrSort")
    @RequiresPermissions("setting:allot:update")
    @ApiOperation(value = "修改数字货币排序", notes = "修改数字货币排序")
    public R updateCrSort(@RequestBody AllotDto allotDto) {
        Assert.isNull(allotDto, "数据不能为空");
        Assert.isNull(allotDto.getGroupId(), "数据不能为空");
        cryptoCurrenciesService.updateCrSort(allotDto);
        return R.ok();
    }

    @GetMapping("/qrCrList")
    @ApiOperation(value = "加密货币入款设置列表（不分页）", notes = "加密货币入款设置列表（不分页）")
    public R qrCrList() {
        return R.ok().put(cryptoCurrenciesService.qrCrList());
    }

    private void checkoutCr(SetBasicSysCryptoCurrencies setBasicSysCryptoCurrencies) {
        if(Collections3.isEmpty(setBasicSysCryptoCurrencies.getBankIds())){
            throw new R200Exception("bankIds不能为空");
        }
        if(Collections3.isEmpty(setBasicSysCryptoCurrencies.getGroupIds())){
            throw new R200Exception("会员组不能为空");
        }
        // isHot，isRecommend 只有一个为true
 		if (setBasicSysCryptoCurrencies.getIsHot() != null && setBasicSysCryptoCurrencies.getIsRecommend() != null
 				&& setBasicSysCryptoCurrencies.getIsHot() && setBasicSysCryptoCurrencies.getIsRecommend()) {
 			throw new R200Exception("操作失败，推荐和热门按钮只允许同时勾选一个！");
 		}
        Assert.isNull(setBasicSysCryptoCurrencies.getAvailable(), "状态不能为空");
        Assert.isNull(setBasicSysCryptoCurrencies.getMinAmout(), "单笔最低限额不能为空");
    }
}
