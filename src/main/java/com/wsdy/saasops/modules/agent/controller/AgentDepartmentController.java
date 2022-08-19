package com.wsdy.saasops.modules.agent.controller;

import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.agent.entity.AgentDepartment;
import com.wsdy.saasops.modules.agent.service.AgentDepartmentService;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bkapi/agent/dmet")
@Api(tags = "部门-代理2.0")
public class AgentDepartmentController extends AbstractController {

    @Autowired
    private AgentDepartmentService departmentService;

    @GetMapping("agentShareholderList")
    @ApiOperation(value = "查询查询股东", notes = "查询查询股东")
    public R agentShareholderList() {
        return R.ok().put(departmentService.agentShareholderList());
    }

    @GetMapping("alldDepartmentList")
    @ApiOperation(value = "部门查询所有", notes = "部门查询所有")
    public R alldDepartmentList() {
        return R.ok().put(departmentService.departmentList());
    }

    @GetMapping("departmentList")
    @RequiresPermissions("agent:dmet:list")
    @ApiOperation(value = "部门查询查询", notes = "部门查询查询")
    public R departmentList() {
        return R.ok().put(departmentService.departmentList());
    }

    @PostMapping("addDepartment")
    @RequiresPermissions("agent:department:add")
    @ApiOperation(value = "部门新增", notes = "部门新增")
    public R addDepartment(@RequestBody List<AgentDepartment> department) {
        //Assert.isBlank(department.getDepartmentName(), "名称不能为空");
        //Assert.isNull(department.getAgentId(), "股东不能为空");
        //department.setCreateUser(getUser().getUsername());
        //department.setModifyUser(getUser().getUsername());
        departmentService.addDepartment(department, getUser().getUsername());
        return R.ok();
    }

    /*@PostMapping("updateDepartment")
    @RequiresPermissions("agent:department:update")
    @ApiOperation(value = "部门修改", notes = "部门修改")
    public R updateDepartment(@RequestBody AgentDepartment department) {
        Assert.isNull(department.getId(), "ID不能为空");
        Assert.isBlank(department.getDepartmentName(), "名称不能为空");
        Assert.isNull(department.getAgentId(), "股东不能为空");
        department.setModifyUser(getUser().getUsername());
        departmentService.updateDepartment(department);
        return R.ok();
    }*/

    @PostMapping("deleteDepartment")
    @RequiresPermissions("agent:department:delete")
    @ApiOperation(value = "部门删除", notes = "部门删除")
    public R deleteDepartment(@RequestBody AgentDepartment department) {
        Assert.isNull(department.getId(), "ID不能为空");
        departmentService.deleteDepartment(department);
        return R.ok();
    }
}
