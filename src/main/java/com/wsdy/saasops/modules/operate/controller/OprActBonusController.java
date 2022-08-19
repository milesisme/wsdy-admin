package com.wsdy.saasops.modules.operate.controller;

import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.common.annotation.SysLog;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.operate.entity.OprActActivity;
import com.wsdy.saasops.modules.operate.entity.OprActBonus;
import com.wsdy.saasops.modules.operate.service.ActivityAddService;
import com.wsdy.saasops.modules.operate.service.OprActActivityService;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import com.wsdy.saasops.modules.sys.service.SysFileExportRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.nonNull;


@RestController
@RequestMapping("/bkapi/operate/bonus")
@Api(tags = "红利")
public class OprActBonusController extends AbstractController {

    @Autowired
    private RedisService redisService;
    @Autowired
    private OprActActivityService oprActBaseService;
    @Autowired
    private ActivityAddService activityAddService;

    @Value("${analysis.bonus.excel.path.allAllActivity:excelTemplate/bonus/红利报表所有活动.xls}")
    private String allActivityAuditListExportPath;

    private final String allActivityAuditListExportModule = "allActivityAuditListExport";
    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;

    @PostMapping("/save")
    @RequiresPermissions("operate:bonus:save")
    @SysLog(module = "红利列表", methodText = "新增红利")
    @ApiOperation(value = "保存", notes = "保存")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R saveBonus(@RequestBody List<OprActBonus> oprActBonusList, HttpServletRequest request) {
        List<String> errorList = new ArrayList<>();
        OprActBonus oprActBonus = null;
        for (int i=0; i<oprActBonusList.size(); i++) {
            try {
                oprActBonus = oprActBonusList.get(i);
                saveABonus(oprActBonus, request);
            }catch (Exception e) {
                if (oprActBonus != null){
                    errorList.add(oprActBonus.getLoginName());
                }
            }
        }

        return R.ok().put("failUserList", errorList);


    }

    private R saveABonus(OprActBonus oprActBonus, HttpServletRequest request){
        Assert.isNull(oprActBonus.getActivityId(), "活动不能为空");
        Assert.isNull(oprActBonus.getLoginName(), "会员名不能为空");
        Assert.isNull(oprActBonus.getDiscountAudit(), "流水倍数不能为空");
        Assert.isNull(oprActBonus.getBonusAmount(), "赠送金额不能为空");

        if (oprActBonus.getBonusAmount().compareTo(BigDecimal.ZERO) != 1) {
            throw new R200Exception("赠送金额必须大于0");
        }
        // 存款金额可以为Null,但如果不为null，则校验大于等于0
        if (nonNull(oprActBonus.getDepositedAmount()) && oprActBonus.getDepositedAmount().compareTo(BigDecimal.ZERO) == -1
//                ||   new BigDecimal(oprActBonus.getDepositedAmount().intValue()).compareTo(oprActBonus.getDepositedAmount()) !=0
        ) {
            throw new R200Exception("入款金额不能是负数");
        }

        String redisKey = RedisConstants.SAVE_BONUS_LOCK + CommonUtil.getSiteCode() + getUser().getUserId() + oprActBonus.getActivityId() + oprActBonus.getSubRuleTmplCode();
        boolean flag = redisService.setRedisExpiredTimeBo(redisKey, oprActBonus.getActivityId(), 5, TimeUnit.SECONDS);
        if (flag) {
            try {
                // 保存红利
                activityAddService.save(oprActBonus, getUser().getUsername(), CommonUtil.getIpAddress(request));
            } finally {
                redisService.del(redisKey);
            }
        } else {
            return R.error("正在处理中，请稍后重试！");
        }

        return R.ok();
    }

    @GetMapping("availableCatAndActList")
    @RequiresPermissions("operate:bonus:save")
    @ApiOperation(value = "红利列表页面", notes = "新增红利时获取可用活动类型及其下可用活动")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R availableCatAndActList() {
        return R.ok().put(oprActBaseService.getActivityAndCat());
    }

    @GetMapping("getDiscountAudit")
    @RequiresPermissions("operate:bonus:save")
    @ApiOperation(value = "红利列表页面", notes = "查询稽核倍数（流水倍数）")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R getAuditMulti(OprActBonus bonus) {
        bonus.setDiscountAudit(null);
        bonus.setBonusAmount(null);
        activityAddService.getAuditMulti(bonus);
        return R.ok().put("discountAudit", bonus.getDiscountAudit()).put("bonusAmount", bonus.getBonusAmount());
    }

    @GetMapping("getActivitiesWithAuditCount")
    @RequiresPermissions("operate:bonus:list")
    @ApiOperation(value = "红利列表页面活动list", notes = "红利列表页面活动list")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R getActivitiesWithAuditCount(OprActActivity activity,
                                         @RequestParam("pageNo") @NotNull Integer pageNo,
                                         @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().put(oprActBaseService.getActivitiesWithAuditCount(activity, pageNo, pageSize));
    }

    @GetMapping("/allActivityAuditListExport")
    @RequiresPermissions("operate:bonus:list")
    @ApiOperation(value = "所有活动审核集合导出", notes = "所有活动审核集合导出")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R allActivityAuditListExport(@ModelAttribute OprActActivity activity) {
        //获取所有条件内的活动
        List<OprActActivity> list = oprActBaseService.getAllActivitiesWithAuditCount(activity);

        SysFileExportRecord record = oprActBaseService.allActivityAuditListExport(list, getUserId(), allActivityAuditListExportModule);

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
        SysFileExportRecord record = sysFileExportRecordService.getAsynFileExportRecordByUserId(userId,allActivityAuditListExportModule);
        if(null != record){
            String fileName = allActivityAuditListExportPath.substring(allActivityAuditListExportPath.lastIndexOf("/")+1,allActivityAuditListExportPath.length());
            record.setDownloadFileName(fileName);
            return R.ok().put(record);
        }
        return R.ok(false);
    }
}
