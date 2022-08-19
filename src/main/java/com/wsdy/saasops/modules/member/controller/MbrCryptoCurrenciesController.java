package com.wsdy.saasops.modules.member.controller;

import com.wsdy.saasops.common.annotation.SysLog;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.Constants.Available;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.base.entity.BaseBank;
import com.wsdy.saasops.modules.base.service.BaseBankService;
import com.wsdy.saasops.modules.member.entity.MbrCryptoCurrencies;
import com.wsdy.saasops.modules.member.service.MbrCryptoCurrenciesService;
import com.wsdy.saasops.modules.system.pay.service.CryptoCurrenciesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/bkapi/member/cryptoCurrencies")
@Api(value = "MbrCryptoCurrencies", tags = "会员加密货币信息")
public class MbrCryptoCurrenciesController extends AbstractController {

    @Autowired
    private MbrCryptoCurrenciesService mbrCryptoCurrenciesService;
    @Autowired
    private BaseBankService baseBankService;
    @Autowired
    private CryptoCurrenciesService cryptoCurrenciesService;

    @GetMapping("/list")
//    @RequiresPermissions("member:cryptocurrencies:list")
    @ApiOperation(value = "会员加密货币-列表", notes = "根据当前页及每页笔数显示")
    public R list(@ModelAttribute MbrCryptoCurrencies mbrCryptoCurrencies,
                  @RequestParam("userId") @NotNull Integer userId,
                  @RequestParam("pageNo") @NotNull Integer pageNo,
                  @RequestParam("pageSize") @NotNull Integer pageSize,
                  @RequestParam(value = "orderBy", required = false) String orderBy) {
        //不包含删除的加密货币
        mbrCryptoCurrencies.setIsDel(Available.disable);
        mbrCryptoCurrencies.setAccountId(userId);
        return R.ok().put("page", mbrCryptoCurrenciesService.queryListPage(mbrCryptoCurrencies, pageNo, pageSize, orderBy));
    }

    @GetMapping("/cryptoCurrenciesList")
    @ApiOperation(value = "会员加密货币-获取数字货币钱包/平台", notes = "获取数字货币钱包/平台")
    public R cryptoCurrenciesList() {
        return R.ok().put("cryptoCurrenciesList", cryptoCurrenciesService.getCrLogo());
    }


    @GetMapping("/info/{id}")
//    @RequiresPermissions("member:cryptocurrencies:info")
    @ApiOperation(value = "会员加密货币-查看", notes = "根据会员加密货币Id,显示会员加密货币信息")
    public R info(@PathVariable("id") Integer id) {
        MbrCryptoCurrencies mbrCryptoCurrencies = mbrCryptoCurrenciesService.queryObject(id);
        return R.ok().put("mbrCryptoCurrencies", mbrCryptoCurrencies);
    }


    @PostMapping("/save")
    @RequiresPermissions("member:cryptocurrencies:save")
    @ApiOperation(value = "会员加密货币-保存", notes = "保存会员加密货币明细信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    @SysLog(module = "会员模块-加密货币", methodText = "新增加密货币")
    public R save(@RequestBody MbrCryptoCurrencies mbrCryptoCurrencies, HttpServletRequest request) {
        //会员与加密货币关联表增加id字段进行关联
        if (mbrCryptoCurrencies.getBankCardId()!=null) {
            BaseBank baseBank = baseBankService.queryObject(mbrCryptoCurrencies.getBankCardId());
            mbrCryptoCurrencies.setCurrencyCode(baseBank.getBankCode());
            mbrCryptoCurrencies.setCurrencyProtocol(baseBank.getCategory());
        } else {
            BaseBank baseBank = new BaseBank();
            baseBank.setBankCode(mbrCryptoCurrencies.getCurrencyCode());
            baseBank.setCategory(mbrCryptoCurrencies.getCurrencyProtocol());
            List<BaseBank> baseBanks = baseBankService.select(baseBank);
            mbrCryptoCurrencies.setBankCardId(baseBanks.get(0).getId());
        }
        verifyCryptoCurrencies(mbrCryptoCurrencies);
        return mbrCryptoCurrenciesService.saveCryptoCurrencies(mbrCryptoCurrencies, Constants.EVNumber.two);
    }

    @PostMapping("/update")
    @RequiresPermissions("member:cryptocurrencies:update")
    @ApiOperation(value = "会员加密货币-更新", notes = "更新会员加密货币明细信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    @SysLog(module = "会员模块-加密货币", methodText = "更新加密货币")
    public R update(@RequestBody MbrCryptoCurrencies mbrCryptoCurrencies, HttpServletRequest request) {
        Assert.isNull(mbrCryptoCurrencies.getId(), "记不ID不能为空!");
        verifyCryptoCurrencies(mbrCryptoCurrencies);
        return mbrCryptoCurrenciesService.updateCryptoCurrencies(mbrCryptoCurrencies, getUser().getUsername(), CommonUtil.getIpAddress(request));
    }

    @PostMapping("/available")
    @RequiresPermissions("member:cryptocurrencies:available")
    @ApiOperation(value = "会员加密货币-更新状态", notes = "状态更新")
    @SysLog(module = "会员模块-加密货币", methodText = "更新加密货币状态")
    public R available(@RequestBody MbrCryptoCurrencies mbrCryptoCurrenciesDto, HttpServletRequest request) {
        // 参数校验
        Assert.isNull(mbrCryptoCurrenciesDto.getId(), "ID不能为空!");
        Assert.isNull(mbrCryptoCurrenciesDto.getAvailable(), "available不能为空!");
        return mbrCryptoCurrenciesService.availableCryptoCurrencies(mbrCryptoCurrenciesDto);

    }

    @PostMapping("/delete")
    @RequiresPermissions("member:cryptocurrencies:delete")
    @ApiOperation(value = "会员加密货币-删除", notes = "根据加密货币Id逻辑删除加密货币")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    @SysLog(module = "会员模块-加密货币", methodText = "删除加密货币")
    public R delete(@RequestBody MbrCryptoCurrencies mbrCryptoCurrencies, HttpServletRequest request) {
        // 参数校验
        Assert.isNull(mbrCryptoCurrencies.getIds(), "ids不能为空!");
        mbrCryptoCurrenciesService.deleteBatch(mbrCryptoCurrencies.getIds(), getUser().getUsername(), Constants.EVNumber.two, CommonUtil.getIpAddress(request));
        return R.ok();
    }

    private void verifyCryptoCurrencies(MbrCryptoCurrencies mbrCryptoCurrencies) {
        Assert.isNull(mbrCryptoCurrencies.getAccountId(), "会员ID不能为空!");
        Assert.isBlank(mbrCryptoCurrencies.getWalletName(), "钱包类型不能为空!");
        Assert.isBlank(mbrCryptoCurrencies.getWalletAddress(), "钱包地址不能为空!");

    }

    @GetMapping("/queryAddress")
//    @RequiresPermissions("member:cryptocurrencies:list")
    @ApiOperation(value = "存款钱包地址查询", notes = "存款钱包地址查询")
    public R list(@RequestParam("userId") @NotNull Integer userId) {
        // 查询会员存款钱包地址
        return R.ok().put(cryptoCurrenciesService.queryAddress(userId));
    }
}
