package com.wsdy.saasops.modules.agent.controller;

import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.agent.entity.AgentContract;
import com.wsdy.saasops.modules.agent.service.AgentContractService;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;


@RestController
@RequestMapping("/bkapi/agent/contract")
@Api(tags = "契约-代理2.0")
public class AgentContractController extends AbstractController {

    @Autowired
    private AgentContractService contractService;

    @GetMapping("allContractList")
    @ApiOperation(value = "契约查询所有", notes = "契约查询所有")
    public R allContractList() {
        return R.ok().put(contractService.allContractList());
    }


    @GetMapping("contractList")
    @RequiresPermissions("agent:contract:list")
    @ApiOperation(value = "契约查询", notes = "契约查询")
    public R contractList(@RequestParam("pageNo") @NotNull Integer pageNo,
                          @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(contractService.contractList(pageNo, pageSize));
    }

    @PostMapping("addContract")
    @RequiresPermissions("agent:contract:add")
    @ApiOperation(value = "契约新增", notes = "契约新增")
    public R addContract(@RequestBody AgentContract contract) {
        Assert.isBlank(contract.getContractName(), "名称不能为空");
        Assert.isBlank(contract.getRule(), "规则不能为空");
        contract.setCreateUser(getUser().getUsername());
        contractService.addContract(contract);
        return R.ok();
    }

    @GetMapping("contractInfo")
    @RequiresPermissions("agent:contract:info")
    @ApiOperation(value = "契约单个查询", notes = "契约单个查询")
    public R contractInfo(@ModelAttribute AgentContract contract) {
        Assert.isNull(contract.getId(), "ID不能为空");
        return R.ok().put(contractService.contractInfo(contract));
    }

    @PostMapping("updateContract")
    @RequiresPermissions("agent:contract:update")
    @ApiOperation(value = "契约修改", notes = "契约修改")
    public R updateContract(@RequestBody AgentContract contract) {
        Assert.isNull(contract.getId(), "ID不能为空");
        contract.setModifyUser(getUser().getUsername());
        contractService.updateContract(contract);
        return R.ok();
    }
}
