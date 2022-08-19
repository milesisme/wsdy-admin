package com.wsdy.saasops.modules.member.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.member.dto.DepositAutoLockDto;
import com.wsdy.saasops.modules.member.dto.MbrFriendTransDetailDto;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrDepositLockLog;
import com.wsdy.saasops.modules.member.entity.MbrFriendTransDetail;
import com.wsdy.saasops.modules.member.service.MbrDepositCondService;
import com.wsdy.saasops.modules.member.service.MbrDepositLockLogService;
import com.wsdy.saasops.modules.member.service.MbrFriendTransDetailService;
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
import java.util.List;


@RestController
@RequestMapping("/bkapi/mbrdepositlock")
@Api(value = "MbrFreindTransDetail", tags = "好友转账")
public class MbrDepositLockController extends AbstractController {
    @Autowired
    private MbrDepositLockLogService mbrDepositLockLogService;
    @Autowired
    private SysSettingService sysSettingService;

    @GetMapping("/list")
    @ApiOperation(value="存款锁定列表", notes="存款锁定列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @RequiresPermissions("member:mbrdepositlock:list")
    public R list(MbrDepositLockLog mbrDepositLockLog, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().put("page", mbrDepositLockLogService.listPage(mbrDepositLockLog,pageNo,pageSize));
    }

    @GetMapping("/getLockInfo")
    @ApiOperation(value="获取锁定信息", notes="获取锁定信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @RequiresPermissions("member:mbrdepositlock:depositunlock")
    public R getLockInfo(MbrDepositLockLog mbrDepositLockLog) {
        Assert.isNull(mbrDepositLockLog.getAccountId(), "会员ID不能为空");
        return R.ok().put(mbrDepositLockLogService.getLastLock(mbrDepositLockLog));
    }

    @GetMapping("/getAutoLockSetting")
    @ApiOperation(value="获取锁定设置", notes="获取锁定设置")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @RequiresPermissions("member:mbrdepositlock:saveautolock")
    public R getAutoLockSetting() {
        SysSetting sysSetting = sysSettingService.getSysSetting(SystemConstants.DEPOSIT_AUTO_LOCK);
        List<DepositAutoLockDto> dtoList = JSONArray.parseArray(sysSetting.getSysvalue(), DepositAutoLockDto.class);
        return R.ok().put(dtoList);
    }

    @PostMapping("/saveAutoLockSetting")
    @ApiOperation(value="保存锁定设置", notes="保存锁定设置")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @RequiresPermissions("member:mbrdepositlock:saveautolock")
    public R saveAutoLockSetting(@RequestBody List<DepositAutoLockDto> depositAutoLockDto) {
        SysSetting sysSetting = sysSettingService.getSysSetting(SystemConstants.DEPOSIT_AUTO_LOCK);
        depositAutoLockDto.stream().forEach(e->e.setStartTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME)));
        sysSetting.setSysvalue(JSON.toJSONString(depositAutoLockDto));
        sysSettingService.update(sysSetting);
        return R.ok();
    }

    @PostMapping("/depositUnlock")
    @ApiOperation(value="存款锁定-解锁", notes="存款锁定-解锁")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @RequiresPermissions("member:mbrdepositlock:depositunlock")
    public R depositUnlock(@RequestBody MbrDepositLockLog mbrDepositLockLog) {
        Assert.isNull(mbrDepositLockLog.getAccountId(), "会员ID不能为空");
        Assert.isNull(mbrDepositLockLog.getDepositLock(), "会员ID不能为空");
        mbrDepositLockLogService.depositUnlock(mbrDepositLockLog, getUser().getUsername());
        return R.ok();
    }

    @PostMapping("/depositLock")
    @ApiOperation(value = "存款锁定-新增锁定", notes = "存款锁定-新增锁定")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    @RequiresPermissions("member:mbrdepositlock:depositlock")
    public R depositLock(@RequestBody MbrDepositLockLog mbrDepositLockLog) {
        Assert.isNull(mbrDepositLockLog.getLoginName(), "会员账号不能为空");
        Assert.isNumeric(mbrDepositLockLog.getLockMinute(), "锁定时长只能是数字");
        mbrDepositLockLogService.depositLock(mbrDepositLockLog, getUser().getUsername());
        return R.ok();
    }

    @GetMapping("/depositLockLog")
    @ApiOperation(value = "存款锁定-锁定记录", notes = "存款锁定-锁定记录")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    public R depositLockLog(@RequestParam("accountId") @NotNull Integer accountId,
                            @RequestParam("startTime") @NotNull String startTime, @RequestParam("endTime") @NotNull String endTime,
                            @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        Assert.isNull(accountId, "会员账号不能为空");
        return R.ok().put("page", mbrDepositLockLogService.listDepositLockLog(accountId, startTime, endTime, pageNo, pageSize));
    }

}
