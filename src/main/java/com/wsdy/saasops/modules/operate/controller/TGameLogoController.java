package com.wsdy.saasops.modules.operate.controller;

import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.operate.entity.TGameLogo;
import com.wsdy.saasops.modules.operate.service.TGameLogoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/bkapi/game/tgamelogo")
@Api(value = "TGameLogo", tags = "个性图和LOGO")
public class TGameLogoController extends AbstractController {
    @Autowired
    private TGameLogoService tGameLogoService;

    @PostMapping("/update")
    @RequiresPermissions(value = {"operate:tgmcat:available", "operate:tgmcat:updateOrExport"}, logical = Logical.OR)
    @ApiOperation(value = "修改", notes = "修改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R update(@RequestBody TGameLogo tGameLogo, HttpServletRequest request) {
        tGameLogoService.update(tGameLogo, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }
}
