package com.wsdy.saasops.modules.member.controller;

import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.analysis.entity.GameReportQueryModel;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.fund.entity.AccWithdraw;
import com.wsdy.saasops.modules.fund.entity.FundAudit;
import com.wsdy.saasops.modules.fund.entity.FundDeposit;
import com.wsdy.saasops.modules.log.entity.LogMbrLogin;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrBillManage;
import com.wsdy.saasops.modules.member.entity.MbrMemo;
import com.wsdy.saasops.modules.member.service.MbrWindService;
import com.wsdy.saasops.modules.operate.entity.OprActBonus;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/bkapi/member/wind")
@Api(value = "mbrAccount", tags = "会员风控信息")
public class MbrWindController extends AbstractController {
    @Autowired
    private MbrWindService mbrWindService;

   @GetMapping("/mbrList")
   @RequiresPermissions(value = {"member:wind:mbrList", "member:wind:info"}, logical = Logical.OR)
   @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
           required = true, dataType = "Integer", paramType = "header")})
   @ApiOperation(value = "风控会员列表", notes = "风控会员列表")
   public R mbrList(@ModelAttribute MbrAccount mbrAccount,
                        @RequestParam("pageNo") @NotNull Integer pageNo,
                        @RequestParam("pageSize") @NotNull Integer pageSize){
       PageUtils page  = mbrWindService.mbrList(mbrAccount, pageNo, pageSize);
       return R.ok().put("page", page);
   }
    @GetMapping("/mbrInfoByAcc")
    @RequiresPermissions("member:wind:balance")
    @ApiOperation(value = "风控会员信息", notes = "风控会员信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    public R mbrInfoByAccount(@RequestParam("accountId") @NotNull Integer accountId){
        return R.ok().put("mbr",mbrWindService.mbrInfoByAccount(accountId));
    }

    @GetMapping("/memolist")
    @RequiresPermissions("member:wind:memolist")
    @ApiOperation(value = "备注信息List", notes = "备注信息List")
    public R memoList(@ModelAttribute MbrMemo mbrMemo,
                      @RequestParam("pageNo") @NotNull Integer pageNo,
                      @RequestParam("pageSize") @NotNull Integer pageSize){
        return R.ok().putPage(mbrWindService.memoList(mbrMemo,pageNo,pageSize));
    }
    @PostMapping("/mbrNewMemo")
    @RequiresPermissions("member:wind:newmemo")
    @ApiOperation(value = "新增备注接口", notes = "新增备注接口")
    public R mbrNewMemo(@RequestBody MbrMemo mbrMemo,HttpServletRequest request){
        mbrWindService.mbrNewMemo(mbrMemo, getUser(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @GetMapping("/balance")
    @RequiresPermissions("member:wind:balance")
    @ApiOperation(value = "余额tab-主账户余额获取和刷新", notes = "主账户余额获取和刷新")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    public R balance(@RequestParam("accountId") @NotNull Integer accountId){
        return R.ok().put("totalBalance",mbrWindService.balance( accountId));
    }
    @GetMapping("/depotBalance")
    @RequiresPermissions("member:wind:balance")
    @ApiOperation(value = "余额tab-平台余额列表", notes = "余额tab-平台余额列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    public R depotBalance(@RequestParam("accountId") @NotNull Integer accountId,
                              @RequestParam("pageNo") @NotNull Integer pageNo,
                              @RequestParam("pageSize") @NotNull Integer pageSize){
        return mbrWindService.depotBalance(pageNo, pageSize, accountId);
    }
    @GetMapping("flushBalance")
    @ResponseBody
    @ApiOperation(value = "余额tab-单平台余额刷新", notes = "余额tab-单平台余额刷新")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R flushBalance(@RequestParam("accountId") @NotNull Integer accountId, @RequestParam("platformId") @NotNull Integer platformId) {
        return R.ok().put(mbrWindService.flushBalance(accountId, platformId));
    }


    @GetMapping("/depositList")
    @RequiresPermissions("member:wind:deposit")
    @ApiOperation(value = "存款tab", notes = "存款tab")
    public R depositList(@ModelAttribute FundDeposit deposit,
                         @RequestParam("pageNo") @NotNull Integer pageNo,
                         @RequestParam("pageSize") @NotNull Integer pageSize) {
       Assert.isNull(deposit.getAccountId(),"accountId 不为空");
        return R.ok().put("depotList",mbrWindService.mbrdepositList(deposit,pageNo, pageSize));
    }

    @GetMapping("/mbrwithdrawList")
    @RequiresPermissions("member:wind:withdraw")
    @ApiOperation(value = "提款tab", notes = "提款tab")
    public R mbrwithdrawList(@ModelAttribute AccWithdraw accWithdraw,
                             @RequestParam("pageNo") @NotNull Integer pageNo,
                             @RequestParam("pageSize") @NotNull Integer pageSize){
        return R.ok().put("withdraw",mbrWindService.mbrwithdrawList(accWithdraw,pageNo, pageSize));
    }

    @RequiresPermissions("member:wind:deviceiP")
    @GetMapping("/mbrDeviceIpTop")
    @ApiOperation(value = "设备IP tab", notes = "tab-设备ip-表头")
    public R mbrDeviceIpTop(@RequestParam("accountId") @NotNull Integer accountId){
        Map<String,Integer> map = mbrWindService.mbrDeviceIpTop(accountId);
        return R.ok().put(map);
    }
    @GetMapping("/mbrPreferIP")
    @RequiresPermissions("member:wind:activity")
    @ApiOperation(value = "风控-设备IP-同IP用户数", notes = "点击ip同用户查询")
     public R mbrPreferNum1(@RequestParam("accountId") @NotNull Integer accountId,
                           @RequestParam("pageNo") @NotNull Integer pageNo,
                           @RequestParam("pageSize") @NotNull Integer pageSize){
        PageUtils pageUtils = mbrWindService.activitymbrIP(accountId, pageNo, pageSize);
        return R.ok().put("activityIP",pageUtils);
     }
    @RequiresPermissions("member:wind:activity")
    @ApiOperation(value = "风控-设备IP-同IP优惠数", notes = "点击同IP优惠数查询")
    @GetMapping("/mbrwithIPPrefNum")
    public R mbrwithIPPrefNum(@RequestParam("accountId") @NotNull Integer accountId,
                              @RequestParam("pageNo") @NotNull Integer pageNo,
                              @RequestParam("pageSize") @NotNull Integer pageSize){
        PageUtils pageUtils1 = mbrWindService.mbrwithIPPrefNum(accountId, pageNo, pageSize);
        return R.ok().put("activityDevice",pageUtils1);
    }
    @RequiresPermissions("member:wind:activity")
    @ApiOperation(value = "风控-设备IP-同设备用户数", notes = "点击同设备查询")
    @GetMapping("/mbrPreferDevice")
    public R mbrPreferNum2(@RequestParam("accountId") @NotNull Integer accountId,
                          @RequestParam("pageNo") @NotNull Integer pageNo,
                          @RequestParam("pageSize") @NotNull Integer pageSize){
        PageUtils pageUtils1 = mbrWindService.activitymbrDevice(accountId, pageNo, pageSize);
        return R.ok().put("activityDevice",pageUtils1);
    }
    @GetMapping("/mbrwithDevicePrefNum")
    @RequiresPermissions("member:wind:activity")
    @ApiOperation(value = "风控-设备IP-同Device优惠数", notes = "点击同Device优惠数查询")
    public R mbrwithDevicePrefNum(@RequestParam("accountId") @NotNull Integer accountId,
                              @RequestParam("pageNo") @NotNull Integer pageNo,
                              @RequestParam("pageSize") @NotNull Integer pageSize){
        PageUtils pageUtils1 = mbrWindService.mbrwithDevicePrefNum(accountId, pageNo, pageSize);
        return R.ok().put("activityDevice",pageUtils1);
    }
    @GetMapping("/getBonusList")
    @RequiresPermissions("member:wind:activity")
    @ApiOperation(value = "获得会员优惠列表", notes = "点击领取数量查询会员的优惠列表")
    public R getBonusList(@RequestParam("accountId") @NotNull Integer accountId,
                              @RequestParam("pageNo") @NotNull Integer pageNo,
                              @RequestParam("pageSize") @NotNull Integer pageSize){
        PageUtils pageUtils1 = mbrWindService.getBonusList(accountId, pageNo, pageSize);
        return R.ok().put("bonusList",pageUtils1);
    }

    @GetMapping("/mbrbillList")
    @RequiresPermissions("member:wind:billList")
    @ApiOperation(value = "tab-户内转账", notes = "tab-户内转账")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R mbrbillList(@ModelAttribute MbrBillManage mbrBillManage,
                         @RequestParam("pageNo") @NotNull Integer pageNo,
                         @RequestParam("pageSize") @NotNull Integer pageSize) {
        Assert.isNull(mbrBillManage.getAccountId(),"accountId 不为空");
        return R.ok().putPage(mbrWindService.mbrbillList(mbrBillManage, pageNo, pageSize));
    }

    @GetMapping("/mbrfinalBetDetailsAll")
    @RequiresPermissions("member:wind:Beta")
    @ApiOperation(value = "tab-投注记录", notes = "投注记录")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R mbrbetDetailsData(@RequestParam("pageNo") @NotNull Integer pageNo,
                               @RequestParam("pageSize") @NotNull Integer pageSize,
                               GameReportQueryModel model) {
        Assert.isNull(model.getLoginName(),"loginName 不为空");
        return mbrWindService.mbrbetDetailsData(pageNo,pageSize,model);
    }

    @GetMapping("/mbrbonusList")
    @RequiresPermissions("member:wind:bonus")
    @ApiOperation(value = "tab-红利优惠", notes = "tab-红利优惠")
    public R mbrBonusList(@ModelAttribute OprActBonus bonus,
                       @RequestParam("pageNo") @NotNull Integer pageNo,
                       @RequestParam("pageSize") @NotNull Integer pageSize) {
        Assert.isNull(bonus.getAccountId(),"accountId 不为空");
        return R.ok().putPage(mbrWindService.bonusList(bonus, pageNo, pageSize));
    }

    @GetMapping("/mbrauditList")
    @RequiresPermissions("member:wind:audit")
    @ApiOperation(value = "tab-资金调整", notes = "tab-资金调整")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R mbrauditList(@ModelAttribute FundAudit fundAudit, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        Assert.isNull(fundAudit.getAccountId(),"accountId 不为空");
        return R.ok().put(mbrWindService.mbrauditList(fundAudit,pageNo,pageSize));
    }

    @GetMapping("/mbrLoginlist")
    @RequiresPermissions("member:wind:loginlog")
    @ApiOperation(value = "tab-登陆历史", notes = "tab-登陆历史")
    public R mbrLoginlist(@ModelAttribute LogMbrLogin logMbrlogin,
                          @RequestParam("pageNo") @NotNull Integer pageNo,
                          @RequestParam("pageSize") @NotNull Integer pageSize) {
        Assert.isNull(logMbrlogin.getAccountId(),"accountId 不为空");
        return R.ok().putPage(mbrWindService.mbrLoginlist(logMbrlogin, pageNo, pageSize));
    }
}
