package com.wsdy.saasops.modules.member.controller;

import java.math.BigDecimal;
import java.util.List;

import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.common.annotation.SysLog;
import com.wsdy.saasops.modules.analysis.service.AnalysisService;
import com.wsdy.saasops.modules.member.dto.AuditDetailDto;
import com.wsdy.saasops.modules.member.entity.MbrAuditBonus;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.member.service.AuditAccountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;


@RestController
@RequestMapping("/bkapi/member/audit")
@Api(value = "MbrAccountAudit", tags = "会员稽核")
public class MbrAudiController extends AbstractController {

    @Autowired
    private AuditAccountService auditService;
    @Autowired
    private AnalysisService analysisService;

    @GetMapping("/immediatelyAudit")
    @RequiresPermissions("member:audit:list")
    @SysLog(module = "稽核报表", methodText = "即时稽核")
    @ApiOperation(value = "查询会员即时稽核", notes = "查询会员即时稽核")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R immediatelyAudit(@RequestParam("loginName") String loginName) {
        Assert.isBlank(loginName, "会员名不能为空");
        return R.ok().put(auditService.immediatelyAudit(loginName));
    }

    @GetMapping("withdrawAudit")
    @RequiresPermissions("member:audit:list")
    public R withdrawAudit(@RequestParam("loginName") String loginName) {
        Assert.isBlank(loginName, "会员名不能为空");
        return R.ok().put(auditService.withdrawAudit(loginName));
    }

    @GetMapping("/findAccouGroupByName")
    @ApiOperation(value = "查询会员组跟放宽额度", notes = "查询会员组跟放宽额度")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findAccouGroupByName(@RequestParam("loginName") String loginName) {
        Assert.isBlank(loginName, "会员名不能为空");
        return R.ok().put(auditService.findAccouGroupByName(loginName));
    }


    @GetMapping("/auditBonusInfo")
    @RequiresPermissions("member:audit:list")
    @ApiOperation(value = "查询会员优惠稽核单条", notes = "查询会员优惠稽核单条")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R auditBonusInfo(@RequestParam("auditBonusId") Integer auditBonusId) {
        Assert.isNull(auditBonusId, "ID不能为空");
        return R.ok().put(auditService.auditBonusInfo(auditBonusId));
    }


    @GetMapping("/auditHistoryBonusList")
    @RequiresPermissions("member:audit:list")
    @ApiOperation(value = "查询会员历史稽核未提款", notes = "查询会员历史稽核未提款")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R auditHistoryBonusList(@RequestParam("loginName") String loginName) {
        Assert.isBlank(loginName, "会员名不能为空");
        return R.ok().putPage(auditService.auditHistoryBonusList(loginName));
    }

    @GetMapping("/auditHistoryList")
    @RequiresPermissions("member:audit:list")
    @ApiOperation(value = "查询会员历史稽核", notes = "查询会员历史稽核")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R auditHistoryList(@RequestParam("loginName") String loginName,
                              @RequestParam("pageNo") @NotNull Integer pageNo,
                              @RequestParam("pageSize") @NotNull Integer pageSize) {
        Assert.isBlank(loginName, "会员名不能为空");
        return R.ok().putPage(auditService.auditHistoryList(loginName, pageNo, pageSize));
    }

    @PostMapping("/updateAudit")
    @RequiresPermissions("member:audit:update")
    @SysLog(module = "稽核报表", methodText = "修改稽核")
    @ApiOperation(value = "修改稽核只能更新不违规的", notes = "修改稽核只能更新不违规的")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateAudit(@RequestBody List<AuditDetailDto> detailDtos, HttpServletRequest request) {
        String ip = (String) request.getAttribute(ApiConstants.ip);
        auditService.updateAccountAuditList(detailDtos, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @GetMapping("/clearAudit")
    @RequiresPermissions("member:audit:clear")
    @SysLog(module = "稽核报表", methodText = "清空稽核")
    @ApiOperation(value = "clear稽核", notes = "clear稽核")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R clearAudit(@RequestParam("loginName") String loginName, HttpServletRequest request) {
        Assert.isBlank(loginName, "会员名不能为空");
        auditService.clearAccountAudit(loginName, getUser().getUsername(), CommonUtil.getSiteCode(), null, CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @GetMapping("/updateNormal")
    @RequiresPermissions("member:audit:normal")
    @SysLog(module = "稽核报表", methodText = "修改稽核违规变更正常")
    @ApiOperation(value = "修改稽核只能更新不违规的", notes = "修改稽核只能更新不违规的")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateNormal(@ModelAttribute MbrAuditBonus auditBonus, HttpServletRequest request) {
        auditService.updateNormal(auditBonus, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @GetMapping("/addAuditBonus")
    @RequiresPermissions("member:audit:add")
    @SysLog(module = "稽核报表", methodText = "增加稽核")
    @ApiOperation(value = "修改稽核违规,增加稽核", notes = "修改稽核违规,增加稽核")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R addAuditBonus(@ModelAttribute MbrAuditBonus auditBonus, HttpServletRequest request) {
        Assert.isBigDecimalNum(auditBonus.getAuditAmount(), "只能为正整数");
        auditService.addAuditBonus(auditBonus, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @GetMapping("/clearAuditBonus")
    @RequiresPermissions("member:audit:clearAudit")
    @SysLog(module = "稽核报表", methodText = "清空违规稽核")
    @ApiOperation(value = "清空违规稽核", notes = "清空违规稽核")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R clearAuditBonus(@ModelAttribute MbrAuditBonus auditBonus, HttpServletRequest request) {
        Assert.isNull(auditBonus.getId(), "ID不能为空");
        auditService.clearAuditBonus(auditBonus, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }


    @GetMapping("/findAuditAccountBalance")
    @SysLog(module = "稽核报表", methodText = "查询会员余额")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findAuditAccountBalance(@ModelAttribute MbrAuditBonus auditBonus) {
        Assert.isNull(auditBonus.getId(), "ID不能为空");
        return R.ok().put(auditService.findAuditAccountBalance(auditBonus, CommonUtil.getSiteCode()));
    }

    @GetMapping("/auditCharge")
    @RequiresPermissions("member:audit:charge")
    @SysLog(module = "稽核报表", methodText = "扣款")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R auditCharge(@RequestParam("id") Integer id,
                         @RequestParam("amount") BigDecimal amount,
                         @RequestParam("memo") String memo,
                         HttpServletRequest request) {
        Assert.isNull(id, "ID不能为空");
        Assert.isBigDecimalNum(amount, "只能为正整数");
        auditService.auditCharge(id, amount, memo, CommonUtil.getIpAddress(request),
                CommonUtil.getSiteCode(), getUser().getUsername());
        return R.ok();
    }

    @GetMapping("/getPlatformMaxTime")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R getPlatformMaxTime(@RequestParam("loginName") String loginName) {
        Assert.isNull(loginName, "会员名不能为空");
        return R.ok().put(analysisService.getPlatformMaxTime(CommonUtil.getSiteCode(), null,null));
    }

}
