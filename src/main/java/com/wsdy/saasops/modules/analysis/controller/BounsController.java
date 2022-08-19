package com.wsdy.saasops.modules.analysis.controller;

import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.analysis.entity.BounsReportQueryModel;
import com.wsdy.saasops.modules.analysis.service.AnalysisService;
import com.wsdy.saasops.modules.analysis.service.BounsService;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import com.wsdy.saasops.modules.sys.entity.SysUserEntity;
import com.wsdy.saasops.modules.sys.service.SysFileExportRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/bkapi/analysis/bouns")
@Api(value = "Bouns", tags = "红利报表")
public class BounsController extends AbstractController {

    @Autowired
    private AnalysisService analysisService;
    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;
    @Autowired
    private BounsService bounsService;

    @Value("${analysis.mbrBonus.excel.path}")
    private String mbrBonusExcelTempPath;
    private final String module = "mbrBonus";
    /***
     * 查询红利报表
     * @return
     */
    @RequestMapping("/findBonusReportPage")
    @RequiresPermissions("analysis.bouns.view")
    @ApiOperation(value = "红利报表", notes = "全部游戏")
    public R findBonusReportPage(BounsReportQueryModel model) {
        return R.ok().put("page", analysisService.findBonusReportPage(model));
    }

    /***
     * 查询红利报表,聚合代理
     * @return
     */
    @RequestMapping("/findBonusGroupTopAgentReportPage")
    @RequiresPermissions("analysis.bouns.view")
    @ApiOperation(value = "红利报表", notes = "全部游戏")
    public R findBonusGroupTopAgentReportPage(BounsReportQueryModel model) {
        return R.ok().put("page", analysisService.findBonusGroupTopAgentReportPage(model));
    }

    /***
     * 查询红利报表,聚合代理
     * @return
     */
    @RequestMapping("/findBonusGroupAgentReportPage")
    @RequiresPermissions("analysis.bouns.view")
    @ApiOperation(value = "红利报表", notes = "全部游戏")
    public R findBonusGroupAgentReportPage(BounsReportQueryModel model) {
        return R.ok().put("page", analysisService.findBonusGroupAgentReportPage(model));
    }

    /***
     * 查询红利报表,聚合会员 会员组
     * @return
     */
    @RequestMapping("/findBonusGroupUserReportPage")
    @RequiresPermissions(value = {"analysis:betDetails:finalBetDetailsAll", "analysis.bouns.view"}, logical = Logical.OR)
    @ApiOperation(value = "红利报表", notes = "全部游戏")
    public R findBonusGroupUserReportPage(BounsReportQueryModel model) {
        return R.ok().put("page", analysisService.findBonusGroupUserReportPage(model));
    }

    /***
     * 查询红利报表,聚合 总代 代理 会员 会员组
     * @return
     */
    @RequestMapping("/findBonusGroupUserTotal")
    @RequiresPermissions("analysis.bouns.view")
    @ApiOperation(value = "红利报表", notes = "全部游戏")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            ,@ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R findBonusGroupUserTotal(BounsReportQueryModel model) {
        /** 获取登录用户的信息 **/
        SysUserEntity sysUserEntity=getUser();
        model.setLoginSysUserName(sysUserEntity.getUsername());
        return R.ok().put("page", analysisService.findBonusGroupUserTotal(model));
    }

    /***
     * 查询红利报表,展示列表
     * @return
     */
    @RequestMapping("/findBonusPage")
    @RequiresPermissions("analysis.bouns.view")
    @ApiOperation(value = "红利报表", notes = "全部游戏")
    public R findBonusPage(BounsReportQueryModel model) {
        return R.ok().put("page", analysisService.findBonusPage(model));
    }

    @GetMapping("/exportBonus")
    @RequiresPermissions("analysis:bouns:export")
    @ApiOperation(value = "导出会员红利信息",notes = "导出会员红利信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    public R exportBonus(BounsReportQueryModel model){
        SysFileExportRecord record =  analysisService.exportBonus(model, getUser(), mbrBonusExcelTempPath, module);
        if(record == null){
            throw new R200Exception("正在处理中!");
        }
        return R.ok();
    }

    @GetMapping("/checkFile")
    @ApiOperation(value = "查询文件是否可下载",notes = "查询文件是否可下载")
    public R checkFile(){
        Long userId = getUserId();
        SysFileExportRecord record = sysFileExportRecordService.getAsynFileExportRecordByUserId(userId,module);
        if(null != record){
            String fileName = mbrBonusExcelTempPath.substring(mbrBonusExcelTempPath.lastIndexOf("/")+1,mbrBonusExcelTempPath.length());
            record.setDownloadFileName(fileName);
            return R.ok().put(record);
        }
        return R.ok(false);
    }


    /***
     * 红利报表,第一 查询红利报表,聚合总代-新---暂时不用
     * @return
     */
    @RequestMapping("/findBonusGroupTopAgentReportPageNew")
    @RequiresPermissions("analysis.bouns.view")
    @ApiOperation(value = "红利报表,第一 新", notes = "红利报表,第一 新")
    public R findBonusGroupTopAgentReportPageNew(BounsReportQueryModel model) {
        return R.ok().put("page", analysisService.findBonusGroupTopAgentReportPage(model));
    }

    /***
     * 下级代理报表
     * @return
     */
    @RequestMapping("/findSubAgent")
    @RequiresPermissions("analysis.bouns.view")
    @ApiOperation(value = "下级代理", notes = "下级代理")
    public R findSubordinateAgent(BounsReportQueryModel model) {
        return R.ok().put("page", bounsService.findSubordinateAgent(model));
    }

    /***
     * 下级代理跟直属会员-新
     * @return
     */
    @RequestMapping("/findSubordinateBonus")
    @RequiresPermissions("analysis.bouns.view")
    @ApiOperation(value = "下级代理跟直属会员", notes = "下级代理跟直属会员")
    public R findfindSubordinateBonus(BounsReportQueryModel model) {
        return R.ok().putPage(bounsService.findSubordinateBonus(model));
    }
}
