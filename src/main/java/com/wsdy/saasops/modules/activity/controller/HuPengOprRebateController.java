package com.wsdy.saasops.modules.activity.controller;


import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.activity.service.HuPengOprRebateService;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.operate.service.OprActActivityService;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/bkapi/activity/hupeng")
public class HuPengOprRebateController extends AbstractController {

    @Value("${activity.hupeng.excel.path:excelTemplate/activity/呼朋换友.xls}")
    private String huPengReportExport;

    private String module = "huPengHuYou";
    @Autowired
    private OprActActivityService oprActActivityService;

    @Autowired
    private HuPengOprRebateService huPengOprRebateService;

    @GetMapping("/huPengRebateRewardList")
    @ApiOperation(value = "查询返利列表", notes = "呼朋换友")
    @RequiresPermissions("operate:hupengrebate:rewardlist")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R huPengRebateRewardList(@RequestParam(value = "loginName", required = false) String loginName, @RequestParam(value = "groupId", required = false) Integer groupId, @RequestParam(value = "startTime", required = false)String startTime, @RequestParam(value = "endTime", required = false)String endTime,  @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(huPengOprRebateService.huPengRebateRewardList( loginName, startTime,  endTime,  groupId,  pageNo,  pageSize));
    }

    @GetMapping("/huPengRebateRewardDetails")
    @ApiOperation(value = "查询返利列表详情", notes = "呼朋换友")
    @RequiresPermissions("operate:hupengrebate:rewardlist")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R huPengRebateRewardDetails(@RequestParam("loginName") String loginName,  @RequestParam(value = "startTime", required = false)String startTime, @RequestParam(value = "endTime", required = false)String endTime,  @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(huPengOprRebateService.huPengRebateRewardDetails( loginName, startTime,  endTime,   pageNo,  pageSize));
    }


    @GetMapping("/huPengRebateRewardDetailsSummary")
    @ApiOperation(value = "查询返利列表详情汇总", notes = "呼朋换友")
    @RequiresPermissions("operate:hupengrebate:rewardlist")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R huPengRebateRewardDetailsSummary(@RequestParam("loginName") String loginName,  @RequestParam(value = "startTime", required = false)String startTime, @RequestParam(value = "endTime", required = false)String endTime) {
        return R.ok().put( "total",huPengOprRebateService.huPengRebateRewardDetailsSummary( loginName, startTime,  endTime));
    }


    @GetMapping("/huPengFriendsRebateRewardList")
    @ApiOperation(value = "查询返利列表", notes = "呼朋换友")
    @RequiresPermissions("operate:hupengrebate:rewardlist")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R huPengFriendsRebateRewardList(@RequestParam(value = "loginName", required = false) String loginName, @RequestParam(value = "groupId", required = false) Integer groupId, @RequestParam(value = "startTime", required = false)String startTime, @RequestParam(value = "endTime", required = false)String endTime,  @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(huPengOprRebateService.huPengFriendsRebateRewardList( loginName, startTime,  endTime,  groupId,  pageNo,  pageSize));
    }

    @GetMapping("orderExport")
    @RequiresPermissions("operate:hupengrebate:export")
    @ApiOperation(value = "导出", notes = "呼朋换友")
    public R orderExport(@RequestParam("module") String module,@RequestParam(value = "loginName", required = false) String loginName, @RequestParam(value = "groupId", required = false) Integer groupId, @RequestParam(value = "startTime", required = false)String startTime, @RequestParam(value = "endTime", required = false)String endTime) {
        SysFileExportRecord record = huPengOprRebateService.orderExport(loginName,groupId, startTime, endTime, getUserId(), module, huPengReportExport);
        if (record == null) {
            throw new R200Exception("正在处理中!");
        }
        return R.ok();
    }

    @GetMapping("/checkFile")
    @ApiOperation(value = "呼朋统计导出文件是否可下载",notes = "呼朋换友")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token头部，随便填数字",  required = true, dataType = "Integer", paramType = "header"),
    })
    public R checkFile(@RequestParam("module") String module){
        return huPengOprRebateService.checkFile(module, huPengReportExport, getUser().getUserId());
    }

}
