package com.wsdy.saasops.api.modules.user.controller;

import com.wsdy.saasops.api.annotation.Login;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.user.dto.RebateAccSanGongDetailDto;
import com.wsdy.saasops.api.modules.user.dto.RebateAccSanGongDto;
import com.wsdy.saasops.api.modules.user.dto.RebateAccSanGongSumDto;
import com.wsdy.saasops.api.modules.user.service.ApiPromotionService;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.service.MbrAccountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;


@Slf4j
@RestController
@RequestMapping("/api/promotion")
@Api(value = "PromotionEgSanGongController", tags = "EG三公佣金")
public class PromotionEgSanGongController {
    @Autowired
    private ApiPromotionService promotionService;
    @Autowired
    private MbrAccountService mbrAccountService;

    @Login
    @GetMapping("getSubAccRebateRatio")
    @ApiOperation(value = "获得下级会员数据", notes = "获得下级会员数据")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "Integer", paramType = "header")})
    public R getSubAccRebateRatio(@ModelAttribute RebateAccSanGongDto rebateAccSanGongDto, HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        rebateAccSanGongDto.setParentAccId(accountId);
        PageUtils pageUtil = promotionService.getSubAccRebateRatio(rebateAccSanGongDto);
        return R.ok().put("page", pageUtil);
    }

    @Login
    @PostMapping("/modifyRebateRatio")
    @ApiOperation(value = "修改会员返利比例", notes = "修改会员返利比例")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    public R modifyRebateRatio(@RequestBody MbrAccount mbrAccount, HttpServletRequest request) {
        Assert.isNull(mbrAccount.getId(), "会员id不能为空");
        Assert.isPercent(mbrAccount.getRebateRatio(), "请输入正确的反水比例");

        // 校验修改的会员与登录会员的上下级关系
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        if(!promotionService.verifyMbrRelation(mbrAccount.getId(),accountId)){
            throw new R200Exception("仅能修改下级会员的返利比例！");
        }
        mbrAccountService.modifyRebateRatio(mbrAccount);
        return R.ok();
    }

    @Login
    @GetMapping("/getSubAccRebateSum")
    @ApiOperation(value = "下级会员返利收益总览数据 ", notes = "下级会员返利收益总览数据 ")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    public R getSubAccRebateSum(@ModelAttribute RebateAccSanGongSumDto rebateAccSanGongSumDto,HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        if(Objects.isNull(rebateAccSanGongSumDto.getParentId())){   // h5允许查看下下级会员总览
            rebateAccSanGongSumDto.setParentId(accountId);
        }
        RebateAccSanGongSumDto sum = promotionService.getSubAccRebateSum(rebateAccSanGongSumDto);
        return R.ok().put("data",sum);
    }

    @Login
    @GetMapping("/getSubAccRebateDetail")
    @ApiOperation(value = "下级会员返利收益明细数据 ", notes = "下级会员返利收益明细数据 ")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    public R getSubAccRebateDetail(@ModelAttribute RebateAccSanGongDetailDto rebateAccSanGongDetailDto, HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        rebateAccSanGongDetailDto.setParentId(accountId);
        PageUtils pageUtil = promotionService.getSubAccRebateDetail(rebateAccSanGongDetailDto);
        return R.ok().put("page", pageUtil);
    }

    @Login
    @GetMapping("/getRebateInfo")
    @ApiOperation(value = "好友返利总收益、昨日收益、会员数", notes = "好友返利总收益、昨日收益、会员数")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "Integer", paramType = "header")})
    public R getRebateInfo(HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put(promotionService.getRebateInfo(accountId));
    }
}
