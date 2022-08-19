package com.wsdy.saasops.modules.agent.controller;

import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.agent.dto.AgentComReportDto;
import com.wsdy.saasops.modules.agent.service.AgentComReportService;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/bkapi/agent/comReport")
@Api(value = "代理报表--综合报表", tags = "代理报表--综合报表")
public class AgentComReportController extends AbstractController {

    @Autowired
    private AgentComReportService agentComReportService;

    @GetMapping("/totalInfo")
    @RequiresPermissions("agent:report:comviewlist")
    @ApiOperation(value = "综合报表--总览详情", notes = "代理报表--综合报表--总览详情")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R totalInfo(@ModelAttribute AgentComReportDto agentComReportDto) {
        return R.ok().put("info", agentComReportService.totalInfo(agentComReportDto, getUserId()));
    }

    @GetMapping("/totalListByDay")
    @RequiresPermissions("agent:report:comviewlist")
    @ApiOperation(value = "综合报表--按天汇总视图--分页", notes = "代理报表--综合报表--按天汇总视图--分页")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R totalListByDay(@ModelAttribute AgentComReportDto agentComReportDto) {
        return R.ok().put("page", agentComReportService.totalListByDay(agentComReportDto, getUserId()));
    }

    @GetMapping("/tagencyList")
    @RequiresPermissions("agent:report:comviewlist")
    @ApiOperation(value = "综合报表--(股东/总代/全选)视图/下级代理列表", notes = "综合报表--(股东/总代/全选)视图/下级代理列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R tagencyList(@ModelAttribute AgentComReportDto agentComReportDto) {
        return R.ok().put("page", agentComReportService.tagencyList(agentComReportDto));
    }

    @GetMapping("/categoryList")
    @RequiresPermissions("agent:report:comviewlist")
    @ApiOperation(value = "综合报表--总代下部门(类别)视图", notes = "综合报表--总代下部门(类别)视图")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R categoryList(@ModelAttribute AgentComReportDto agentComReportDto) {
        Assert.isNull(agentComReportDto.getAgyId(), "agyId不能为空");
        return R.ok().put("page", agentComReportService.categoryList(agentComReportDto));
    }

    @GetMapping("/subAgentTotalViewList")
    @RequiresPermissions("agent:report:comviewlist")
    @ApiOperation(value = "综合报表--子代代理视图汇总表头", notes = "综合报表--子代代理视图汇总表头")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R subAgentTotalViewList(@ModelAttribute AgentComReportDto agentComReportDto) {
        return R.ok().put(agentComReportService.subAgentTotalViewList(agentComReportDto));
    }

    @GetMapping("/memberList")
    @RequiresPermissions("agent:report:comviewlist")
    @ApiOperation(value = "综合报表--下级会员列表", notes = "综合报表--下级会员列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R memberList(@ModelAttribute AgentComReportDto agentComReportDto) {
        return R.ok().put("page", agentComReportService.memberList(agentComReportDto));
    }

    @PostMapping("/agentLineReportExportCount")
    @ApiOperation(value = "综合报表--代理线导出--核对信息",notes = "代理报表--代理线导出--核对信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",required = true, dataType = "Integer", paramType = "header")})
    public R agentLineReportExportCount(@RequestBody AgentComReportDto agentComReportDto){
        Assert.isBlank(agentComReportDto.getAgyAccount(), "请填写代理账号");
        Assert.isNull(agentComReportDto.getAgentLineExportTypes(), "请勾选需要导出的代理");

        Integer count = agentComReportService.agentLineReportExportCount(agentComReportDto, getUser().getUserId());

        return R.ok().put("count", count);
    }

    @PostMapping("/agentLineReportExport")
    @RequiresPermissions("agent:report:agentlintexport")
    @ApiOperation(value = "综合报表--代理线导出",notes = "代理报表--代理线导出")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",required = true, dataType = "Integer", paramType = "header")})
    public R agentLineReportExport(@RequestBody AgentComReportDto agentComReportDto){
        Assert.isBlank(agentComReportDto.getModule(), "module不能为空");
        Assert.isNull(agentComReportDto.getAgentLineExportTypes(), "请勾选需要导出的代理");

        SysFileExportRecord record = agentComReportService.comReportExport(agentComReportDto, getUser().getUserId());

        if(record == null){
            throw new R200Exception("正在处理中!");
        }
        return R.ok();
    }

    @GetMapping("/comReportExport")
    @RequiresPermissions("agent:report:comviewexport")
    @ApiOperation(value = "综合报表--统一导出文件生成接口",notes = "代理报表--综合报表--统一导出文件生成接口")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",required = true, dataType = "Integer", paramType = "header")})
    public R comReportExport(@ModelAttribute AgentComReportDto agentComReportDto){
        Assert.isBlank(agentComReportDto.getModule(), "module不能为空");

        SysFileExportRecord record = agentComReportService.comReportExport(agentComReportDto, getUser().getUserId());

        if(record == null){
            throw new R200Exception("正在处理中!");
        }
        return R.ok();
    }

    @GetMapping("/checkFile")
    @ApiOperation(value = "综合报表--统一查询导出文件是否可下载",notes = "代理报表--综合报表--统一查询导出文件是否可下载\"")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token头部，随便填数字",  required = true, dataType = "Integer", paramType = "header"),
    })
    public R checkFile(@RequestParam("module") String module){
        return agentComReportService.checkFile(module,getUser().getUserId());
    }

    @GetMapping("/modifyAgentCateGory")
    @ApiOperation(value = "修改代理部门--测试用",notes = "修改代理部门--测试用")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token头部，随便填数字",  required = true, dataType = "Integer", paramType = "header"),
    })
    public R modifyAgentCateGory(@ModelAttribute AgentComReportDto agentComReportDto){
        Assert.isBlank(agentComReportDto.getAgyAccount(),"agyAccount不为空！");
        Assert.isNull(agentComReportDto.getDepartmentid(),"部门不为空！");
        agentComReportService.modifyAgentCateGory(agentComReportDto);
        return R.ok();
    }

    @GetMapping("/agentMemberTotalView")
    @RequiresPermissions("agent:report:agentMemberTotalView")
    @ApiOperation(value = "综合报表--代理直属会员总览", notes = "综合报表--代理直属会员总览")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R agentMemberTotalView(@ModelAttribute AgentComReportDto agentComReportDto) {
        return R.ok().put("page", agentComReportService.agentMemberTotalView(agentComReportDto));
    }

}
