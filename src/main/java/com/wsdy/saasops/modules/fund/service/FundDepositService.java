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
            throw new R200Exception("???????????????????????????????????????");
        }
        // ????????????????????????????????????24?????????????????????????????????
        Date after24 = DateUtil.addHours(deposit.getCreateTime(), 48);
        if (after24.before(new Date())) {
            throw new R200Exception("???????????????????????????48?????????????????????");
        }
        // ????????????????????????10???????????????????????????????????????
        Date after10min = DateUtil.addDate(deposit.getModifyTime(), 0);
        if (after10min.after(new Date())) {
            throw new R200Exception("????????????10??????????????????????????????????????????10???????????????");
        }
        // ???????????????????????????????????????????????????
        String msg = "????????????????????????????????????????????????????????????";
        // ??????????????????
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
        // ???????????????????????????????????????deposit
        FundDeposit deposit = checkoutFund(fundDeposit);
        // ??????????????????????????????????????????,?????????update
        if (Objects.nonNull(fundDeposit.getActualArrival())) {
            deposit.setActualArrival(fundDeposit.getActualArrival());
        }
        // ????????????
        if (Constants.IsStatus.succeed.equals(fundDeposit.getStatus())) {
            updateDepositSucceed(deposit, false, true);   // ???????????????????????????????????????
            // ??????????????????????????????
            mbrAccountService.unlockDepositLock(deposit.getAccountId());
            // ??????????????????????????????
            mbrAccountService.resetDepositLockNum(deposit.getAccountId());
            // ?????????????????????????????????????????????????????????
            updateFastDWCertificate(deposit);
        }
        deposit.setStatus(fundDeposit.getStatus());
        deposit.setAuditUser(userName);
        deposit.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
        // ??????????????????
        if (!StringUtil.isEmpty(fundDeposit.getMemo())) {
            deposit.setMemo(fundDeposit.getMemo());
        }
        deposit.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        deposit.setModifyUser(userName);
        FundDeposit oldDeposit = fundDepositMapper.selectByPrimaryKey(deposit);
        fundDepositMapper.updateByPrimaryKey(deposit);

        //??????????????????
        mbrAccountLogService.updateMemberDepositInfo(deposit, oldDeposit, userName, ip);

        // ??????
        if (Constants.IsStatus.succeed.equals(fundDeposit.getStatus()) && deposit.getDepositAmount().compareTo(WarningConstants.WARING_FUND_DEPOSIT) >= 0) {
            // ?????????????????????????????????
            if (!"????????????".equals(userName)) {
                MbrAccount m = new MbrAccount();
                m.setId(deposit.getAccountId());
                MbrAccount mbrAccount = accountMapper.selectByPrimaryKey(m);
                String content = String.format(WarningConstants.FUND_DEPOSIT_PASS_TMP, deposit.getDepositAmount());
                mbrAccountLogService.addWarningLog(new WarningLogDto(mbrAccount.getLoginName(), userName, content, Constants.EVNumber.five));
            }
        }
        //??????????????????????????????
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
                log.error("mtDataService??????????????????" + e);
            }

            try {
                mtDataService.mtCallBack(siteCode, deposit.getAccountId(), deposit.getDepositAmount(),  deposit.getSpreadCode());
            }catch (Exception e){
                log.error("mtDataService??????????????????" + e);
            }


            try {
                firstChargeOprService.applyFirstCharge(deposit.getAccountId(), deposit.getIp(), siteCode);
            }catch (Exception e){
                log.error("firstChargeOprService?????????????????????" + e);
            }
        });
    }

    @Transactional
    public FundDeposit updateDepositSucToFail(FundDeposit fundDeposit, String userName, String ip) {
        // ???????????????????????????????????????deposit
        FundDeposit deposit = checkoutFundSucToFail(fundDeposit);
        // ??????????????????????????????????????????,?????????update
        if (Objects.nonNull(fundDeposit.getActualArrival())) {
            deposit.setActualArrival(fundDeposit.getActualArrival());
        }
        // ????????????
        if (Constants.IsStatus.defeated.equals(fundDeposit.getStatus())) {
            updateDepositSucTotail(deposit, false, true, userName);   // ??????????????????
        }
        deposit.setStatus(fundDeposit.getStatus());
        deposit.setModifyUser(userName);
        deposit.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        // ??????????????????
        if (!StringUtil.isEmpty(fundDeposit.getMemo())) {
            deposit.setMemo(fundDeposit.getMemo());
        }
        deposit.setMemo(deposit.getMemo().replace("????????????????????????????????????????????????????????????", "")
                .concat(" ????????????????????????".concat(userName).concat("????????????").concat(getCurrentDate(FORMAT_18_DATE_TIME))));

        //deposit.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        //deposit.setModifyUser(userName);
        FundDeposit oldDeposit = fundDepositMapper.selectByPrimaryKey(deposit);
        fundDepositMapper.updateByPrimaryKey(deposit);

        //??????????????????
        mbrAccountLogService.updateMemberDepositInfo(deposit, oldDeposit, userName, ip);

        // ???????????????????????????mbrFundsReport
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
        // ???????????????????????????????????????????????????
        fundDeposit.setStatus(Constants.IsStatus.succeed);
        fundDeposit.setAuditUser(SYSTEM_USER);
        fundDeposit.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
        fundDeposit.setIsPayment(Boolean.TRUE);
        int i = 0;
        if (isUpdateStatus) {
            i = fundMapper.updatePayStatus(fundDeposit);
        }
        if (i > 0 || !isUpdateStatus) {
            // ????????????
            MbrAuditAccount accountAudit = new MbrAuditAccount();
            accountAudit.setDepositId(fundDeposit.getId());
            MbrAuditAccount audit = accountAuditMapper.selectOne(accountAudit);
            if (Objects.isNull(audit)) {
                // ?????????????????????????????????
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
            // ??????
            MbrAccount account = accountMapper.selectByPrimaryKey(fundDeposit.getAccountId());
            MbrBillDetail billDetail = walletService.castWalletAndBillDetail(account.getLoginName(),
                    fundDeposit.getAccountId(), fundDeposit.getOrderPrefix(),
                    fundDeposit.getActualArrival(), fundDeposit.getOrderNo(), Boolean.TRUE, null, null);

            // ??????????????????billid
            fundDeposit.setBillDetailId(billDetail.getId());
            if (isUpdateStatus) {
                FundDeposit depositBill = new FundDeposit();
                depositBill.setId(fundDeposit.getId());
                depositBill.setBillDetailId(billDetail.getId());
                fundDepositMapper.updateByPrimaryKeySelective(depositBill);
            }

            // ??????????????????????????????????????????????????????
            if (Objects.nonNull(fundDeposit.getCompanyPayId())) {
                payMapper.updateDepositAmount(fundDeposit.getDepositAmount(), fundDeposit.getCompanyPayId());
            }
            if (Objects.nonNull(fundDeposit.getOnlinePayId())) {
                payMapper.updateOnlinePayAmount(fundDeposit.getDepositAmount(), fundDeposit.getOnlinePayId());
            }
            if (Objects.nonNull(fundDeposit.getQrCodeId())) {
                payMapper.updateQrCodeAmount(fundDeposit.getDepositAmount(), fundDeposit.getQrCodeId());
            }
            // ????????????
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
        // ???????????????????????????????????????????????????
        fundDeposit.setStatus(Constants.IsStatus.defeated);
        fundDeposit.setIsPayment(Boolean.FALSE);
        int i = 0;
        if (isUpdateStatus) {
            i = fundMapper.updatePayStatus(fundDeposit);
        }
        if (i > 0 || !isUpdateStatus) {
            // ??????
            MbrAccount account = accountMapper.selectByPrimaryKey(fundDeposit.getAccountId());
            MbrBillDetail billDetail = walletService.castWalletAndBillDetail(account.getLoginName(),
                    fundDeposit.getAccountId(), fundDeposit.getOrderPrefix(),
                    fundDeposit.getActualArrival(), fundDeposit.getOrderNo(), Boolean.FALSE, null, null);

            if (billDetail == null) {
                throw new R200Exception("????????????????????????????????????");
            }

            // ??????????????????billid
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
            throw new R200Exception("???????????????");
        }
        if (!deposit.getStatus().equals(Constants.IsStatus.pending)) {
            throw new R200Exception("??????????????????????????????????????????");
        }
        if (Boolean.TRUE.equals(deposit.getIsPayment())) {
            throw new R200Exception("?????????????????????????????????????????????");
        }
        return deposit;
    }

    private FundDeposit checkoutFundSucToFail(FundDeposit fundDeposit) {
        FundDeposit deposit = fundDepositMapper.selectByPrimaryKey(fundDeposit.getId());
        if (Objects.isNull(deposit)) {
            throw new R200Exception("???????????????");
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
        String fileName = isOnLine == true ? "????????????" : "????????????" +
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
     * ???????????????????????????????????????
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
                sysFileExportRecordService.exportExcel(depositExcelPath, list, userId, module, siteCode);//????????????
            });
        }
        return record;
    }

    private void setNum2Char(FundDeposit deposit) {
        if (StringUtil.isEmpty(deposit.getMark())) {
            deposit.setMarkStr("");
        } else if (deposit.getMark() == 1) {
            deposit.setMarkStr("????????????");
        } else if (deposit.getMark() == 0) {
            deposit.setMarkStr("????????????");
        } else if (deposit.getMark() == 2) {
            deposit.setMarkStr("??????????????????");
        } else if (deposit.getMark() == 3) {
            deposit.setMarkStr("????????????");
        } else if (deposit.getMark() == 4) {
            deposit.setMarkStr("????????????");
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
                deposit.setStatusStr("??????");
                break;
            case 1:
                deposit.setStatusStr("??????");
                break;
            case 2:
                deposit.setStatusStr("?????????");
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
     * ??????????????????
     *
     * @param fundDeposit
     * @return
     */
    public List<CountEntity> depositCountByStatus(FundDeposit fundDeposit) {
        List<CountEntity> list = fundMapper.depositCountByStatus(fundDeposit);
        return list;
    }

    /**
     * ????????????--????????????????????????????????????
     */
    public List<DepositStatisticsByPayRespDto> depositStatisticByPay(DepositStatisticsByPayDto depositStatisticsByPayDto) {
        List<DepositStatisticsByPayDto> list = fundMapper.depositStatisticByPay(depositStatisticsByPayDto);
        // ????????????
        Map<String, List<DepositStatisticsByPayDto>> depositGroupingBy =
                list.stream().collect(
                        Collectors.groupingBy(
                                DepositStatisticsByPayDto::getType));
        List<DepositStatisticsByPayRespDto> respList = new ArrayList<>();
        for (String type : depositGroupingBy.keySet()) {
            DepositStatisticsByPayRespDto resp = new DepositStatisticsByPayRespDto();

            List<DepositStatisticsByPayDto> deposits = depositGroupingBy.get(type);
            // ???????????????
            Optional<BigDecimal> depositAmountSum = deposits.stream().map(DepositStatisticsByPayDto::getDepositAmountTotal).reduce(BigDecimal::add);
            List<DepositStatisticsByPayDto> orderList = depositGroupingBy.get(type);
            // ???????????????
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
            throw new R200Exception("??????????????????");
        }
        if (!account.getRealName().equals(fundDeposit.getDepositUser())) {
            throw new R200Exception("?????????????????????????????????????????????");
        }
        FundDeposit deposit = new FundDeposit();
        Long despoitId = new SnowFlake().nextId();
        deposit.setOrderNo(despoitId.toString());      // ?????????
        deposit.setDepositUser(fundDeposit.getDepositUser());               // ???????????????
        deposit.setAccountId(account.getId());                // ??????id
        Byte b = 0;
        deposit.setFundSource(b); //pc
        deposit.setIp(fundDeposit.getIp());
        deposit.setDepositAmount(fundDeposit.getDepositAmount());                  // ????????????
        deposit.setActualArrival(fundDeposit.getActualArrival());  // ????????????

        deposit.setCreateUser(getUser().getUsername());              // ????????????
        deposit.setLoginName(fundDeposit.getLoginName());               // ?????????
        deposit.setCompanyPayId(fundDeposit.getCompanyPayId());
        deposit.setOnlinePayId(fundDeposit.getOnlinePayId());
        // ??????????????????
        if (FundDeposit.Mark.onlinePay == fundDeposit.getMark()) {
            deposit.setMark(FundDeposit.Mark.onlinePay);
        } else {
            deposit.setMark(FundDeposit.Mark.offlinePay);                           // ???????????????1 ????????????
        }
        deposit.setStatus(FundDeposit.Status.apply);                            // ?????????2 ?????????
        deposit.setIsPayment(FundDeposit.PaymentStatus.unPay);                  // ???????????? false ?????????
        deposit.setHandingback(Constants.Available.disable);        // ?????????????????????(???1 ??????????????? ??????0 ??????????????????????????????)"
        deposit.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME)); // ????????????
        deposit.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME)); // ????????????
        deposit.setOrderPrefix(OrderConstants.FUND_ORDER_COMPANYDEPOSIT);   // ??????????????? CP ????????????
        deposit.setDepositPostscript(fundDeposit.getDepositPostscript());
        fundDepositMapper.insert(deposit);
        //????????????????????????
        //??????????????????
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
