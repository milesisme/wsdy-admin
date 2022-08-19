package com.wsdy.saasops.modules.system.domain.controller;

import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.system.domain.entity.SystemDomain;
import com.wsdy.saasops.modules.system.domain.service.SystemDomainService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/bkapi/system/systemdomain")
@Api(value = "SystemDomain站点域名", tags = "站点域名")
public class SystemDomainController {

    @Autowired
    private SystemDomainService systemDomainService;

    /**
     * 列表
     */
    @GetMapping("/list")
    @RequiresPermissions("system:systemdomain:list")
    @ApiOperation(value="列表", notes="列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R list(@ModelAttribute SystemDomain domain, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().put("page", systemDomainService.queryListPage(domain,pageNo,pageSize));
    }


    /**
     * 信息
     */
    @GetMapping("/info/{id}")
    @RequiresPermissions("system:systemdomain:info")
    @ApiOperation(value="信息", notes="信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R info(@PathVariable("id") Integer id) {
        SystemDomain systemDomain =systemDomainService.queryObject(id);
        return R.ok().put("systemDomain", systemDomain);
    }

    /**
     * 保存
     */
    @PostMapping("/save")
    @RequiresPermissions("system:systemdomain:save")
    @ApiOperation(value="保存", notes="保存")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R save(@RequestBody SystemDomain systemDomain) throws CloneNotSupportedException {
        systemDomainService.save(systemDomain);
        return R.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @RequiresPermissions("system:systemdomain:update")
    @ApiOperation(value="修改", notes="修改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R update(@RequestBody SystemDomain systemDomain) {
        systemDomainService.update(systemDomain);
        return R.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @RequiresPermissions("system:systemdomain:delete")
    @ApiOperation(value="删除", notes="删除")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R delete(@RequestBody SystemDomain systemDomain) {
        systemDomainService.deleteBatch(StringUtils.join(systemDomain.getIds(),","));
        return R.ok();
    }

    /**
     * 导出报表
     * @param domain
     * @param response
     */
    @GetMapping("/domainExportExcel")
    @RequiresPermissions("system:systemdomain:save")
    @ApiOperation(value="导出报表", notes="导出报表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public void domainExportExcel(SystemDomain domain, HttpServletResponse response){
        systemDomainService.domainExportExcel(domain,response);
    }

    /**
     * 初始化查询数据
     * @return
     */
    @GetMapping("/getAllDomainList")
    @RequiresPermissions("system:systemdomain:save")
    @ApiOperation(value="初始化查询数据", notes="初始化查询数据")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    @ResponseBody
    public R getAllDomainList(@RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize, SystemDomain domain){
        return R.ok().put("page", systemDomainService.queryByConditions(domain,pageNo,pageSize));
    }
}
