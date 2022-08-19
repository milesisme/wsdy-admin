package com.wsdy.saasops.modules.member.controller;

import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.member.entity.MbrActivityLevel;
import com.wsdy.saasops.modules.member.service.MbrActivityLevelCastService;
import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/bkapi/member/mbractlevel")
@Api(value = "MbrActivityLevelController", tags = "会员活动层级")
public class MbrActivityLevelController extends AbstractController {

    @Autowired
    private MbrActivityLevelCastService activityLevelCastService;
    @Autowired
    private SysSettingService sysSettingService;

    @GetMapping("findAutomatic")
    @ApiOperation(value = "会员活动层级-自动晋升", notes = "会员活动层级-自动晋升")
    public R findAutomatic() {
        return R.ok().put(sysSettingService.findAutomatic(SystemConstants.AUTOMATIC_PROMOTION));
    }

    @GetMapping("setAutomatic")
    @RequiresPermissions("member:mbractlevel:setAutomatic")
    @ApiOperation(value = "会员活动层级-设置自动晋升", notes = "会员活动层级-设置自动晋升")
    public R setAutomatic(@RequestParam("value") Integer value) {
        sysSettingService.setAutomatic(value, SystemConstants.AUTOMATIC_PROMOTION);
        return R.ok();
    }

    @GetMapping("findDowngrade")
    @ApiOperation(value = "会员活动层级-开启自动降级", notes = "会员活动层级-开启自动降级")
    public R findDowngrade() {
        return R.ok().put(sysSettingService.findAutomatic(SystemConstants.DOWNGRADA_PROMOTION));
    }

    @GetMapping("setDowngrade")
    @RequiresPermissions("member:mbractlevel:setDowngrade")
    @ApiOperation(value = "会员活动层级-开启自动降级", notes = "会员活动层级-开启自动降级")
    public R setDowngrade(@RequestParam("value") Integer value) {
        sysSettingService.setAutomatic(value, SystemConstants.DOWNGRADA_PROMOTION);
        return R.ok();
    }

    @GetMapping("/list")
    @ApiOperation(value = "会员活动层级-查询所有", notes = "会员活动层级-查询所有")
    public R mbrActivityLevelList() {
        return R.ok().put(activityLevelCastService.mbrActivityLevelList());
    }

    @GetMapping("/info/{id}")
    @RequiresPermissions("member:mbractlevel:info")
    @ApiOperation(value = "会员活动层级-查询单条", notes = "会员活动层级-查询单条")
    public R mbrActivityLevelInfo(@PathVariable("id") Integer id) {
        return R.ok().put(activityLevelCastService.mbrActivityLevelInfo(id));
    }

    @GetMapping("/delete/{id}")
    @RequiresPermissions("member:mbractlevel:delete")
    @ApiOperation(value = "会员活动层级-删除", notes = "会员活动层级-删除")
    public R deleteMbrActivityLevel(@PathVariable("id") Integer id) {
        activityLevelCastService.deleteMbrActivityLevel(id);
        return R.ok();
    }

    @PostMapping("/save")
    @RequiresPermissions("member:mbractlevel:save")
    @ApiOperation(value = "会员活动层级-保存", notes = "会员活动层级-保存")
    public R insertMbrActivityLevel(@RequestBody MbrActivityLevel activityLevel) {
        checkoutActivityLevel(activityLevel);
        activityLevel.setCreateUser(getUser().getUsername());
        activityLevelCastService.insertMbrActivityLevel(activityLevel);
        return R.ok();
    }

    @PostMapping("/update")
    @RequiresPermissions("member:mbractlevel:update")
    @ApiOperation(value = "会员活动层级-修改", notes = "会员活动层级-修改")
    public R updateMbrActivityLevel(@RequestBody MbrActivityLevel activityLevel) {
        Assert.isNull(activityLevel.getAccountLevel(), "会员等级不能为空");

        // 入参校验
        if (activityLevel.getAccountLevel() == Constants.EVNumber.zero) {   // 零级别会员仅校验等级名称/前台说明
            Assert.isBlank(activityLevel.getTierName(), "名称不能为空");
        } else {
            checkoutActivityLevel(activityLevel);
        }
        Assert.isBlank(activityLevel.getDescription(), "前台说明不能为空");
        Assert.isNull(activityLevel.getId(), "ID不能为空");
        activityLevel.setModifyUser(getUser().getUsername());
        activityLevelCastService.updateMbrActivityLevel(activityLevel);
        return R.ok();
    }

    @PostMapping("/updateAvailable")
    @RequiresPermissions("member:mbractlevel:update")
    @ApiOperation(value = "会员活动层级-禁用启用", notes = "会员活动层级-禁用启用")
    public R updateAvailableMbrActivityLevel(@RequestBody MbrActivityLevel activityLevel) {
        Assert.isNull(activityLevel.getId(), "ID不能为空");
        Assert.isNull(activityLevel.getAvailable(), "状态不能为空");
        activityLevel.setModifyUser(getUser().getUsername());
        activityLevelCastService.updateAvailableMbrActivityLevel(activityLevel);
        return R.ok();
    }

    @PostMapping("/updateAvailableBatch")
    @RequiresPermissions("member:mbractlevel:update")
    @ApiOperation(value = "会员活动层级-批量禁用启用", notes = "会员活动层级-批量禁用启用")
    public R updateAvailableBatch(@RequestBody MbrActivityLevel activityLevel) {
        Assert.isNull(activityLevel.getIds(), "ID不能为空");
        Assert.isNull(activityLevel.getAvailable(), "状态不能为空");
        activityLevel.setModifyUser(getUser().getUsername());
        activityLevelCastService.updateAvailableMbrActivityLevelBatch(activityLevel);
        return R.ok();
    }

    private void checkoutActivityLevel(MbrActivityLevel activityLevel) {
        Assert.isBlank(activityLevel.getTierName(), "名称不能为空");
        Assert.isNull(activityLevel.getAccountLevel(), "会员等级不能为空");
        Assert.isNull(activityLevel.getPromoteSign(), "晋升条件不能为空");
        Assert.isNull(activityLevel.getConditions(), "晋升条件不能为空");
        Assert.isNull(activityLevel.getAvailable(), "状态不能为空");

//        // 每日取款限制
//        Assert.isNull(activityLevel.getFeeAvailable(), "是否取款限制不能为空");
//        if ( activityLevel.getFeeAvailable() == Constants.Available.enable) {
//            Assert.isNumeric(activityLevel.getWithDrawalTimes(), "每日充许取款次数为数字,并且最大长充为12位!", 12);
//            Assert.isNumeric(activityLevel.getWithDrawalQuota(), "每日取款限额为数字,并且最大长充为12位!", 12);
//        }
    }

    @GetMapping("findActLevelStaticsRule")
    @ApiOperation(value = "会员活动层级-统计周期规则", notes = "会员活动层级-统计周期规则")
    public R findStaticsRule() {
        return R.ok().put(sysSettingService.findActLevelStaticsRuleAndDescript());
    }

    @GetMapping("setStaticsRule")
    @RequiresPermissions("member:mbractlevel:setStaticsRule")
    @ApiOperation(value = "会员活动层级-设置统计周期规则", notes = "会员活动层级-设置统计周期规则")
    public R setStaticsRule(@RequestParam("value") Integer value,
                            @RequestParam("description") String description,
                            @RequestParam("downgradePromotionDay") String downgradePromotionDay,
                            @RequestParam("recoverPromotionDay") String recoverPromotionDay) {
        sysSettingService.setActLevelStaticsRule(value, description, downgradePromotionDay, recoverPromotionDay);
        return R.ok();
    }
}
