package com.wsdy.saasops.api.modules.activity.controller;

import com.wsdy.saasops.api.annotation.Login;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.activity.service.FirstChargeApiService;
import com.wsdy.saasops.common.utils.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/activity/firstcharge")
@Api(value = "首冲返上级", tags = "首冲返上级")
public class FirstChargeApiController {

    @Autowired
    private FirstChargeApiService firstChargeApiService;
    @Login
    @GetMapping("getFirstChargeRebateList")
    @ApiOperation(value = "查询我的好友推荐", notes = "呼朋唤友")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R getFirstChargeRebateList(HttpServletRequest request,
                                  @RequestParam(value = "startTime", required = false)String startTime,
                                  @RequestParam(value = "endTime", required = false)String endTime,
                                 @RequestParam("pageNo")Integer pageNo, @RequestParam("pageSize")Integer pageSize){
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok(firstChargeApiService.getApiFirstChargeList(accountId,  startTime, endTime,  pageNo,  pageSize));
    }

}
