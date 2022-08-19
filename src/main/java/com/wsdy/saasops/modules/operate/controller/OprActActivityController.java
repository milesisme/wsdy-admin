package com.wsdy.saasops.modules.operate.controller;

import com.google.gson.Gson;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.common.annotation.SysLog;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.service.AgentAccountService;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.operate.dto.ActBonusAuditDto;
import com.wsdy.saasops.modules.operate.dto.ActivityDto;
import com.wsdy.saasops.modules.operate.entity.OprActActivity;
import com.wsdy.saasops.modules.operate.entity.OprActBlacklist;
import com.wsdy.saasops.modules.operate.entity.OprActBonus;
import com.wsdy.saasops.modules.operate.entity.OprActRule;
import com.wsdy.saasops.modules.operate.service.OprActActivityService;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import com.wsdy.saasops.modules.sys.service.SysFileExportRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/bkapi/operate/activity")
@Api(tags = "运营管理-活动设置")
public class OprActActivityController extends AbstractController {

    @Value("${analysis.bonus.excel.path}")
    private String activityAuditListExportPath;
    private final String activityAuditListExportModule = "activityAuditListExport";
    @Autowired
    private OprActActivityService oprActBaseService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private AgentAccountService agentAccountService;
    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;

    @GetMapping("/activityAuditList")
    @RequiresPermissions("operate:activity:activityAuditList")
    @ApiOperation(value = "活动审核集合", notes = "活动审核集合")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R activityAuditList(@ModelAttribute OprActBonus oprActBonus, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(oprActBaseService.activityAuditList(oprActBonus, pageNo, pageSize));
    }

    @PostMapping("/activityAudit")
    @RequiresPermissions("operate:activity:activityAudit")
    @SysLog(module = "活动设置", methodText = "活动审核")
    @ApiOperation(value = "活动审核", notes = "活动审核")
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

    @PostMapping("/activityAuditRefuse")
    @RequiresPermissions("operate:activity:activityAudit")
    @SysLog(module = "活动设置-拒绝", methodText = "活动审核-拒绝")
    @ApiOperation(value = "活动审核-拒绝", notes = "活动审核-拒绝")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R activityAuditRefuse(@RequestBody ActBonusAuditDto bonusAuditDto) {
        Assert.isNull(bonusAuditDto.getBonusAuditListDtos(), "不能为空");
        Assert.isNull(bonusAuditDto.getStatus(), "状态不能为空");
        if(!(Integer.valueOf(Constants.EVNumber.zero).equals(bonusAuditDto.getStatus()))){
            throw new R200Exception("status值错误");
        }
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

    @PostMapping("/activityModifyAmount")
    @RequiresPermissions("operate:activity:ModifyAmount")
    @SysLog(module = "红利金额调整", methodText = "红利金额调整")
    @ApiOperation(value = "红利金额调整", notes = "红利金额调整")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R activityModifyAmount(@RequestBody OprActBonus dto) {
        Assert.isNull(dto.getId(), "id不能为空");
        Assert.isNull(dto.getBonusAmount(), "调整后赠送金额不能为空");
        Assert.isNull(dto.getAuditAmount(), "调整后流水金额不能为空");

        // 同审核的key相同
        String key = RedisConstants.ACTIVITY_AUDIT_ACCOUNT + dto.getId() + CommonUtil.getSiteCode();
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, dto.getId(), 200, TimeUnit.SECONDS);
        if (isExpired) {
            try {
                oprActBaseService.activityModifyAmount(dto,getUser());
            } finally {
                redisService.del(key);
            }
        } else {
            return R.error("请耐心等候，请勿重复操作...");
        }
        return R.ok();
    }


    @GetMapping("/activityAuditCountByStatus")
    @RequiresPermissions("operate:activity:activityAuditList")
    @ApiOperation(value = "红利申请统计", notes = "根据状态统红利申请")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R activityAuditCountByStatus(@ModelAttribute OprActBonus oprActBonus) {

        return R.ok(oprActBaseService.activityAuditCountByStatus(oprActBonus));
    }

    @GetMapping("activityRuleList")
    @RequiresPermissions("operate:activity:rulelist")
    @ApiOperation(value = "活动规则查询", notes = "活动规则查询")
    public R activityRuleList(@ModelAttribute OprActRule actRule, @RequestParam("pageNo") @NotNull Integer pageNo,
                              @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().put(oprActBaseService.activityRuleList(actRule, pageNo, pageSize));
    }

    @GetMapping("ruleList")
    @ApiOperation(value = "活动规则查询所有", notes = "活动规则查询所有")
    public R ruleList() {
        return R.ok().put(oprActBaseService.ruleList());
    }

    @GetMapping("activityRuleList/{id}")
    @RequiresPermissions("operate:activity:ruleinfo")
    @ApiOperation(value = "活动规则查询单条", notes = "活动规则查询单条")
    public R activityRuleInfo(@PathVariable("id") Integer id) {
        return R.ok().put(oprActBaseService.activityRuleInfo(id));
    }


    @PostMapping("saveActivityRule")
    @RequiresPermissions("operate:activity:saveRule")
    @ApiOperation(value = "新增活动规则", notes = "新增活动规则")
    public R saveActivityRule(@RequestBody OprActRule actRule) {
        Assert.isNull(actRule.getRuleName(), "规则名称不能为空");
        Assert.isLenght(actRule.getRuleName(), "规则名称不能超过20个汉字", 1, 20);
        Assert.isNull(actRule.getActTmplId(), "活动模板不能为空");
        Assert.isNull(actRule.getAvailable(), "状态不能为空");
        actRule.setCreateUser(getUser().getUsername());
        actRule.setIsDelete(Constants.EVNumber.zero);
        //String key = RedisConstants.ACTIVITY_RULE + CommonUtil.getSiteCode() + getUser().getUsername();
        String key = RedisConstants.ACTIVITY_RULE + CommonUtil.getSiteCode();
        try {
            Boolean isExpired = redisService.setRedisExpiredTimeBo(key, CommonUtil.getSiteCode(), 200, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(isExpired)) {
                    oprActBaseService.saveActivityRule(actRule);
                return R.ok();
            }
        } finally {
            redisService.del(key);
        }
        throw new R200Exception("正在处理，请稍后");
    }

    @PostMapping("updateActivityRule")
    @RequiresPermissions("operate:activity:updateRule")
    @ApiOperation(value = "编辑活动规则", notes = "编辑活动规则")
    public R updateActivityRule(@RequestBody OprActRule actRule) {
        Assert.isNull(actRule.getId(), "ID不能为空");
        Assert.isBlank(actRule.getRuleName(), "规则名称不能为空");
        Assert.isNull(actRule.getActTmplId(), "活动模板不能为空");
        Assert.isNull(actRule.getAvailable(), "状态不能为空");
        actRule.setModifyUser(getUser().getUsername());
        oprActBaseService.updateActivityRule(actRule);
        return R.ok();
    }

    @PostMapping("availableRule")
    @RequiresPermissions("operate:activity:availableRule")
    @ApiOperation(value = "活动规则状态编辑", notes = "活动规则状态编辑")
    public R updateAvailableActivityRule(@RequestBody OprActRule actRule) {
        Assert.isNull(actRule.getId(), "ID不能为空");
        Assert.isNull(actRule.getAvailable(), "状态不能为空");
        oprActBaseService.updateAvailableActivityRule(actRule);
        return R.ok();
    }

    @PostMapping("saveActivity")
    @RequiresPermissions("operate:activity:save")
    @ApiOperation(value = "活动介绍新增", notes = "活动介绍新增")
    public R saveActivity(@ModelAttribute ActivityDto activityDto,
                          @RequestParam(value = "uploadPcFile", required = false) MultipartFile uploadPcFile,
                          @RequestParam(value = "uploadMbFile", required = false) MultipartFile uploadMbFile,
                          HttpServletRequest request) {
        Assert.isNull(activityDto, "不能为空");
        OprActActivity actActivity = new Gson().fromJson(activityDto.getActivity().toString(), OprActActivity.class);
        Assert.isNull(actActivity.getSort(), "是否置顶不能为空");
        Assert.isBlank(actActivity.getActivityName(), "活动名称不能为空");
        actActivity.setCreateUser(getUser().getUsername());
        oprActBaseService.saveActivity(actActivity, uploadPcFile, uploadMbFile, CommonUtil.getIpAddress(request), getUser().getUserId());
        return R.ok();
    }

    @PostMapping("updateActivity")
    @RequiresPermissions("operate:activity:update")
    @ApiOperation(value = "活动介绍编辑", notes = "活动介绍编辑")
    public R updateActivity(@ModelAttribute ActivityDto activityDto,
                            @RequestParam(value = "uploadPcFile", required = false) MultipartFile uploadPcFile,
                            @RequestParam(value = "uploadMbFile", required = false) MultipartFile uploadMbFile,
                            HttpServletRequest request) {
        log.info("接收到的值" + activityDto.getActivity());
        Assert.isNull(activityDto, "不能为空");
        OprActActivity actActivity = new Gson().fromJson(activityDto.getActivity().toString(), OprActActivity.class);
        Assert.isNull(actActivity.getSort(), "排序号不能为空");
        if (actActivity.getSort() < 1) {
            throw new R200Exception("排序号不能小于1");
        }
        Assert.isNull(actActivity.getId(), "ID不能为空");
        Assert.isBlank(actActivity.getActivityName(), "活动名称不能为空");
        actActivity.setModifyUser(getUser().getUsername());
        oprActBaseService.updateActivity(actActivity, uploadPcFile, uploadMbFile, CommonUtil.getIpAddress(request), getUser().getUserId());
        return R.ok();
    }

    @GetMapping("activityInfo/{id}")
    @RequiresPermissions("operate:activity:info")
    @ApiOperation(value = "活动介绍查询单个", notes = "活动介绍查询单个")
    public R activityInfo(@PathVariable("id") Integer id) {
        return R.ok().put(oprActBaseService.activityInfo(id));
    }

    @GetMapping("activityList")
    @RequiresPermissions("operate:activity:list")
    @ApiOperation(value = "活动页面list", notes = "活动页面list")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R activityList(OprActActivity activity,
                          @RequestParam("pageNo") @NotNull Integer pageNo,
                          @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().put(oprActBaseService.queryListPage(activity, pageNo, pageSize));
    }

    @GetMapping("activityListAll")
    @ApiOperation(value = "活动页面list", notes = "活动页面list")
    public R activityListAll(OprActActivity activity) {
        return R.ok().put(oprActBaseService.activityListAll(activity));
    }

    @GetMapping("deleteRule")
    @RequiresPermissions("operate:activity:deleteDisableRule")
    @ApiOperation(value = "逻辑删除禁用的活动规则", notes = "逻辑删除禁用的活动规则")
    public R deleteDisableRule(@ModelAttribute OprActRule actRule) {
        Assert.isNull(actRule.getId(), "ID不能为空");
        oprActBaseService.deleteDisableRule(actRule, getUser().getUsername());
        return R.ok();
    }

    @PostMapping("saveActBlacklist")
    @RequiresPermissions("operate:activity:updateActBlacklist")
    @ApiOperation(value = "添加活动黑名单", notes = "添加活动黑名单")
    public R saveActBlacklist(@RequestBody OprActBlacklist blacklist) {
        Assert.isNull(blacklist.getLoginName(), "规则名称不能为空");
        Assert.isNull(blacklist.getTmplCode(), "活动分类不能为空");
        Assert.isNull(blacklist.getIsAgent(), "是否代理不能为空");
        blacklist.setCreateTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        oprActBaseService.saveOprActBlacklist(blacklist);
        return R.ok();
    }

    @GetMapping("getActBlacklist")
    @RequiresPermissions(value = {"operate:activity:queryActBlacklist", "operate:activity:updateActBlacklist"}, logical = Logical.OR)
    @ApiOperation(value = "查询活动黑名单", notes = "查询活动黑名单")
    public R getActBlacklist(@ModelAttribute OprActBlacklist blacklist, @RequestParam("pageNo") @NotNull Integer pageNo,
                             @RequestParam("pageSize") @NotNull Integer pageSize) {
        Assert.isNull(blacklist.getTmplCode(), "活动分类不能为空");
        Assert.isNull(blacklist.getIsAgent(), "是否代理不能为空");
        return R.ok().put(oprActBaseService.getBlackList(blacklist, pageNo, pageSize));
    }

    @PostMapping("deleteActBlacklist")
    @RequiresPermissions("operate:activity:updateActBlacklist")
    @ApiOperation(value = "删除活动黑名单", notes = "删除活动黑名单")
    public R deleteActBlacklist(@RequestBody OprActBlacklist blacklist) {
        Assert.isNull(blacklist.getId(), "规则id不能为空");
        oprActBaseService.deleteBlackList(blacklist);
        return R.ok();
    }

    @PostMapping("setSelfHelp")
    @RequiresPermissions("operate:activity:setSelfHelp")
    @ApiOperation(value = "设置自助返水限制", notes = "设置自助返水限制")
    public R setSelfHelp(@RequestBody OprActRule rule) {
        Assert.isNull(rule.getId(), "规则id不能为空");
        Assert.isNull(rule.getIsSelfHelp(), "洗码开启状态不能为空");
        Assert.isNull(rule.getIsLimit(), "洗码申请限制不能为空");
        Assert.isNull(rule.getIsSelfHelpShow(), "自助申请状态不能为空");
        rule.setModifyUser(getUser().getUsername());
        rule.setModifyTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        oprActBaseService.setSelfHelp(rule);
        return R.ok();
    }

    @GetMapping("getAgent")
    @RequiresPermissions("operate:activity:updateRule")
    public R getAgent(@ModelAttribute AgentAccount agentAccount, @RequestParam("pageNo") @NotNull Integer pageNo,
                      @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().put(agentAccountService.getAgent(agentAccount, pageNo, pageSize));
    }

    @PostMapping("deleteAgentWaterRule")
    @RequiresPermissions("operate:activity:updateRule")
    @ApiOperation(value = "返水代理线活动规则", notes = "删除代理规则")
    public R deleteAgentWaterRule(@RequestBody OprActRule rule) {
        Assert.isNull(rule.getId(), "规则id不能为空");
        Assert.isNull(rule.getAgentId(), "代理id不能为空");
        oprActBaseService.deleteAgentWaterRule(rule);
        return R.ok();
    }


    @GetMapping("oprActLabelList")
    public R oprActLabelList() {
        return R.ok().put(oprActBaseService.oprActLabelList());
    }



    @GetMapping("/activityAuditListExport")
    @RequiresPermissions("operate:activity:export")
    @ApiOperation(value = "活动审核集合导出", notes = "活动审核集合导出")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R activityAuditListExport(@ModelAttribute OprActBonus oprActBonus) {
        SysFileExportRecord record = oprActBaseService.activityAuditListExport(oprActBonus, getUserId(), activityAuditListExportModule);

        if (Objects.isNull(record)) {
            throw new R200Exception("正在处理中,请10分钟后再试!");
        }
        if("fail".equals(record.getSaveFlag())){
            throw new R200Exception("导出失败，请重试！");
        }
        return R.ok();
    }

    @GetMapping("/checkFile")
    @ApiOperation(value = "查询文件是否可下载",notes = "查询文件是否可下载")
    public R checkFile(){
        Long userId = getUserId();
        SysFileExportRecord record = sysFileExportRecordService.getAsynFileExportRecordByUserId(userId,activityAuditListExportModule);
        if(null != record){
            String fileName = activityAuditListExportPath.substring(activityAuditListExportPath.lastIndexOf("/")+1,activityAuditListExportPath.length());
            record.setDownloadFileName(fileName);
            return R.ok().put(record);
        }
        return R.ok(false);
    }

}
