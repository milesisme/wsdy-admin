package com.wsdy.saasops.modules.operate.controller;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wsdy.saasops.common.annotation.SysLog;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.operate.entity.HelpGuessAsk;
import com.wsdy.saasops.modules.operate.service.HelpGuessAskService;
import com.wsdy.saasops.modules.sys.entity.SysUserEntity;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/bkapi/operate/guessAsk")
@Api(value = "猜你想问")
public class HelpGuessAskController {
    @Autowired
    private HelpGuessAskService helpGuessAskService;

    @SysLog(module = "猜你想问",methodText = "查询列表")
    @GetMapping("/list")
    @RequiresPermissions("operate:guessAsk:list")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R list(@RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
    	return R.ok().put("page", helpGuessAskService.queryListPage(pageNo, pageSize, null));
    }
	
    @SysLog(module = "猜你想问",methodText = "新增")
    @PostMapping("/save")
    @RequiresPermissions("operate:guessAsk:save")
    @ApiOperation(value="保存", notes="保存")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R save(@RequestBody HelpGuessAsk helpGuessAsk) {
    	Assert.isNull(helpGuessAsk, "不能为空");
    	SysUserEntity sysUserEntity = (SysUserEntity) SecurityUtils.getSubject().getPrincipal();
    	String username = sysUserEntity.getUsername();
    	helpGuessAsk.setUpdater(username);
    	helpGuessAsk.setUpdatetime(new Date());
    	helpGuessAskService.save(helpGuessAsk);
        return R.ok();
    }

    @SysLog(module = "猜你想问",methodText = "更新")
    @PostMapping("/update")
    @RequiresPermissions("operate:guessAsk:update")
    @ApiOperation(value="修改", notes="修改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R update(@RequestBody HelpGuessAsk helpGuessAsk) {
    	Assert.isNull(helpGuessAsk, "不能为空");
        SysUserEntity sysUserEntity = (SysUserEntity) SecurityUtils.getSubject().getPrincipal();
        String username = sysUserEntity.getUsername();
        helpGuessAsk.setUpdater(username);
        helpGuessAsk.setUpdatetime(new Date());
        helpGuessAskService.update(helpGuessAsk);
        return R.ok();
    }

    @SysLog(module = "猜你想问",methodText = "删除")
    @PostMapping("/delete")
    @RequiresPermissions("operate:guessAsk:delete")
    @ApiOperation(value="删除", notes="删除")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R delete(@RequestBody HelpGuessAsk helpGuessAsk, HttpServletRequest request) {
    	Assert.isNull(helpGuessAsk, "不能为空");
        helpGuessAskService.deleteById(helpGuessAsk.getId());
        return R.ok();
    }
    
}
