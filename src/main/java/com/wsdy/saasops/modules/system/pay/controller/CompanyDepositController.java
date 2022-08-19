package com.wsdy.saasops.modules.system.pay.controller;

import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.system.pay.entity.SetBacicFastPay;
import com.wsdy.saasops.modules.system.pay.entity.SysDeposit;
import com.wsdy.saasops.modules.system.pay.entity.SysQrCode;
import com.wsdy.saasops.modules.system.pay.service.SysDepositService;
import com.wsdy.saasops.modules.system.pay.service.SysQrCodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/bkapi/company/deposit")
@Api(value = "公司入款Controller", tags = "公司入款Controller")
public class CompanyDepositController extends AbstractController {

    @Autowired
    private SysDepositService sysDepositService;
    @Autowired
    private SysQrCodeService sysQrCodeService;

    @GetMapping("/list")
    @RequiresPermissions("setting:company:list")
    @ApiOperation(value = "银行卡List", notes = "银行卡List")
    public R queryList(@ModelAttribute SysDeposit deposit) {
        return R.ok().put(sysDepositService.queryList(deposit));
    }

    @PostMapping("/updateStatus")
    @RequiresPermissions("setting:company:available")
    @ApiOperation(value = "update", notes = "update状态")
    public R updateStatus(@RequestBody SysDeposit deposit, HttpServletRequest request) {
        Assert.isNull(deposit.getId(), "id不能为空");
        Assert.isNull(deposit.getAvailable(), "状态不能为空");
        Assert.isNull(deposit.getIsHot(), "是否热门不能为空");
        Assert.isNull(deposit.getIsRecommend(), "是否推荐不能为空");
        sysDepositService.updateStatus(deposit, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }
    
    @GetMapping("/delete/{id}")
    @RequiresPermissions("setting:company:delete")
    @ApiOperation(value = "公司入款delete", notes = "公司入款delete")
    public R companyDelete(@PathVariable("id") Integer id, HttpServletRequest request) {
        Assert.isNull(id, "id不能为空");
        sysDepositService.companyDelete(id, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @GetMapping("/companyInfo/{id}")
    @RequiresPermissions("setting:company:list")
    @ApiOperation(value = "公司入款信息（根据ID）", notes = "公司入款信息（根据ID）")
    public R companyInfo(@PathVariable("id") Integer id) {
        Assert.isNull(id, "id不能为空");
        return R.ok().put(sysDepositService.companyInfo(id));
    }

    @PostMapping("/save")
    @RequiresPermissions("setting:company:save")
    @ApiOperation(value = "公司入款新增", notes = "公司入款新增")
    public R companySave(@RequestBody SysDeposit deposit, HttpServletRequest request) {
        checkoutSysDeposit(deposit);
        sysDepositService.companySave(deposit, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @PostMapping("/update")
    @RequiresPermissions("setting:company:update")
    @ApiOperation(value = "公司入款编辑", notes = "公司入款编辑")
    public R companyUpdate(@RequestBody SysDeposit deposit, HttpServletRequest request) {
        checkoutSysDeposit(deposit);
        sysDepositService.companyUpdate(deposit, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    private void checkoutSysDeposit(SysDeposit deposit) {
        Assert.isNull(deposit.getBankName(), "银行名称不能为空");
        Assert.isBlank(deposit.getRealName(), "姓名不能为空");
        Assert.isNull(deposit.getBankId(), "开户银行不能为空");
        Assert.isBlank(deposit.getBankAccount(), "开户账号不能为空");
        Assert.isBlank(deposit.getBankBranch(), "开户支行不能为空");
        Assert.isNull(deposit.getDayMaxAmout(), "每日最大限额不能为空");
        Assert.isNull(deposit.getAvailable(), "状态不能为空");
        Assert.isNull(deposit.getMinAmout(), "单笔限额不能为空");
        Assert.isNull(deposit.getMaxAmout(), "单笔限额不能为空");
        Assert.isMaxAmout(deposit.getMinAmout(), deposit.getMaxAmout(), "单笔最小金额不能比最大金额大");
        Assert.isMaxAmout(deposit.getMaxAmout(), deposit.getDayMaxAmout(), "单笔限额不能比每日限额大");
    }

    @GetMapping("/sysDepositList")
    @ApiOperation(value = "公司入款设置列表（不分页）", notes = "公司入款设置列表（不分页）")
    public R querySysDepositList() {
        return R.ok().put(sysDepositService.querySysDepositList());
    }

    @PostMapping("/fastDepositWithdrawSave")
    @RequiresPermissions("setting:fastPay:save")
    @ApiOperation(value = "极速入款新增", notes = "极速入款新增")
    public R fastDepositWithdrawSave(@RequestBody SetBacicFastPay fastPay, HttpServletRequest request) {
        Assert.isNull(fastPay.getPayId(), "收款渠道不能为空");
        Assert.isBlank(fastPay.getName(), "支付名称不能为空");
        Assert.isNull(fastPay.getAvailable(), "状态不能为空");
        Assert.isNull(fastPay.getFastDWDayMaxAmout(), "每日限额不能为空");
        Assert.isNull(fastPay.getFastDWAmount(), "固定金额不能为空");
        Assert.isNull(fastPay.getAlipayFlg(), "支付宝转卡标志不能为空");
        sysDepositService.fastPaySave(fastPay, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @PostMapping("/fastDepositWithdrawUpdate")
    @RequiresPermissions("setting:fastPay:update")
    @ApiOperation(value = "极速入款修改", notes = "极速入款修改")
    public R fastDepositWithdrawUpdate(@RequestBody SetBacicFastPay fastPay, HttpServletRequest request) {
        Assert.isNull(fastPay.getPayId(), "收款渠道不能为空");
        Assert.isBlank(fastPay.getName(), "支付名称不能为空");
        Assert.isNull(fastPay.getAvailable(), "状态不能为空");
        Assert.isNull(fastPay.getFastDWDayMaxAmout(), "每日限额不能为空");
        Assert.isNull(fastPay.getFastDWAmount(), "固定金额不能为空");
        sysDepositService.fastPayUpdate(fastPay, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @PostMapping("/fastPaySave")
    @RequiresPermissions("setting:fastPay:save")
    @ApiOperation(value = "自动入款新增", notes = "自动入款新增")
    public R fastPaySave(@RequestBody SetBacicFastPay fastPay, HttpServletRequest request) {
        Assert.isNull(fastPay.getPayId(), "收款渠道不能为空");
        Assert.isBlank(fastPay.getName(), "支付名称不能为空");
        Assert.isBlank(fastPay.getPassword(), "秘钥不能为空");
        Assert.isNull(fastPay.getAvailable(), "状态不能为空");
        Assert.isNotEmpty(fastPay.getDeposits(), "银行卡不能为空");
        sysDepositService.fastPaySave(fastPay, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @PostMapping("/fastPayUpdate")
    @RequiresPermissions("setting:fastPay:update")
    @ApiOperation(value = "自动入款修改", notes = "自动入款修改")
    public R fastPayUpdate(@RequestBody SetBacicFastPay fastPay, HttpServletRequest request) {
        Assert.isNull(fastPay.getPayId(), "收款渠道不能为空");
        Assert.isBlank(fastPay.getName(), "支付名称不能为空");
        Assert.isBlank(fastPay.getPassword(), "秘钥不能为空");
        Assert.isNull(fastPay.getAvailable(), "状态不能为空");
        Assert.isNotEmpty(fastPay.getDeposits(), "银行卡不能为空");
        sysDepositService.fastPayUpdate(fastPay, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @GetMapping("/fastPayList")
    @RequiresPermissions("setting:fastPay:list")
    @ApiOperation(value = "自动入款查看", notes = "自动入款查看")
    public R fastPayList(@ModelAttribute SetBacicFastPay fastPay) {
        return R.ok().put(sysDepositService.fastPayList(fastPay));
    }

    @GetMapping("/fastPayInfo")
    @RequiresPermissions("setting:fastPay:list")
    @ApiOperation(value = "自动入款查看单个", notes = "自动入款查看单个")
    public R fastPayInfo(@RequestParam("id") Integer id) {
        Assert.isNull(id, "id不能为空");
        return R.ok().put(sysDepositService.fastPayInfo(id));
    }

    @PostMapping("/fastPayAvailable")
    @RequiresPermissions("setting:fastPay:available")
    @ApiOperation(value = "自动入款禁用启用", notes = "自动入款禁用启用")
    public R fastPayAvailable(@RequestBody SetBacicFastPay fastPay, HttpServletRequest request) {
        Assert.isNull(fastPay.getId(), "id不能为空");
        Assert.isNull(fastPay.getAvailable(), "状态不能为空");
        Assert.isNull(fastPay.getIsHot(), "是否热门不能为空");
        Assert.isNull(fastPay.getIsRecommend(), "是否推荐不能为空");
        sysDepositService.fastPayAvailable(fastPay, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @GetMapping("/fastPayDelete")
    @RequiresPermissions("setting:fastPay:delete")
    @ApiOperation(value = "自动入款删除", notes = "自动入款删除")
    public R fastPayDelete(@RequestParam("id") Integer id, HttpServletRequest request) {
        Assert.isNull(id, "id不能为空");
        sysDepositService.fastPayDelete(id, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @GetMapping("/qrCodePayList")
    @RequiresPermissions("setting:qrcodepay:list")
    @ApiOperation(value = "个人二维码列表", notes = "个人二维码列表")
    public R qrCodePayList(@ModelAttribute SysQrCode qrCode) {
        return R.ok().put(sysQrCodeService.qrCodePayList(qrCode));
    }

    @GetMapping("/qrCodePayInfo")
    @RequiresPermissions("setting:qrcodepay:list")
    @ApiOperation(value = "二维码支付查看单个", notes = "二维码支付查看单个")
    public R qrCodePayInfo(@RequestParam("id") Integer id) {
        Assert.isNull(id, "id不能为空");
        return R.ok().put(sysQrCodeService.qrCodePayInfo(id));
    }

    @PostMapping("/qrCodeSave")
    @RequiresPermissions("setting:qrcodepay:save")
    @ApiOperation(value = "个人二维码入款新增", notes = "个人二维码入款新增")
    public R qrCodeSave(@RequestBody SysQrCode qrCode, HttpServletRequest request) {
        checkoutSysQrCode(qrCode);
        sysQrCodeService.qrCodeSave(qrCode, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @PostMapping("/qrCodeUpdate")
    @RequiresPermissions("setting:qrcodepay:update")
    @ApiOperation(value = "个人二维码入款新增", notes = "个人二维码入款新增")
    public R qrCodeUpdate(@RequestBody SysQrCode qrCode, HttpServletRequest request) {
        Assert.isNull(qrCode.getId(),"id不能为空");
        checkoutSysQrCode(qrCode);
        sysQrCodeService.qrCodeUpdate(qrCode, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @PostMapping("/qrCodeAvailable")
    @RequiresPermissions("setting:qrcodepay:available")
    @ApiOperation(value = "个人二维码入款禁用启用", notes = "个人二维码入款禁用启用")
    public R qrCodeAvailable(@RequestBody SysQrCode qrCode, HttpServletRequest request) {
        Assert.isNull(qrCode.getId(), "id不能为空");
        Assert.isNull(qrCode.getAvailable(), "状态不能为空");
        sysQrCodeService.qrCodeUpdateStatus(qrCode, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @GetMapping("/qrCodeDelete")
    @RequiresPermissions("setting:qrcodepay:delete")
    @ApiOperation(value = "个人二维码入款删除", notes = "个人二维码入款删除")
    public R qrCodeDelete(@ModelAttribute SysQrCode qrCode, HttpServletRequest request) {
        Assert.isNull(qrCode.getId(), "id不能为空");
        sysQrCodeService.qrCodeDelete(qrCode.getId(), getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @GetMapping("/qrCodeList")
    @ApiOperation(value = "个人二维码入款设置列表（不分页）", notes = "个人二维码入款设置列表（不分页）")
    public R queryQrCodeLis() {
        return R.ok().put(sysQrCodeService.queryQrCodeList());
    }

    private void checkoutSysQrCode(SysQrCode qrCode) {
        if(Collections3.isEmpty(qrCode.getBankIds())){
            throw new R200Exception("二维码平台不能为空");
        }
        if(Collections3.isEmpty(qrCode.getGroupIds())){
            throw new R200Exception("会员组不能为空");
        }
        Assert.isNull(qrCode.getQrImgUrl(), "文件不能为空");
        Assert.isNull(qrCode.getName(), "支付名称不能为空");
        Assert.isNull(qrCode.getDayMaxAmout(), "每日最大限额不能为空");
        Assert.isNull(qrCode.getAvailable(), "状态不能为空");
        if (qrCode.getAmountType().intValue() == 0) {
            Assert.isNull(qrCode.getMinAmout(), "单笔限额不能为空");
            Assert.isNull(qrCode.getMaxAmout(), "单笔限额不能为空");
            Assert.isMaxAmout(qrCode.getMinAmout(), qrCode.getMaxAmout(), "单笔最小金额不能比最大金额大");
            Assert.isMaxAmout(qrCode.getMaxAmout(), qrCode.getDayMaxAmout(), "单笔限额不能比每日限额大");
        } else if (qrCode.getAmountType().intValue() == 1) {
            Assert.isNull(qrCode.getFixedAmount(), "固定限额不能为空");
        }
    }
}
