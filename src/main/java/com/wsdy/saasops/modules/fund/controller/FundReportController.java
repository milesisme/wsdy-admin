package com.wsdy.saasops.modules.fund.controller;

import com.wsdy.saasops.api.modules.transfer.dto.BillRequestDto;
import com.wsdy.saasops.api.modules.transfer.service.TransferService;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.common.annotation.SysLog;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.Constants.Available;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.constants.WarningConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.listener.BizEvent;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.fund.entity.FundAudit;
import com.wsdy.saasops.modules.fund.service.AgentTypeService;
import com.wsdy.saasops.modules.fund.service.FundReportService;
import com.wsdy.saasops.modules.member.dto.AuditBonusDto;
import com.wsdy.saasops.modules.member.dto.BillRecordDto;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrBillManage;
import com.wsdy.saasops.modules.member.entity.MbrWallet;
import com.wsdy.saasops.modules.member.service.AuditAccountService;
import com.wsdy.saasops.modules.member.service.MbrAccountLogService;
import com.wsdy.saasops.modules.member.service.MbrWalletService;
import com.wsdy.saasops.modules.operate.entity.TGmDepot;
import com.wsdy.saasops.modules.operate.service.TGmDepotService;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import com.wsdy.saasops.modules.sys.service.SysFileExportRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.wsdy.saasops.common.utils.DateUtil.daysBetween;

@RestController
@RequestMapping("/bkapi/fund/report")
@Api(tags = "调整报表,转账报表")
public class FundReportController extends AbstractController {

    @Autowired
    private RedisService redisService;
    @Autowired
    private TransferService transferService;
    @Autowired
    private TGmDepotService tGmDepotService;
    @Autowired
    private FundReportService fundReportService;
    @Autowired
    private MbrAccountLogService mbrAccountLogService;
    @Autowired
    private AuditAccountService auditAccountService;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;
    @Autowired
    private AgentTypeService agentTypeService;

    @Resource
    private ThreadPoolExecutor retentionRateDailyActiveExecutor;
    
    @Autowired
    private MbrWalletService mbrWalletService;

    @Value("${fund.audit.excel.path}")
    private String auditReportExcelPath;
    private final String module = "fundAudit";

    @GetMapping("/billList")
    @RequiresPermissions("fund:billReport:list")
    @ApiOperation(value = "转账报表查询列表", notes = "转账报表查询列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R billList(@ModelAttribute MbrBillManage mbrBillManage, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(fundReportService.queryListPage(mbrBillManage, pageNo, pageSize));
    }

    @GetMapping("/billInfo/{id}")
    @RequiresPermissions("fund:billReport:info")
    @ApiOperation(value = "转账报表查询(根据ID)", notes = "转账报表查询(根据ID)")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R billInfo(@PathVariable("id") Integer id) {
        return R.ok().put("info", fundReportService.queryObject(id));
    }

    @PostMapping("/updateBillMemo")
    @RequiresPermissions("fund:billReport:update")
    @SysLog(module = "转账报表", methodText = "转账报表修改备注")
    @ApiOperation(value = "转账报表修改备注", notes = "转账报表修改备注")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateBillMemo(@RequestBody MbrBillManage mbrBillManage) {
        Assert.isNull(mbrBillManage.getId(), "id不能为空");
        mbrBillManage.setModifyUser(getUser().getUsername());
        fundReportService.updateBillMemo(mbrBillManage);
        return R.ok();
    }

    @GetMapping("/billExportExecl")
    @RequiresPermissions("fund:company:exportExecl")
    @SysLog(module = "转账报表", methodText = "导出转账报表数据")
    @ApiOperation(value = "导出转账报表数据", notes = "导出转账报表数据")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public void billReportExportExecl(@ModelAttribute MbrBillManage mbrBillManage, HttpServletResponse response) {
        fundReportService.billReportExportExecl(mbrBillManage, response);
    }

    @PostMapping("/auditAdd")
    @RequiresPermissions(value = {"fund:audit:add", "fund:audit:mbradd"}, logical = Logical.OR)
    @SysLog(module = "人工增加调整报表", methodText = "人工增加调整报表")
    @ApiOperation(value = "报表新增", notes = "报表新增")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R auditAdd(@RequestBody FundAudit fundAudit, HttpServletRequest request) {
        Assert.isNotEmpty(fundAudit.getIds(), "会员不能为空");
        Assert.isBlank(fundAudit.getFinancialCode(), "FinancialCode不能为空");
        if (OrderConstants.FUND_ORDER_CODE_AA.equals(fundAudit.getFinancialCode())) {
            Assert.isNull(fundAudit.getDepositType(), "存款类型不能为空");
            if (fundAudit.getDepositType() == Constants.EVNumber.two && Objects.isNull(fundAudit.getActivityId())) {
                throw new R200Exception("请选择优惠活动");
            }
        }
        Assert.isNumeric(fundAudit.getAmount(), "调整金额只能为数字,并且长度不能大于12位!", 12);
        Assert.isLenght(fundAudit.getMemo(), "备注长度为1-100!", 1, 100);
        if (!StringUtils.isEmpty(fundAudit.getAuditType()) && fundAudit.getAuditType() == Available.enable) {
            Assert.isNumericInterregional(fundAudit.getAuditMultiple(), "稽核倍数只能为1-999!", 1d, 999d);
        }
        fundAudit.setCreateUser(getUser().getUsername());
        fundAudit.setModifyUser(getUser().getUsername());
        fundReportService.auditSave(fundAudit);
        mbrAccountLogService.addAuditSave(fundAudit, getUser().getUsername(), CommonUtil.getIpAddress(request));

        // 额度增加
        if(fundAudit.getAuditType() != null &&  fundAudit.getAmount().compareTo(WarningConstants.FUND_AUDIT) >= 0){
            String types[] = {"优惠添加", "额度补回", "其他",  "代理充值"};
            String content = String.format(WarningConstants.FUND_AUDIT_WARNING_TMP, types[fundAudit.getAuditType()], fundAudit.getAmount());
            mbrAccountLogService.addWarningLog(fundAudit.getIds(), getUser().getUsername() , content, Constants.EVNumber.two);
        }
        return R.ok();
    }
    
	
	@PostMapping("/updateAdjustment")
	@RequiresPermissions(value = {"fund:adjustment:update"})
	@ApiOperation(value="资金核对调整", notes="资金核对调整")
	@SysLog(module = "资金核对调整",methodText = "资金核对调整")
	public R updateAdjustment(@RequestBody MbrWallet mbrWallet, HttpServletRequest request) {
		Assert.isNull(mbrWallet.getAccountId(), "用户id不可为空");
		Assert.isNull(mbrWallet.getAdjustment(), "调整金额不能为空");
		// 更新之前的
		MbrWallet queryPram = new MbrWallet();
    	queryPram.setAccountId(mbrWallet.getAccountId());
    	MbrWallet queryObjectCond = mbrWalletService.queryObjectCond(queryPram);
    	 
		mbrWalletService.updateAdjustment(mbrWallet);
		mbrAccountLogService.addUpdateAdjustment(mbrWallet, getUser().getUsername(), CommonUtil.getIpAddress(request), queryObjectCond.getAdjustment());
		return R.ok();
	}

    @PostMapping("/auditReduce")
    @RequiresPermissions(value = {"fund:audit:reduce", "fund:audit:mbrreduce"}, logical = Logical.OR)
    @SysLog(module = "人工减少调整报表", methodText = "人工减少调整报表")
    @ApiOperation(value = "报表减少", notes = "报表减少")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R auditReduce(@RequestBody FundAudit fundAudit, HttpServletRequest request) {
        Assert.isNotEmpty(fundAudit.getIds(), "会员不能为空");
        Assert.isBlank(fundAudit.getFinancialCode(), "FinancialCode不能为空");
        Assert.isNull(fundAudit.getReduceType(), "减少类型不能为空");

        Assert.isNumeric(fundAudit.getAmount(), "调整金额只能为数字,并且长度不能大于12位!", 12);
        Assert.isLenght(fundAudit.getMemo(), "备注长度为1-100!", 1, 100);
        if (!StringUtils.isEmpty(fundAudit.getAuditType()) && fundAudit.getAuditType() == Available.enable) {
            Assert.isNumericInterregional(fundAudit.getAuditMultiple(), "稽核倍数只能为1-999!", 1d, 999d);
        }
        fundAudit.setCreateUser(getUser().getUsername());
        fundAudit.setModifyUser(getUser().getUsername());
        fundReportService.auditSave(fundAudit);
        mbrAccountLogService.reduceAuditSave(fundAudit, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @GetMapping("/auditList")
    @RequiresPermissions("fund:audit:list")
    @ApiOperation(value = "调整报表查询列表", notes = "调整报表查询列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R auditList(@ModelAttribute FundAudit fundAudit, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        fundAudit.setCagencyIds(agentTypeService.checkAgentType(fundAudit.getCagencyIds()));
        return R.ok().putPage(fundReportService.queryAuditListPage(fundAudit, pageNo, pageSize));
    }

    @GetMapping("/auditInfo/{id}")
    @RequiresPermissions("fund:audit:info")
    @ApiOperation(value = "调整报表查询(根据ID)", notes = "调整报表查询(根据ID)")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R auditInfo(@PathVariable("id") Integer id) {
        return R.ok().put("info", fundReportService.queryAuditObject(id));
    }

    @PostMapping("/auditUpdateStatus")
    @RequiresPermissions("fund:audit:update")
    @SysLog(module = "调整报表", methodText = "调整报表审核")
    @ApiOperation(value = "调整报表修改状态", notes = "调整报表修改状态")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R auditUpdateStatus(@RequestBody List<FundAudit> fundAudits, HttpServletRequest request) throws Exception {

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        List<CompletableFuture> runAsyncList = new ArrayList<>();

        fundAudits.forEach(fundAudit -> {

            CompletableFuture<Void> runAsync = CompletableFuture.runAsync(() -> {
                RequestContextHolder.setRequestAttributes(requestAttributes);
                Assert.isNull(fundAudit.getId(), "id不能为空");
                Assert.isNull(fundAudit.getStatus(), "状态不能为空");
                Integer fundAuditId = fundAudit.getId();
                String key = RedisConstants.FUND_AUDIT_UPDATE + CommonUtil.getSiteCode() + fundAuditId;
                Boolean isExpired = redisService.setRedisExpiredTimeBo(key, fundAuditId, 200, TimeUnit.SECONDS);
                if (Boolean.FALSE.equals(isExpired)) {
                    throw new R200Exception("任务处理中，请勿重复点击！");
                }
                try {
                    fundAudit.setModifyUser(getUser().getUsername());
                    BizEvent bizEvent = new BizEvent(this, CommonUtil.getSiteCode(), null, null);
                    fundReportService.auditUpdateStatus(fundAudit, CommonUtil.getSiteCode(), getUser().getUsername(), CommonUtil.getIpAddress(request), bizEvent);
                    if (Objects.nonNull(bizEvent.getEventType())) {
                        applicationEventPublisher.publishEvent(bizEvent);
                    }
                } finally {
                    redisService.del(key);
                }
            }, retentionRateDailyActiveExecutor);

            runAsyncList.add(runAsync);

        });

        try {

            CompletableFuture.allOf(runAsyncList.toArray(new CompletableFuture[runAsyncList.size()])).get();
        }catch (Exception e) {
            throw new R200Exception(e.getMessage());
        }

        return R.ok();

    }

    @PostMapping("/auditUpdateStatusRefuse")
    @RequiresPermissions("fund:audit:update")
    @SysLog(module = "调整报表-拒绝", methodText = "调整报表审核-拒绝")
    @ApiOperation(value = "调整报表修改状态-拒绝", notes = "调整报表修改状态-拒绝")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R auditUpdateStatusRefuse(@RequestBody List<FundAudit> fundAudits, HttpServletRequest request) throws ExecutionException, InterruptedException {

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        List<CompletableFuture> runAsyncList = new ArrayList<>();

        fundAudits.forEach(fundAudit -> {
            CompletableFuture<Void> runAsync = CompletableFuture.runAsync(() -> {
                RequestContextHolder.setRequestAttributes(requestAttributes);
                Assert.isNull(fundAudit.getId(), "id不能为空");
                Assert.isNull(fundAudit.getStatus(), "状态不能为空");
                if (!(Integer.valueOf(Constants.EVNumber.zero).equals(fundAudit.getStatus()))) {
                    throw new R200Exception("status值错误");
                }
                Integer fundAuditId = fundAudit.getId();
                String key = RedisConstants.FUND_AUDIT_UPDATE + CommonUtil.getSiteCode() + fundAuditId;
                Boolean isExpired = redisService.setRedisExpiredTimeBo(key, fundAuditId, 200, TimeUnit.SECONDS);
                if (Boolean.FALSE.equals(isExpired)) {
                    throw new R200Exception("任务处理中，请勿重复点击！");
                }
                try {
                    fundAudit.setModifyUser(getUser().getUsername());
                    BizEvent bizEvent = new BizEvent(this, CommonUtil.getSiteCode(), null, null);
                    fundReportService.auditUpdateStatus(fundAudit, CommonUtil.getSiteCode(), getUser().getUsername(), CommonUtil.getIpAddress(request), bizEvent);
                    if (Objects.nonNull(bizEvent.getEventType())) {
                        applicationEventPublisher.publishEvent(bizEvent);
                    }

                } finally {
                    redisService.del(key);
                }
            }, retentionRateDailyActiveExecutor);

            runAsyncList.add(runAsync);

        });

        try {

            CompletableFuture.allOf(runAsyncList.toArray(new CompletableFuture[runAsyncList.size()])).get();
        }catch (Exception e) {
            throw new R200Exception(e.getMessage());
        }

        return R.ok();

    }

    @PostMapping("/auditUpdateMemo")
    @RequiresPermissions("fund:audit:update")
    @SysLog(module = "调整报表", methodText = "调整报表修改备注")
    @ApiOperation(value = "调整报表修改备注", notes = "调整报表修改备注")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R auditUpdateMemo(@RequestBody FundAudit fundAudit) {
        Assert.isNull(fundAudit.getId(), "id不能为空");
        fundReportService.auditUpdateMemo(fundAudit);
        return R.ok();
    }

    @GetMapping("/apiTrfRefresh")
    @RequiresPermissions(value = {"fund:audit:update", "fund:billReport:add"}, logical = Logical.OR)
    @SysLog(module = "转账报表", methodText = "会员第三方接口订单状态查询")
    @ApiOperation(value = "会员第三方接口订单状态查询", notes = "会员第三方接口订单状态查询")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R apiTrfRefresh(@RequestParam("orderNo") Long orderNo) {
        Assert.isNull(orderNo, "订单号不能为空!");
        return transferService.checkTransfer(orderNo, CommonUtil.getSiteCode());
    }

    @PostMapping("updateManageStatus")
    @RequiresPermissions("fund:audit:depotUpdate")
    @SysLog(module = "转账报表调整状态", methodText = "转账报表调整状态")
    public R updateManageStatus(@RequestBody MbrBillManage billManage) {
        Assert.isNull(billManage.getId(), "ID不能为空!");
        Assert.isNull(billManage.getStatus(), "状态不能为空!");
        transferService.updateManageStatus(billManage, getUser().getUsername());
        return R.ok();
    }


    @GetMapping("/auditExportExecl")
    @RequiresPermissions("fund:audit:exportExecl")
    @SysLog(module = "调整报表", methodText = "导出调整报表")
    @ApiOperation(value = "导出调整报表", notes = "导出调整报表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public void auditExportExecl(@RequestBody FundAudit fundAudit, HttpServletResponse response) {
        fundReportService.auditExportExecl(fundAudit, response);
    }

    @GetMapping("/queryDepotOrAccountBalance")
    @SysLog(module = "转账报表->新增", methodText = "获取主账户余额及平台余额")
    @ApiOperation(value = "获取主账户余额及平台余额", notes = "获取主账户余额及平台余额")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R queryDepotOrAccountBalance(@ModelAttribute MbrBillManage mbrBillManage) {
        return R.ok().put("info", fundReportService.queryDepotOrAccountBalance(mbrBillManage, CommonUtil.getSiteCode()));
    }

    @PostMapping ("/queryAccountBalance")
    @SysLog(module = "转账报表->新增", methodText = "获取主账户余额")
    @ApiOperation(value = "获取主账户余额", notes = "获取主账户余额")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R queryAccountBalance(@RequestBody List<String> loginNames) {
        return R.ok().put("info", fundReportService.queryAccountBalanceByLoginNames(loginNames));
    }

    @GetMapping("/createOrderNumber")
    @SysLog(module = "转账报表->新增", methodText = "生成转账单号")
    @ApiOperation(value = "生成转账单号", notes = "生成转账单号")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R createOrderNumber() {
        return R.ok().put(fundReportService.createOrderNumber());
    }

    @RequiresPermissions("fund:billReport:save")
    @PostMapping("/save")
    @SysLog(module = "转账报表->新增", methodText = "创建报表")
    @ApiOperation(value = "保存", notes = "保存")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R save(@RequestBody BillRequestDto requestDto, HttpServletRequest request) {
        Assert.isNull(requestDto.getAccountId(), "会员ID不能为空！");
        Assert.isNull(requestDto.getDepotId(), "平台ID不能为空！");
        Assert.isPInt(requestDto.getAmount(), "转账金额只能为正整数");
        String orderNo = requestDto.getOrderNo();
        MbrAccount mbrAccount = new MbrAccount();
        mbrAccount.setId(requestDto.getAccountId());
        requestDto.setIp(CommonUtil.getIpAddress(request));
        requestDto.setTransferSource((byte) 2);

        //TODO 判断该游戏平台是否是维护，如果是就不执行下面操作
        List<TGmDepot> tGmDepotList = tGmDepotService.findDepotList(requestDto.getAccountId(), (byte) 0, CommonUtil.getSiteCode(), Constants.EVNumber.zero);
        List<Byte> listAvailableWh = tGmDepotList.stream().filter(ls -> ls.getId().equals(requestDto.getDepotId()))
                .map(TGmDepot::getAvailableWh).collect(Collectors.toList());
        List<String> listDepotName = tGmDepotList.stream().filter(ls -> ls.getId().equals(requestDto.getDepotId()))
                .map(TGmDepot::getDepotName).collect(Collectors.toList());
        if (listAvailableWh.get(0).byteValue() == Constants.EVNumber.two) {
            throw new RRException(listDepotName.get(0) + "平台正在维护！");
        }

        AuditBonusDto auditBonusDto = auditAccountService.outAuditBonus(mbrAccount, requestDto.getDepotId());
        if (Boolean.FALSE.equals(auditBonusDto.getIsFraud()) || Boolean.FALSE.equals(auditBonusDto.getIsSucceed())) {
            throw new R200Exception("该游戏平台上有稽核，不可转入转出");
        }
        String key = RedisConstants.ACCOUNT_DEPOT_TRANSFER + CommonUtil.getSiteCode() + requestDto.getAccountId();
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, requestDto.getAccountId(), 10, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(isExpired)) {
            try {
                fundReportService.save(requestDto, CommonUtil.getSiteCode());
                requestDto.setOrderNo(orderNo);
                mbrAccountLogService.addAccountTransferLog(requestDto, getUser().getUsername(), CommonUtil.getIpAddress(request));
            } finally {
                redisService.del(key);
            }
        } else {
            throw new R200Exception("玩命加载中,请稍等...");
        }
        return R.ok();
    }

    @GetMapping("/billRecordList")
    @RequiresPermissions("fund:billReport:list")
    @ApiOperation(value = "账变流水查询", notes = "账变流水查询")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R billRecordList(@ModelAttribute BillRecordDto billRecordDto, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        Assert.isBlank(billRecordDto.getStartTime(), "创建时间不能为空");
        Assert.isBlank(billRecordDto.getEndTime(), "创建时间不能为空");
        int num = daysBetween(billRecordDto.getStartTime(), billRecordDto.getEndTime());
        if (num > 30) {
            throw new R200Exception("单次查询范围不可超过30天，请重新选择合适的创建时间查询！");
        }
        billRecordDto.setAgyAccountId(agentTypeService.checkAgentType(billRecordDto.getAgyAccountId()));
        return R.ok().putPage(fundReportService.queryBillRecordListPage(billRecordDto, pageNo, pageSize));
    }

    @GetMapping("/exportAuditList")
    @RequiresPermissions("fund:audit:export")
    @ApiOperation(value = "导出资金调整信息列表", notes = "导出资金调整信息列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R exportAuditList(@ModelAttribute FundAudit fundAudit) {
        SysFileExportRecord record = fundReportService.exportAuditList(fundAudit, getUser(), auditReportExcelPath, module);
        if (record == null) {
            throw new R200Exception("正在处理中!");
        }
        return R.ok();
    }

    @GetMapping("/checkFile")
    @ApiOperation(value = "查询文件是否可下载", notes = "查询文件是否可下载")
    public R checkFile() {
        Long userId = getUserId();
        SysFileExportRecord record = sysFileExportRecordService.getAsynFileExportRecordByUserId(userId, module);
        if (null != record) {
            String fileName = auditReportExcelPath.substring(auditReportExcelPath.lastIndexOf("/") + 1, auditReportExcelPath.length());
            record.setDownloadFileName(fileName);
            return R.ok().put(record);
        }
        return R.ok(false);
    }
}
