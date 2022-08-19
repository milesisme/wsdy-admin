package com.wsdy.saasops.modules.agent.controller;

import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.agent.entity.AgentAudit;
import com.wsdy.saasops.modules.agent.service.AgentAuditService;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/bkapi/agent/audit")
@Api(tags = "总代人工调整")
public class AgentAuditController extends AbstractController {

    @Autowired
    private AgentAuditService agentAuditService;
    @Autowired
    private RedisService redisService;

    @PostMapping("/auditAdd")
    @RequiresPermissions(value = {"agent:audit:add", "agent:audit:mbradd"}, logical = Logical.OR)
    @ApiOperation(value = "报表新增", notes = "报表新增")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R auditAdd(@RequestBody AgentAudit agentAudit, HttpServletRequest request) {
        Assert.isNull(agentAudit.getAgyAccount(), "代理不能为空");
        Assert.isNull(agentAudit.getWalletType(), "钱包类型不能为空");
        Assert.isBlank(agentAudit.getFinancialCode(), "FinancialCode不能为空");
        Assert.isNumeric(agentAudit.getAmount(), "调整金额只能为数字,并且长度不能大于12位!", 12);
        Assert.isLenght(agentAudit.getMemo(), "备注长度为1-100!", 1, 100);
        agentAudit.setCreateUser(getUser().getUsername());
        agentAudit.setModifyUser(getUser().getUsername());
        agentAuditService.auditSave(agentAudit);
        return R.ok();
    }

    @PostMapping("/auditReduce")
    @RequiresPermissions(value = {"agent:audit:reduce", "agent:audit:mbrreduce"}, logical = Logical.OR)
    @ApiOperation(value = "报表减少", notes = "报表减少")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R auditReduce(@RequestBody AgentAudit fundAudit, HttpServletRequest request) {
        Assert.isNull(fundAudit.getAgyAccount(), "代理不能为空");
        Assert.isBlank(fundAudit.getFinancialCode(), "FinancialCode不能为空");
        Assert.isNumeric(fundAudit.getAmount(), "调整金额只能为数字,并且长度不能大于12位!", 12);
        Assert.isLenght(fundAudit.getMemo(), "备注长度为1-100!", 1, 100);
        Assert.isNull(fundAudit.getWalletType(), "钱包类型不能为空");
        fundAudit.setCreateUser(getUser().getUsername());
        fundAudit.setModifyUser(getUser().getUsername());
        agentAuditService.auditSave(fundAudit);
        return R.ok();
    }

    @PostMapping("auditUpdateStatus")
    @RequiresPermissions("agent:audit:update")
    @ApiOperation(value = "调整报表修改状态", notes = "调整报表修改状态")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R auditUpdateStatus(@RequestBody AgentAudit fundAudit, HttpServletRequest request) {
        Assert.isNull(fundAudit.getId(), "id不能为空");
        Assert.isNull(fundAudit.getStatus(), "状态不能为空");
        Integer fundAuditId = fundAudit.getId();
        String key = RedisConstants.AGENT_AUDIT_UPDATE + CommonUtil.getSiteCode() + fundAuditId;
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, fundAuditId, 200, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(isExpired)) {
            throw new R200Exception("任务处理中，请勿重复点击！");
        }
        try {
            fundAudit.setModifyUser(getUser().getUsername());
            agentAuditService.auditUpdateStatus(fundAudit, CommonUtil.getSiteCode(), CommonUtil.getIpAddress(request));
            return R.ok();
        } finally {
            redisService.del(key);
        }
    }

    @GetMapping("/auditList")
    @RequiresPermissions("agent:audit:list")
    @ApiOperation(value = "代理调整报表查询列表", notes = "代理调整报表查询列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R auditList(@ModelAttribute AgentAudit fundAudit, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(agentAuditService.auditList(fundAudit, pageNo, pageSize));
    }
}
