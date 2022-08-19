package com.wsdy.saasops.modules.member.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import com.wsdy.saasops.common.annotation.SysLog;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.base.entity.BaseBank;
import com.wsdy.saasops.modules.base.service.BaseBankService;
import com.wsdy.saasops.modules.member.entity.MbrBankcardHistory;
import com.wsdy.saasops.modules.member.service.MbrAccountLogService;
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
import com.wsdy.saasops.common.constants.Constants.Available;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.member.entity.MbrBankcard;
import com.wsdy.saasops.modules.member.service.MbrAccountService;
import com.wsdy.saasops.modules.member.service.MbrBankcardService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import java.util.List;

@RestController
@RequestMapping("/bkapi/member/mbrbankcard")
@Api(value = "MbrBankcard", tags = "会员银行卡信息")
public class MbrBankcardController extends AbstractController {

    @Autowired
    private MbrBankcardService mbrBankcardService;
    @Autowired
    MbrAccountService mbrAccountService;
    @Autowired
    private BaseBankService baseBankService;
    @Autowired
    private MbrAccountLogService mbrAccountLogService;

    @GetMapping("/list")
//    @RequiresPermissions("member:mbrbankcard:list")
    @ApiOperation(value = "会员银行卡-列表", notes = "根据当前页及每页笔数显示")
    public R list(@ModelAttribute MbrBankcard mbrBankcard, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("userId") @NotNull Integer userId, @RequestParam("pageSize") @NotNull Integer pageSize, @RequestParam(value = "orderBy", required = false) String orderBy) {
        //不包含删除的银行卡
        mbrBankcard.setIsDel(Available.disable);
        mbrBankcard.setAccountId(userId);
        return R.ok().put("page", mbrBankcardService.queryListPage(mbrBankcard, pageNo, pageSize, orderBy));
    }

    @GetMapping("/historyList")
//    @RequiresPermissions("member:mbrbankcard:list")
    @ApiOperation(value = "会员银行卡-历史记录", notes = "根据当前页及每页笔数显示")
    public R historyList(@ModelAttribute MbrBankcardHistory mbrBankcardHistory, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("userId") @NotNull Integer userId, @RequestParam("pageSize") @NotNull Integer pageSize, @RequestParam(value = "orderBy", required = false) String orderBy) {
        //不包含删除的银行卡
        //mbrBankcard.setIsDel(Available.enable);
        mbrBankcardHistory.setAccountId(userId);
        return R.ok().put("page", mbrBankcardService.queryBankHistoryListPage(mbrBankcardHistory, pageNo, pageSize, orderBy));
    }

    @GetMapping("/info/{id}")
//    @RequiresPermissions("member:mbrbankcard:info")
    @ApiOperation(value = "会员银行卡-查看", notes =  "根据会员银行卡Id,显示会员银行卡信息")
    public R info(@PathVariable("id") Integer id) {
        MbrBankcard mbrBankcard = mbrBankcardService.queryObject(id);
        return R.ok().put("mbrBankcard", mbrBankcard);
    }


    @PostMapping("/save")
    @RequiresPermissions("member:mbrbankcard:save")
    @ApiOperation(value = "会员银行卡-保存", notes = "保存会员银行卡明细信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    @SysLog(module = "会员模块-银行卡", methodText = "新增银行卡")
    public R save(@RequestBody MbrBankcard mbrBankcard, HttpServletRequest request) {
        //会员与银行卡关联表增加id字段进行关联
        if (mbrBankcard.getBankCardId()!=null) {
            BaseBank baseBank = baseBankService.queryObject(mbrBankcard.getBankCardId());
            mbrBankcard.setBankName(baseBank.getBankName());
        } else {
            BaseBank baseBank = new BaseBank();
            baseBank.setBankName(mbrBankcard.getBankName());
            List<BaseBank> baseBanks = baseBankService.select(baseBank);
            mbrBankcard.setBankCardId(baseBanks.get(0).getId());
        }
        if (mbrBankcard.getBankType() == Constants.EVNumber.two) {
            verifyAlipayAccount(mbrBankcard);
            // 支付宝wdenable无法开启，直接通过name和code查询支付宝
            BaseBank zfb = new BaseBank(){{
                setBankName("支付宝");
                setBankCode("ZFB");
            }};
            zfb = baseBankService.selectOne(zfb);
            mbrBankcard.setBankCardId(zfb.getId());
            mbrBankcard.setBankName(zfb.getBankName());
        } else {
            verifyBankCard(mbrBankcard);
        }
        return mbrBankcardService.saveBankCard(mbrBankcard, Constants.EVNumber.two, getUser().getUsername(), CommonUtil.getIpAddress(request));
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @RequiresPermissions("member:mbrbankcard:update")
    @ApiOperation(value = "会员银行卡-更新", notes = "更新会员银行卡明细信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    @SysLog(module = "会员模块-银行卡", methodText = "更新银行卡")
    public R update(@RequestBody MbrBankcard mbrBankcard, HttpServletRequest request) {
        Assert.isNull(mbrBankcard.getId(), "ID不能为空!");
        verifyBankCard(mbrBankcard);
        return mbrBankcardService.updateBankCard(mbrBankcard, getUser().getUsername(), CommonUtil.getIpAddress(request));
    }

    /**
     * 会员银行卡禁用启用
     *
     * @return
     */
    @PostMapping("/available")
    @RequiresPermissions("member:mbrbankcard:available")
    @ApiOperation(value = "会员银行卡-更新状态", notes = "状态更新")
    @SysLog(module = "会员模块-银行卡", methodText = "更新银行卡状态")
    public R available(@RequestBody MbrBankcard mbrBankcardDto, HttpServletRequest request) {
        MbrBankcard mbrBankcard = new MbrBankcard();
        mbrBankcard.setId(mbrBankcardDto.getId());
        mbrBankcard.setAvailable(mbrBankcardDto.getAvailable());
        mbrBankcardService.update(mbrBankcard);
        mbrAccountLogService.updateAccountBankStatus(mbrBankcardDto, getUser().getUsername(), Constants.EVNumber.two, CommonUtil.getIpAddress(request));
        return R.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @RequiresPermissions("member:mbrbankcard:delete")
    @ApiOperation(value = "会员银行卡-删除", notes = "根据银行卡Id逻辑删除银行卡")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    @SysLog(module = "会员模块-银行卡", methodText = "删除银行卡")
    public R delete(@RequestBody MbrBankcard mbrBankcard, HttpServletRequest request) {
        mbrBankcardService.deleteBatch(mbrBankcard.getIds(), getUser().getUsername(), Constants.EVNumber.two, CommonUtil.getIpAddress(request));
        return R.ok();
    }

    private void verifyBankCard(MbrBankcard mbrBankcard) {
        Assert.isNull(mbrBankcard.getAccountId(), "会员ID不能为空!");
        Assert.isBlank(mbrBankcard.getBankName(), "开户银行不能为空!");
        Assert.isBlank(mbrBankcard.getCardNo(), "开户账号不能为空!");
        Assert.isNumeric(mbrBankcard.getCardNo(), "开户账号只能为数字!");
        Assert.isBankCardNo(mbrBankcard.getCardNo(), "开户账号长度只能为16与19位!", 16, 19);
    }

    private void verifyAlipayAccount(MbrBankcard mbrAlipay) {
        Assert.isNull(mbrAlipay.getAccountId(), "会员ID不能为空!");
        Assert.isBlank(mbrAlipay.getCardNo(), "开户账号不能为空!");
    }
}
