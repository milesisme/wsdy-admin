package com.wsdy.saasops.modules.fund.service;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static java.util.Objects.nonNull;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.wsdy.saasops.api.modules.apisys.entity.TGmApi;
import com.wsdy.saasops.api.modules.apisys.service.TGmApiService;
import com.wsdy.saasops.api.modules.transfer.dto.BillRequestDto;
import com.wsdy.saasops.api.modules.transferNew.service.DepotService;
import com.wsdy.saasops.api.modules.user.service.DepotWalletService;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.ExcelUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.utils.SnowFlake;
import com.wsdy.saasops.common.utils.StringUtil;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.listener.BizEvent;
import com.wsdy.saasops.listener.BizEventType;
import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.fund.dao.FundAuditMapper;
import com.wsdy.saasops.modules.fund.entity.FundAudit;
import com.wsdy.saasops.modules.fund.mapper.FundMapper;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.dao.MbrBillManageMapper;
import com.wsdy.saasops.modules.member.dto.BillRecordDto;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrAuditAccount;
import com.wsdy.saasops.modules.member.entity.MbrBillDetail;
import com.wsdy.saasops.modules.member.entity.MbrBillManage;
import com.wsdy.saasops.modules.member.entity.MbrWallet;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.member.service.AuditAccountService;
import com.wsdy.saasops.modules.member.service.MbrAccountLogService;
import com.wsdy.saasops.modules.member.service.MbrWalletService;
import com.wsdy.saasops.modules.operate.dao.OprActActivityMapper;
import com.wsdy.saasops.modules.operate.dao.OprActBonusMapper;
import com.wsdy.saasops.modules.operate.entity.OprActActivity;
import com.wsdy.saasops.modules.operate.entity.OprActBonus;
import com.wsdy.saasops.modules.operate.mapper.OperateActivityMapper;
import com.wsdy.saasops.modules.operate.service.OprActActivityCastService;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import com.wsdy.saasops.modules.sys.entity.SysUserEntity;
import com.wsdy.saasops.modules.sys.service.SysFileExportRecordService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class FundReportService extends BaseService<MbrBillManageMapper, MbrBillManage> {

    @Autowired
    private FundMapper fundMapper;
    @Autowired
    private FundAuditMapper fundAuditMapper;
    @Autowired
    private MbrBillManageMapper mbrBillManageMapper;
    @Autowired
    private MbrWalletService walletService;
    @Autowired
    private MbrAccountMapper accountMapper;
    @Value("${fund.billReport.excel.path}")
    private String billReportExcelPath;
    @Value("${fund.audit.excel.path}")
    private String auditReportExcelPath;
    @Value("${fund.auditTopAgy.excel.path}")
    private String auditTopAgyExcelPath;
    @Value("${fund.auditAgy.excel.path}")
    private String auditAgyExcelPath;
    @Autowired
    private MbrWalletService mbrWalletService;
    @Autowired
    private AuditAccountService auditAccountService;
    @Autowired
    private DepotWalletService depotWalletService;
    @Autowired
    private TGmApiService gmApiService;
    @Autowired
    private MbrAccountLogService mbrAccountLogService;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private DepotService depotService;
    @Autowired
    private MbrAccountMapper mbrAccountMapper;
    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private OperateActivityMapper operateActivityMapper;
    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    private OprActActivityCastService actActivityCastService;
    @Autowired
    private OprActBonusMapper oprActBonusMapper;
    @Autowired
    private OprActActivityCastService oprActActivityCastService;
    @Autowired
    private OprActActivityMapper oprActActivityMapper;
    

    public PageUtils queryListPage(MbrBillManage mbrBillManage, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<MbrBillManage> list = fundMapper.findMbrBillManageList(mbrBillManage);
        return BeanUtil.toPagedResult(list);
    }

    @Override
    public MbrBillManage queryObject(Integer id) {
        MbrBillManage billReport = new MbrBillManage();
        billReport.setId(id);
        return Optional.ofNullable(
                fundMapper.findMbrBillManageList(billReport)
                        .stream().findAny()).get().orElse(null);
    }

    public void updateBillMemo(MbrBillManage mbrBillManage) {
        MbrBillManage report = mbrBillManageMapper.selectByPrimaryKey(mbrBillManage.getId());
        report.setMemo(mbrBillManage.getMemo());
        report.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        report.setModifyUser(mbrBillManage.getModifyUser());
        mbrBillManageMapper.updateByPrimaryKey(report);
    }

    public void billReportExportExecl(MbrBillManage mbrBillManage, HttpServletResponse response) {
        String fileName = "????????????" + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xls";
        List<MbrBillManage> billReports = fundMapper.findMbrBillManageList(mbrBillManage);
        List<Map<String, Object>> list = Lists.newArrayList();
        billReports.stream().forEach(rs -> {
            Map<String, Object> paramr = new HashMap<>(8);
            paramr.put("depotNo", rs.getOrderNo());
            paramr.put("agyAccount", rs.getAgyAccount());
            paramr.put("topAgyAccount", rs.getTopAgyAccount());
            paramr.put("loginName", rs.getLoginName());
            paramr.put("realName", rs.getRealName());
            paramr.put("depotName", rs.getDepotName());
            list.add(paramr);
        });
        Workbook workbook = ExcelUtil.commonExcelExportList("mapList", billReportExcelPath, list);
        try {
            ExcelUtil.writeExcel(response, workbook, fileName);
        } catch (IOException e) {
            throw new RRException(e.getMessage());
        }
    }

    /**
     * 	??????????????????????????????
     * 
     * @param audit
     */
    public void auditSave(FundAudit audit) {
        List<FundAudit> fundAudits = audit.getIds()
                .stream().map(id ->
                        castFundAudit(audit, id))
                .collect(Collectors.toList());
        fundAuditMapper.insertList(fundAudits);
    }

    /**
     * 	???????????????set
     * 
     * @param audit
     * @param id
     * @return
     */
    private FundAudit castFundAudit(FundAudit audit, Integer id) {
        MbrAccount account = accountMapper.selectByPrimaryKey(id);
        if (nonNull(account)) {
            audit.setAccountId(id);
            audit.setLoginName(account.getLoginName());
            FundAudit fundAudit = new FundAudit();
            fundAudit.setAccountId(id);
            fundAudit.setActivityId(audit.getActivityId());
            fundAudit.setAmount(audit.getAmount());
            fundAudit.setDepositType(audit.getDepositType());
            fundAudit.setAuditType(audit.getAuditType());
            fundAudit.setAuditMultiple(audit.getAuditMultiple());
            fundAudit.setIsClear(audit.getIsClear());
            fundAudit.setIsCalculateProfit(audit.getIsCalculateProfit());
            fundAudit.setMemo(audit.getMemo());
            fundAudit.setFinancialCode(audit.getFinancialCode());
            fundAudit.setLoginName(account.getLoginName());
            fundAudit.setStatus(Constants.IsStatus.pending);
            fundAudit.setOrderNo(new SnowFlake().nextId() + "");
            fundAudit.setOrderPrefix(OrderConstants.FUND_ORDER_AUDIT);
            fundAudit.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
            fundAudit.setCreateUser(audit.getCreateUser());
            fundAudit.setAuditAddType(audit.getAuditAddType()); // ??????????????????
            fundAudit.setReduceType(audit.getReduceType());     // ??????????????????
            // d:????????????????????????????????????
            if (OrderConstants.FUND_ORDER_CODE_AM.equals(audit.getFinancialCode())) {
                MbrBillDetail mbrBillDetail = mbrWalletService.castWalletAndBillDetail(account.getLoginName(),
                        account.getId(), audit.getFinancialCode(), audit.getAmount(), audit.getOrderNo(), Boolean.FALSE,null,null);
                if (Objects.isNull(mbrBillDetail)) {
                    throw new R200Exception("??????????????????" + account.getLoginName() + ",????????????");
                }
                fundAudit.setBillDetailId(mbrBillDetail.getId());
            }
            return fundAudit;
        }
        return null;
    }

    /**
     * 	??????????????????????????????
     * 
     * @param audit
     * @param pageNo
     * @param pageSize
     * @return
     */
    public PageUtils queryAuditListPage(FundAudit audit, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<FundAudit> list = fundMapper.findFundAuditList(audit);
        list.stream().filter(e->"AM".equals(e.getFinancialCode())).forEach(e2->e2.setAmount(e2.getAmount().multiply(new BigDecimal(-1))));
        return BeanUtil.toPagedResult(list);
    }

    public FundAudit queryAuditObject(Integer id) {
        FundAudit fundAudit = new FundAudit();
        fundAudit.setId(id);
        return Optional.ofNullable(
                fundMapper.findFundAuditList(fundAudit).stream()
                        .findAny()).get().orElse(null);
    }

    /**
     *  	???????????? ?????????????????????????????? 
     * @param fundAudit
     * @param siteCode
     * @param userName
     * @param ip
     * @param bizEvent
     */
    public void auditUpdateStatus(FundAudit fundAudit, String siteCode, String userName, String ip, BizEvent bizEvent) {
        FundAudit audit = fundAuditMapper.selectByPrimaryKey(fundAudit.getId());
        if (!audit.getStatus().equals(Constants.IsStatus.pending)) {
            throw new R200Exception("??????"+audit.getOrderNo()+"????????????????????????????????????");
        }
       
        FundAudit oldAudit = new FundAudit();
        oldAudit.setStatus(audit.getStatus());

        audit.setStatus(fundAudit.getStatus());
        audit.setMemo(fundAudit.getMemo());
        audit.setAuditUser(fundAudit.getModifyUser());
        audit.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
        audit.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        audit.setModifyUser(fundAudit.getModifyUser());
        audit.setIsCalculateProfit(fundAudit.getIsCalculateProfit());
        if (fundAudit.getStatus() == Constants.EVNumber.one) {
        	// ActivityId != null ???????????????????????????????????????????????????
        	if (audit.getActivityId() != null) {
        		auditToBonus(audit, userName, ip);
        		return;
        	} else {
        		artificialIncrease(audit, siteCode, ip);
        		if (OrderConstants.ACCOUNT_REBATE_FA.equals(fundAudit.getFinancialCode())) {
        			rebateMessage(audit, siteCode);
        		}
        	}
        } else if (fundAudit.getStatus() == Constants.EVNumber.zero) {
    		auditTurnDown(audit);
        }
        
        bizEvent.setUserId(audit.getAccountId());
        bizEvent.setDespoitMoney(audit.getAmount());
        bizEvent.setUserName(userName);
        if ("AA".equals(audit.getFinancialCode()) && 1 == fundAudit.getStatus()) {
            bizEvent.setEventType(BizEventType.ACCOUNT_MANUAL_ADD);
        }
        if ("AM".equals(audit.getFinancialCode()) && 1 == fundAudit.getStatus()) {
            bizEvent.setEventType(BizEventType.ACCOUNT_MANUAL_REDUCE);
        }
    	fundAuditMapper.updateByPrimaryKey(audit);
    	//??????????????????
    	mbrAccountLogService.updateFundAuditInfo(audit, oldAudit, userName, ip);
    }

    /**
     * 	??????????????????????????? ??????????????????
     * @param audit
     * @param userName
     * @param ip
     */
    private void auditToBonus(FundAudit audit, String userName, String ip) {
    	BigDecimal amount = audit.getAmount();
        //d: 0???????????? 1???????????? 2 ???????????? 3 ????????????
        Integer source = 2;
        Boolean isAdd = true;
        if ("AM".equals(audit.getFinancialCode())) {
        	source = 3;
        	isAdd = false;
        }
    	// 1. ????????????????????????
		fundAuditMapper.deleteByPrimaryKey(audit.getId());
    	// 2. ?????????????????????
		MbrAccount mbrAccount = mbrMapper.findAccountByAccIds(Arrays.asList(audit.getAccountId())).get(0);
		// s????????????id????????????
		OprActActivity oprActActivity = oprActActivityMapper.selectByPrimaryKey(audit.getActivityId());
		// ??????bonus????????????
        OprActBonus bonus = actActivityCastService.setOprActBonus(audit.getAccountId(), mbrAccount.getLoginName(),
        		audit.getActivityId(), amount, null, oprActActivity.getRuleId());

	     // ??????bonus????????? tip ????????????????????????financialCode??????RS??????
        bonus.setIp(ip);
        bonus.setCreateUser(userName);
        bonus.setApplicationTime(audit.getCreateTimeFrom());
//        bonus.setCatId(oprActBonus.getCatId());                     // ??????id

        bonus.setMemo(audit.getMemo());
        bonus.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));// ????????????
        bonus.setStatus(Constants.EVNumber.one);
        bonus.setSource(source);                    // 0???????????? 1???????????? 2 ???????????? 3 ????????????
        bonus.setBonusAmount(amount);         			// ????????????
        bonus.setDiscountAudit(new BigDecimal(audit.getAuditMultiple() == null ? 0 : audit.getAuditMultiple()));     // ????????????
        bonus.setAuditAmount(auditAccountService.getAddAuditAmount(bonus.getDiscountAudit(), bonus.getDepositedAmount(), bonus.getBonusAmount()));
        SysUserEntity sysUserEntity = getUser();
        bonus.setCreateUser(sysUserEntity.getUsername());
        bonus.setApplicationTime(getCurrentDate(FORMAT_18_DATE_TIME));
        bonus.setAuditUser(sysUserEntity.getUsername());
        bonus.setModifyAmountUser(sysUserEntity.getUsername());
        oprActBonusMapper.insert(bonus);
        
        // d:????????????
        bonus.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
        bonus.setDepositedAmount(null);
        oprActActivityCastService.auditOprActBonusOrSubtract(bonus, OrderConstants.ACTIVITY_AC, oprActActivity.getActivityName(), Boolean.FALSE, isAdd);
		
        log.info("????????????????????????????????? ????????????{}", mbrAccount.getLoginName());
    	// 3. ??????????????????
		OprActBonus actBonus = new OprActBonus();
		actBonus.setStatus(Constants.EVNumber.one);
		actBonus.setAccountId(audit.getAccountId());
		actBonus.setLoginName(mbrAccount.getLoginName());
		OprActBonus oldBonus = new OprActBonus();
		oldBonus.setStatus(Constants.EVNumber.two);
		OprActActivity findOprActActivity = operateActivityMapper.findOprActActivity(audit.getActivityId());
		mbrAccountLogService.bonusActivityAudit(actBonus, oldBonus, findOprActActivity.getActivityName());
    }
    
    private void rebateMessage(FundAudit audit, String siteCode) {
        if (OrderConstants.ACCOUNT_REBATE_FA.equals(audit.getFinancialCode())) {
            applicationEventPublisher.publishEvent(
                    new BizEvent(this, siteCode, audit.getAccountId(), BizEventType.ACCOUNT_REBATE,
                            audit.getAmount(), audit.getOrderPrefix() + audit.getOrderNo()));
        }
    }

    private void auditTurnDown(FundAudit audit) {
        if (OrderConstants.FUND_ORDER_CODE_AM.equals(audit.getFinancialCode())) {
            walletService.castWalletAndBillDetail(audit.getLoginName(),
                    audit.getAccountId(), audit.getFinancialCode(), audit.getAmount(),
                    audit.getOrderNo(), Boolean.TRUE,null,null);
        }
    }

    private void artificialIncrease(FundAudit fundAudit, String siteCode, String ip) {
        if (OrderConstants.FUND_ORDER_CODE_AA.equals(fundAudit.getFinancialCode())
                || OrderConstants.ACCOUNT_REBATE_FA.equals(fundAudit.getFinancialCode())) {

            if (fundAudit.getAuditType() == Constants.EVNumber.one) {
                MbrAuditAccount auditAccount = auditAccountService.insertAccountAudit(
                        fundAudit.getAccountId(), fundAudit.getAmount(),
                        null, new BigDecimal(fundAudit.getAuditMultiple()), null,null,
                        null, Constants.EVNumber.one);
                fundAudit.setAuditId(auditAccount.getId());
            }

            MbrBillDetail mbrBillDetail = walletService.castWalletAndBillDetail(fundAudit.getLoginName(),
                    fundAudit.getAccountId(), fundAudit.getFinancialCode(), fundAudit.getAmount(),
                    fundAudit.getOrderNo(), Boolean.TRUE,null,null);
            fundAudit.setBillDetailId(mbrBillDetail.getId());

        }
        if (OrderConstants.FUND_ORDER_CODE_AM.equals(fundAudit.getFinancialCode())) {
            if (nonNull(fundAudit.getIsClear()) && fundAudit.getIsClear() == Constants.EVNumber.one) {
                auditAccountService.clearAccountAudit(fundAudit.getLoginName(),
                        fundAudit.getModifyUser(), siteCode, "?????????????????????", ip);
            }
        }
    }

    public void auditUpdateMemo(FundAudit fundAudit) {
        FundAudit audit = fundAuditMapper.selectByPrimaryKey(fundAudit.getId());
        audit.setMemo(fundAudit.getMemo());
        audit.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        audit.setModifyUser(fundAudit.getLoginName());
        fundAuditMapper.updateByPrimaryKey(audit);
    }

    public void auditExportExecl(FundAudit fundAudit, HttpServletResponse response) {
        String fileName = "????????????" + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xls";
        List<FundAudit> fundAudits = fundMapper.findFundAuditList(fundAudit);
        List<Map<String, Object>> list = Lists.newArrayList();
        fundAudits.stream().forEach(fs -> {
            Map<String, Object> paramr = new HashMap<>(8);
            paramr.put("orderNo", fs.getOrderPrefix() + fs.getOrderNo());
            paramr.put("agyAccount", fs.getAgyAccount());
            paramr.put("topAgyAccount", fs.getTopAgyAccount());
            paramr.put("loginName", fs.getLoginName());
            paramr.put("amount", fs.getAmount());
            paramr.put("auditUser", fs.getAuditUser());
            list.add(paramr);
        });
        Workbook workbook = ExcelUtil.commonExcelExportList("mapList", billReportExcelPath, list);
        try {
            ExcelUtil.writeExcel(response, workbook, fileName);
        } catch (IOException e) {
            throw new RRException(e.getMessage());
        }
    }

    public long createOrderNumber() {
        return new SnowFlake().nextId();
    }

    public MbrWallet queryAccountBalance(String loginName) {
        return fundMapper.findAccountBalance(loginName);
    }

    public MbrWallet queryDepotOrAccountBalance(MbrBillManage mbrBillManage, String siteCode) {
        TGmApi gmApi = gmApiService.queryApiObject(mbrBillManage.getDepotId(), siteCode);
        Assert.isNull(mbrBillManage.getLoginName(), "???????????????????????????????????????");
        MbrWallet mbrWallet = queryAccountBalance(mbrBillManage.getLoginName());
        if (null == mbrWallet) {
            throw new R200Exception("???????????????????????????????????????");
        }
        mbrWallet.setDepotBeforeBalance(depotWalletService.queryDepotBalance(mbrWallet.getId(), gmApi).getBalance());
        return mbrWallet;
    }

    public int save(BillRequestDto requestDto, String siteCode) {
        MbrAccount mbrAccount = mbrAccountMapper.selectByPrimaryKey(requestDto.getAccountId());
        MbrWallet mbrWallet = queryAccountBalance(mbrAccount.getLoginName());
        requestDto.setAccountId(mbrWallet.getId());
        requestDto.setTransferSource((byte) 0);
        requestDto.setLoginName(mbrAccount.getLoginName());
        if (requestDto.getOpType() == Constants.EVNumber.zero) {
            depotService.accountTransferOut(requestDto, siteCode);
        } else {
            depotService.accountTransferIn(requestDto, siteCode);
        }
        return Constants.EVNumber.one;
    }

    public PageUtils queryBillRecordListPage(BillRecordDto billRecordDto, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<BillRecordDto> list = fundMapper.findBillRecordList(billRecordDto);
        return BeanUtil.toPagedResult(list);
    }


    public SysFileExportRecord exportAuditList(FundAudit fundAudit, SysUserEntity user, String auditReportExcelPath, String module) {
        // ????????????????????????
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(user.getUserId(), module);
        if (null != record) {
            List<FundAudit> fundAuditlist = fundMapper.findFundAuditList(fundAudit);
            fundAuditlist.stream().filter(e->"AM".equals(e.getFinancialCode())).forEach(e2->e2.setAmount(e2.getAmount().multiply(new BigDecimal(-1))));
//            if (fundAuditlist.size() > 10000) {
//                throw new R200Exception("??????????????????1W????????????????????????????????????????????????");
//            }
            List<Map<String, Object>> list = fundAuditlist.stream().map(e -> {
                setNum2Char(e);
                Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
                return entityMap;
            }).collect(Collectors.toList());
            String siteCode = CommonUtil.getSiteCode();
            sysFileExportRecordService.exportExcel(auditReportExcelPath, list, user.getUserId(), module, siteCode);
        }
        return record;
    }

    private void setNum2Char(FundAudit fundAudit){
        // ????????????
        if(StringUtil.isEmpty(fundAudit.getFinancialCode())) {
            fundAudit.setFinancialCode("");
        } else if("AA".equals(fundAudit.getFinancialCode())) {
            fundAudit.setFinancialCode("????????????");
            // ????????????
            if(StringUtil.isEmpty(fundAudit.getAuditAddType())) {
                fundAudit.setAuditAddTypeStr("");
            } else if(fundAudit.getAuditAddType()==0) {
                fundAudit.setAuditAddTypeStr("????????????");
            } else if(fundAudit.getAuditAddType()==1) {
                fundAudit.setAuditAddTypeStr("???????????? ");
            } else if(fundAudit.getAuditAddType()==2) {
                fundAudit.setAuditAddTypeStr("???????????? ");
            } else if (fundAudit.getAuditAddType()==3){
                fundAudit.setAuditAddTypeStr("?????? ");
            }else if (fundAudit.getAuditAddType()==4){
                fundAudit.setAuditAddTypeStr("???????????? ");
            }
        } else if("AM".equals(fundAudit.getFinancialCode())) {
            fundAudit.setFinancialCode("????????????");
            // ??????
            if(StringUtil.isEmpty(fundAudit.getReduceType())) {
                fundAudit.setAuditAddTypeStr("");
            } else if(fundAudit.getReduceType()==0) {
                fundAudit.setAuditAddTypeStr("????????????");
            } else if(fundAudit.getReduceType()==1) {
                fundAudit.setAuditAddTypeStr("???????????? ");
            } else if(fundAudit.getReduceType()==2) {
                fundAudit.setAuditAddTypeStr("???????????? ");
            } else if (fundAudit.getReduceType()==3){
                fundAudit.setAuditAddTypeStr("???????????? ");
            }else if (fundAudit.getReduceType()==4){
                fundAudit.setAuditAddTypeStr("?????? ");
            }
        } else if("FA".equals(fundAudit.getFinancialCode())) {
            fundAudit.setFinancialCode("????????????");
        }
        if (fundAudit.getIsCalculateProfit() != null && fundAudit.getIsCalculateProfit()) {
        	fundAudit.setIsCalculateProfitStr("???");
        } else {
        	fundAudit.setIsCalculateProfitStr("???");
        }
        // ??????
        switch (fundAudit.getStatus()){
            case 0:fundAudit.setStatusStr("??????");break;
            case 1:fundAudit.setStatusStr("??????");break;
            case 2:fundAudit.setStatusStr("?????????");break;
            default:fundAudit.setStatusStr("");
        }
    }

    public List<MbrWallet> queryAccountBalanceByLoginNames(List<String> loginNames) {
        return fundMapper.queryAccountBalanceByLoginNames(loginNames);
    }
}
