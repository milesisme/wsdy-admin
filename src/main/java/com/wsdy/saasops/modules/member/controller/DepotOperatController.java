package com.wsdy.saasops.modules.member.controller;

import com.wsdy.saasops.api.modules.user.service.DepotOperatService;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/bkapi/depotOperat")
@Api(value = "bkapi", tags = "前端平台控制登录登出")
public class DepotOperatController {

    @Autowired
    DepotOperatService depotOperatService;

    @GetMapping("list")
    @ResponseBody
    @ApiOperation(value = "平台列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R getAllDepotOperat(Integer pageNo, Integer pageSize, Integer accountId) {
        return depotOperatService.getDepotList(pageNo, pageSize, accountId);
    }

    @PostMapping("loginOut")
    @ResponseBody
    @ApiOperation(value = "用户登出")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R playerLoginOut(@RequestParam("accountId") Integer accountId, @RequestParam("platformId") Integer platformId) {
        return depotOperatService.LoginOutGateway(platformId, accountId, CommonUtil.getSiteCode());
    }

    @PostMapping("flushBalance")
    @ResponseBody
    @ApiOperation(value = "余额刷新")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R flushBalance(@RequestParam("accountId") Integer accountId, @RequestParam("platformId") Integer platformId) {
        return R.ok().put(depotOperatService.flushBalance(accountId, platformId, CommonUtil.getSiteCode()));
    }

    @PostMapping("lockPlayer")
    @ResponseBody
    @ApiOperation(value = "锁定用户")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R lockPlayer(@RequestParam("accountId") Integer accountId, @RequestParam("platformId") Integer platformId) {
        return depotOperatService.lockPlayer(accountId, platformId, CommonUtil.getSiteCode());
    }

    @PostMapping("unlockPlayer")
    @ResponseBody
    @ApiOperation(value = "解锁用户,暂不可用")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R unlockPlayer(@RequestParam("accountId") Integer accountId, @RequestParam("platformId") Integer platformId) {
        return depotOperatService.unlockPlayer(accountId, platformId, CommonUtil.getSiteCode());
    }

}
