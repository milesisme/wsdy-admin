package com.wsdy.saasops.modules.fund.controller;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wsdy.saasops.api.annotation.Login;
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
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.fund.entity.AccWithdraw;
import com.wsdy.saasops.modules.fund.service.AgentTypeService;
import com.wsdy.saasops.modules.fund.service.FundWithdrawService;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import com.wsdy.saasops.modules.sys.service.SysFileExportRecordService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/bkapi/fund/withdraw")
@Api(tags = "会员提款,代理提款")
public class FundWithdrawController extends AbstractController {

    @Autowired
    private FundWithdrawService fundWithdrawService;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private AgentTypeService agentTypeService;
    @Autowired
    private SysSettingService sysSettingService;

    @Value("${fund.accWithdraw.excel.path}")
    private String accExcelPath;
    private final String module = "accWithdraw";

    @GetMapping("/accList")
    @RequiresPermissions("fund:accWithdraw:list")
    @ApiOperation(value = "会员提款查询列表", notes = "会员提款查询列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R accList(@ModelAttribute AccWithdraw accWithdraw, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        accWithdraw.setLoginSysUserName(getUser().getUsername());
        accWithdraw.setAgyAccountIds(agentTypeService.checkAgentType(accWithdraw.getAgyAccountIds()));
        Integer time = sysSettingService.queryCuiDanSet();
        return R.ok().putPage(fundWithdrawService.queryAccListPage(accWithdraw, pageNo, pageSize, Constants.EVNumber.one)).put("time", time);
    }

    @GetMapping("/accSumDrawingAmount")
    @ApiOperation(value = "会员提款今日取款", notes = "会员提款今日取款")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R accSumDrawingAmount() {
        return R.ok().put("sum", fundWithdrawService.accSumDrawingAmount(getUser().getUsername()));
    }

    @GetMapping("/accInfo/{id}")
    @RequiresPermissions("fund:accWithdraw:info")
    @ApiOperation(value = "会员提款查询(根据ID查询)", notes = "会员提款查询(根据ID查询)")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R accInfo(@PathVariable("id") Integer id) {
        return R.ok().put("accWithdraw", fundWithdrawService.queryAccObject(id, null, getUser().getUsername()));
    }
    
    @GetMapping("/accInfoByOrderno/{orderNo}")
    @RequiresPermissions("fund:accWithdraw:info")
    @ApiOperation(value = "会员提款查询(根据订单号查询)", notes = "会员提款查询(根据订单号查询)")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R accInfo(@PathVariable("orderNo") String orderNo) {
    	return R.ok().put("accWithdraw", fundWithdrawService.queryAccObject(null, orderNo, getUser().getUsername()));
    }

    @PostMapping("/updateAccStatusFinial")
    @RequiresPermissions("fund:accWithdraw:FinialUpdate")
    @SysLog(module = "会员提款复审-财务", methodText = "会员提款复审-财务")
    @ApiOperation(value = "会员提款修改(审核)状态", notes = "会员提款修改(审核)状态")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateAccStatusFinial(@RequestBody AccWithdraw accWithdraw, HttpServletRequest request) {
        Assert.isNull(accWithdraw.getId(), "id不能为空");
        Assert.isNull(accWithdraw.getStatus(), "状态不能为空");
        // 初始化事件
        BizEvent bizEvent = new BizEvent(this, CommonUtil.getSiteCode(), null, null);
        fundWithdrawService.updateAccStatus(accWithdraw, getUser().getUsername(), bizEvent, CommonUtil.getIpAddress(request));
        if (Objects.nonNull(bizEvent.getEventType())) {
            applicationEventPublisher.publishEvent(bizEvent);
        }
        return R.ok();
    }

    @PostMapping("/updateAccStatusFinialRefuse")
    @RequiresPermissions("fund:accWithdraw:FinialUpdate")
    @SysLog(module = "会员提款复审-财务-拒绝", methodText = "会员提款复审-财务-拒绝")
    @ApiOperation(value = "会员提款修改(审核)状态-拒绝", notes = "会员提款修改(审核)状态-拒绝")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateAccStatusFinialRefuse(@RequestBody AccWithdraw accWithdraw, HttpServletRequest request) {
        Assert.isNull(accWithdraw.getId(), "id不能为空");
        Assert.isNull(accWithdraw.getStatus(), "状态不能为空");
        if(!(Integer.valueOf(Constants.EVNumber.zero).equals(accWithdraw.getStatus()))){
            throw new R200Exception("status值错误");
        }
        BizEvent bizEvent = new BizEvent(this, CommonUtil.getSiteCode(), null, null);
        fundWithdrawService.updateAccStatus(accWithdraw, getUser().getUsername(), bizEvent, CommonUtil.getIpAddress(request));
        if (Objects.nonNull(bizEvent.getEventType())) {
            applicationEventPublisher.publishEvent(bizEvent);
        }
        return R.ok();
    }

    @PostMapping("/updateAccStatus")
    @RequiresPermissions("fund:accWithdraw:update")
    @SysLog(module = "会员提款-初审", methodText = "会员提款审核初审")
    @ApiOperation(value = "会员提款修改(审核)状态初审", notes = "会员提款修改(审核)状态初审")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateAccStatus(@RequestBody AccWithdraw accWithdraw,  HttpServletRequest request) {
        Assert.isNull(accWithdraw.getId(), "id不能为空");
        Assert.isNull(accWithdraw.getStatus(), "状态不能为空");
        BizEvent bizEvent = new BizEvent(this, CommonUtil.getSiteCode(), null, null);
        fundWithdrawService.checkoutStatusByTwo(accWithdraw.getId());
        fundWithdrawService.updateAccStatus(accWithdraw, getUser().getUsername(), bizEvent, CommonUtil.getIpAddress(request));
        if (Objects.nonNull(bizEvent.getEventType())) {
            applicationEventPublisher.publishEvent(bizEvent);
        }
        return R.ok();
    }

    @PostMapping("/updateAccStatusRefuse")
    @RequiresPermissions("fund:accWithdraw:update")
    @SysLog(module = "会员提款-初审-拒绝", methodText = "会员提款审核初审-拒绝")
    @ApiOperation(value = "会员提款修改(审核)状态初审", notes = "会员提款修改(审核)状态初审")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateAccStatusRefuse(@RequestBody AccWithdraw accWithdraw,  HttpServletRequest request) {
        Assert.isNull(accWithdraw.getId(), "id不能为空");
        Assert.isNull(accWithdraw.getStatus(), "状态不能为空");
        if(!(Integer.valueOf(Constants.EVNumber.zero).equals(accWithdraw.getStatus()))){
            throw new R200Exception("status值错误");
        }
        BizEvent bizEvent = new BizEvent(this, CommonUtil.getSiteCode(), null, null);
        fundWithdrawService.checkoutStatusByTwo(accWithdraw.getId());
        fundWithdrawService.updateAccStatus(accWithdraw, getUser().getUsername(), bizEvent, CommonUtil.getIpAddress(request));
        if (Objects.nonNull(bizEvent.getEventType())) {
            applicationEventPublisher.publishEvent(bizEvent);
        }
        return R.ok();
    }

    @PostMapping("/updateAccStatusSucToFail")
    @RequiresPermissions("fund:accWithdraw:SucToFail")
    @SysLog(module = "会员提款-成功改失败", methodText = "会员提款-成功改失败")
    @ApiOperation(value = "会员提款成功改失败", notes = "会员提款成功改失败")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateAccStatusSucToFail(@RequestBody AccWithdraw accWithdraw,  HttpServletRequest request) {
        Assert.isNull(accWithdraw.getId(), "id不能为空");
        Assert.isNull(accWithdraw.getStatus(), "状态不能为空");
        if(!(Integer.valueOf(Constants.EVNumber.zero).equals(accWithdraw.getStatus()))){
            throw new R200Exception("status值错误");
        }
        accWithdraw.setStatus(Constants.EVNumber.seven); // 前端传0，0为拒绝，状态需要为失败，新增状态7为失败
        BizEvent bizEvent = new BizEvent(this, CommonUtil.getSiteCode(), null, null);
        fundWithdrawService.checkoutStatusBySucToFail(accWithdraw.getId());
        fundWithdrawService.updateAccStatusSucToFail(accWithdraw, getUser().getUsername(), bizEvent, CommonUtil.getIpAddress(request));
        if (Objects.nonNull(bizEvent.getEventType())) {
            applicationEventPublisher.publishEvent(bizEvent);
        }
        return R.ok();
    }


    @PostMapping("/updateMerchantPayment")
    @SysLog(module = "会员提款", methodText = "更新第三方代付状态")
    @ApiOperation(value = "更新第三方代付状态", notes = "更新第三方代付状态")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateMerchantPayment(@RequestParam("accountId") Integer accountId) {
        List<AccWithdraw> accWithdraws = fundWithdrawService.fundAccWithdrawMerchant(null);
        if (Collections3.isNotEmpty(accWithdraws)) {
            accWithdraws.stream().forEach(as -> {
                fundWithdrawService.updateMerchantPayment(as, CommonUtil.getSiteCode());
            });
        }
        return R.ok();
    }

    @PostMapping("/updateExemptMemo")
    @RequiresPermissions("fund:accWithdraw:update")
    @SysLog(module = "会员提款修改免审备注", methodText = "会员提款修改免审备注")
    @ApiOperation(value = "会员提款修改免审备注", notes = "会员提款修改备注")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateExemptMemo(@RequestBody AccWithdraw accWithdraw) {
        Assert.isNull(accWithdraw.getId(), "id不能为空");
        fundWithdrawService.updateExemptMemo(accWithdraw.getId(), accWithdraw.getExemptMemo(), getUser().getUsername());
        return R.ok();
    }
    
    @PostMapping("/updateAccMemo")
    @RequiresPermissions("fund:accWithdraw:update")
    @SysLog(module = "会员提款", methodText = "会员提款修改备注")
    @ApiOperation(value = "会员提款修改备注", notes = "会员提款修改备注")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateAccMemo(@RequestBody AccWithdraw accWithdraw) {
    	Assert.isNull(accWithdraw.getId(), "id不能为空");
    	fundWithdrawService.updateAccMemo(accWithdraw.getId(), accWithdraw.getMemo(), getUser().getUsername());
    	return R.ok();
    }

    @GetMapping("/accWithdrawExportExcel")
    @RequiresPermissions("fund:accWithdraw:exportExcel")
    @ApiOperation(value = "导出会员提款", notes = "导出会员提款")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R accExportExcel(@ModelAttribute AccWithdraw accWithdraw) {

        accWithdraw.setWithdrawCount(1);//作为查询参数时，表示需要统计提款次数
        SysFileExportRecord record = fundWithdrawService.accWithdrawExportExcel(accWithdraw, getUserId(),module);
        if (record == null) {
            throw new R200Exception("正在处理中!");
        }
        return R.ok();
    }

    @GetMapping("checkFile")
    @ApiOperation(value = "查询文件是否可下载",notes = "查询文件是否可下载")
    public R checkFile(){
        Long userId = getUserId();
        SysFileExportRecord record = sysFileExportRecordService.getAsynFileExportRecordByUserId(userId,module);
        if(null != record){
            String fileName = accExcelPath.substring(accExcelPath.lastIndexOf("/")+1,accExcelPath.length());
            record.setDownloadFileName(fileName);
            return R.ok().put(record);
        }
        return R.ok(false);
    }

    @GetMapping("/withdrawCountByStatus")
    @RequiresPermissions("fund:accWithdraw:list")
    @ApiOperation(value = "会员提款统计", notes = "根据状态统计会员提款")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            ,@ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R withdrawCountByStatus(@ModelAttribute AccWithdraw accWithdraw) {
        accWithdraw.setAgyAccountIds(agentTypeService.checkAgentType(accWithdraw.getAgyAccountIds()));
        return R.ok(fundWithdrawService.withdrawCountByStatus(accWithdraw));
    }


    @GetMapping("/accSumStatistics")
    @RequiresPermissions("fund:accWithdraw:list")
    @ApiOperation(value = "会员提款查询列表-合计统计", notes = "会员提款查询列表-合计统计")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    @SysLog(module = "会员取款", methodText = "会员提款查询列表-合计统计")
    public R accSumStatistics(@ModelAttribute AccWithdraw accWithdraw) {
        accWithdraw.setAgyAccountIds(agentTypeService.checkAgentType(accWithdraw.getAgyAccountIds()));
        return R.ok().putPage(fundWithdrawService.accSumStatistics(accWithdraw));
    }

    @GetMapping("/lockStatus")
    @ApiOperation(value = "操作锁定--状态查询", notes = "操作锁定--状态查询审")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R lockStatus(@ModelAttribute AccWithdraw accWithdraw,  HttpServletRequest request) {
        Assert.isNull(accWithdraw.getId(), "id不能为空");

        String redisKey = RedisConstants.UPDATE_WITHDRAW_LOCK + CommonUtil.getSiteCode() + accWithdraw.getId();
        boolean flag = redisService.setRedisExpiredTimeBo(redisKey, accWithdraw.getId(), 3, TimeUnit.SECONDS);
        AccWithdraw acc;
        if (flag) {
            try{
               acc = fundWithdrawService.lockstatus(accWithdraw,getUser().getUsername());
            }finally {
                redisService.del(redisKey);
            }
        }else{
            return R.error("正在处理中，请稍后重试！");
        }

        return R.ok(acc);
    }

    @PostMapping("/lock")
    @ApiOperation(value = "操作锁定--锁定", notes = "操作锁定--锁定")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R lock(@RequestBody AccWithdraw accWithdraw,  HttpServletRequest request) {
        Assert.isNull(accWithdraw.getId(), "id不能为空");
        Assert.isNull(accWithdraw.getLockStatus(), "id不能为空");
        String redisKey = RedisConstants.UPDATE_WITHDRAW_LOCK + CommonUtil.getSiteCode() + accWithdraw.getId();
        boolean flag = redisService.setRedisExpiredTimeBo(redisKey, accWithdraw.getId(), 3, TimeUnit.SECONDS);
        if (flag) {
            try{
                fundWithdrawService.lock(accWithdraw,getUser().getUsername());
            }finally {
                redisService.del(redisKey);
            }
        }else{
            return R.error("正在处理中，请稍后重试！");
        }

        return R.ok();
    }

    @PostMapping("/unLock")
    @ApiOperation(value = "操作锁定--解锁", notes = "操作锁定--解锁")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R unLock(@RequestBody AccWithdraw accWithdraw,  HttpServletRequest request) {
        Assert.isNull(accWithdraw.getId(), "id不能为空");
        Assert.isNull(accWithdraw.getLockStatus(), "id不能为空");

        String redisKey = RedisConstants.UPDATE_WITHDRAW_LOCK + CommonUtil.getSiteCode() + accWithdraw.getId();
        boolean flag = redisService.setRedisExpiredTimeBo(redisKey, accWithdraw.getId(), 3, TimeUnit.SECONDS);
        if (flag) {
            try{
                fundWithdrawService.unLock(accWithdraw,getUser().getUsername());
            }finally {
                redisService.del(redisKey);
            }
        }else{
            return R.error("正在处理中，请稍后重试！");
        }
        return R.ok();
    }

    @GetMapping("/updateAllLockStatus")
    @ApiOperation(value = "操作锁定--更新锁定状态", notes = "操作锁定--更新锁定状态")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateAllLockStatus(@ModelAttribute AccWithdraw accWithdraw,  HttpServletRequest request) {

        fundWithdrawService.updateAllLockStatus();

        return R.ok();
    }

    @PostMapping("/updateAccStatusPend")
    @RequiresPermissions("fund:accWithdraw:update")
    @SysLog(module = "会员提款初审待定", methodText = "会员提款初审待定")
    @ApiOperation(value = "会员提款修改(审核)状态-通过", notes = "会员提款修改(审核)状态-通过")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateAccStatusPend(@RequestBody AccWithdraw accWithdraw, HttpServletRequest request) {
        Assert.isNull(accWithdraw.getId(), "id不能为空");
        Assert.isNull(accWithdraw.getStatus(), "状态不能为空");
        if(!(Integer.valueOf(Constants.EVNumber.six).equals(accWithdraw.getStatus()))){
            throw new R200Exception("status值错误");
        }
        // 初始化事件
        BizEvent bizEvent = new BizEvent(this, CommonUtil.getSiteCode(), null, null);
        fundWithdrawService.updateAccStatus(accWithdraw, getUser().getUsername(), bizEvent, CommonUtil.getIpAddress(request));
//        if (Objects.nonNull(bizEvent.getEventType())) {
//            applicationEventPublisher.publishEvent(bizEvent);
//        }
        return R.ok();
    }
    @PostMapping("/updateAccStatusPendPass")
    @RequiresPermissions("fund:accWithdraw:update")
    @SysLog(module = "会员提款初审待定-通过", methodText = "会员提款初审待定-通过")
    @ApiOperation(value = "会员提款修改(审核)状态-通过", notes = "会员提款修改(审核)状态-通过")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateAccStatusPendPass(@RequestBody AccWithdraw accWithdraw, HttpServletRequest request) {
        Assert.isNull(accWithdraw.getId(), "id不能为空");
        Assert.isNull(accWithdraw.getStatus(), "状态不能为空");
        if(!(Integer.valueOf(Constants.EVNumber.one).equals(accWithdraw.getStatus()))){
            throw new R200Exception("status值错误");
        }
        // 初始化事件
        BizEvent bizEvent = new BizEvent(this, CommonUtil.getSiteCode(), null, null);
        fundWithdrawService.updateAccStatus(accWithdraw, getUser().getUsername(), bizEvent, CommonUtil.getIpAddress(request));
        if (Objects.nonNull(bizEvent.getEventType())) {
            applicationEventPublisher.publishEvent(bizEvent);
        }
        return R.ok();
    }

    @PostMapping("/updateAccStatusPendRefuse")
    @RequiresPermissions("fund:accWithdraw:update")
    @SysLog(module = "会员提款初审待定-拒绝", methodText = "会员提款初审待定-拒绝")
    @ApiOperation(value = "会员提款修改(审核)状态-拒绝", notes = "会员提款修改(审核)状态-拒绝")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateAccStatusPendRefuse(@RequestBody AccWithdraw accWithdraw, HttpServletRequest request) {
        Assert.isNull(accWithdraw.getId(), "id不能为空");
        Assert.isNull(accWithdraw.getStatus(), "状态不能为空");
        if(!(Integer.valueOf(Constants.EVNumber.zero).equals(accWithdraw.getStatus()))){
            throw new R200Exception("status值错误");
        }
        BizEvent bizEvent = new BizEvent(this, CommonUtil.getSiteCode(), null, null);
        fundWithdrawService.updateAccStatus(accWithdraw, getUser().getUsername(), bizEvent, CommonUtil.getIpAddress(request));
        if (Objects.nonNull(bizEvent.getEventType())) {
            applicationEventPublisher.publishEvent(bizEvent);
        }
        return R.ok();
    }
    
    @PostMapping("/artificialWithdrawal")
    @RequiresPermissions("fund:accWithdraw:manual")
    @ApiOperation(value = "人工提款", notes = "人工提款，创建一个与关联订单一样金额的提款订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R withdrawal(@RequestBody AccWithdraw accWithdraw) {
        fundWithdrawService.artificialWithdrawal(accWithdraw);
        return R.ok();
    }
}
