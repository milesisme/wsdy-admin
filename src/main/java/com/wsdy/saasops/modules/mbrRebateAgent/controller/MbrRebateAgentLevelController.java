package com.wsdy.saasops.modules.mbrRebateAgent.controller;

import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.mbrRebateAgent.entity.MbrRebateAgentLevel;
import com.wsdy.saasops.modules.mbrRebateAgent.service.MbrRebateAgentLevelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;


@RestController
@RequestMapping("/bkapi/mbrAgent/level")
@Api(value = "会员代理级别列表", tags = "会员代理级别列表")
public class MbrRebateAgentLevelController extends AbstractController {
    @Autowired
    private MbrRebateAgentLevelService mbrRebateAgentLevelService;

    @GetMapping("/list")
//    @RequiresPermissions("member:mbragentactlevel:list")
    @ApiOperation(value = "级别查询", notes = "级别查询")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R getMbrAgentLevelList() {
        List<MbrRebateAgentLevel> ret = mbrRebateAgentLevelService.getMbrAgentLevelList();
        return R.ok().put(ret);
    }

    @PostMapping("/save")
    @RequiresPermissions("member:mbragentactlevel:save")
    @ApiOperation(value = "级别新增", notes = "级别新增")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R insertMbrAgentLevel(@RequestBody MbrRebateAgentLevel dto) {
        Assert.isNull(dto.getAccountLevel(), "等级不能为空");
        Assert.isNull(dto.getTierName(), "等级名称不能为空");

        dto.setCreateUser(getUser().getUsername());
        dto.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        dto.setModifyUser(getUser().getUsername());
        dto.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        dto.setAvailable(Constants.EVNumber.one);   // 默认 是否启用：1开启

        mbrRebateAgentLevelService.insertMbrAgentLevel(dto);
        return R.ok();
    }
    @GetMapping("/delete")
    @RequiresPermissions("member:mbragentactlevel:delete")
    @ApiOperation(value = "级别删除", notes = "级别删除")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R deleteMbrAgentLevel(@ModelAttribute MbrRebateAgentLevel dto) {
        Assert.isNull(dto.getId(), "id不能为空");
        mbrRebateAgentLevelService.deleteMbrAgentLevel(dto);
        return R.ok();
    }
}
