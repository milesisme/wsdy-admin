package com.wsdy.saasops.api.modules.activity.controller;

import com.wsdy.saasops.api.annotation.Login;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.activity.dto.HuPengFriendRewardSummaryDto;
import com.wsdy.saasops.api.modules.activity.dto.HuPengRebateDto;
import com.wsdy.saasops.api.modules.activity.dto.HuPengSummaryDto;
import com.wsdy.saasops.api.modules.activity.service.HuPengRebateService;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.api.utils.JwtUtils;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.utils.StringUtil;
import com.wsdy.saasops.common.validator.Assert;
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
@RequestMapping("/api/activity/hupeng")
@Api(value = "hupeng", tags = "呼朋换友")
public class HuPengRebateController {

    @Autowired
    private HuPengRebateService huPengRebateService;

    @Autowired
    private JsonUtil jsonUtil;

    @Autowired
    private JwtUtils jwtUtils;
    @Login
    @GetMapping("rule")
    @ApiOperation(value = "好友推荐规则", notes = "呼朋唤友")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R  getRule(HttpServletRequest request){
        OprActRule oprActRule  = huPengRebateService.findActRuleByCode(TOpActtmpl.mbrRebateHuPengCode);
        HuPengRebateDto  huPengRebateDto = jsonUtil.fromJson(oprActRule.getRule(), HuPengRebateDto.class);
        return R.ok(huPengRebateDto.getHuPengLevelRewardDtoList());
    }

    @Login
    @GetMapping("getHuPengRebateList")
    @ApiOperation(value = "查询我的好友推荐", notes = "呼朋唤友")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R  getHuPengRebateList(HttpServletRequest request,
                                     @RequestParam(value = "startTime", required = false)String startTime, @RequestParam(value = "endTime", required = false)String endTime,
                                     @RequestParam(value = "subLoginName", required = false)String subLoginName,
                                    @RequestParam("pageNo")Integer pageNo,  @RequestParam("pageSize")Integer pageSize){
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok(huPengRebateService.getApiHuPengRebateDtoList(startTime,  endTime,  accountId,  subLoginName, pageNo,  pageSize));
    }

    @Login
    @GetMapping("getHupengRebateRewardSummary")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @ApiOperation(value = "个人收益统计", notes = "呼朋唤友")
    public R  getHupengRebateRewardSummary(HttpServletRequest request,
                                                   @RequestParam(value = "startTime", required = false)String startTime,
                                                   @RequestParam(value = "endTime", required = false)String endTime,
                                                   @RequestParam(value = "subLoginName", required = false)String subLoginName){
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        HuPengFriendRewardSummaryDto hupengRebateRewardSummary = huPengRebateService.getHupengRebateRewardSummary(startTime, endTime, accountId, subLoginName);
        if(hupengRebateRewardSummary == null){
            hupengRebateRewardSummary = new HuPengFriendRewardSummaryDto();
        }
        return R.ok(hupengRebateRewardSummary);
    }


    @Login
    @GetMapping("getHuPengRebateRewardReportForDay")
    @ApiOperation(value = "日报表", notes = "呼朋唤友")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header"),
             @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R  getFriendRebateRewardReportForDay(HttpServletRequest request, @RequestParam(value = "startTime", required = false)String startTime, @RequestParam(value = "endTime", required = false)String endTime, @RequestParam("pageNo")Integer pageNo,  @RequestParam("pageSize")Integer pageSize){
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok(huPengRebateService.getHuPengRebateRewardReportForDay( startTime,  endTime,  accountId, pageNo, pageSize));
    }

    @Login
    @GetMapping("getHuPengRebateRewardReportForMonth")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header"),
             @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @ApiOperation(value = "月报表", notes = "呼朋唤友")
    public R  getFriendRebateRewardReportForMonth(HttpServletRequest request, @RequestParam(value = "startTime" , required = false)String startTime, @RequestParam(value = "endTime", required = false)String endTime, @RequestParam("pageNo")Integer pageNo,  @RequestParam("pageSize")Integer pageSize){
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok(huPengRebateService.getHuPengRebateRewardReportForMonth( startTime,  endTime,  accountId, pageNo, pageSize));
    }


    @GetMapping("getHuPengRebateInfo")
    @ApiOperation(value = "呼朋换友开启信息", notes = "呼朋唤友")
    @ApiImplicitParams({
             @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R  getHuPengRebateInfo(HttpServletRequest request){
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
        return R.ok(huPengRebateService.getHuPengRebateActInfo(accountId));
    }

    @Login
    @GetMapping("getBalance")
    @ApiOperation(value = "查询奖励", notes = "呼朋唤友")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R  getBalance(HttpServletRequest request){
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok(huPengRebateService.getHuPengBalance(accountId));
    }


    @Login
    @GetMapping("withdrawal")
    @ApiOperation(value = "提款", notes = "呼朋唤友")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R  withdrawal(HttpServletRequest request, @RequestParam(value = "amount")BigDecimal amount, @RequestParam(value = "type") Integer type, @RequestParam(value = "bankCardId" , required = false) Integer bankCardId){
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        Assert.isNull(amount, "金额不能为空");
        Assert.isNull(type, "类型不能为空");

        if(amount.compareTo(BigDecimal.ZERO) ==-1){
            throw  new R200Exception("提款金额必须大于0");
        }

        if(type == Constants.EVNumber.two){
            Assert.isNull(bankCardId, "银行卡不能为空");
        }
        huPengRebateService.withdrawal(accountId, loginName, amount,type,  bankCardId,  request);
        return R.ok();
    }


    @Login
    @GetMapping("rewardList")
    @ApiOperation(value = "交易记录", notes = "呼朋唤友")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R  rewardList(HttpServletRequest request, @RequestParam(value = "startTime", required = false)String startTime, @RequestParam(value = "endTime", required = false)String endTime, @RequestParam("pageNo")Integer pageNo,  @RequestParam("pageSize")Integer pageSize){
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok(huPengRebateService.rewardList(startTime, endTime, accountId, pageNo, pageSize));
    }



    @Login
    @GetMapping("getHuPengSummary")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @ApiOperation(value = "昨日汇总信息", notes = "呼朋唤友")
    public R  getHuPengSummary(HttpServletRequest request){
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        HuPengSummaryDto huPengSummaryDto = huPengRebateService.getHuPengSummary(accountId);
        return R.ok(huPengSummaryDto);
    }


}
