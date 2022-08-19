package com.wsdy.saasops.modules.system.pay.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.system.pay.dto.AllotDto;
import com.wsdy.saasops.modules.system.pay.dto.PayQuotaDto;
import com.wsdy.saasops.modules.system.pay.entity.SetBacicOnlinepay;
import com.wsdy.saasops.modules.system.pay.service.SetOnlinePayService;
import com.wsdy.saasops.modules.system.pay.service.SysQrCodeService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;


@RestController
@RequestMapping("/bkapi/onlinepay")
@Api(value = "线上支付配置", tags = "线上支付配置")
public class SetOnlinePayController extends AbstractController {

    @Autowired
    private SetOnlinePayService onlinePayService;
    @Autowired
    private SysQrCodeService sysQrCodeService;

    @GetMapping("/list")
    @RequiresPermissions("setting:onlinepay:list")
    @ApiOperation(value = "线上支付List", notes = "线上支付List")
    public R queryList(@ModelAttribute SetBacicOnlinepay onlinepay) {
        return R.ok().put(onlinePayService.queryList(onlinepay));
    }

    @PostMapping("/updateStatus")
    @RequiresPermissions("setting:onlinepay:available")
    @ApiOperation(value = "修改状态", notes = "修改状态")
    public R updateStatus(@RequestBody SetBacicOnlinepay onlinepay, HttpServletRequest request) {
        Assert.isNull(onlinepay.getId(), "id不能为空");
        Assert.isNull(onlinepay.getAvailable(), "状态不能为空");
        onlinePayService.updateStatus(onlinepay, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @PostMapping("/updateJump")
    @RequiresPermissions("setting:onlinepay:update")
    @ApiOperation(value = "修改跳转类型", notes = "修改跳转类型")
    public R updateJump(@RequestBody SetBacicOnlinepay onlinepay, HttpServletRequest request) {
        Assert.isNull(onlinepay.getId(), "id不能为空");
        Assert.isNull(onlinepay.getIsJump(), "跳转方式不能为空");
        onlinePayService.updateJump(onlinepay, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @GetMapping("/delete/{id}")
    @RequiresPermissions("setting:onlinepay:delete")
    @ApiOperation(value = "线上支付delete", notes = "线上支付delete")
    public R onlinepayDelete(@PathVariable("id") Integer id, HttpServletRequest request) {
        Assert.isNull(id, "id不能为空");
        onlinePayService.onlinepayDelete(id, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @GetMapping("/onlinepayInfo/{id}")
    @RequiresPermissions("setting:onlinepay:list")
    @ApiOperation(value = "线上支付信息（根据ID）", notes = "线上支付信息（根据ID）")
    public R onlinepayInfo(@PathVariable("id") Integer id) {
        Assert.isNull(id, "id不能为空");
        return R.ok().put(onlinePayService.onlinepayInfo(id));
    }

    @PostMapping("/save")
    @RequiresPermissions("setting:onlinepay:save")
    @ApiOperation(value = "线上支付新增", notes = "线上支付新增")
    public R companySave(@RequestBody SetBacicOnlinepay onlinepay, HttpServletRequest request) {
        checkoutOnlinepay(onlinepay);
        onlinePayService.onlinepaySave(onlinepay, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @PostMapping("/update")
    @RequiresPermissions("setting:onlinepay:update")
    @ApiOperation(value = "线上支付编辑", notes = "线上支付编辑")
    public R companyUpdate(@RequestBody SetBacicOnlinepay onlinepay, HttpServletRequest request) {
        checkoutOnlinepay(onlinepay);
        onlinePayService.onlinepayUpdate(onlinepay, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @GetMapping("/payList")
    @ApiOperation(value = "查询所有支付平台", notes = "查询所有支付平台")
    public R findPayList() {
        return R.ok().put(onlinePayService.findPayList(CommonUtil.getSiteCode()));
    }

    @GetMapping("/jDepositPayList")
    @ApiOperation(value = "存就送查询支付平台", notes = "存就送查询支付平台")
    public R jDepositPayList() {
        return R.ok().put(onlinePayService.findPayListForJDepositActivity(CommonUtil.getSiteCode()));
    }

    @GetMapping("/shortcutPayList")
    @ApiOperation(value = "查询所有快捷支付平台", notes = "查询所有快捷支付平台")
    public R shortcutPayList() {
        return R.ok().put(onlinePayService.shortcutPayList(CommonUtil.getSiteCode()));
    }

    @GetMapping("/allotList")
    @RequiresPermissions("setting:allot:list")
    @ApiOperation(value = "支付分配List", notes = "支付分配List")
    public R queryAllotList() {
        return R.ok().put(onlinePayService.findPayAllotList());
    }

    @GetMapping("/allotListByGroupId")
    @RequiresPermissions("setting:allot:list")
    @ApiOperation(value = "会员组支付分配List", notes = "会员组支付分配List")
    public R queryallotListByGroupId(@RequestParam("groupId") @NotNull Integer groupId) {
        Assert.isNull(groupId, "groupId不能为空");

        return R.ok().put(onlinePayService.findPayAllotListByGroupId(groupId));
    }


    @PostMapping("/updateBankSort")
    @RequiresPermissions("setting:allot:update")
    @ApiOperation(value = "修改支付分配银行卡排序", notes = "修改支付分配银行卡排序")
    public R updateBankSort(@RequestBody AllotDto allotDto) {
        Assert.isNull(allotDto, "数据不能为空");
        Assert.isNull(allotDto.getGroupId(), "数据不能为空");
        // 先删除该会员组所有旧的银行卡关联关系
        onlinePayService.deleteSysDepMbr(allotDto);
        // 插入全量的该会员组的银行卡入款关系
        onlinePayService.updateBankSort(allotDto);
        return R.ok();
    }

    @PostMapping("/updateQrCodeSort")
    @RequiresPermissions("setting:allot:update")
    @ApiOperation(value = "修改个人二维码排序", notes = "修改个人二维码排序")
    public R updateQrCodeSort(@RequestBody AllotDto allotDto) {
        Assert.isNull(allotDto, "数据不能为空");
        Assert.isNull(allotDto.getGroupId(), "数据不能为空");
        sysQrCodeService.updateQrCodeSort(allotDto);
        return R.ok();
    }

    @PostMapping("/updateFastPaySort")
    @RequiresPermissions("setting:allot:update")
    @ApiOperation(value = "修改支付分配自动入款排序", notes = "修改支付分配自动入款排序")
    public R updateFastPaySort(@RequestBody AllotDto allotDto) {
    	Assert.isNull(allotDto, "数据不能为空");
    	Assert.isNull(allotDto.getGroupId(), "数据不能为空");
        onlinePayService.updateFastPaySort(allotDto);
        return R.ok();
    }

    @PostMapping("/updateOnlineSort")
    @RequiresPermissions("setting:allot:update")
    @ApiOperation(value = "修改支付分配线上排序", notes = "修改支付分配线上排序")
    public R updateOnlineSort(@RequestBody AllotDto allotDto) {
        Assert.isNull(allotDto, "数据不能为空");
        Assert.isNull(allotDto.getGroupId(), "数据不能为空");
        onlinePayService.updateOnlineSort(allotDto);
        return R.ok();
    }

    @PostMapping("/updateQuota")
    @RequiresPermissions("setting:allot:updateQuota")
    @ApiOperation(value = "修改支付分配限额", notes = "修改支付分配限额")
    public R updateQuota(@RequestBody PayQuotaDto quotaDto, HttpServletRequest request) {
        Assert.isNull(quotaDto.getId(), "id不能为空");
        Assert.isNull(quotaDto.getQuotaType(), "类型不能为空");
        Assert.isNull(quotaDto.getMinAmout(), "单笔最小限额不能为空");

        if (quotaDto.getQuotaType() != Constants.EVNumber.five) {   // 加密货币只判断单笔最小限额
            Assert.isNull(quotaDto.getDayMaxAmout(), "每日限额不能为空");
            Assert.isNull(quotaDto.getMaxAmout(), "单笔最大限额不能为空");
            Assert.isMaxAmout(quotaDto.getMinAmout(), quotaDto.getMaxAmout(), "单笔最小金额不能比最大金额大");
            Assert.isMaxAmout(quotaDto.getMaxAmout(), quotaDto.getDayMaxAmout(), "单笔限额不能比每日限额大");
        }

        onlinePayService.updateQuota(quotaDto, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @PostMapping("/updateQuotaBatch")
    @RequiresPermissions("setting:allot:updateQuota")
    @ApiOperation(value = "修改支付分配限额批量", notes = "修改支付分配限额批量")
    public R updateQuotaBatch(@RequestBody List<PayQuotaDto> quotaDtos, HttpServletRequest request) {
        Assert.isNotEmpty(quotaDtos, "不能为空");
        quotaDtos.forEach(qs -> onlinePayService.updateQuota(qs, getUser().getUsername(), CommonUtil.getIpAddress(request)));
        return R.ok();
    }

    private void checkoutOnlinepay(SetBacicOnlinepay onlinepay) {
        Assert.isNull(onlinepay.getDevSource(), "显示终端不能为空");
        Assert.isNull(onlinepay.getPayId(), "支付平台不能为空");
        Assert.isBlank(onlinepay.getName(), "商户名称不能为空");
        Assert.isBlank(onlinepay.getMerNo(), "商户号不能为空");
        Assert.isBlank(onlinepay.getDevSource(), "显示端口不能为空");
        Assert.isBlank(onlinepay.getPassword(), "密钥不能为空");
        Assert.isNull(onlinepay.getAvailable(), "状态不能为空");
        Assert.isNull(onlinepay.getDayMaxAmout(), "每日限额不能为空");
        if (onlinepay.getAmountType().intValue() == 0) {
            Assert.isNull(onlinepay.getMaxAmout(), "单笔限额不能为空");
            Assert.isNull(onlinepay.getMinAmout(), "单笔限额不能为空");
            Assert.isMaxAmout(onlinepay.getMinAmout(), onlinepay.getMaxAmout(), "单笔最小金额不能比最大金额大");
            Assert.isMaxAmout(onlinepay.getMaxAmout(), onlinepay.getDayMaxAmout(), "单笔限额不能比每日限额大");
        } else if (onlinepay.getAmountType().intValue() == 1) {
            Assert.isNull(onlinepay.getFixedAmount(), "固定限额不能为空");
        }
    }

    @GetMapping("/onlinePayList")
    @ApiOperation(value = "线上支付所有数据(不分页)", notes = "线上支付所有数据(不分页)")
    public R querySetBacicOnlinepayList() {
        return R.ok().put(onlinePayService.querySetBacicOnlinepayList());
    }
}
