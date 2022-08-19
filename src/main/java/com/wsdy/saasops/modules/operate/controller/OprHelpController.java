package com.wsdy.saasops.modules.operate.controller;

import com.wsdy.saasops.common.annotation.SysLog;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.operate.entity.OprAdv;
import com.wsdy.saasops.modules.operate.entity.OprHelpCategory;
import com.wsdy.saasops.modules.operate.entity.OprHelpContent;
import com.wsdy.saasops.modules.operate.entity.OprHelpTitle;
import com.wsdy.saasops.modules.operate.service.OprAdvService;
import com.wsdy.saasops.modules.sys.entity.SysUserEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.IOException;

@RestController
@RequestMapping("/bkapi/operate/oprhelp")
@Api(value = "OprAdv", tags = "")
public class OprHelpController {
    @Autowired
    private OprAdvService oprAdvService;

    @SysLog(module = "帮助中心",methodText = "分类列表查询")
    @GetMapping("/helpCategoryList")
    @RequiresPermissions("operate:oprhelp:categorylist")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R helpCategoryList(@ModelAttribute OprHelpCategory oprHelpCategory, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize, @RequestParam(value="orderBy",required=false) String orderBy) throws IOException {
    	return R.ok().put("page", oprAdvService.queryCategoryListPage(oprHelpCategory,pageNo,pageSize,orderBy));
    }

    @GetMapping("/categoryInfo/{id}")
    @RequiresPermissions("operate:oprhelp:categoryInfo")
    @ApiOperation(value = "信息", notes = "信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header") })
    public R categoryInfo(@PathVariable("id") Integer id) {
        OprHelpCategory oprHelpCategory = oprAdvService.queryCategoryInfo(id);
        return R.ok().put("oprHelpCategory", oprHelpCategory);
    }
	
    @SysLog(module = "帮助中心",methodText = "新增分类")
    @PostMapping("/saveHelpCategory")
    @RequiresPermissions("operate:oprhelp:save")
    @ApiOperation(value="保存", notes="保存")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R saveHelpCategory(@RequestBody OprHelpCategory oprHelpCategory, HttpServletRequest request) {
    	Assert.isNull(oprHelpCategory, "不能为空");
    	SysUserEntity sysUserEntity = (SysUserEntity) SecurityUtils.getSubject().getPrincipal();
    	String username = sysUserEntity.getUsername();
    	oprAdvService.saveHelpCategory(oprHelpCategory,username, CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @SysLog(module = "帮助中心",methodText = "更新分类")
    @PostMapping("/updateHelpCategory")
    @RequiresPermissions("operate:oprhelp:update")
    @ApiOperation(value="修改", notes="修改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateHelpCategory(@RequestBody OprHelpCategory oprHelpCategory, HttpServletRequest request) {
    	Assert.isNull(oprHelpCategory, "不能为空");
        SysUserEntity sysUserEntity = (SysUserEntity) SecurityUtils.getSubject().getPrincipal();
        String username = sysUserEntity.getUsername();
    	oprAdvService.updateHelpCategory(oprHelpCategory, username, CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @SysLog(module = "帮助中心",methodText = "是否启用分类")
    @PostMapping("/enableCategory")
    @RequiresPermissions("operate:oprhelp:enableCategory")
    @ApiOperation(value="修改", notes="修改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R enableCategory(@RequestBody OprHelpCategory oprHelpCategory, HttpServletRequest request) {
    	Assert.isNull(oprHelpCategory, "不能为空");
        SysUserEntity sysUserEntity = (SysUserEntity) SecurityUtils.getSubject().getPrincipal();
        String username = sysUserEntity.getUsername();
    	oprAdvService.enableCategory(oprHelpCategory, username, CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @SysLog(module = "帮助中心",methodText = "删除分类")
    @PostMapping("/deleteCategory")
    @RequiresPermissions("operate:oprhelp:deleteCategory")
    @ApiOperation(value="删除", notes="删除")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R deleteCategory(@RequestBody OprHelpCategory oprHelpCategory, HttpServletRequest request) {
    	Assert.isNull(oprHelpCategory, "不能为空");
        SysUserEntity sysUserEntity = (SysUserEntity) SecurityUtils.getSubject().getPrincipal();
        String username = sysUserEntity.getUsername();
    	oprAdvService.deleteCategory(oprHelpCategory, username, CommonUtil.getIpAddress(request));
        return R.ok();
    }


    @SysLog(module = "帮助中心",methodText = "标题列表查询")
    @GetMapping("/helpTitleList")
    @RequiresPermissions("operate:oprhelp:titleList")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R helpTitleList(@ModelAttribute OprHelpTitle oprHelpTitle, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize, @RequestParam(value="orderBy",required=false) String orderBy) throws IOException {
        return R.ok().put("page", oprAdvService.queryTitleListPage(oprHelpTitle,pageNo,pageSize,orderBy));
    }

    @GetMapping("/titleInfo/{id}")
    @RequiresPermissions("operate:oprhelp:titleInfo")
    @ApiOperation(value = "信息", notes = "信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header") })
    public R titleInfo(@PathVariable("id") Integer id) {
        OprHelpTitle oprHelpTitle = oprAdvService.queryTitleInfo(id);
        return R.ok().put("oprHelpTitle", oprHelpTitle);
    }

    @SysLog(module = "帮助中心",methodText = "新增标题")
    @PostMapping("/saveHelpTitle")
    @RequiresPermissions("operate:oprhelp:save")
    @ApiOperation(value="保存", notes="保存")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R saveHelpTitle(@RequestBody OprHelpTitle oprHelpTitle, HttpServletRequest request) {
        Assert.isNull(oprHelpTitle, "不能为空");
        SysUserEntity sysUserEntity = (SysUserEntity) SecurityUtils.getSubject().getPrincipal();
        String username = sysUserEntity.getUsername();
        oprAdvService.saveHelpTitle(oprHelpTitle,username, CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @SysLog(module = "帮助中心",methodText = "更新标题")
    @PostMapping("/updateHelpTitle")
    @RequiresPermissions("operate:oprhelp:update")
    @ApiOperation(value="修改", notes="修改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateHelpTitle(@RequestBody OprHelpTitle oprHelpTitle, HttpServletRequest request) {
        Assert.isNull(oprHelpTitle, "不能为空");
        SysUserEntity sysUserEntity = (SysUserEntity) SecurityUtils.getSubject().getPrincipal();
        String username = sysUserEntity.getUsername();
        oprAdvService.updateHelpTitle(oprHelpTitle, username, CommonUtil.getIpAddress(request));
        return R.ok();
    }

//    @SysLog(module = "帮助中心",methodText = "是否启用标题")
//    @PostMapping("/enableTitle")
//    @RequiresPermissions("operate:oprhelp:enableTitle")
//    @ApiOperation(value="修改", notes="修改")
//    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
//    public R enableCategory(@RequestBody OprHelpTitle oprHelpTitle, HttpServletRequest request) {
//        Assert.isNull(oprHelpTitle, "不能为空");
//        SysUserEntity sysUserEntity = (SysUserEntity) SecurityUtils.getSubject().getPrincipal();
//        String username = sysUserEntity.getUsername();
//        oprAdvService.enableTitle(oprHelpTitle, username, CommonUtil.getIpAddress(request));
//        return R.ok();
//    }

    @SysLog(module = "帮助中心",methodText = "删除标题")
    @PostMapping("/deleteTitle")
    @RequiresPermissions("operate:oprhelp:deleteTitle")
    @ApiOperation(value="删除", notes="删除")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R deleteTitle(@RequestBody OprHelpTitle oprHelpTitle, HttpServletRequest request) {
        Assert.isNull(oprHelpTitle, "不能为空");
        SysUserEntity sysUserEntity = (SysUserEntity) SecurityUtils.getSubject().getPrincipal();
        String username = sysUserEntity.getUsername();
        oprAdvService.deleteTitle(oprHelpTitle, username, CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @SysLog(module = "帮助中心",methodText = "分类下拉框查询")
    @GetMapping("/findCategory")
    @RequiresPermissions("operate:oprhelp:categorylist")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findCategory(@ModelAttribute OprHelpCategory oprHelpCategory) throws IOException {
        return R.ok().put("list", oprAdvService.findCategory(oprHelpCategory));
    }


    @SysLog(module = "帮助中心",methodText = "标题内容列表查询")
    @GetMapping("/helpContentList")
    @RequiresPermissions("operate:oprhelp:helpContentList")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R helpContentList(@ModelAttribute OprHelpContent oprHelpContent, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize, @RequestParam(value="orderBy",required=false) String orderBy) throws IOException {
        return R.ok().put("page", oprAdvService.queryContentListPage(oprHelpContent,pageNo,pageSize,orderBy));
    }

    @SysLog(module = "帮助中心",methodText = "标题下拉框查询")
    @GetMapping("/findTitle")
    @RequiresPermissions("operate:oprhelp:titleList")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findCategory(@ModelAttribute OprHelpTitle oprHelpTitle) throws IOException {
        return R.ok().put("list", oprAdvService.findTitle(oprHelpTitle));
    }

    @GetMapping("/contentInfo/{id}")
    @RequiresPermissions("operate:oprhelp:contentInfo")
    @ApiOperation(value = "信息", notes = "信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header") })
    public R contentInfo(@PathVariable("id") Integer id) {
        OprHelpContent oprHelpContent = oprAdvService.queryContetInfo(id);
        return R.ok().put("oprHelpContent", oprHelpContent);
    }

    @SysLog(module = "帮助中心",methodText = "新增内容")
    @PostMapping("/saveHelpContent")
    @RequiresPermissions("operate:oprhelp:save")
    @ApiOperation(value="保存", notes="保存")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R saveHelpContent(@RequestBody OprHelpContent oprHelpContent, HttpServletRequest request) {
        Assert.isNull(oprHelpContent, "不能为空");
        SysUserEntity sysUserEntity = (SysUserEntity) SecurityUtils.getSubject().getPrincipal();
        String username = sysUserEntity.getUsername();
        oprAdvService.saveHelpContent(oprHelpContent,username, CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @SysLog(module = "帮助中心",methodText = "更新内容")
    @PostMapping("/updateHelpContent")
    @RequiresPermissions("operate:oprhelp:update")
    @ApiOperation(value="修改", notes="修改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateHelpContent(@RequestBody OprHelpContent oprHelpContent, HttpServletRequest request) {
        Assert.isNull(oprHelpContent, "不能为空");
        SysUserEntity sysUserEntity = (SysUserEntity) SecurityUtils.getSubject().getPrincipal();
        String username = sysUserEntity.getUsername();
        oprAdvService.updateHelpContent(oprHelpContent, username, CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @SysLog(module = "帮助中心",methodText = "删除内容")
    @PostMapping("/deleteContent")
    @RequiresPermissions("operate:oprhelp:deleteContent")
    @ApiOperation(value="删除", notes="删除")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R deleteContent(@RequestBody OprHelpContent oprHelpContent, HttpServletRequest request) {
        Assert.isNull(oprHelpContent, "不能为空");
        SysUserEntity sysUserEntity = (SysUserEntity) SecurityUtils.getSubject().getPrincipal();
        String username = sysUserEntity.getUsername();
        oprAdvService.deleteContent(oprHelpContent, username, CommonUtil.getIpAddress(request));
        return R.ok();
    }

}
