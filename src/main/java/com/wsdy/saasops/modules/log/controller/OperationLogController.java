package com.wsdy.saasops.modules.log.controller;


import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.wsdy.saasops.modules.log.entity.OperationLog;
import com.wsdy.saasops.modules.log.service.OperationLogService;
import com.wsdy.saasops.common.utils.R;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/bkapi/operate/operationlog")
@Api(value = "OperationLog", tags = "")
public class OperationLogController {
    @Autowired
    private OperationLogService operationLogService;

    @GetMapping("/list")
    @RequiresPermissions("operate:operationlog:list")
    public R list(@ModelAttribute OperationLog operationLog, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize,@RequestParam(value="orderBy",required=false) String orderBy) {
        return R.ok().put("page", operationLogService.queryListPage(operationLog,pageNo,pageSize,orderBy));
    }


    @GetMapping("/info/{id}")
    @RequiresPermissions("operate:operationlog:info")
    @ApiOperation(value="信息", notes="信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R info(@PathVariable("id") Integer id) {
        OperationLog operationLog =operationLogService.queryObject(id);

        return R.ok().put("operationLog", operationLog);
    }

    @PostMapping("/save")
    @RequiresPermissions("operate:operationlog:save")
    @ApiOperation(value="保存", notes="保存")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R save(@RequestBody OperationLog operationLog) {
            operationLogService.save(operationLog);

        return R.ok();
    }

    @PostMapping("/update")
    @RequiresPermissions("operate:operationlog:update")
    @ApiOperation(value="修改", notes="修改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R update(@RequestBody OperationLog operationLog) {
            operationLogService.update(operationLog);

        return R.ok();
    }

    @PostMapping("/delete")
    @RequiresPermissions("operate:operationlog:delete")
    @ApiOperation(value="删除", notes="删除")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R delete(@RequestBody Integer[]ids) {
          
        return R.ok();
    }
}
