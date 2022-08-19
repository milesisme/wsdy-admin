package com.wsdy.saasops.modules.operate.controller;

import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.common.annotation.SysLog;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.operate.dto.ActBonusAuditDto;
import com.wsdy.saasops.modules.operate.entity.OprActBonus;
import com.wsdy.saasops.modules.operate.service.OprActActivityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/bkapi/operate/activity/water")
@Api(tags = "运营管理-返水设置")
public class OprActActivityForWaterController extends AbstractController {

    @Autowired
    private OprActActivityService oprActBaseService;
    @Autowired
    private RedisService redisService;

    @GetMapping("/waterAuditList")
    @RequiresPermissions("operate:activity:waterAuditListIssue")
    @ApiOperation(value = "返水审核集合", notes = "返水审核集合")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R waterAuditList(@ModelAttribute OprActBonus oprActBonus, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(oprActBaseService.waterAuditList(oprActBonus, pageNo, pageSize));
    }

    @GetMapping("/waterList")
    @RequiresPermissions("operate:activity:waterAuditList")
    @ApiOperation(value = "返水列表", notes = "返水列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R waterList(@ModelAttribute OprActBonus oprActBonus, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(oprActBaseService.waterList(oprActBonus, pageNo, pageSize));
    }

    @PostMapping("/activityAudit")
    @RequiresPermissions("operate:activity:waterAudit")
    @SysLog(module = "活动设置", methodText = "返水审核")
    @ApiOperation(value = "活动审核", notes = "返水审核")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R activityAudit(@RequestBody ActBonusAuditDto bonusAuditDto) {
        Assert.isNull(bonusAuditDto.getBonusAuditListDtos(), "不能为空");
        Assert.isNull(bonusAuditDto.getStatus(), "状态不能为空");
        String siteCode = CommonUtil.getSiteCode();
        if (Collections3.isNotEmpty(bonusAuditDto.getBonusAuditListDtos())) {
            bonusAuditDto.getBonusAuditListDtos().stream().forEach(bs -> {
                String key = RedisConstants.ACTIVITY_AUDIT_ACCOUNT + bs.getBonuseId() + siteCode;
                Boolean isExpired = redisService.setRedisExpiredTimeBo(key, bs.getBonuseId(), 200, TimeUnit.SECONDS);
                if (isExpired) {
                    try {
                        oprActBaseService.isActivity(bs.getBonuseId(), bs.getTmplCode(),
                                bonusAuditDto.getMemo(), bonusAuditDto.getStatus(), getUser().getUsername(), bonusAuditDto);
                    } finally {
                        redisService.del(key);
                    }
                }
            });
        }
        oprActBaseService.activityAuditMsg(bonusAuditDto, CommonUtil.getSiteCode(), getUser().getUsername());

        return R.ok();
    }

}
