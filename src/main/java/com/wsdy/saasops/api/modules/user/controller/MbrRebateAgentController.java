package com.wsdy.saasops.api.modules.user.controller;

import com.wsdy.saasops.api.annotation.Login;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.mbrRebateAgent.dto.MbrRebateAgentQryDto;
import com.wsdy.saasops.modules.mbrRebateAgent.dto.MbrRebateAgentRespDto;
import com.wsdy.saasops.modules.mbrRebateAgent.service.MbrRebateAgentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/api/mbrRebateAgent")
@Api(value = "mbrRebateAgent", tags = "全民代理")
public class MbrRebateAgentController {

    @Autowired
    private MbrRebateAgentService mbrRebateAgentService;

    @Login
    @GetMapping("/mbrRebateAgentInfo")
    @ApiOperation(value = "全民代理页面-基础信息", notes = "全民代理页面-基础信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "Integer", paramType = "header")})
    public R mbrRebateAgentInfo(HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        MbrRebateAgentRespDto ret = mbrRebateAgentService.mbrRebateAgentInfo(accountId);
        return R.ok().put(ret);
    }

    @Login
    @GetMapping("/applyMbrAgent")
    @ApiOperation(value = "申请全民代理", notes = "申请全民代理")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "Integer", paramType = "header")})
    public R applyMbrAgent(HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);

        mbrRebateAgentService.applyMbrAgent(accountId);
        return R.ok();
    }

    @Login
    @GetMapping("/qryRebateInfo")
    @ApiOperation(value = "代理会员数据", notes = "代理会员数据")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "Integer", paramType = "header")})
    public R qryRebateInfo(@ModelAttribute MbrRebateAgentQryDto dto, HttpServletRequest request) {
        Assert.isNull(dto.getCreateTime(), "时间不能为空");
        Assert.isNull(dto.getCreateTimeStart(), "时间不能为空!");
        Assert.isNull(dto.getCreateTimeEnd(), "时间不能为空!!");

        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        dto.setChildNodeId(accountId);

        MbrRebateAgentRespDto ret = mbrRebateAgentService.qryRebateInfo(dto);
        return R.ok().put(ret);
    }
}
