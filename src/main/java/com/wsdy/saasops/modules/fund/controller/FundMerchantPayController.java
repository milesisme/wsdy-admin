package com.wsdy.saasops.modules.fund.controller;

import com.wsdy.saasops.common.annotation.SysLog;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.fund.entity.FundMerchantPay;
import com.wsdy.saasops.modules.fund.entity.FundWhiteList;
import com.wsdy.saasops.modules.fund.service.MerchantPayService;
import io.swagger.annotations.*;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.Objects;


@RestController
@RequestMapping("/bkapi/fund/deposit")
@Api(tags = "代付相关接口")
public class FundMerchantPayController extends AbstractController {

    @Autowired
    private MerchantPayService merchantPayService;

    @Deprecated
    @GetMapping("/findFundWhiteListOne/{id}")
    @RequiresPermissions("member:fundWhiteList:info")
    @ApiOperation(value = "查询单个会员白名单信息", notes = "查询单个会员白名单信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findFundWhiteListOne(@PathVariable("id") Integer accountId) {
        return R.ok().put(merchantPayService.findFundWhiteList(accountId));
    }

    @Deprecated
    @PostMapping("/addFundWhiteList")
    @RequiresPermissions("member:fundWhiteList:add")
    @SysLog(module = "会员列表",methodText = "新增会员白名单")
    @ApiOperation(value = "新增会员白名单", notes = "新增会员白名单")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R addFundWhiteList(@RequestBody FundWhiteList whiteList) {
        whiteList.setCreateUser(getUser().getUsername());
        merchantPayService.addFundWhiteList(whiteList);
        return R.ok();
    }

    @Deprecated
    @GetMapping("/deleteFundWhiteList/{id}")
    @RequiresPermissions("member:fundWhiteList:delete")
    @SysLog(module = "会员列表",methodText = "会员白名单移除")
    @ApiOperation(value = "会员白名单移除", notes = "会员白名单移除")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R deleteFundWhiteList(@PathVariable("id") Integer id) {
        merchantPayService.deleteFundWhiteList(id);
        return R.ok();
    }

    @GetMapping("/findFundMerchantPayListPage")
    @RequiresPermissions(value = {"merchant:fundMerchantPay:list", "merchant:fundMerchantPay:cryptocurrencies:list"}, logical = Logical.OR)
    @ApiOperation(value = "出款管理分页查询", notes = "出款管理分页查询")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findFundMerchantPayListPage(@ModelAttribute FundMerchantPay merchantPay,
                                         @RequestParam("pageNo") @NotNull Integer pageNo,
                                         @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(merchantPayService.findFundMerchantPayList(merchantPay, pageNo, pageSize));
    }

    @PostMapping("/addFundMerchantPay")
    @RequiresPermissions(value = {"merchant:fundMerchantPay:save", "merchant:fundMerchantPay:cryptocurrencies:save"}, logical = Logical.OR)
    @SysLog(module = "出款管理",methodText = "出款管理新增")
    @ApiOperation(value = "出款管理新增", notes = "出款管理新增")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R addFundMerchantPay(@RequestBody FundMerchantPay merchantPay, HttpServletRequest request) {
        merchantPay.setCreateUser(getUser().getUsername());
        merchantPay.setModifyUser(getUser().getUsername());
        // 入参校验 TODO
        Assert.isNull(merchantPay.getMethodType(), "methodType不能为空");
        Assert.isNull(merchantPay.getMerchantNo(), "商户号不能为空");
        Assert.isNull(merchantPay.getChannelId(), "channelId不能为空");
        Assert.isNull(merchantPay.getAvailable(), "available不能为空");
        if(Constants.EVNumber.one == merchantPay.getMethodType().intValue()){  // 加密钱包
            Assert.isNull(merchantPay.getCurrencyCode(), "币种不能为空");
            Assert.isNull(merchantPay.getCurrencyProtocol(), "协议类型不能为空");
        }
        // 支付宝 methodType=2
        // 其他钱包methodType=3
        merchantPayService.addFundMerchantPay(merchantPay);
        return R.ok();
    }

    @PostMapping("/updateFundMerchantPay")
    @RequiresPermissions(value = {"merchant:fundMerchantPay:update", "merchant:fundMerchantPay:cryptocurrencies:update"}, logical = Logical.OR)
    @SysLog(module = "出款管理",methodText = "出款管理修改")
    @ApiOperation(value = "出款管理修改", notes = "出款管理修改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateFundMerchantPay(@RequestBody FundMerchantPay merchantPay, HttpServletRequest request) {
        merchantPay.setModifyUser(getUser().getUsername());
        // 入参校验 TODO
        Assert.isNull(merchantPay.getMethodType(), "methodType不能为空");
        if(Constants.EVNumber.one == merchantPay.getMethodType().intValue()){  // 加密钱包
            Assert.isNull(merchantPay.getCurrencyCode(), "币种不能为空");
            Assert.isNull(merchantPay.getCurrencyProtocol(), "协议类型不能为空");
        }
        merchantPayService.updateFundMerchantPay(merchantPay, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @GetMapping("/deleteFundMerchantPay/{id}")
    @RequiresPermissions(value = {"merchant:fundMerchantPay:delete", "merchant:fundMerchantPay:cryptocurrencies:delete"}, logical = Logical.OR)
    @SysLog(module = "出款管理删除",methodText = "出款管理删除")
    @ApiOperation(value = "出款管理删除", notes = "出款管理删除")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R deleteFundMerchantPay(@PathVariable("id") Integer id) {
        merchantPayService.deleteFundMerchantPay(id);
        return R.ok();
    }

    @GetMapping("/updateFundMerchantPayAvailable")
    @RequiresPermissions(value = {"merchant:fundMerchantPay:available", "merchant:fundMerchantPay:cryptocurrencies:available"}, logical = Logical.OR)
    @SysLog(module = "出款管理",methodText = "出款管理修改状态")
    @ApiOperation(value = "出款管理修改状态", notes = "出款管理修改状态")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateFundMerchantPayAvailable(@ModelAttribute FundMerchantPay merchantPay, HttpServletRequest request) {
        merchantPay.setModifyUser(getUser().getUsername());
        // 入参校验 TODO
        Assert.isNull(merchantPay.getId(), "id不能为空");
        merchantPayService.updateFundMerchantPayAvailable(merchantPay, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @GetMapping("/findFundMerchantPayOne/{id}")
    @RequiresPermissions(value = {"merchant:fundMerchantPay:info", "merchant:fundMerchantPay:cryptocurrencies:info"}, logical = Logical.OR)
    @ApiOperation(value = "出款管理查询单个", notes = "出款管理查询单个")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findFundMerchantPayOne(@PathVariable("id") Integer id) {
        return R.ok().put(merchantPayService.findFundMerchantPayOne(id));
    }

    @GetMapping("/findTChannelPayList")
    @ApiOperation(value = "查询所有代付渠道", notes = "查询所有代付渠道")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findTChannelPayList( @RequestParam("methodType")  Integer methodType) {
        if (Objects.nonNull(methodType) && Constants.EVNumber.two == methodType) {
            // 支付宝与银行卡共用代付渠道
            methodType = Constants.EVNumber.zero;
        }
        return R.ok().put(merchantPayService.findTChannelPayList(methodType));
    }

    @GetMapping("/findTChannelPayListCr")
    @ApiOperation(value = "查询所有代付渠道", notes = "查询所有代付渠道")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findTChannelPayListCr( @RequestParam("methodType")  Integer methodType) {
        return R.ok().put(merchantPayService.findTChannelPayListCr(methodType));
    }

}
