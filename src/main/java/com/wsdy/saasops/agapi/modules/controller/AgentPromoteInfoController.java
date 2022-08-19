package com.wsdy.saasops.agapi.modules.controller;

import com.wsdy.saasops.agapi.annotation.AgentLogin;
import com.wsdy.saasops.agapi.modules.service.AgentPromoteInfoService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.entity.AgentMaterial;
import com.wsdy.saasops.modules.agent.entity.AgentMaterialDetail;
import com.wsdy.saasops.modules.agent.service.AgentMaterialService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;


@RestController
@Slf4j
@RequestMapping("/agapi/n2")
@Api(tags = "推广链接及推广素材")
public class AgentPromoteInfoController {

    @Autowired
    private AgentPromoteInfoService promoteInfoService;

    @Autowired
    private AgentMaterialService agentMaterialService;

    @AgentLogin
    @GetMapping("sponsoredLinks")
    @ApiOperation(value = "推广链接", notes = "推广链接")
    public R sponsoredLinks(
            @RequestParam("pageNo") @NotNull Integer pageNo,
            @RequestParam("pageSize") @NotNull Integer pageSize,
            HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        return R.ok().put(promoteInfoService.sponsoredLinks(pageNo, pageSize, account));
    }

    @AgentLogin
    @GetMapping("promotionMaterials")
    @ApiOperation(value = "推广素材", notes = "推广素材")
    public R promotionMaterials(@RequestParam("pageNo") @NotNull Integer pageNo,
                                @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().put(promoteInfoService.promotionMaterials(pageNo, pageSize));
    }
    
    @AgentLogin
    @GetMapping("materialList")
    @ApiOperation(value = "推广素材文件夹查询", notes = "推广素材文件夹查询")
    public R materialList(@ModelAttribute AgentMaterial material,
                          @RequestParam("pageNo") @NotNull Integer pageNo,
                          @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(agentMaterialService.materialList(material, pageNo, pageSize));
    }

    @AgentLogin
    @GetMapping("materialDetailList")
    @ApiOperation(value = "推广素材文件明细查询", notes = "推广素材文件明细查询")
    public R materialDetailList(@ModelAttribute AgentMaterialDetail material,
                                @RequestParam("pageNo") @NotNull Integer pageNo,
                                @RequestParam("pageSize") @NotNull Integer pageSize) {
        Assert.isNull(material.getMaterialId(), "materialId不能为空");
        return R.ok().putPage(agentMaterialService.materialDetailList(material, pageNo, pageSize));
    }
}
