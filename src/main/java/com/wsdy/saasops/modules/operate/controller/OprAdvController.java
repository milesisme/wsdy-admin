package com.wsdy.saasops.modules.operate.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import com.wsdy.saasops.common.utils.CommonUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import com.wsdy.saasops.modules.operate.entity.OprAdv;
import com.wsdy.saasops.modules.operate.service.OprAdvService;
import com.wsdy.saasops.modules.sys.entity.SysUserEntity;
import com.wsdy.saasops.common.annotation.SysLog;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;

@RestController
@RequestMapping("/bkapi/operate/opradv")
@Api(value = "OprAdv", tags = "")
public class OprAdvController {
    @Autowired
    private OprAdvService oprAdvService;

    @SysLog(module = "广告管理",methodText = "查询列表")
    @GetMapping("/list")
    @RequiresPermissions("operate:opradv:list")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R list(@ModelAttribute OprAdv oprAdv, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize,@RequestParam(value="orderBy",required=false) String orderBy) throws IOException {
    	return R.ok().put("page", oprAdvService.queryListPage(oprAdv,pageNo,pageSize,orderBy));
    }

    @GetMapping("/info/{id}")
	@RequiresPermissions("operate:opradv:list")
	@ApiOperation(value = "信息", notes = "信息")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header") })
	public R info(@PathVariable("id") Integer id) {
    	OprAdv oprAdv = oprAdvService.queryOprAdvInfo(id);
		return R.ok().put("oprAdv", oprAdv);
	}
	
    @SysLog(module = "广告管理",methodText = "新增广告")
    @PostMapping("/save")
    @RequiresPermissions("operate:opradv:save")
    @ApiOperation(value="保存", notes="保存")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R save(@RequestBody OprAdv oprAdv, HttpServletRequest request) {
    	Assert.isNull(oprAdv, "不能为空");
    	SysUserEntity sysUserEntity = (SysUserEntity) SecurityUtils.getSubject().getPrincipal();
    	String username = sysUserEntity.getUsername();
    	oprAdvService.save(oprAdv,username, CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @SysLog(module = "广告管理",methodText = "更新广告")
    @PostMapping("/update")
    @RequiresPermissions("operate:opradv:update")
    @ApiOperation(value="修改", notes="修改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R update(@RequestBody OprAdv oprAdv, HttpServletRequest request) {
    	Assert.isNull(oprAdv, "不能为空");
        SysUserEntity sysUserEntity = (SysUserEntity) SecurityUtils.getSubject().getPrincipal();
        String username = sysUserEntity.getUsername();
    	oprAdvService.update(oprAdv, username, CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @SysLog(module = "广告管理",methodText = "是否启用广告")
    @PostMapping("/enable")
    @RequiresPermissions("operate:opradv:available")
    @ApiOperation(value="修改", notes="修改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R enable(@RequestBody OprAdv oprAdv, HttpServletRequest request) {
    	Assert.isNull(oprAdv, "不能为空");
        SysUserEntity sysUserEntity = (SysUserEntity) SecurityUtils.getSubject().getPrincipal();
        String username = sysUserEntity.getUsername();
    	oprAdvService.enableOprAdv(oprAdv, username, CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @PostMapping("/uploadImage")
//    @RequiresPermissions("operate:opradv:update")
    @ApiOperation(value="上传图片", notes="上传图片")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R getSingleImageUrl(@RequestParam(value = "uploadFile", required = false) MultipartFile uploadFile) {
        Assert.isNull(uploadFile, "不能为空");
        return R.ok().put("path", oprAdvService.getSingleImageUrl(uploadFile));
    }

    @PostMapping("/uploadImages")
//    @RequiresPermissions("operate:opradv:update")
    @ApiOperation(value="上传图片", notes="上传图片:同时上传多张")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R getMultiImageUrl(@RequestParam(value = "uploadFile", required = false) MultipartFile[] uploadFile) {
        Assert.isNull(uploadFile, "不能为空");
        return R.ok().put(oprAdvService.getMultiImageUrl(uploadFile));
    }


    @SysLog(module = "广告管理",methodText = "删除广告")
    @PostMapping("/delete")
    @RequiresPermissions("operate:opradv:delete")
    @ApiOperation(value="删除", notes="删除")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R delete(@RequestBody OprAdv oprAdv, HttpServletRequest request) {
    	Assert.isNull(oprAdv, "不能为空");
        SysUserEntity sysUserEntity = (SysUserEntity) SecurityUtils.getSubject().getPrincipal();
        String username = sysUserEntity.getUsername();
    	oprAdvService.deleteBatch(oprAdv, username, CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @GetMapping("/exportExcel")
    @RequiresPermissions("operate:opradv:list")
    @ApiOperation(value = "总代设置数据导出", notes = "总代设置数据导出")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public void accountExportExcel(@ModelAttribute OprAdv oprAdv, HttpServletResponse response) {
    	Assert.isNull(oprAdv, "不能为空");
    	oprAdvService.accountExportExcel(oprAdv, response);
    }
    
}
