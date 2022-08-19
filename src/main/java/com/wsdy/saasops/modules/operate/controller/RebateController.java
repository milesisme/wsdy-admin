package com.wsdy.saasops.modules.operate.controller;

import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.operate.entity.RebateInfo;
import com.wsdy.saasops.modules.operate.service.RebateService;
import com.wsdy.saasops.modules.system.systemsetting.entity.SysSetting;
import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.Objects;


@RestController
@RequestMapping("/bkapi/operate/rebate")
@Api(tags = "市场活动-返利列表")
public class RebateController extends AbstractController {

    @Autowired
    private RebateService rebateService;
    @Autowired
    private SysSettingService sysSettingService;

    @GetMapping("/listAll")
    @RequiresPermissions("member:rebate:list")
    @ApiOperation(value = "查询返利列表", notes = "查询返利列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R listAll(@ModelAttribute RebateInfo rebateInfo, @RequestParam("pageNo") @NotNull Integer pageNo,
                     @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().put(rebateService.queryListAll(rebateInfo, pageNo, pageSize));
    }


    @GetMapping("/refferList")
    @RequiresPermissions("member:rebate:list")
    @ApiOperation(value = "推荐返利列表", notes = "推荐返利列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R refferList(@ModelAttribute RebateInfo rebateInfo, @RequestParam("pageNo") @NotNull Integer pageNo,
                        @RequestParam("pageSize") @NotNull Integer pageSize) {

        SysSetting setting = sysSettingService.getSysSetting(SystemConstants.EG_SANGONG_FLG);   // 获取三公标志
        if(Objects.nonNull(setting) && Objects.nonNull(setting.getSysvalue()) && String.valueOf(Constants.EVNumber.one).equals(setting.getSysvalue())){
            return R.ok().put(rebateService.refferListEgSanGong(rebateInfo, pageNo, pageSize));
        }else{
            return R.ok().put(rebateService.refferList(rebateInfo, pageNo, pageSize));
        }
    }
}
