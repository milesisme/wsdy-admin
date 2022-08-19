package com.wsdy.saasops.modules.agent.controller;

import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.common.annotation.SysLog;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.agent.entity.AgentDeposit;
import com.wsdy.saasops.modules.agent.service.AgentDepositService;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/bkapi/agent/deposit")
@Api(tags = "线上入款,公司入款")
public class AgentDepositController extends AbstractController {

    @Autowired
    private AgentDepositService agentDepositService;
    @Autowired
    private RedisService redisService;

    @GetMapping("/depositList")
    @RequiresPermissions("agent:onLine:list")
    @ApiOperation(value = "会员入款查询列表", notes = "会员入款查询列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R depositList(@ModelAttribute AgentDeposit fundDeposit, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(agentDepositService.queryListPage(fundDeposit, pageNo, pageSize));
    }

    @GetMapping("/sumDepositAmount")
    @ApiOperation(value = "线上（公司）入款今日存款", notes = "线上（公司）入款今日存款")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R sumDepositAmount(@ModelAttribute AgentDeposit agentDeposit, @RequestParam("make") Integer make) {
        agentDeposit.setMark(make);
        agentDeposit.setLoginSysUserName(getUser().getUsername());
        return R.ok().put("sum", agentDepositService.findSumDepositAmount(agentDeposit));
    }

    @GetMapping("/info/{id}")
    @RequiresPermissions("agent:onLine:info")
    @ApiOperation(value = "线上入款查询(根据ID)", notes = "线上入款查询(根据ID)")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R info(@PathVariable("id") Integer id) {
        return R.ok().put("agentDeposit", agentDepositService.queryObject(id));
    }

    @PostMapping("/updateStatus")
    @RequiresPermissions(value = {"agent:onLine:update", "agent:onLine:updateStatus"}, logical = Logical.OR)
    @SysLog(module = "入款状态审核", methodText = "入款状态审核")
    @ApiOperation(value = "入款状态审核", notes = "入款状态审核")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R onLineUpdate(@RequestBody AgentDeposit agentDeposit, HttpServletRequest request) {
        Assert.isNull(agentDeposit.getId(), "id不能为空");
        Assert.isNull(agentDeposit.getStatus(), "状态不能为空");
        String key = RedisConstants.AGENT_DEPOSiT_AUDIT + CommonUtil.getSiteCode() + agentDeposit.getId();
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, agentDeposit.getId(), 200, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(isExpired)) {
            throw new R200Exception("任务处理中，请勿重复点击！");
        }
        try {
            AgentDeposit agentDeposit1 = agentDepositService.updateDeposit(agentDeposit, getUser().getUsername(), CommonUtil.getIpAddress(request));
            //agentDepositService.accountDepositMsg(agentDeposit1, CommonUtil.getSiteCode());
            return R.ok();
        } finally {
            redisService.del(key);
        }
    }

    @PostMapping("/updateStatusRefuse")
    @RequiresPermissions(value = {"agent:onLine:update", "agent:onLine:updateStatus"}, logical = Logical.OR)
    @SysLog(module = "入款状态审核-拒绝", methodText = "入款状态审核-拒绝")
    @ApiOperation(value = "入款状态审核-拒绝", notes = "入款状态审核-拒绝")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateStatusRefuse(@RequestBody AgentDeposit agentDeposit, HttpServletRequest request) {
        Assert.isNull(agentDeposit.getId(), "id不能为空");
        Assert.isNull(agentDeposit.getStatus(), "状态不能为空");
        if (!(Integer.valueOf(Constants.EVNumber.zero).equals(agentDeposit.getStatus()))) {
            throw new R200Exception("status值错误");
        }
        String key = RedisConstants.AGENT_DEPOSiT_AUDIT + CommonUtil.getSiteCode() + agentDeposit.getId();
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, agentDeposit.getId(), 200, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(isExpired)) {
            throw new R200Exception("任务处理中，请勿重复点击！");
        }
        try {
            AgentDeposit agentDeposit1 = agentDepositService.updateDeposit(agentDeposit, getUser().getUsername(), CommonUtil.getIpAddress(request));
            // agentDepositService.accountDepositMsg(fundDeposit, CommonUtil.getSiteCode());
            return R.ok();
        } finally {
            redisService.del(key);
        }
    }


    @GetMapping("/depositSumStatistic")
    @RequiresPermissions("agent:onLine:list")
    @ApiOperation(value = "入款查询列表-合计统计", notes = "入款查询列表-合计统计")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            ,@ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    @SysLog(module = "会员取款", methodText = "会员提款查询列表-合计统计")
    public R depositSumStatistic(@ModelAttribute AgentDeposit fundDeposit) {
        return R.ok().putPage(agentDepositService.depositSumStatistic(fundDeposit));
    }
}
