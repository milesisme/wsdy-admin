package com.wsdy.saasops.modules.operate.controller;

import com.wsdy.saasops.common.annotation.SysLog;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.modules.member.service.MbrAccountLogService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wsdy.saasops.common.constants.Constants.Available;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.operate.entity.OprActCat;
import com.wsdy.saasops.modules.operate.service.OprActCatService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/bkapi/operate/opractcat")
@Api(tags = "活动分类")
public class OprActCatController {

    @Autowired
    private OprActCatService oprActCatService;
    @Autowired
    private MbrAccountLogService mbrAccountLogService;

    @GetMapping("listAll")
    @ApiOperation(value = "查询所有", notes = "查询所有")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R listAll() {
        OprActCat actCat = new OprActCat();
        actCat.setAvailable(Available.enable);
        return R.ok().putPage(oprActCatService.queryListCond(actCat));
    }

    @GetMapping("list")
    @RequiresPermissions("operate:activitycat:list")
    @ApiOperation(value = "查询所有分类", notes = "查询所有分类")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R list() {
        return R.ok().put(oprActCatService.queryListPage());
    }

    @PostMapping("save")
    @RequiresPermissions("operate:activitycat:save")
    @SysLog(module = "活动设置",methodText = "活动分类新增")
    @ApiOperation(value = "保存", notes = "保存")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R save(@RequestBody OprActCat oprActCat) {
        oprActCat.setAvailable(Available.enable);
        oprActCat.setCreateTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        oprActCat.setDisable(String.valueOf(Constants.EVNumber.zero));
        oprActCatService.save(oprActCat);
        // 操作日志
        mbrAccountLogService.saveOprActCat(oprActCat);
        return R.ok();
    }

    @PostMapping("update")
    @RequiresPermissions("operate:activitycat:update")
    @ApiOperation(value = "活动分类修改", notes = "活动分类修改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R update(@RequestBody OprActCat oprActCat) {
        oprActCatService.updateOprActCat(oprActCat);
        return R.ok();
    }

    @PostMapping("updateAvailable")
    @RequiresPermissions("operate:activitycat:updateAvailable")
    @ApiOperation(value = "活动分类状态编辑", notes = "活动分类状态编辑")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateAvailable(@RequestBody OprActCat oprActCat) {
        oprActCatService.updateAvailable(oprActCat);
        return R.ok();
    }

    @PostMapping("delete")
    @RequiresPermissions("operate:activitycat:delete")
    @ApiOperation(value = "活动分类删除", notes = "活动分类删除")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R delete(@RequestBody OprActCat oprActCat) {
        oprActCatService.delete(oprActCat.getId());
        return R.ok();
    }
}
