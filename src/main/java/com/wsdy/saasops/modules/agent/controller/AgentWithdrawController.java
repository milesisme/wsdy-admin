package com.wsdy.saasops.modules.agent.controller;

import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.common.annotation.SysLog;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.listener.BizEvent;
import com.wsdy.saasops.modules.agent.entity.AgyWithdraw;
import com.wsdy.saasops.modules.agent.service.AgentWithdrawService;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.fund.entity.AccWithdraw;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/bkapi/agent/withdraw")
@Api(tags = "代理提款列表")
public class AgentWithdrawController extends AbstractController {

    @Autowired
    private AgentWithdrawService fundWithdrawService;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private RedisService redisService;


    @GetMapping("/accList")
    @RequiresPermissions("agent:accWithdraw:list")
    @ApiOperation(value = "会员提款查询列表", notes = "会员提款查询列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R accList(@ModelAttribute AgyWithdraw accWithdraw, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        accWithdraw.setLoginSysUserName(getUser().getUsername());
        return R.ok().putPage(fundWithdrawService.queryAccListPage(accWithdraw, pageNo, pageSize));
    }

    @GetMapping("/accSumDrawingAmount")
    @ApiOperation(value = "会员提款今日取款", notes = "会员提款今日取款")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R accSumDrawingAmount() {
        return R.ok().put("sum", fundWithdrawService.accSumDrawingAmount(getUser().getUsername()));
    }

    @GetMapping("/accInfo/{id}")
    @RequiresPermissions("agent:accWithdraw:info")
    @ApiOperation(value = "会员提款查询(根据ID查询)", notes = "会员提款查询(根据ID查询)")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R accInfo(@PathVariable("id") Integer id) {
        return R.ok().put("accWithdraw", fundWithdrawService.queryAccObject(id));
    }

    @PostMapping("/updateAccStatusFinial")
    @RequiresPermissions("agent:accWithdraw:FinialUpdate")
    @SysLog(module = "会员提款复审-财务", methodText = "会员提款复审-财务")
    @ApiOperation(value = "会员提款修改(审核)状态", notes = "会员提款修改(审核)状态")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateAccStatusFinial(@RequestBody AgyWithdraw accWithdraw, HttpServletRequest request) {
        Assert.isNull(accWithdraw.getId(), "id不能为空");
        Assert.isNull(accWithdraw.getStatus(), "状态不能为空");
        //BizEvent bizEvent = new BizEvent(this, CommonUtil.getSiteCode(), null, null);
        fundWithdrawService.updateAccStatus(accWithdraw, getUser().getUsername(), null, CommonUtil.getIpAddress(request));
        /*if (Objects.nonNull(bizEvent.getEventType())) {
            applicationEventPublisher.publishEvent(bizEvent);
        }*/
        return R.ok();
    }

    @PostMapping("/updateAccStatusFinialRefuse")
    @RequiresPermissions("agent:accWithdraw:FinialUpdate")
    @SysLog(module = "会员提款复审-财务-拒绝", methodText = "会员提款复审-财务-拒绝")
    @ApiOperation(value = "会员提款修改(审核)状态-拒绝", notes = "会员提款修改(审核)状态-拒绝")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateAccStatusFinialRefuse(@RequestBody AgyWithdraw accWithdraw, HttpServletRequest request) {
        Assert.isNull(accWithdraw.getId(), "id不能为空");
        Assert.isNull(accWithdraw.getStatus(), "状态不能为空");
        if (!(Integer.valueOf(Constants.EVNumber.zero).equals(accWithdraw.getStatus()))) {
            throw new R200Exception("status值错误");
        }
        //BizEvent bizEvent = new BizEvent(this, CommonUtil.getSiteCode(), null, null);
        fundWithdrawService.updateAccStatus(accWithdraw, getUser().getUsername(), null, CommonUtil.getIpAddress(request));
        /*if (Objects.nonNull(bizEvent.getEventType())) {
            applicationEventPublisher.publishEvent(bizEvent);
        }*/
        return R.ok();
    }

    @PostMapping("/updateAccStatus")
    @RequiresPermissions("agent:accWithdraw:update")
    @SysLog(module = "会员提款-初审", methodText = "会员提款审核初审")
    @ApiOperation(value = "会员提款修改(审核)状态初审", notes = "会员提款修改(审核)状态初审")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateAccStatus(@RequestBody AgyWithdraw accWithdraw, HttpServletRequest request) {
        Assert.isNull(accWithdraw.getId(), "id不能为空");
        Assert.isNull(accWithdraw.getStatus(), "状态不能为空");
        // BizEvent bizEvent = new BizEvent(this, CommonUtil.getSiteCode(), null, null);
        fundWithdrawService.checkoutStatusByTwo(accWithdraw.getId());
        fundWithdrawService.updateAccStatus(accWithdraw, getUser().getUsername(), null, CommonUtil.getIpAddress(request));
       /* if (Objects.nonNull(bizEvent.getEventType())) {
            applicationEventPublisher.publishEvent(bizEvent);
        }*/
        return R.ok();
    }

    @PostMapping("/updateAccStatusRefuse")
    @RequiresPermissions("agent:accWithdraw:update")
    @SysLog(module = "会员提款-初审-拒绝", methodText = "会员提款审核初审-拒绝")
    @ApiOperation(value = "会员提款修改(审核)状态初审", notes = "会员提款修改(审核)状态初审")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateAccStatusRefuse(@RequestBody AgyWithdraw accWithdraw, HttpServletRequest request) {
        Assert.isNull(accWithdraw.getId(), "id不能为空");
        Assert.isNull(accWithdraw.getStatus(), "状态不能为空");
        if (!(Integer.valueOf(Constants.EVNumber.zero).equals(accWithdraw.getStatus()))) {
            throw new R200Exception("status值错误");
        }
        //  BizEvent bizEvent = new BizEvent(this, CommonUtil.getSiteCode(), null, null);
        fundWithdrawService.checkoutStatusByTwo(accWithdraw.getId());
        fundWithdrawService.updateAccStatus(accWithdraw, getUser().getUsername(), null, CommonUtil.getIpAddress(request));
   /*     if (Objects.nonNull(bizEvent.getEventType())) {
            applicationEventPublisher.publishEvent(bizEvent);
        }*/
        return R.ok();
    }


    @PostMapping("/updateAccMemo")
    @RequiresPermissions("agent:accWithdraw:update")
    @SysLog(module = "会员提款", methodText = "会员提款修改备注")
    @ApiOperation(value = "会员提款修改备注", notes = "会员提款修改备注")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateAccMemo(@RequestBody AccWithdraw accWithdraw) {
        Assert.isNull(accWithdraw.getId(), "id不能为空");
        fundWithdrawService.updateAccMemo(accWithdraw.getId(), accWithdraw.getMemo(), getUser().getUsername());
        return R.ok();
    }

    @GetMapping("/withdrawCountByStatus")
    @RequiresPermissions("agent:accWithdraw:list")
    @ApiOperation(value = "会员提款统计", notes = "根据状态统计会员提款")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R withdrawCountByStatus(@ModelAttribute AgyWithdraw accWithdraw) {
        return R.ok(fundWithdrawService.withdrawCountByStatus(accWithdraw));
    }


    @GetMapping("/accSumStatistics")
    @RequiresPermissions("agent:accWithdraw:list")
    @ApiOperation(value = "会员提款查询列表-合计统计", notes = "会员提款查询列表-合计统计")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    @SysLog(module = "会员取款", methodText = "会员提款查询列表-合计统计")
    public R accSumStatistics(@ModelAttribute AgyWithdraw accWithdraw) {
        return R.ok().putPage(fundWithdrawService.accSumStatistics(accWithdraw));
    }

    @GetMapping("/lockStatus")
    @ApiOperation(value = "操作锁定--状态查询", notes = "操作锁定--状态查询审")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R lockStatus(@ModelAttribute AgyWithdraw accWithdraw) {
        Assert.isNull(accWithdraw.getId(), "id不能为空");

        String redisKey = RedisConstants.AGENT_UPDATE_WITHDRAW_LOCK + CommonUtil.getSiteCode() + accWithdraw.getId();
        boolean flag = redisService.setRedisExpiredTimeBo(redisKey, accWithdraw.getId(), 3, TimeUnit.SECONDS);
        AgyWithdraw acc;
        if (flag) {
            try {
                acc = fundWithdrawService.lockstatus(accWithdraw, getUser().getUsername());
            } finally {
                redisService.del(redisKey);
            }
        } else {
            return R.error("正在处理中，请稍后重试！");
        }

        return R.ok(acc);
    }

    @PostMapping("/lock")
    @ApiOperation(value = "操作锁定--锁定", notes = "操作锁定--锁定")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R lock(@RequestBody AgyWithdraw accWithdraw, HttpServletRequest request) {
        Assert.isNull(accWithdraw.getId(), "id不能为空");
        Assert.isNull(accWithdraw.getLockStatus(), "id不能为空");

        String redisKey = RedisConstants.AGENT_UPDATE_WITHDRAW_LOCK + CommonUtil.getSiteCode() + accWithdraw.getId();
        boolean flag = redisService.setRedisExpiredTimeBo(redisKey, accWithdraw.getId(), 3, TimeUnit.SECONDS);
        if (flag) {
            try {
                fundWithdrawService.lock(accWithdraw, getUser().getUsername());
            } finally {
                redisService.del(redisKey);
            }
        } else {
            return R.error("正在处理中，请稍后重试！");
        }

        return R.ok();
    }

    @PostMapping("/unLock")
    @ApiOperation(value = "操作锁定--解锁", notes = "操作锁定--解锁")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R unLock(@RequestBody AgyWithdraw accWithdraw) {
        Assert.isNull(accWithdraw.getId(), "id不能为空");
        Assert.isNull(accWithdraw.getLockStatus(), "id不能为空");

        String redisKey = RedisConstants.AGENT_UPDATE_WITHDRAW_LOCK + CommonUtil.getSiteCode() + accWithdraw.getId();
        boolean flag = redisService.setRedisExpiredTimeBo(redisKey, accWithdraw.getId(), 3, TimeUnit.SECONDS);
        if (flag) {
            try {
                fundWithdrawService.unLock(accWithdraw);
            } finally {
                redisService.del(redisKey);
            }
        } else {
            return R.error("正在处理中，请稍后重试！");
        }
        return R.ok();
    }

    @GetMapping("/updateAllLockStatus")
    @ApiOperation(value = "操作锁定--更新锁定状态", notes = "操作锁定--更新锁定状态")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateAllLockStatus(@ModelAttribute AgyWithdraw accWithdraw) {
        fundWithdrawService.updateAllLockStatus();
        return R.ok();
    }
}
