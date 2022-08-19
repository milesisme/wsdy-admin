package com.wsdy.saasops.modules.fund.service;

import static com.wsdy.saasops.common.constants.Constants.SYSTEM_USER;
import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_10_DATE;
import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static java.util.Objects.nonNull;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import com.wsdy.saasops.common.constants.WarningConstants;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.modules.activity.service.FirstChargeOprService;
import com.wsdy.saasops.modules.fund.dao.FastDepositWithdrawCertificateMapper;
import com.wsdy.saasops.modules.fund.entity.AccWithdraw;
import com.wsdy.saasops.modules.fund.entity.FastDepositWithdrawCertificate;
import com.wsdy.saasops.modules.member.dao.MbrFundsReportMapper;
import com.wsdy.saasops.modules.member.dto.WarningLogDto;
import com.wsdy.saasops.modules.member.entity.MbrFundsReport;
import com.wsdy.saasops.mt.service.MTDataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.wsdy.saasops.api.modules.pay.dto.DepositListDto;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.listener.BizEvent;
import com.wsdy.saasops.listener.BizEventType;
import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.fund.dao.FundDepositMapper;
import com.wsdy.saasops.modules.fund.dto.CountEntity;
import com.wsdy.saasops.modules.fund.dto.DepositStatisticsByPayDto;
import com.wsdy.saasops.modules.fund.dto.DepositStatisticsByPayRespDto;
import com.wsdy.saasops.modules.fund.entity.FundDeposit;
import com.wsdy.saasops.modules.fund.entity.QuickFunction;
import com.wsdy.saasops.modules.fund.mapper.FundMapper;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.dao.MbrAuditAccountMapper;
import com.wsdy.saasops.modules.member.dao.MbrDepositCountMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrAuditAccount;
import com.wsdy.saasops.modules.member.entity.MbrBillDetail;
import com.wsdy.saasops.modules.member.service.AuditAccountService;
import com.wsdy.saasops.modules.member.service.MbrAccountLogService;
import com.wsdy.saasops.modules.member.service.MbrAccountService;
import com.wsdy.saasops.modules.member.service.MbrWalletService;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import com.wsdy.saasops.modules.sys.service.SysFileExportRecordService;
import com.wsdy.saasops.modules.system.pay.mapper.PayMapper;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@Transactional
public class FundDepositService extends BaseService<FundDepositMapper, FundDeposit> {

    @Autowired
    private FundDepositMapper fundDepositMapper;
    @Autowired
    private MbrWalletService walletService;
    @Autowired
    private MbrAuditAccountMapper accountAuditMapper;
    @Autowired
    private AuditAccountService accountAuditService;
    @Autowired
    private FundMapper fundMapper;
    @Autowired
    private FastDepositWithdrawCertificateMapper certificateMapper;
    @Autowired
    private MbrFundsReportMapper mbrFundsReportMapper;
    @Autowired
    private FirstChargeOprService firstChargeOprService;

    @Value("${fund.onLine.excel.path}")
    private String onLineExcelPath;
    @Value("${fund.company.excel.path}")
    private String companyExcelPath;
    @Value("${fund.deposit.excel.path}")
    private String depositExcelPath;
    @Autowired
    private MbrAccountMapper accountMapper;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private PayMapper payMapper;
    @Autowired
    private MbrAccountLogService mbrAccountLogService;
    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private MbrAccountService mbrAccountService;
    @Autowired
    private QiNiuYunUtil qiNiuYunUtil;
    @Autowired
    private MTDataService mtDataService;

    @Override
    public FundDeposit queryObject(Integer id) {
        FundDeposit fundDeposit = new FundDeposit();
        fundDeposit.setId(id);
        Optional<FundDeposit> optional = Optional.ofNullable(
                fundMapper.findDepositList(fundDeposit)
                        .stream().findAny()).get();
        if (optional.isPresent()) {
            FundDeposit deposit = optional.get();
            FundDeposit sit = new FundDeposit();
            sit.setMark(deposit.getMark());
            sit.setStatus(Constants.IsStatus.succeed);
            sit.setAccountId(deposit.getAccountId());
            int depositCount = fundMapper.findDepositCount(sit);
            deposit.setDepositCount(String.valueOf(depositCount));
            return deposit;
        }
        return null;
    }

    public List<FundDeposit> selectList(FundDeposit fundDeposit) {
        return fundDepositMapper.select(fundDeposit);
    }

    public PageUtils queryListPage(FundDeposit fundDeposit, Integer pageNo, Integer pageSize, int flag) {
        PageHelper.startPage(pageNo, pageSize);
        List<FundDeposit> list = fundMapper.findDepositList(fundDeposit);
        for (FundDeposit target : list) {
            if (target.getStatus() == 1) {
                target.setDepositCount(fundMapper.findAccDepositCount(target).toString());
            } else {
                target.setDepositCount("-");
            }
        }

        return BeanUtil.toPagedResult(list);
    }

    public FundDeposit depositSumStatistic(FundDeposit fundDeposit) {
        FundDeposit allTotal = fundMapper.findDepositListSum(fundDeposit);
        return allTotal;
    }

    public PageUtils queryListPage(DepositListDto fundDeposit, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<FundDeposit> list;
        if (fundDeposit.getMark() == Constants.EVNumber.zero) {
            list = fundMapper.findDepositListApi(fundDeposit);
        } else if (fundDeposit.getMark() == Constants.EVNumber.one) {
            list = fundMapper.findDepositListApiCompany(fundDeposit);
        } else if (fundDeposit.getMark() == Constants.EVNumber.two) {
            fundDeposit.setFinancialCode(OrderConstants.FUND_ORDER_CODE_AA);
            list = fundMapper.findDepositListApiOther(fundDeposit);
        } else if (fundDeposit.getMark() == Constants.EVNumber.four) {
            fundDeposit.setFinancialCode(OrderConstants.ACCOUNT_REBATE_FA);
            list = fundMapper.findDepositListApiOther(fundDeposit);
        } else {
            list = fundMapper.findDepositAndOtherList(fundDeposit);
        }
        return BeanUtil.toPagedResult(list);
    }

    public Double findDepositSum(DepositListDto fundDeposit) {
        if (fundDeposit.getMark() == Constants.EVNumber.zero
                || fundDeposit.getMark() == Constants.EVNumber.one) {
            return fundMapper.findDepositSum(fundDeposit);
        }
        if (fundDeposit.getMark() == Constants.EVNumber.two) {
            fundDeposit.setFinancialCode(OrderConstants.FUND_ORDER_CODE_AA);
            return fundMapper.findDepositSumOther(fundDeposit);
        }
        if (fundDeposit.getMark() == Constants.EVNumber.four) {
            fundDeposit.setFinancialCode(OrderConstants.ACCOUNT_REBATE_FA);
            return fundMapper.findDepositSumOther(fundDeposit);
        }
        return fundMapper.findDepositSumAndAudit(fundDeposit);
    }

    public Double findSumDepositAmount(FundDeposit fundDeposit) {
        fundDeposit.setStatus(Constants.IsStatus.succeed);
        fundDeposit.setCreateTime(getCurrentDate(FORMAT_10_DATE));
        return fundMapper.findSumDepositAmount(fundDeposit);
    }

    public void updateFastDWCertificate(FundDeposit deposit) {
        if (deposit.getMark() == Constants.EVNumber.five) {
            if (deposit.getFastDepositPicture() != null && deposit.getFastDepositPicture().size() > 0) {
                FastDepositWithdrawCertificate param = new FastDepositWithdrawCertificate();
                param.setAccountId(deposit.getAccountId());
                param.setDepositorderno(deposit.getOrderNo());
                FastDepositWithdrawCertificate exist = certificateMapper.selectOne(param);
                if (exist == null) {
                    param.setDepositPictures(String.join(",", deposit.getFastDepositPicture()));
                    certificateMapper.insert(param);
                } else {
                    exist.setDepositPictures(String.join(",", deposit.getFastDepositPicture())
                            .concat(",").concat(exist.getDepositPictures()));
                    certificateMapper.updateByPrimaryKeySelective(exist);
                }
            }

        }
    }

    public void checkoutStatusBySucToFail(Integer id) {
        FundDeposit deposit = fundDepositMapper.selectByPrimaryKey(id);
        if (!deposit.getStatus().equals(Constants.IsStatus.succeed)) {
            throw new R200Exception("订单不是成功状态，无法修改");
        }
        // 当成功订单审核时间不超过24小时，可以修改订单状态
        Date after24 = DateUtil.addHours(deposit.getCreateTime(), 48);
        if (after24.before(new Date())) {
            throw new R200Exception("订单已超过申请时间48小时，无法修改");
        }
        // 涉及到报表统计，10分钟内的订单无法标记为失败
        Date after10min = DateUtil.addDate(deposit.getModifyTime(), 0);
        if (after10min.after(new Date())) {
            throw new R200Exception("存款完成10分钟内的订单无法修改，请超过10分钟后处理");
        }
        // 产品要求就算无法扣除也要写入该备注
        String msg = "余额不足，无法修改为失败订单，请限制出款";
        // 避免重复记录
        if (StringUtil.isNotEmpty(deposit.getMemo())) {
            if (!deposit.getMemo().contains(msg)) {
                deposit.setMemo(deposit.getMemo().concat(";").concat(msg));
            }
        } else {
            deposit.setMemo(msg);
        }
        fundDepositMapper.updateByPrimaryKeySelective(deposit);
    }

    @Transactional
    public FundDeposit updateDeposit(FundDeposit fundDeposit, String userName, String ip, String siteCode) {
        // 校验订单状态，并获得数据库deposit
        FundDeposit deposit = checkoutFund(fundDeposit);
        // 此处增加前端实际金额修改处理,下面会update
        if (Objects.nonNull(fundDeposit.getActualArrival())) {
            deposit.setActualArrival(fundDeposit.getActualArrival());
        }
        // 审核通过
        if (Constants.IsStatus.succeed.equals(fundDeposit.getStatus())) {
            updateDepositSucceed(deposit, false, true);   // 使用实际金额生成稽核和入款
            // 更新会员存款锁定状态
            mbrAccountService.unlockDepositLock(deposit.getAccountId());
            // 充值存款申请次数重置
            mbrAccountService.resetDepositLockNum(deposit.getAccountId());
            // 如果是极速存款，上传了凭证，则保存凭证
            updateFastDWCertificate(deposit);
        }
        deposit.setStatus(fundDeposit.getStatus());
        deposit.setAuditUser(userName);
        deposit.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
        // 允许备注为空
        if (!StringUtil.isEmpty(fundDeposit.getMemo())) {
            deposit.setMemo(fundDeposit.getMemo());
        }
        deposit.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        deposit.setModifyUser(userName);
        FundDeposit oldDeposit = fundDepositMapper.selectByPrimaryKey(deposit);
        fundDepositMapper.updateByPrimaryKey(deposit);

        //添加操作日志
        mbrAccountLogService.updateMemberDepositInfo(deposit, oldDeposit, userName, ip);

        // 预警
        if (Constants.IsStatus.succeed.equals(fundDeposit.getStatus()) && deposit.getDepositAmount().compareTo(WarningConstants.WARING_FUND_DEPOSIT) >= 0) {
            // 删除运营账号为系统审核
            if (!"系统审核".equals(userName)) {
                MbrAccount m = new MbrAccount();
                m.setId(deposit.getAccountId());
                MbrAccount mbrAccount = accountMapper.selectByPrimaryKey(m);
                String content = String.format(WarningConstants.FUND_DEPOSIT_PASS_TMP, deposit.getDepositAmount());
                mbrAccountLogService.addWarningLog(new WarningLogDto(mbrAccount.getLoginName(), userName, content, Constants.EVNumber.five));
            }
        }
        //更新蜜桃绑定异步处理
        if (Constants.IsStatus.succeed.equals(fundDeposit.getStatus())) {
            syncHandler(siteCode, deposit);
        }
        return deposit;
    }

    private void syncHandler(String siteCode, FundDeposit deposit) {
        CompletableFuture.runAsync(() -> {
            ThreadLocalCache.setSiteCodeAsny(siteCode);
            try {
                mtDataService.updateAgentId(deposit.getAccountId(), deposit.getSpreadCode());
            }catch (Exception e){
                log.error("mtDataService蜜桃绑定错误" + e);
            }

            try {
                mtDataService.mtCallBack(siteCode, deposit.getAccountId(), deposit.getDepositAmount(),  deposit.getSpreadCode());
            }catch (Exception e){
                log.error("mtDataService蜜桃回调错误" + e);
            }


            try {
                firstChargeOprService.applyFirstCharge(deposit.getAccountId(), deposit.getIp(), siteCode);
            }catch (Exception e){
                log.error("firstChargeOprService充值返上级错误" + e);
            }
        });
    }

    @Transactional
    public FundDeposit updateDepositSucToFail(FundDeposit fundDeposit, String userName, String ip) {
        // 校验订单状态，并获得数据库deposit
        FundDeposit deposit = checkoutFundSucToFail(fundDeposit);
        // 此处增加前端实际金额修改处理,下面会update
        if (Objects.nonNull(fundDeposit.getActualArrival())) {
            deposit.setActualArrival(fundDeposit.getActualArrival());
        }
        // 修改失败
        if (Constants.IsStatus.defeated.equals(fundDeposit.getStatus())) {
            updateDepositSucTotail(deposit, false, true, userName);   // 使用实际金额
        }
        deposit.setStatus(fundDeposit.getStatus());
        deposit.setModifyUser(userName);
        deposit.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        // 允许备注为空
        if (!StringUtil.isEmpty(fundDeposit.getMemo())) {
            deposit.setMemo(fundDeposit.getMemo());
        }
        deposit.setMemo(deposit.getMemo().replace("余额不足，无法修改为失败订单，请限制出款", "")
                .concat(" 成功改失败操作人".concat(userName).concat("操作时间").concat(getCurrentDate(FORMAT_18_DATE_TIME))));

        //deposit.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        //deposit.setModifyUser(userName);
        FundDeposit oldDeposit = fundDepositMapper.selectByPrimaryKey(deposit);
        fundDepositMapper.updateByPrimaryKey(deposit);

        //添加操作日志
        mbrAccountLogService.updateMemberDepositInfo(deposit, oldDeposit, userName, ip);

        // 更新审核日期会员的mbrFundsReport
        MbrFundsReport model = new MbrFundsReport();
        model.setAccountId(deposit.getAccountId());
        model.setReportDate(DateUtil.format(deposit.getAuditTime(), FORMAT_10_DATE));
        MbrFundsReport exsit = mbrFundsReportMapper.getMbrTodayReport(model);
        if (exsit != null) {
            exsit.setDeposit(exsit.getDeposit().subtract(deposit.getDepositAmount()));
            exsit.setActualDeposit(exsit.getActualDeposit().subtract(deposit.getActualArrival()));
            exsit.setLastupdate(DateUtil.getCurrentDate(FORMAT_18_DATE_TIME));
            mbrFundsReportMapper.updateByPrimaryKeySelective(exsit);
        }

        return deposit;
    }

    public void accountDepositMsg(FundDeposit fundDeposit, String siteCode) {
        BizEvent bizEvent = new BizEvent(this, siteCode, fundDeposit.getAccountId(), null);
        if (Constants.IsStatus.succeed.equals(fundDeposit.getStatus())) {
            bizEvent.setEventType(BizEventType.DEPOSIT_VERIFY_SUCCESS);
        }
        if (Constants.IsStatus.defeated.equals(fundDeposit.getStatus())) {
            bizEvent.setEventType(BizEventType.DEPOSIT_VERIFY_FAILED);
        }
        bizEvent.setDespoitMoney(fundDeposit.getDepositAmount());
        applicationEventPublisher.publishEvent(bizEvent);
    }

    public void updateDepositSucceed(FundDeposit fundDeposit, boolean isUpdateStatus, boolean isActualArrival) {
        // 先更新状态，避免并发导致的多次上分
        fundDeposit.setStatus(Constants.IsStatus.succeed);
        fundDeposit.setAuditUser(SYSTEM_USER);
        fundDeposit.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
        fundDeposit.setIsPayment(Boolean.TRUE);
        int i = 0;
        if (isUpdateStatus) {
            i = fundMapper.updatePayStatus(fundDeposit);
        }
        if (i > 0 || !isUpdateStatus) {
            // 增加稽核
            MbrAuditAccount accountAudit = new MbrAuditAccount();
            accountAudit.setDepositId(fundDeposit.getId());
            MbrAuditAccount audit = accountAuditMapper.selectOne(accountAudit);
            if (Objects.isNull(audit)) {
                // 用实际存款金额增加稽核
                if (isActualArrival) {
                    accountAuditService.insertAccountAudit(
                            fundDeposit.getAccountId(), fundDeposit.getActualArrival(),
                            fundDeposit.getId(), null, null,
                            null, null, Constants.EVNumber.one);
                } else {
                    accountAuditService.insertAccountAudit(
                            fundDeposit.getAccountId(), fundDeposit.getDepositAmount(),
                            fundDeposit.getId(), null, null,
                            null, null, Constants.EVNumber.one);
                }
            }
            // 上分
            MbrAccount account = accountMapper.selectByPrimaryKey(fundDeposit.getAccountId());
            MbrBillDetail billDetail = walletService.castWalletAndBillDetail(account.getLoginName(),
                    fundDeposit.getAccountId(), fundDeposit.getOrderPrefix(),
                    fundDeposit.getActualArrival(), fundDeposit.getOrderNo(), Boolean.TRUE, null, null);

            // 此处单独更新billid
            fundDeposit.setBillDetailId(billDetail.getId());
            if (isUpdateStatus) {
                FundDeposit depositBill = new FundDeposit();
                depositBill.setId(fundDeposit.getId());
                depositBill.setBillDetailId(billDetail.getId());
                fundDepositMapper.updateByPrimaryKeySelective(depositBill);
            }

            // 额度变化：使用存款金额，不用实际金额
            if (Objects.nonNull(fundDeposit.getCompanyPayId())) {
                payMapper.updateDepositAmount(fundDeposit.getDepositAmount(), fundDeposit.getCompanyPayId());
            }
            if (Objects.nonNull(fundDeposit.getOnlinePayId())) {
                payMapper.updateOnlinePayAmount(fundDeposit.getDepositAmount(), fundDeposit.getOnlinePayId());
            }
            if (Objects.nonNull(fundDeposit.getQrCodeId())) {
                payMapper.updateQrCodeAmount(fundDeposit.getDepositAmount(), fundDeposit.getQrCodeId());
            }
            // 添加预警
            if (fundDeposit.getDepositAmount().compareTo(WarningConstants.WARNING_DEPOSIT) >= 0) {
                Integer count = fundMapper.findTotalsDepositCount(fundDeposit.getAccountId());
                if (count <= WarningConstants.WARNING_DEPOSIT_COUNT) {
                    String content = String.format(WarningConstants.FUND_DEPOSIT_WARNING_TMP, fundDeposit.getDepositAmount());
                    mbrAccountLogService.addWarningLog(new WarningLogDto(account.getLoginName(), null, content, Constants.EVNumber.three));
                }
            }
        }
    }

    public void updateDepositSucTotail(FundDeposit fundDeposit, boolean isUpdateStatus, boolean isActualArrival, String userName) {
        // 先更新状态，避免并发导致的多次扣款
        fundDeposit.setStatus(Constants.IsStatus.defeated);
        fundDeposit.setIsPayment(Boolean.FALSE);
        int i = 0;
        if (isUpdateStatus) {
            i = fundMapper.updatePayStatus(fundDeposit);
        }
        if (i > 0 || !isUpdateStatus) {
            // 扣款
            MbrAccount account = accountMapper.selectByPrimaryKey(fundDeposit.getAccountId());
            MbrBillDetail billDetail = walletService.castWalletAndBillDetail(account.getLoginName(),
                    fundDeposit.getAccountId(), fundDeposit.getOrderPrefix(),
                    fundDeposit.getActualArrival(), fundDeposit.getOrderNo(), Boolean.FALSE, null, null);

            if (billDetail == null) {
                throw new R200Exception("无法操作，会员余额不足！");
            }

            // 此处单独更新billid
            fundDeposit.setBillDetailId(billDetail.getId());
            if (isUpdateStatus) {
                FundDeposit depositBill = new FundDeposit();
                depositBill.setId(fundDeposit.getId());
                depositBill.setBillDetailId(billDetail.getId());
                fundDepositMapper.updateByPrimaryKeySelective(depositBill);
            }
        }
    }

    private FundDeposit checkoutFund(FundDeposit fundDeposit) {
        FundDeposit deposit = fundDepositMapper.selectByPrimaryKey(fundDeposit.getId());
        if (Objects.isNull(deposit)) {
            throw new R200Exception("无此存款！");
        }
        if (!deposit.getStatus().equals(Constants.IsStatus.pending)) {
            throw new R200Exception("订单已处理完成，不可再次审核");
        }
        if (Boolean.TRUE.equals(deposit.getIsPayment())) {
            throw new R200Exception("订单已支付成功，不能再次审核！");
        }
        return deposit;
    }

    private FundDeposit checkoutFundSucToFail(FundDeposit fundDeposit) {
        FundDeposit deposit = fundDepositMapper.selectByPrimaryKey(fundDeposit.getId());
        if (Objects.isNull(deposit)) {
            throw new R200Exception("无此存款！");
        }
        return deposit;
    }

    public void updateDepositMemo(FundDeposit fundDeposit, String userName) {
        FundDeposit deposit = fundDepositMapper.selectByPrimaryKey(fundDeposit.getId());
        deposit.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        deposit.setModifyUser(userName);
        deposit.setMemo(fundDeposit.getMemo());
        fundDepositMapper.updateByPrimaryKey(deposit);
    }

    public void depositExportExecl(FundDeposit fundDeposit, Boolean isOnLine, HttpServletResponse response) {
        String fileName = isOnLine == true ? "线上入款" : "公司入款" +
                "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xls";
        List<FundDeposit> fundDeposits = fundMapper.findDepositList(fundDeposit);
        List<Map<String, Object>> list = Lists.newArrayList();
        fundDeposits.stream().forEach(deposit -> {
            Map<String, Object> paramr = new HashMap<>(16);
            paramr.put("orderNo", deposit.getOrderPrefix() + deposit.getOrderNo());
            paramr.put("loginName", deposit.getLoginName());
            paramr.put("groupName", deposit.getGroupName());
            paramr.put("depositAmount", deposit.getDepositAmount());
            paramr.put("ip", deposit.getIp());
            paramr.put("status", deposit.getStatus() == 0
                    ? Constants.ChineseStatus.defeated : deposit.getStatus() == 1
                    ? Constants.ChineseStatus.succeed : Constants.ChineseStatus.pending);
            list.add(paramr);
        });
        Workbook workbook = ExcelUtil.commonExcelExportList("mapList",
                isOnLine == true ? onLineExcelPath : companyExcelPath, list);
        try {
            ExcelUtil.writeExcel(response, workbook, fileName);
        } catch (IOException e) {
            throw new RRException(e.getMessage());
        }
    }

    /**
     * 入款导出（不区分入款方式）
     *
     * @param fundDeposit
     * @param userId
     * @return
     */
    public SysFileExportRecord depositExportExcel(FundDeposit fundDeposit, Long userId, String module) {
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);
        if (null != record) {
            String siteCode = CommonUtil.getSiteCode();
            CompletableFuture.runAsync(() -> {
                ThreadLocalCache.setSiteCodeAsny(siteCode);
                List<FundDeposit> fundDeposits = fundMapper.findDepositList(fundDeposit);
                for (FundDeposit target : fundDeposits) {
                    if (target.getStatus() == 1) {
                        target.setDepositCount(fundMapper.findAccDepositCount(target).toString());
                    } else {
                        target.setDepositCount("-");
                    }
                }
                List<Map<String, Object>> list = fundDeposits.stream().map(e -> {
                    setNum2Char(e);
                    Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
                    return entityMap;
                }).collect(Collectors.toList());
                sysFileExportRecordService.exportExcel(depositExcelPath, list, userId, module, siteCode);//异步执行
            });
        }
        return record;
    }

    private void setNum2Char(FundDeposit deposit) {
        if (StringUtil.isEmpty(deposit.getMark())) {
            deposit.setMarkStr("");
        } else if (deposit.getMark() == 1) {
            deposit.setMarkStr("公司入款");
        } else if (deposit.getMark() == 0) {
            deposit.setMarkStr("线上入款");
        } else if (deposit.getMark() == 2) {
            deposit.setMarkStr("普通扫码支付");
        } else if (deposit.getMark() == 3) {
            deposit.setMarkStr("加密货币");
        } else if (deposit.getMark() == 4) {
            deposit.setMarkStr("代理充值");
        }
        if (StringUtil.isEmpty(deposit.getFundSource())) {
            deposit.setFundSourceList("");
        } else if (deposit.getFundSource() == 0) {
            deposit.setFundSourceList("PC");
        } else if (deposit.getFundSource() == 1) {
            deposit.setFundSourceList("H5");
        }
        switch (deposit.getStatus()) {
            case 0:
                deposit.setStatusStr("失败");
                break;
            case 1:
                deposit.setStatusStr("成功");
                break;
            case 2:
                deposit.setStatusStr("待处理");
                break;
            default:
                deposit.setStatusStr("");
        }
    }

    public List<QuickFunction> listCount() {
        return fundMapper.listCount();
    }

    public List<FundDeposit> fundFundDepositByTime() {
        return fundMapper.fundFundDepositByTime();
    }


    /**
     * 会员入款统计
     *
     * @param fundDeposit
     * @return
     */
    public List<CountEntity> depositCountByStatus(FundDeposit fundDeposit) {
        List<CountEntity> list = fundMapper.depositCountByStatus(fundDeposit);
        return list;
    }

    /**
     * 资金列表--根据入款渠道统计会员入款
     */
    public List<DepositStatisticsByPayRespDto> depositStatisticByPay(DepositStatisticsByPayDto depositStatisticsByPayDto) {
        List<DepositStatisticsByPayDto> list = fundMapper.depositStatisticByPay(depositStatisticsByPayDto);
        // 分组统计
        Map<String, List<DepositStatisticsByPayDto>> depositGroupingBy =
                list.stream().collect(
                        Collectors.groupingBy(
                                DepositStatisticsByPayDto::getType));
        List<DepositStatisticsByPayRespDto> respList = new ArrayList<>();
        for (String type : depositGroupingBy.keySet()) {
            DepositStatisticsByPayRespDto resp = new DepositStatisticsByPayRespDto();

            List<DepositStatisticsByPayDto> deposits = depositGroupingBy.get(type);
            // 计算总金额
            Optional<BigDecimal> depositAmountSum = deposits.stream().map(DepositStatisticsByPayDto::getDepositAmountTotal).reduce(BigDecimal::add);
            List<DepositStatisticsByPayDto> orderList = depositGroupingBy.get(type);
            // 按金额降序
            orderList = orderList.stream().sorted(
                    Comparator.comparing(DepositStatisticsByPayDto::getDepositAmountTotal).reversed()
            ).collect(Collectors.toList());

            resp.setType(type);
            resp.setDepositAmountSum(depositAmountSum.get());
            resp.setList(orderList);
            respList.add(resp);
        }

        return respList;
    }

    public void saveFundDespoit(FundDeposit fundDeposit) {
        MbrAccount mbr = new MbrAccount();
        mbr.setLoginName(fundDeposit.getLoginName());
        MbrAccount account = accountMapper.selectOne(mbr);
        if (!nonNull(account) || StringUtil.isEmpty(account.getRealName())) {
            throw new R200Exception("会员名不存在");
        }
        if (!account.getRealName().equals(fundDeposit.getDepositUser())) {
            throw new R200Exception("存款人姓名与会员真实姓名不一致");
        }
        FundDeposit deposit = new FundDeposit();
        Long despoitId = new SnowFlake().nextId();
        deposit.setOrderNo(despoitId.toString());      // 订单号
        deposit.setDepositUser(fundDeposit.getDepositUser());               // 存款人姓名
        deposit.setAccountId(account.getId());                // 会员id
        Byte b = 0;
        deposit.setFundSource(b); //pc
        deposit.setIp(fundDeposit.getIp());
        deposit.setDepositAmount(fundDeposit.getDepositAmount());                  // 存款金额
        deposit.setActualArrival(fundDeposit.getActualArrival());  // 实际到账

        deposit.setCreateUser(getUser().getUsername());              // 下单会员
        deposit.setLoginName(fundDeposit.getLoginName());               // 会员名
        deposit.setCompanyPayId(fundDeposit.getCompanyPayId());
        deposit.setOnlinePayId(fundDeposit.getOnlinePayId());
        // 新加线上入款
        if (FundDeposit.Mark.onlinePay == fundDeposit.getMark()) {
            deposit.setMark(FundDeposit.Mark.onlinePay);
        } else {
            deposit.setMark(FundDeposit.Mark.offlinePay);                           // 存款类型：1 公司入款
        }
        deposit.setStatus(FundDeposit.Status.apply);                            // 状态：2 待处理
        deposit.setIsPayment(FundDeposit.PaymentStatus.unPay);                  // 付款状态 false 未支付
        deposit.setHandingback(Constants.Available.disable);        // 手续费还返默认(为1 扣（减少） ，为0 手续费已处理（增加）)"
        deposit.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME)); // 创建时间
        deposit.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME)); // 修改时间
        deposit.setOrderPrefix(OrderConstants.FUND_ORDER_COMPANYDEPOSIT);   // 订单前缀： CP 公司入款
        deposit.setDepositPostscript(fundDeposit.getDepositPostscript());
        fundDepositMapper.insert(deposit);
        //添加会员存款日志
        //添加操作日志
    }

    public String getSingleImageUrl(MultipartFile uploadFile) {
        String fileName = null;
        if (Objects.nonNull(uploadFile)) {
            try {
                String prefix = uploadFile.getOriginalFilename()
                        .substring(uploadFile.getOriginalFilename().indexOf("."));
                byte[] fileBuff = IOUtils.toByteArray(uploadFile.getInputStream());
                fileName = qiNiuYunUtil.uploadFile(fileBuff, UUID.randomUUID().toString() + prefix);
            } catch (Exception e) {
                log.error("getSingleImageUrljias==error==", e);
                throw new RRException(e.getMessage());
            }
        }
        return fileName;
    }

    public Integer findTotalsDepositCount(Integer accountId) {
        return fundMapper.findTotalsDepositCount(accountId);
    }
}
