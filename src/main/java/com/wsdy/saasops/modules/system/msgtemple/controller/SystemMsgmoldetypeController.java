package com.wsdy.saasops.modules.system.msgtemple.controller;

import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.system.msgtemple.entity.SystemMsgmoldetype;
import com.wsdy.saasops.modules.system.msgtemple.service.SystemMsgmoldetypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/bkapi/msgtemple/systemmsgmoldetype")
@Api(value = "SystemMsgmoldetype", tags = "")
public class SystemMsgmoldetypeController {
    @Autowired
    private SystemMsgmoldetypeService systemMsgmoldetypeService;

    /**
     * 列表
     */
    @GetMapping("/list")
    @RequiresPermissions("msgtemple:systemmsgmoldetype:list")
    public R list(@ModelAttribute SystemMsgmoldetype systemMsgmoldetype, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize,String orderBy) {
        return R.ok().put("page", systemMsgmoldetypeService.queryListPage(systemMsgmoldetype,pageNo,pageSize,orderBy));
    }
    /**
     * 全部信息
     */
    @GetMapping("/listAll")
    @ResponseBody
    public R list() {
        return R.ok().put("list", systemMsgmoldetypeService.queryListAll());
    }


    /**
     * 信息
     */
    @GetMapping("/info/{id}")
    @RequiresPermissions("msgtemple:systemmsgmoldetype:info")
    @ApiOperation(value="信息", notes="信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R info(@PathVariable("id") Integer id) {
        SystemMsgmoldetype systemMsgmoldetype =systemMsgmoldetypeService.queryObject(id);
        return R.ok().put("systemMsgmoldetype", systemMsgmoldetype);
    }
    /**
     * 信息
     */
    @GetMapping("/formatter/{id}")
    @ApiOperation(value="格式化数据", notes="格式化数据")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R formatter(@PathVariable("id") Integer id) {
        SystemMsgmoldetype systemMsgmoldetype =systemMsgmoldetypeService.queryObject(id);
        return R.ok().put("objName", systemMsgmoldetype.getName());
    }

    /**
     * 保存
     */
    @PostMapping("/save")
    @RequiresPermissions("msgtemple:systemmsgmoldetype:save")
    @ApiOperation(value="保存", notes="保存")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R save(@ModelAttribute SystemMsgmoldetype systemMsgmoldetype) {
            systemMsgmoldetypeService.save(systemMsgmoldetype);

        return R.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @RequiresPermissions("msgtemple:systemmsgmoldetype:update")
    @ApiOperation(value="修改", notes="修改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R update(@RequestBody SystemMsgmoldetype systemMsgmoldetype) {
            systemMsgmoldetypeService.update(systemMsgmoldetype);

        return R.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @RequiresPermissions("msgtemple:systemmsgmoldetype:delete")
    @ApiOperation(value="删除", notes="删除")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R delete(@RequestBody Integer[]ids) {
            systemMsgmoldetypeService.deleteBatch(ids);

        return R.ok();
    }

}
