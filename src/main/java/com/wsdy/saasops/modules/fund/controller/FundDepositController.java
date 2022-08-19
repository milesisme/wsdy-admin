package com.wsdy.saasops.modules.fund.controller;

import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.annotation.SysLog;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.fund.dto.CuiDanDto;
import com.wsdy.saasops.modules.fund.dto.DepositStatisticsByPayDto;
import com.wsdy.saasops.modules.fund.entity.FundDeposit;
import com.wsdy.saasops.modules.fund.entity.QuickFunction;
import com.wsdy.saasops.modules.fund.service.FundDepositService;
import com.wsdy.saasops.modules.fund.service.FundWithdrawService;
import com.wsdy.saasops.modules.sys.dao.SysMenuDao;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import com.wsdy.saasops.modules.sys.service.SysFileExportRecordService;
import com.wsdy.saasops.modules.system.pay.entity.SetBacicFastPay;
import com.wsdy.saasops.modules.system.pay.mapper.PayMapper;
import com.wsdy.saasops.modules.system.systemsetting.dao.SysSettingMapper;
import com.wsdy.saasops.modules.system.systemsetting.dto.CuiDanSet;
import com.wsdy.saasops.modules.system.systemsetting.entity.SysSetting;
import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.support.PayloadMethodArgumentResolver;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/bkapi/fund/deposit")
@Api(tags = "线上入款,公司入款")
public class FundDepositController extends AbstractController {

    @Autowired
    private FundDepositService fundDepositService;
    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private PayMapper payMapper;
    @Autowired
    private FundWithdrawService  fundWithdrawService;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private SysMenuDao sysMenuDao;

    @Autowired
    private SysSettingMapper sysSettingMapper;

    @Value("${fund.deposit.excel.path}")
    private String depositExcelPath;
    private final String module = "mbrDeposit";

    @GetMapping("/list")
    @RequiresPermissions("fund:onLine:list")
    @ApiOperation(value = "线上入款查询列表", notes = "线上入款查询列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R list(@ModelAttribute FundDeposit fundDeposit, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        fundDeposit.setLoginSysUserName(getUser().getUsername());
        fundDeposit.setMark(Constants.EVNumber.zero);
        return R.ok().putPage(fundDepositService.queryListPage(fundDeposit, pageNo, pageSize, Constants.EVNumber.zero));
    }

    @GetMapping("/depositList")
    @RequiresPermissions("fund:onLine:list")
    @ApiOperation(value = "会员入款查询列表", notes = "会员入款查询列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R depositList(@ModelAttribute FundDeposit fundDeposit, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(fundDepositService.queryListPage(fundDeposit, pageNo, pageSize, Constants.EVNumber.one));
    }

    @GetMapping("/sumDepositAmount")
    @ApiOperation(value = "线上（公司）入款今日存款", notes = "线上（公司）入款今日存款")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R sumDepositAmount(@ModelAttribute FundDeposit fundDeposit, @RequestParam("make") Integer make) {
        fundDeposit.setMark(make);
        fundDeposit.setLoginSysUserName(getUser().getUsername());
        return R.ok().put("sum", fundDepositService.findSumDepositAmount(fundDeposit));
    }

    @GetMapping("/info/{id}")
    @RequiresPermissions("fund:onLine:info")
    @ApiOperation(value = "线上入款查询(根据ID)", notes = "线上入款查询(根据ID)")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R info(@PathVariable("id") Integer id) {
        FundDeposit fundDeposit = fundDepositService.queryObject(id);
        // 查询是否极速取款订单
        List<SetBacicFastPay> fastDeposit = payMapper.findBasicFastPay(new SetBacicFastPay() {{
            setId(fundDeposit.getCompanyPayId());
        }});
        if (fastDeposit != null && fastDeposit.size() > 0 && fastDeposit.get(0).getPaymentType() == 15) {
            fundDeposit.setFastDeposit(Constants.EVNumber.one);
            // 查询存款凭证
            fundDeposit.setFastDepositPicture(new ArrayList<String>() {{
                add("http://www.qaohu.com/upload/201612/28/201612281308030000.jpg");
            }});
        } else {
            fundDeposit.setFastDeposit(Constants.EVNumber.zero);
        }
        return R.ok().put("fundDeposit", fundDeposit);
    }

    @PostMapping("/updateStatus")
    @RequiresPermissions(value = {"fund:onLine:update", "fund:onLine:updateStatus"}, logical = Logical.OR)
    @SysLog(module = "入款状态审核", methodText = "入款状态审核")
    @ApiOperation(value = "入款状态审核", notes = "入款状态审核")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R onLineUpdate(@RequestBody FundDeposit deposit, HttpServletRequest request) {
        Assert.isNull(deposit.getId(), "id不能为空");
        Assert.isNull(deposit.getStatus(), "状态不能为空");
        //Assert.isLenght(deposit.getMemo(), "备注长度为1-400!", 1, 400);
        String key = RedisConstants.ACCOUNT_DEPOSiT_AUDIT + CommonUtil.getSiteCode() + deposit.getId();
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, deposit.getId(), 200, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(isExpired)) {
            throw new R200Exception("任务处理中，请勿重复点击！");
        }
        try {
            FundDeposit fundDeposit = fundDepositService.updateDeposit(deposit, getUser().getUsername(), CommonUtil.getIpAddress(request), CommonUtil.getSiteCode());
            fundDepositService.accountDepositMsg(fundDeposit, CommonUtil.getSiteCode());
            return R.ok();
        } finally {
            redisService.del(key);
        }
    }

    @PostMapping("/updateStatusSucToFail")
    @RequiresPermissions(value = {"fund:onLine:SucToFail"}, logical = Logical.OR)
    @SysLog(module = "入款状态 - 成功改失败", methodText = "入款状态 - 成功改失败")
    @ApiOperation(value = "入款状态 - 成功改失败", notes = "入款状态 - 成功改失败")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateStatusSucToFail(@RequestBody FundDeposit deposit, HttpServletRequest request) {
        Assert.isNull(deposit.getId(), "id不能为空");
        Assert.isNull(deposit.getStatus(), "状态不能为空");
        Assert.isLenght(deposit.getMemo(), "备注长度为1-400!", 1, 400);
        String key = RedisConstants.ACCOUNT_DEPOSiT_SUCTOFAIL + CommonUtil.getSiteCode() + deposit.getId();
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, deposit.getId(), 200, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(isExpired)) {
            throw new R200Exception("任务处理中，请勿重复点击！");
        }
        try {
            fundDepositService.checkoutStatusBySucToFail(deposit.getId());
            FundDeposit fundDeposit = fundDepositService.updateDepositSucToFail(deposit, getUser().getUsername(), CommonUtil.getIpAddress(request));
            //fundDepositService.accountDepositMsg(fundDeposit, CommonUtil.getSiteCode());
            return R.ok();
        } finally {
            redisService.del(key);
        }
    }

    @PostMapping("/updateStatusRefuse")
    @RequiresPermissions(value = {"fund:onLine:update", "fund:onLine:updateStatus"}, logical = Logical.OR)
    @SysLog(module = "入款状态审核-拒绝", methodText = "入款状态审核-拒绝")
    @ApiOperation(value = "入款状态审核-拒绝", notes = "入款状态审核-拒绝")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateStatusRefuse(@RequestBody FundDeposit deposit, HttpServletRequest request) {
        Assert.isNull(deposit.getId(), "id不能为空");
        Assert.isNull(deposit.getStatus(), "状态不能为空");
        if (!(Integer.valueOf(Constants.EVNumber.zero).equals(deposit.getStatus()))) {
            throw new R200Exception("status值错误");
        }
        //Assert.isLenght(deposit.getMemo(), "备注长度为1-400!", 1, 400);
        String key = RedisConstants.ACCOUNT_DEPOSiT_AUDIT + CommonUtil.getSiteCode() + deposit.getId();
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, deposit.getId(), 200, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(isExpired)) {
            throw new R200Exception("任务处理中，请勿重复点击！");
        }
        try {
            FundDeposit fundDeposit = fundDepositService.updateDeposit(deposit, getUser().getUsername(), CommonUtil.getIpAddress(request), CommonUtil.getSiteCode());
            fundDepositService.accountDepositMsg(fundDeposit, CommonUtil.getSiteCode());
            return R.ok();
        } finally {
            redisService.del(key);
        }
    }

    @GetMapping("/onLine/exportExecl")
    @ApiOperation(value = "导出线上入款数据", notes = "导出线上入款数据")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public void onLineExportExecl(@ModelAttribute FundDeposit deposit, HttpServletResponse response) {
        deposit.setMark(Constants.EVNumber.zero);
        fundDepositService.depositExportExecl(deposit, Boolean.TRUE, response);
    }

    @PostMapping("updateMemo")
    @RequiresPermissions("fund:onLine:update")
    @SysLog(module = "线上入款,公司入款", methodText = "线上入款or公司入款修改备注")
    @ApiOperation(value = "线上入款（公司入款）修改备注", notes = "线上入款（公司入款）修改备注")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateMemo(@RequestBody @Valid FundDeposit deposit) {
        Assert.isNull(deposit.getId(), "id不能为空");
        Assert.isLenght(deposit.getMemo(), "备注长度为1-400!", 1, 400);
        fundDepositService.updateDepositMemo(deposit, getUser().getUsername());
        return R.ok();
    }

    @GetMapping("/companyList")
    @RequiresPermissions("fund:company:list")
    @ApiOperation(value = "公司入款列表", notes = "公司入款列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R companyList(@ModelAttribute FundDeposit fundDeposit, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        fundDeposit.setMark(Constants.EVNumber.one);
        return R.ok().putPage(fundDepositService.queryListPage(fundDeposit, pageNo, pageSize, Constants.EVNumber.zero));
    }

    @GetMapping("/companyInfo/{id}")
    @RequiresPermissions("fund:company:info")
    @ApiOperation(value = "公司入款信息（根据ID）", notes = "公司入款信息（根据ID）")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R companyInfo(@PathVariable("id") Integer id) {
        FundDeposit fundDeposit = fundDepositService.queryObject(id);
        return R.ok().put("fundDeposit", fundDeposit);
    }

    @GetMapping("/company/exportExecl")
    @RequiresPermissions("fund:company:exportExecl")
    @SysLog(module = "公司入款", methodText = "导出公司入款数据")
    @ApiOperation(value = "导出公司入款数据", notes = "导出公司入款数据")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public void companyExportExecl(@ModelAttribute FundDeposit fundDeposit, HttpServletResponse response) {
        fundDeposit.setMark(Constants.EVNumber.one);
        fundDepositService.depositExportExecl(fundDeposit, Boolean.FALSE, response);
    }

    @GetMapping("/listCount")
    @ApiOperation(value = "统计列表", notes = "统计列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R listCount() {
        Long startTime = System.currentTimeMillis();
        List<QuickFunction> list = fundDepositService.listCount();
        List<CuiDanDto>  cuiDanDtos  =fundWithdrawService.getNewCuiDan(60);
        QuickFunction quickFunction = new QuickFunction();
        quickFunction.setQuickName("催单信息");

        // 查询是否有权限
        Integer count = sysMenuDao.querySetCuiCount(getUser().getUserId());
        if(cuiDanDtos!=null && cuiDanDtos.size() > 0 &&  count > 0){
            quickFunction.setIds(jsonUtil.toJson(cuiDanDtos));
        }
        list.add(quickFunction);

        Example example = new Example(SysSetting.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("syskey", Arrays.asList(
                SystemConstants.IS_BROADCASTED_NEW_BONUS_GENERATION,
                SystemConstants.IS_BROADCASTED_NEW_WITHDRAW_ORDER_GENERATION,
                SystemConstants.IS_BROADCASTED_NEW_DEPOSIT_ORDER_GENERATION));
        List<SysSetting> sysSettingList = sysSettingMapper.selectByExample(example);
        Map<String, List<SysSetting>> settingMap = sysSettingList.stream().collect(Collectors.groupingBy(SysSetting::getSyskey));

        list.forEach(item -> {
            String key = "";
            switch (item.getQuickName()) {
                case "会员入款":
                    key = SystemConstants.IS_BROADCASTED_NEW_DEPOSIT_ORDER_GENERATION;
                    break;
                case "会员提款初审":
                    key = SystemConstants.IS_BROADCASTED_NEW_WITHDRAW_ORDER_GENERATION;
                    break;
                case "优惠申请":
                    key = SystemConstants.IS_BROADCASTED_NEW_BONUS_GENERATION;
                    break;
            }

            if (settingMap != null && (settingMap.get(key))!=null) {
                List<SysSetting> sysSettings = settingMap.get(key);
                String sysvalue = sysSettings.get(0).getSysvalue();
                if ("1".equals(sysvalue)) {
                    item.setOpen(true);
                }else {
                    item.setOpen(false);
                }
            }
        });

        long costTime = System.currentTimeMillis() - startTime;
        log.info("listCount方法耗时：:" + costTime);
        return R.ok().putPage(list);
    }


    @GetMapping("/depositCountByStatus")
    @RequiresPermissions("fund:onLine:list")
    @ApiOperation(value = "会员入款统计", notes = "根据状态统计会员入款")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R depositCountByStatus(@ModelAttribute FundDeposit fundDeposit) {

        return R.ok(fundDepositService.depositCountByStatus(fundDeposit));
    }

    @GetMapping("/depositExportExcel")
    @RequiresPermissions("fund:deposit:exportExcel")
    @SysLog(module = "公司入款", methodText = "导出公司入款数据")
    @ApiOperation(value = "导出会员入款数据", notes = "导出会员入款数据")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R depositExportExcel(@ModelAttribute FundDeposit fundDeposit, HttpServletResponse response) {

        Long userId = getUserId();
        SysFileExportRecord record = fundDepositService.depositExportExcel(fundDeposit, userId, module);
        if (record == null) {
            throw new R200Exception("正在处理中!");
        }
        return R.ok();
    }

    @GetMapping("checkFile")
    @ApiOperation(value = "查询文件是否可下载", notes = "查询文件是否可下载")
    public R checkFile() {
        Long userId = getUserId();
        SysFileExportRecord record = sysFileExportRecordService.getAsynFileExportRecordByUserId(userId, module);
        if (null != record) {
            String fileName = depositExcelPath.substring(depositExcelPath.lastIndexOf("/") + 1, depositExcelPath.length());
            record.setDownloadFileName(fileName);
            return R.ok().put(record);
        }
        return R.ok(false);
    }


    @GetMapping("/depositStatisticByPay")
    @RequiresPermissions("fund:onLine:depositStatisticByPay")
    @ApiOperation(value = "入款渠道统计", notes = "入款渠道统计")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @SysLog(module = "资金报表", methodText = "入款渠道统计")
    public R depositStatisticByPay(@ModelAttribute DepositStatisticsByPayDto depositStatisticsByPayDto) {

        return R.ok().putPage(fundDepositService.depositStatisticByPay(depositStatisticsByPayDto));
    }


    @GetMapping("/depositSumStatistic")
    @RequiresPermissions("fund:onLine:list")
    @ApiOperation(value = "入款查询列表-合计统计", notes = "入款查询列表-合计统计")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    @SysLog(module = "会员取款", methodText = "会员提款查询列表-合计统计")
    public R depositSumStatistic(@ModelAttribute FundDeposit fundDeposit) {
        return R.ok().putPage(fundDepositService.depositSumStatistic(fundDeposit));
    }

    @PostMapping("/companyDeposit")
    @RequiresPermissions("fund:deposit:companyDeposit")
    @ApiOperation(value = "后台人工入款", notes = "后台人工入款")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R companyDeposit(@RequestBody FundDeposit deposit, HttpServletRequest request) {
        deposit.setIp(CommonUtil.getIpAddress(request));
        fundDepositService.saveFundDespoit(deposit);
        return R.ok();
    }
}
