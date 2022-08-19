package com.wsdy.saasops.modules.mbrRebateAgent.controller;

import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.mbrRebateAgent.dto.MbrRebateAgentAuditDto;
import com.wsdy.saasops.modules.mbrRebateAgent.dto.MbrRebateAgentQryDto;
import com.wsdy.saasops.modules.mbrRebateAgent.service.MbrRebateAgentService;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import com.wsdy.saasops.modules.sys.service.SysFileExportRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;

@Slf4j
@RestController
@RequestMapping("/bkapi/mbrAgent/rebate")
@Api(value = "全民代理(返利列表)", tags = "全民代理(返利列表)")
public class MbrRebateAgentAuditController extends AbstractController {

    @Value("${mbr.rebate.excel.path}")
    private String excelTempPath;
    private final String module = "mbrAccountRebate";

    @Autowired
    private MbrRebateAgentService mbrRebateAgentService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;

    @GetMapping("/qryBonusList")
    @RequiresPermissions("member:rebate:list")
    @ApiOperation(value = "审核列表", notes = "审核列表查询")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R qryBonusList(@ModelAttribute MbrRebateAgentQryDto dto) {
        Assert.isBlank(dto.getCreateTime(), "统计时间不为空！");
        PageUtils ret = mbrRebateAgentService.qryBonusList(dto);
        return R.ok().put("page",ret);
    }

    @PostMapping("/bonusAuditBatch")
    @RequiresPermissions("member:rebate:audit")
    @ApiOperation(value = "审核列表", notes = "批量审核")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R bonusAuditBatch(@RequestBody MbrRebateAgentAuditDto dto, HttpServletRequest request) {
        Assert.isNull(dto.getStatus(), "状态不能为空");
        Assert.isBlank(dto.getCreateTime(), "统计时间不为空");

        dto.setUserId(getUser().getUserId());
        dto.setUserName(getUser().getUsername());
        dto.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));  // 初始统一审核时间

        String siteCode = CommonUtil.getSiteCode();
        String key = RedisConstants.MBR_REBATE_AGENT_AUDIT_BATCH + dto.getCreateTime() + siteCode;
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, key, 30, TimeUnit.MINUTES);
        if (isExpired) {
            // 异步执行
            CompletableFuture.runAsync(() -> {
                ThreadLocalCache.setSiteCodeAsny(siteCode);
                mbrRebateAgentService.bonusAuditBatch(dto,siteCode);
            });
        } else {
            throw new R200Exception("正在处理中，请误重复提交...");
        }

        return R.ok();
    }

    @PostMapping("/bonusAudit")
    @RequiresPermissions("member:rebate:audit")
    @ApiOperation(value = "审核列表", notes = "单条审核")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R bonusAudit(@RequestBody MbrRebateAgentAuditDto dto, HttpServletRequest request) {
        Assert.isNull(dto.getId(), "id不能为空");
        Assert.isNull(dto.getStatus(), "状态不能为空");

        dto.setUserId(getUser().getUserId());
        dto.setUserName(getUser().getUsername());

        String key = RedisConstants.MBR_REBATE_AGENT_AUDIT + dto.getId() + CommonUtil.getSiteCode();
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, dto.getId(), 200, TimeUnit.SECONDS);
        if (isExpired) {
            try {
                mbrRebateAgentService.bonusAudit(dto);
            } finally {
                redisService.del(key);
            }
        } else {
            throw new R200Exception("正在处理中，请误重复提交...");
        }
        return R.ok();
    }

    @PostMapping("/bonusAuditEdit")
    @RequiresPermissions("member:rebate:edit")
    @ApiOperation(value = "审核列表", notes = "单条编辑(备注)")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R bonusAuditEdit(@RequestBody MbrRebateAgentAuditDto dto, HttpServletRequest request) {
        Assert.isNull(dto.getId(), "id不能为空");
        Assert.isBlank(dto.getMemo(), "备注不能为空");

        dto.setUserId(getUser().getUserId());
        dto.setUserName(getUser().getUsername());

        String key = RedisConstants.MBR_REBATE_AGENT_AUDIT + dto.getId() + CommonUtil.getSiteCode();
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, dto.getId(), 10, TimeUnit.SECONDS);
        if (isExpired) {
            try {
                mbrRebateAgentService.bonusAuditEdit(dto);
            } finally {
                redisService.del(key);
            }
        } else {
            throw new R200Exception("正在处理中，请误重复提交...");
        }
        return R.ok();
    }

    @GetMapping("/exportBonusAudit")
    @RequiresPermissions("member:rebate:export")
    @ApiOperation(value = "审核列表", notes = "导出全民代理审核列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R exportBonusAudit(@ModelAttribute MbrRebateAgentQryDto dto) {
        SysFileExportRecord record = mbrRebateAgentService.exportBonusAudit(dto, getUser(), module, excelTempPath);
        if (record == null) {
            throw new R200Exception("正在处理中，请稍后重试!");
        }
        return R.ok();
    }

    @GetMapping("/checkFile")
    @ApiOperation(value = "审核列表", notes = "查询文件是否可下载")
    public R checkFile() {
        Long userId = getUserId();
        SysFileExportRecord record = sysFileExportRecordService.getAsynFileExportRecordByUserId(userId, module);
        if (null != record) {
            String fileName = excelTempPath.substring(excelTempPath.lastIndexOf("/") + 1, excelTempPath.length());
            record.setDownloadFileName(fileName);
            return R.ok().put(record);
        }
        return R.ok(false);
    }

    @GetMapping("/getChildBonusList")
    @RequiresPermissions("member:rebate:list")
    @ApiOperation(value = "审核列表-下级会员", notes = "上级查询直属下级信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R getChildBonusList(@ModelAttribute MbrRebateAgentQryDto dto) {
        Assert.isNull(dto.getParentId(), "parentId不能为空");
        Assert.isBlank(dto.getCreateTime(), "统计时间不为空！");
        PageUtils ret = mbrRebateAgentService.getChildBonusList(dto);
        return R.ok().put("page",ret);
    }

    @GetMapping("/getMbrRebateAgentDayList")
    @RequiresPermissions("member:rebate:list")
    @ApiOperation(value = "历史表", notes = "历史表查询")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R getMbrRebateAgentDayList(@ModelAttribute MbrRebateAgentQryDto dto) {
        PageUtils ret = mbrRebateAgentService.getMbrRebateAgentDayList(dto);
        return R.ok().put("page",ret);
    }

}
