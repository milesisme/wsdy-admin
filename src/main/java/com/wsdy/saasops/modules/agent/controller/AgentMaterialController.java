package com.wsdy.saasops.modules.agent.controller;

import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.agent.entity.AgentMaterial;
import com.wsdy.saasops.modules.agent.entity.AgentMaterialDetail;
import com.wsdy.saasops.modules.agent.service.AgentMaterialService;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;


@RestController
@RequestMapping("/bkapi/agent/material")
@Api(tags = "素材-代理2.0")
public class AgentMaterialController extends AbstractController {

    @Autowired
    private AgentMaterialService agentMaterialService;

    @GetMapping("materialList")
    @RequiresPermissions("agent:materia:list")
    @ApiOperation(value = "推广素材文件夹查询", notes = "推广素材文件夹查询")
    public R materialList(@ModelAttribute AgentMaterial material,
                          @RequestParam("pageNo") @NotNull Integer pageNo,
                          @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(agentMaterialService.materialList(material, pageNo, pageSize));
    }

    @PostMapping("addMaterial")
    @RequiresPermissions("agent:materia:add")
    @ApiOperation(value = "推广素材文件夹新增", notes = "推广素材文件夹新增")
    public R addMaterial(@RequestBody AgentMaterial material) {
        Assert.isBlank(material.getName(), "名称不能为空");
        material.setCreateUser(getUser().getUsername());
        material.setModifyUser(getUser().getUsername());
        agentMaterialService.addMaterial(material);
        return R.ok();
    }

    @PostMapping("updateMaterial")
    @RequiresPermissions("agent:materia:update")
    @ApiOperation(value = "推广素材文件夹修改", notes = "推广素材文件夹修改")
    public R updateMaterial(@RequestBody AgentMaterial material) {
        Assert.isNull(material.getId(), "ID不能为空");
        Assert.isBlank(material.getName(), "名称不能为空");
        material.setModifyUser(getUser().getUsername());
        agentMaterialService.updateMaterial(material);
        return R.ok();
    }

    @PostMapping("deleteMaterial")
    @RequiresPermissions("agent:materia:delete")
    @ApiOperation(value = "推广素材文件夹删除", notes = "推广素材文件夹删除")
    public R deleteMaterial(@RequestBody AgentMaterial material) {
        if (material.getIds().size() == 0) {
            throw new R200Exception("ids不能为空");
        }
        agentMaterialService.deleteMaterial(material);
        return R.ok();
    }


    @GetMapping("materialDetailList")
    @RequiresPermissions("agent:materiadetail:list")
    @ApiOperation(value = "推广素材文件明细查询", notes = "推广素材文件明细查询")
    public R materialDetailList(@ModelAttribute AgentMaterialDetail material,
                                @RequestParam("pageNo") @NotNull Integer pageNo,
                                @RequestParam("pageSize") @NotNull Integer pageSize) {
        Assert.isNull(material.getMaterialId(), "materialId不能为空");
        return R.ok().putPage(agentMaterialService.materialDetailList(material, pageNo, pageSize));
    }

    @PostMapping("addMaterialDetail")
    @RequiresPermissions("agent:materiadetail:add")
    @ApiOperation(value = "推广素材文件明细新增", notes = "推广素材文件明细新增")
    public R addMaterialDetail(@ModelAttribute AgentMaterialDetail detail,
                               @RequestParam(value = "uploadFile") MultipartFile uploadFile) {
        Assert.isNull(detail.getMaterialId(), "materialId不能为空");
        Assert.isBlank(detail.getName(), "名称不能为空");
        Assert.isNull(uploadFile, "图片不能为空");
        detail.setCreateUser(getUser().getUsername());
        detail.setModifyUser(getUser().getUsername());
        agentMaterialService.addMaterialDetail(detail, uploadFile);
        return R.ok();
    }

    @PostMapping("deleteMaterialDetail")
    @RequiresPermissions("agent:materiadetail:delete")
    @ApiOperation(value = "推广素材文件明细删除", notes = "推广素材文件夹删除")
    public R deleteMaterialDetail(@RequestBody AgentMaterialDetail detail) {
        if (detail.getIds().size() == 0) {
            throw new R200Exception("ids不能为空");
        }
        agentMaterialService.deleteMaterialDetail(detail);
        return R.ok();
    }
}
