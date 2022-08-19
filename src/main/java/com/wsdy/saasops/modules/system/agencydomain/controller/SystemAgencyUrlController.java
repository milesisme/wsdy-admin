package com.wsdy.saasops.modules.system.agencydomain.controller;

import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.agent.service.AgentAccountService;
import com.wsdy.saasops.modules.system.agencydomain.entity.SystemAgencyUrl;
import com.wsdy.saasops.modules.system.agencydomain.service.SystemAgencyUrlService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bkapi/systemAgencyUrl/systemagencyurl")
@Api(value = "SystemAgencyUrl", tags = "")
public class SystemAgencyUrlController {

    @Autowired
    private SystemAgencyUrlService systemAgencyUrlService;
    @Autowired
    private AgentAccountService agentAccountService;

    /**
     * 列表
     */
    @GetMapping("/list")
    @RequiresPermissions("system:systemdomain:list")
    public R list(@ModelAttribute SystemAgencyUrl systemAgencyUrl, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize,String orderBy) {
        return R.ok().put("page", systemAgencyUrlService.queryListPage(pageNo,pageSize,orderBy));
    }

    /**
     * 列表
     */
    @GetMapping("/queryListPage")
    @RequiresPermissions("system:systemdomain:list")
    public R queryListPage(@ModelAttribute SystemAgencyUrl systemAgencyUrl, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize,String orderBy) {
        return R.ok().put("page", systemAgencyUrlService.queryConditions(systemAgencyUrl,pageNo,pageSize,orderBy));
    }


    /**
     * 信息
     */
    @GetMapping("/info/{agencyId}")
    @RequiresPermissions("system:systemdomain:info")
    @ApiOperation(value="信息", notes="信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R info(@PathVariable("agencyId") Integer agencyId) {
        SystemAgencyUrl systemAgencyUrl =systemAgencyUrlService.queryObject(agencyId);
        return R.ok().put("agencydomain", systemAgencyUrl);
    }

    /**
     * 保存
     */
    @PostMapping("/save")
    @RequiresPermissions("system:systemdomain:save")
    @ApiOperation(value="保存", notes="保存")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R save(@RequestBody SystemAgencyUrl systemAgencyUrl) throws CloneNotSupportedException {
        systemAgencyUrlService.save(systemAgencyUrl);
        return R.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @RequiresPermissions("system:systemdomain:update")
    @ApiOperation(value="修改", notes="修改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R update(@RequestBody SystemAgencyUrl systemAgencyUrl) {
        systemAgencyUrlService.update(systemAgencyUrl);
        return R.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @RequiresPermissions("system:systemdomain:delete")
    @ApiOperation(value="删除", notes="删除")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R delete(@RequestBody SystemAgencyUrl systemAgencyUrl) {
        systemAgencyUrlService.deleteBatch(StringUtils.join(systemAgencyUrl.getAgencyIds(),","));
        return R.ok();
    }

    /**
     * 查询未配置域名的代理账号
     * @return
     */
    @GetMapping("/queryAgyCountNoUrl")
    @RequiresPermissions("system:systemdomain:list")
    @ApiOperation(value="查询未配置域名的代理账号", notes="查询未配置域名的代理账号")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R queryAgyCountNoUrl(){
        List<Map<String,Object>> list=agentAccountService.queryAgyCountNoUrl();
        return R.ok().put("list",list);
    }
}
