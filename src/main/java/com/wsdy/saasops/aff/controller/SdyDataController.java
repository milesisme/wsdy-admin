package com.wsdy.saasops.aff.controller;

import com.alibaba.fastjson.JSON;
import com.wsdy.saasops.aff.annotation.EncryptionCheck;
import com.wsdy.saasops.aff.dto.*;
import com.wsdy.saasops.api.modules.apisys.service.TCpSiteService;
import com.wsdy.saasops.aff.service.SdyDataService;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.analysis.entity.GameReportQueryModel;
import com.wsdy.saasops.modules.member.service.MbrAccountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

@Slf4j
@RestController
@RequestMapping("/aff/data")
@Api(value = "api", tags = "sdy代理接口")
public class SdyDataController {

    @Autowired
    private TCpSiteService tCpSiteService;
    @Autowired
    private MbrAccountService mbrAccountService;
    @Autowired
    private SdyDataService sdyDataService;

    @GetMapping("test")
    public R test() {
        return R.ok();
    }

    @EncryptionCheck
    @GetMapping("checkUser")
    @ApiOperation(value = "会员接口-账号检测", notes = "根据会员账号检测账号是否存在，存在msg为真，不存在msg为假!")
    public R chkAccount(@RequestParam("membercode") String membercode,
                        @RequestParam(value = "siteCode") String siteCode,
                        HttpServletRequest request) {
        Assert.isBlank(membercode, "用户名不能为空");
        Assert.isBlank(siteCode, "siteCode不能为空");
        request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(siteCode));
        int count = mbrAccountService.findAccountOrAgentByName(membercode);
        return R.ok(count > 0 ? Boolean.TRUE : Boolean.FALSE);
    }

    @EncryptionCheck
    @GetMapping("createAgent")
    @ApiOperation(value = "创建代理", notes = "创建代理")
    public R createAffiliate(@ModelAttribute CreateUserDto dto,
                             HttpServletRequest request) {
        Assert.isBlank(dto.getSiteCode(), "siteCode不能为空");
        log.info(dto.getMembercode() + "创建代理=" + JSON.toJSONString(dto));
        request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(dto.getSiteCode()));
        sdyDataService.createAffiliate(dto);
        return R.ok();
    }

    @EncryptionCheck
    @GetMapping("updateAgentInfo")
    @ApiOperation(value = "修改代理域名or推广码", notes = "修改代理域名or推广码")
    public R updateAgentInfo(@ModelAttribute CreateUserDto dto,
                             HttpServletRequest request) {
        Assert.isBlank(dto.getMembercode(), "会员不能为空");
        Assert.isBlank(dto.getSiteCode(), "siteCode不能为空");
        log.info(dto.getMembercode() + "修改代理域名or推广码" + JSON.toJSONString(dto));
        request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(dto.getSiteCode()));
        sdyDataService.updateAgent(dto);
        return R.ok();
    }

    @EncryptionCheck
    @GetMapping("updateAccountAgent")
    @ApiOperation(value = "修改会员代理", notes = "修改会员代理")
    public R updateAccountAgent(@ModelAttribute AccountAgentDto dto,
                                HttpServletRequest request) {
        Assert.isBlank(dto.getMembercode(), "会员不能为空");
        Assert.isBlank(dto.getParentmembercode(), "代理名不能为空");
        Assert.isBlank(dto.getSiteCode(), "siteCode不能为空");
        log.info(dto.getMembercode() + "修改会员代理" + JSON.toJSONString(dto));
        request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(dto.getSiteCode()));
        sdyDataService.updateAccountAgent(dto);
        return R.ok();
    }

    @EncryptionCheck
    @GetMapping("updateAgentSuperior")
    @ApiOperation(value = "调整代理上级", notes = "调整代理上级")
    public R updateAgentSuperior(@ModelAttribute AccountAgentDto dto,
                                 HttpServletRequest request) {
        Assert.isBlank(dto.getMembercode(), "代理不能为空");
        Assert.isBlank(dto.getParentmembercode(), "上级代理名不能为空");
        Assert.isBlank(dto.getSiteCode(), "siteCode不能为空");
        log.info(dto.getMembercode() + "修改代理关系" + JSON.toJSONString(dto));
        request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(dto.getSiteCode()));
        sdyDataService.updateAgentSuperior(dto);
        return R.ok();
    }

    @EncryptionCheck
    @GetMapping("accountList")
    @ApiOperation(value = "获取会员", notes = "获取会员")
    public R accountList(@RequestParam("pageNo") @NotNull Integer pageNo,
                         @RequestParam("pageSize") @NotNull Integer pageSize,
                         @ModelAttribute AccountListRequestDto requestDto,
                         HttpServletRequest request) {
        Assert.isBlank(requestDto.getSiteCode(), "siteCode不能为空");
        request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(requestDto.getSiteCode()));
        return R.ok().putPage(sdyDataService.accountList(pageNo, pageSize, requestDto));
    }

    @EncryptionCheck
    @GetMapping("depositAndWithdrawalList")
    @ApiOperation(value = "获取会员存款", notes = "获取会员存款")
    public R depositAndWithdrawalList(@RequestParam("pageNo") @NotNull Integer pageNo,
                                      @RequestParam("pageSize") @NotNull Integer pageSize,
                                      @ModelAttribute AccountDepositRequestDto requestDto,
                                      HttpServletRequest request) {
        Assert.isBlank(requestDto.getSiteCode(), "siteCode不能为空");
        request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(requestDto.getSiteCode()));
        return R.ok().putPage(sdyDataService.depositAndWithdrawalList(pageNo, pageSize, requestDto));
    }

    @EncryptionCheck
    @GetMapping("auditAndBonusList")
    @ApiOperation(value = "获取会员调整和优惠记录", notes = "获取会员存款")
    public R auditAndBonusList(@RequestParam("pageNo") @NotNull Integer pageNo,
                               @RequestParam("pageSize") @NotNull Integer pageSize,
                               @ModelAttribute AccountDepositRequestDto requestDto,
                               HttpServletRequest request) {
        Assert.isBlank(requestDto.getSiteCode(), "siteCode不能为空");
        request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(requestDto.getSiteCode()));
        return R.ok().putPage(sdyDataService.auditAndBonusList(pageNo, pageSize, requestDto));
    }

    @EncryptionCheck
    @GetMapping("/BetDetailList")
    @ApiOperation(value = "投注记录", notes = "全部游戏")
    public R betDetailsData(@RequestParam("pageNo") @NotNull Integer pageNo,
                            @RequestParam("pageSize") @NotNull Integer pageSize,
                            GameReportQueryModel model,
                            HttpServletRequest request) {
        Assert.isBlank(model.getSiteCode(), "siteCode不能为空");
        request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(model.getSiteCode()));
        return R.ok().put("page", sdyDataService.getRptBetListPage(pageNo, pageSize, model));
    }

/*    @EncryptionCheck
    @GetMapping("addBalance")
    @ApiOperation(value = "充值余额", notes = "充值余额")
    public R addBalance(@ModelAttribute AddBalanceDto dto,
                        HttpServletRequest request) {
        Assert.isBlank(dto.getSiteCode(), "siteCode不能为空");
        log.info(dto.getMembercode() + "充值余额" + JSON.toJSONString(dto));
        request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(dto.getSiteCode()));
        sdyDataService.addBalance(dto);
        return R.ok();
    }*/

    @EncryptionCheck
    @GetMapping("findAccountBet")
    @ApiOperation(value = "查询会员sum数据", notes = "查询会员sum数据")
    public R findAccountBet(@ModelAttribute AccountBetRequestDto dto,
                            @RequestParam("pageNo") @NotNull Integer pageNo,
                            @RequestParam("pageSize") @NotNull Integer pageSize,
                            HttpServletRequest request) {
        Assert.isBlank(dto.getSiteCode(), "siteCode不能为空");
        log.info(dto.getMembercode() + "充值余额" + JSON.toJSONString(dto));
        request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(dto.getSiteCode()));
        sdyDataService.findAccountBet(dto, pageNo, pageSize);
        return R.ok();
    }

    @EncryptionCheck
    @GetMapping("findAdjustBonus")
    @ApiOperation(value = "获取会员优惠冲销", notes = "获取会员优惠冲销")
    public R findAdjustBonus(@RequestParam("pageNo") @NotNull Integer pageNo,
                             @RequestParam("pageSize") @NotNull Integer pageSize,
                             @ModelAttribute AccountDepositRequestDto requestDto,
                             HttpServletRequest request) {
        Assert.isBlank(requestDto.getSiteCode(), "siteCode不能为空");
        request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(requestDto.getSiteCode()));
        return R.ok().putPage(sdyDataService.findAdjustBonus(requestDto, pageNo, pageSize));
    }
}
