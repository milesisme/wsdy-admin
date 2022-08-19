package com.wsdy.saasops.api.modules.user.controller;

import com.wsdy.saasops.api.annotation.Login;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.user.dto.*;
import com.wsdy.saasops.api.modules.user.service.FriendRebateService;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.api.utils.JwtUtils;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.utils.StringUtil;
import com.wsdy.saasops.modules.member.dto.RebateDto;
import com.wsdy.saasops.modules.operate.entity.OprActActivity;
import com.wsdy.saasops.modules.operate.entity.OprActRule;
import com.wsdy.saasops.modules.operate.entity.TOpActtmpl;
import io.jsonwebtoken.Claims;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/friendRebate")
@Api(value = "friendRebate", tags = "添加好友，推广")
public class FriendRebateController  {

    @Autowired
    private FriendRebateService friendRebateService;

    @Autowired
    private JsonUtil jsonUtil;

    @Autowired
    private JwtUtils jwtUtils;
    @Login
    @GetMapping("rule")
    @ApiOperation(value = "好友推荐规则", notes = "好友推荐规则")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R  getRule(HttpServletRequest request){
        OprActRule oprActRule  = friendRebateService.findActRuleByCode(TOpActtmpl.mbrRebateCode);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        FriendRebateAccountDto friendRebateAccountDto = friendRebateService.getFriendRebateAccountInfo(loginName);
        RebateDto rebateDto = jsonUtil.fromJson(oprActRule.getRule(), RebateDto.class);
        rebateDto.toMap();
        return R.ok(rebateDto.getLevelDtoMap().get(friendRebateAccountDto.getAccountLevel()));
    }

    @Login
    @GetMapping("getFriendRebateList")
    @ApiOperation(value = "查询我的好友推荐", notes = "查询我的好友推荐")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R  getApiFriendRebateList(HttpServletRequest request, @RequestParam(value = "firstChargeStartTime",required = false )String firstChargeStartTime, @RequestParam(value = "firstChargeEndTime", required = false)String firstChargeEndTime,
                                     @RequestParam(value = "startTime", required = false)String startTime, @RequestParam(value = "endTime", required = false)String endTime,
                                     @RequestParam(value = "subLoginName", required = false)String subLoginName,
                                     @RequestParam(value = "showAll", required = false)Integer showAll,
                                     @RequestParam("pageNo")Integer pageNo,  @RequestParam("pageSize")Integer pageSize){
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok(friendRebateService.getApiFriendRebateDtoList(  firstChargeStartTime,  firstChargeEndTime,  startTime,  endTime,  accountId,  subLoginName, showAll,  pageNo,  pageSize));
    }


    @Login
    @GetMapping("getFriendRebateDetails")
    @ApiOperation(value = "查询我的好友推荐详情", notes = "查询我的好友推荐")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header"),
             @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R  getFriendRebateDetails(HttpServletRequest request, @RequestParam(value = "startTime", required = false)String startTime, @RequestParam(value = "endTime", required = false)String endTime, @RequestParam("subLoginName")String subLoginName){
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        FriendRebateDto apiFriendRebateDto =  friendRebateService.getApiFriendRebateDtoDetails( startTime,  endTime,  loginName,  subLoginName);
        if(apiFriendRebateDto == null){
            apiFriendRebateDto  = new FriendRebateDto();
            apiFriendRebateDto.setCpValidBetReward(BigDecimal.ZERO);
            apiFriendRebateDto.setDjValidBetReward(BigDecimal.ZERO);
            apiFriendRebateDto.setDzValidBetReward(BigDecimal.ZERO);
            apiFriendRebateDto.setQpValidBetReward(BigDecimal.ZERO);
            apiFriendRebateDto.setTyValidBetReward(BigDecimal.ZERO);
            apiFriendRebateDto.setZrValidBetReward(BigDecimal.ZERO);

        }
        FriendRebateAccountDto friendRebateAccountDto = friendRebateService.getFriendRebateAccountInfo(subLoginName);
        apiFriendRebateDto.setLastLoginTime(friendRebateAccountDto.getLoginTime());
        apiFriendRebateDto.setVipLevel(friendRebateAccountDto.getAccountLevel());
        if(apiFriendRebateDto.getSubLoginName() == null){
            apiFriendRebateDto.setSubLoginName(friendRebateAccountDto.getLoginName());
        }
        return R.ok(apiFriendRebateDto);
    }

    @Login
    @GetMapping("getFriendRebateRewardReportForDay")
    @ApiOperation(value = "日报表", notes = "查询我的好友推荐")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header"),
             @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R  getFriendRebateRewardReportForDay(HttpServletRequest request, @RequestParam(value = "startTime", required = false)String startTime, @RequestParam(value = "endTime", required = false)String endTime, @RequestParam("pageNo")Integer pageNo,  @RequestParam("pageSize")Integer pageSize){
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok(friendRebateService.getFriendRebateRewardReportForDay( startTime,  endTime,  accountId, pageNo, pageSize));
    }

    @Login
    @GetMapping("getFriendRebateRewardReportForMonth")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header"),
             @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @ApiOperation(value = "月报表", notes = "查询我的好友推荐")
    public R  getFriendRebateRewardReportForMonth(HttpServletRequest request, @RequestParam(value = "startTime" , required = false)String startTime, @RequestParam(value = "endTime", required = false)String endTime, @RequestParam("pageNo")Integer pageNo,  @RequestParam("pageSize")Integer pageSize){
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok(friendRebateService.getFriendRebateRewardReportForMonth( startTime,  endTime,  accountId, pageNo, pageSize));
    }

    @Login
    @GetMapping("getFriendRebateSummary")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @ApiOperation(value = "汇总信息", notes = "查询我的好友推荐")
    public R  getFriendRebateSummary(HttpServletRequest request){
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        FriendRebateSummaryDto friendRebateSummaryDto = friendRebateService.getFriendRebateSummary(accountId);
        return R.ok(friendRebateSummaryDto);
    }


    @Login
    @GetMapping("getFriendRebatePersonalRewardSummary")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header"),
             @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @ApiOperation(value = "个人收益统计", notes = "查询我的好友推荐")
    public R  getFriendRebatePersonalRewardSummary(HttpServletRequest request, @RequestParam(value = "startTime", required = false)String startTime, @RequestParam(value = "endTime", required = false)String endTime){
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        FriendRebatePersonalRewardSummaryDto friendRebatePersonalRewardSummaryDto = friendRebateService.getFriendRebatePersonalRewardSummary(accountId, startTime, endTime);
        return R.ok(friendRebatePersonalRewardSummaryDto);
    }

    @Login
    @GetMapping("getFriendRebateFriendsRewardSummary")
    @ApiImplicitParams({
              @ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header"),
             @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @ApiOperation(value = "好友收益统计", notes = "查询我的好友推荐")
    public R  getFriendRebateFriendsRewardSummary(HttpServletRequest request, @RequestParam(value = "firstChargeStartTime",required = false )String firstChargeStartTime, @RequestParam(value = "firstChargeEndTime", required = false)String firstChargeEndTime,
                                                  @RequestParam(value = "startTime", required = false)String startTime,
                                                  @RequestParam(value = "endTime", required = false)String endTime,  @RequestParam(value = "subLoginName", required = false) String subLoginName){
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        FriendRebateFriendRewardSummaryDto friendRebateFriendRewardSummaryDto = friendRebateService.getFriendRebateFriendsRewardSummary(firstChargeStartTime, firstChargeEndTime, accountId, startTime, endTime, subLoginName);
        return R.ok(friendRebateFriendRewardSummaryDto);
    }

    @GetMapping("getFriendRebateInfo")
    @ApiOperation(value = "好友推荐开启信息", notes = "好友推荐规则")
    @ApiImplicitParams({
             @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R  getFriendRebateInfo(HttpServletRequest request){
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        if(accountId == null){
            String token = request.getHeader(jwtUtils.getHeader());
            if(StringUtil.isNotEmpty(token)){
                Claims claims = jwtUtils.getClaimByToken(token);
                String userInfo = claims.getSubject();
                String[] urerInfoArr = userInfo.split(ApiConstants.USER_TOKEN_SPLIT);
                accountId = Integer.parseInt(urerInfoArr[0]);
            }
        }
        return R.ok(friendRebateService.getFriendsRebateActInfo(accountId));
    }

    @Login
    @GetMapping("rewardList")
    @ApiOperation(value = "好友奖补", notes = "好友推荐规则")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R  rewardList(HttpServletRequest request, @RequestParam(value = "startTime", required = false)String startTime, @RequestParam(value = "endTime", required = false)String endTime, @RequestParam("pageNo")Integer pageNo,  @RequestParam("pageSize")Integer pageSize){
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok(friendRebateService.rewardList(startTime, endTime, accountId, pageNo, pageSize));
    }
}
