package com.wsdy.saasops.modules.system.msgtemple.controller;

import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.system.msgtemple.entity.MsgModel;
import com.wsdy.saasops.modules.system.msgtemple.service.MsgModelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/bkapi/system/msgtemple")
@Api(value = "MsgModel", tags = "信息模板")
public class MsgModelController  extends AbstractController {

    @Autowired
    private MsgModelService msgModelService;

    /**
     * 列表
     */
    @GetMapping("/list")
    @RequiresPermissions("msgtemple:msgtemple:list")
    @ResponseBody
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R list(@ModelAttribute MsgModel msgModel, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize,String orderBy) {
        return R.ok().put("page", msgModelService.queryListPage(msgModel,pageNo,pageSize,orderBy));
    }


    /**
     * 信息
     */
    @GetMapping("/info/{id}")
    @RequiresPermissions("msgtemple:msgtemple:info")
    @ResponseBody
    @ApiOperation(value="信息", notes="信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R info(@PathVariable("id") Integer id) {
        MsgModel msgModel =msgModelService.queryObject(id);
        return R.ok().put("msgtemple", msgModel);
    }

    /**
     * 保存
     */
    @PostMapping("/save")
    @ResponseBody
    @RequiresPermissions("msgtemple:msgtemple:save")
    @ApiOperation(value="保存", notes="保存")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R save(@RequestBody MsgModel msgModel, HttpServletRequest request) {
    	return msgModelService.save(msgModel,getUser().getUsername(), CommonUtil.getIpAddress(request));
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ResponseBody
    @RequiresPermissions(value = {"msgtemple:msgtemple:update","msgtemple:msgtemple:available"}, logical = Logical.OR)
    @ApiOperation(value="修改", notes="修改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R update(@RequestBody MsgModel msgModel, HttpServletRequest request) {
            msgModelService.update(msgModel, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ResponseBody
    @RequiresPermissions("msgtemple:msgtemple:delete")
    @ApiOperation(value="删除", notes="删除")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R delete(@RequestBody MsgModel msgModel, HttpServletRequest request) {
            msgModelService.deleteBatch(msgModel.getIds(), getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    /**
     * 列表
     */
    @GetMapping("/queryByConditions")
    @RequiresPermissions("msgtemple:msgtemple:list")
    @ResponseBody
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R queryByConditions(@ModelAttribute MsgModel msgModel, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize,String orderBy) {
        return R.ok().put("page", msgModelService.queryByConditions(msgModel,pageNo,pageSize,orderBy));
    }
    /**
     * 导出报表
     * @param msgModel
     * @param response
     */
    @GetMapping("/ExportExcel")
    @RequiresPermissions("system:systemdomain:save")
    @ApiOperation(value="导出报表", notes="导出报表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public void ExportExcel(MsgModel msgModel, HttpServletResponse response){
        msgModelService.exportExcel(msgModel,response);
    }

}
