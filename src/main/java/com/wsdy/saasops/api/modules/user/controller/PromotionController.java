package com.wsdy.saasops.api.modules.user.controller;

import com.wsdy.saasops.api.annotation.Login;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.user.dto.PromotionUrlDto;
import com.wsdy.saasops.api.modules.user.dto.RebateAccountDto;
import com.wsdy.saasops.api.modules.user.service.ApiPromotionService;
import com.wsdy.saasops.api.modules.user.service.ApiUserService;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.common.validator.ValidRegUtils;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrRebateReportNew;
import com.wsdy.saasops.modules.system.systemsetting.entity.SysSetting;
import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;


@Slf4j
@RestController
@RequestMapping("/api/promotion")
@Api(value = "promotion", tags = "添加好友，推广")
public class PromotionController {

    @Autowired
    private ApiPromotionService promotionService;
    @Autowired
    private ApiUserService apiUserService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private SysSettingService sysSettingService;

    @Login
    @GetMapping("info")
    @ApiOperation(value = "推荐好友查看页面", notes = "推荐好友查看页面")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "Integer", paramType = "header")})
    public R promotionInfo(HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        Integer rebateCastDepth = Integer.valueOf(sysSettingService.getSysSetting(SystemConstants.REBATE_CAST_DEPTH).getSysvalue());
        return R.ok().put(promotionService.promotionInfo(accountId,rebateCastDepth));
    }

    @Login
    @GetMapping("url")
    @ApiOperation(value = "推荐好友返回推广链接", notes = "推荐好友返回推广链接")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "Integer", paramType = "header")})
    public R promotionUrl(HttpServletRequest request) {
        String siteCode = CommonUtil.getSiteCode();
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        String key = RedisConstants.PROMOTION_ACCOUNT_ISCHECK + siteCode + accountId;
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, accountId, 10, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(isExpired)) {
            PromotionUrlDto promotionUrlDto = promotionService.promotionUrl(accountId, loginName);
            redisService.del(key);
            return R.ok().put(promotionUrlDto);
        }
        return R.error();
    }

    @Deprecated
    @Login
    @GetMapping("qrCode")
    @ApiOperation(value = "推荐好友生成二维码", notes = "推荐好友生成二维码")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "Integer", paramType = "header")})
    public void promotionQRCode(HttpServletRequest request, HttpServletResponse response) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        promotionService.promotionQRCode(accountId, response);
    }

    @Login
    @PostMapping("addAccount")
    @ApiOperation(value = "推荐好友添加好友", notes = "推荐好友添加好友")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "Integer", paramType = "header")})
    public R addAccount(@RequestBody MbrAccount account, HttpServletRequest request) {
        ValidRegUtils.validloginName(account, SysSetting.SysValueConst.require);
        ValidRegUtils.validPwd(account, SysSetting.SysValueConst.require);
        Assert.isBlank(account.getCaptchareg(), "验证码不能为空");
        account.setLoginName(account.getLoginName().toLowerCase());
        String kaptcha = "";
        if (StringUtils.isNotEmpty(account.getCodeSign())) {
            kaptcha = redisService.getKeyAndDel(account.getCodeSign());
        }
        if (!account.getCaptchareg().equals(kaptcha)) {
            throw new R200Exception("验证码不正确");
        }
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        account.setLoginIp(CommonUtil.getIpAddress(request));
        promotionService.addAccount(accountId, account);
        return R.ok();
    }

    @Login
    @GetMapping("rebateReport")
    @ApiOperation(value = "推荐好友返点统计", notes = "推荐好友返点统计")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "Integer", paramType = "header")})
    public R rebateReport(@ModelAttribute MbrRebateReportNew rebateReport, HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        Integer rebateCastDepth = Integer.valueOf(sysSettingService.getSysSetting(SystemConstants.REBATE_CAST_DEPTH).getSysvalue());
        rebateReport.setAccountId(accountId);
        rebateReport.setDepth(rebateCastDepth);
        return R.ok().put("item",promotionService.rebateReport(rebateReport))
                .put("count",promotionService.findActiveUserCount(rebateReport))
                .put("subRebates1",promotionService.getRebateTotalByDepth(rebateReport, Constants.EVNumber.one,Constants.EVNumber.one))
                .put("subRebates2",promotionService.getRebateTotalByDepth(rebateReport, Constants.EVNumber.two,rebateCastDepth));
        //.put("totalBalance", promotionService.findTotalBalance(rebateReport));
    }

    @Login
    @GetMapping("recentlyActive")
    @ApiOperation(value = "最近活跃推荐好友列表", notes = "最近活跃推荐好友列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "Integer", paramType = "header")})
    public R recentlyActive(@ModelAttribute MbrRebateReportNew rebateReport, HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        Integer rebateCastDepth = Integer.valueOf(sysSettingService.getSysSetting(SystemConstants.REBATE_CAST_DEPTH).getSysvalue());
        rebateReport.setAccountId(accountId);
        rebateReport.setDepth(rebateCastDepth);
        PageUtils pageUtil = promotionService.recentlyActive(rebateReport);
        return R.ok().put("page", pageUtil);
    }

    @Login
    @GetMapping("accountRebateReport")
    @ApiOperation(value = "指定推荐好友返点统计", notes = "指定推荐好友返点统计")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "Integer", paramType = "header")})
    public R accountRebateReport(@ModelAttribute RebateAccountDto rebateAccountDto, HttpServletRequest request) {

        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        MbrRebateReportNew mbrRebateReport = new MbrRebateReportNew();
        mbrRebateReport.setAccountId(accountId);
        mbrRebateReport.setSubAccountId(rebateAccountDto.getAccountId());
        mbrRebateReport.setStartTime(rebateAccountDto.getStartTime());
        mbrRebateReport.setEndTime(rebateAccountDto.getEndTime());
        return R.ok().put("item",promotionService.rebateContributeReport(mbrRebateReport)).
                put("validBet",promotionService.findValidBetTotal(rebateAccountDto)).
                put("info", promotionService.rebateInfo(rebateAccountDto.getAccountId()));
    }
}
