package com.wsdy.saasops.modules.system.pay.controller;

import com.wsdy.saasops.common.annotation.SysLog;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.system.pay.dto.StatisticsSucRateDto;
import com.wsdy.saasops.modules.system.pay.service.SetOnlinePayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/bkapi/deposit")
@Api(value = "入款统计", tags = "入款统计")
public class DepositStatisticController extends AbstractController {

    @Autowired
    private SetOnlinePayService onlinePayService;

    @GetMapping("/statisticSucRate")
    @RequiresPermissions("setting:deposit:statisticSucRate")
    @ApiOperation(value = "线上入款成功率查询", notes = "线上入款成功率查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @SysLog(module = "入款管理", methodText = "线上入款成功率查询")
    public R statisticSucRate(@ModelAttribute StatisticsSucRateDto statisticsSucRateDto) {
        return R.ok().putPage(onlinePayService.statisticSucRate(statisticsSucRateDto));
    }
}
