package com.wsdy.saasops.modules.member.service;

import com.wsdy.saasops.api.modules.apisys.entity.TGmApi;
import com.wsdy.saasops.api.modules.apisys.service.TGmApiService;
import com.wsdy.saasops.api.modules.transfer.dto.DepotFailDtosDto;
import com.wsdy.saasops.api.modules.user.dto.UserBalanceResponseDto;
import com.wsdy.saasops.api.modules.user.service.DepotWalletService;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.modules.fund.dao.FundAuditMapper;
import com.wsdy.saasops.modules.fund.dao.FundMerchantPayMapper;
import com.wsdy.saasops.modules.fund.entity.AccWithdraw;
import com.wsdy.saasops.modules.fund.entity.FundAudit;
import com.wsdy.saasops.modules.fund.entity.FundMerchantPay;
import com.wsdy.saasops.modules.fund.mapper.FundMapper;
import com.wsdy.saasops.modules.member.dao.*;
import com.wsdy.saasops.modules.member.dto.*;
import com.wsdy.saasops.modules.member.entity.*;
import com.wsdy.saasops.modules.member.mapper.AuditMapper;
import com.wsdy.saasops.modules.operate.dao.OprActBonusMapper;
import com.wsdy.saasops.modules.operate.dao.TGmDepotMapper;
import com.wsdy.saasops.modules.operate.entity.OprActBonus;
import com.wsdy.saasops.modules.operate.entity.TGmDepot;
import com.wsdy.saasops.modules.operate.service.OprActActivityCastService;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.wsdy.saasops.modules.system.systemsetting.dto.PaySet;
import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.wsdy.saasops.common.utils.DateUtil.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;


@Slf4j
@Service
@Transactional
public class AuditAccountService {

    @Autowired
    private MbrAuditAccountMapper auditAccountMapper;
    @Autowired
    private AuditMapper auditMapper;
    @Autowired
    private MbrAccountMapper accountMapper;
    @Autowired
    private MbrWithdrawalCondMapper withdrawalCondMapper;
    @Autowired
    private OprActBonusMapper actBonusMapper;
    @Autowired
    private OprActActivityCastService actActivityCastService;
    @Autowired
    private MbrAuditBonusMapper auditBonusMapper;
    @Autowired
    private MbrWalletService walletService;
    @Autowired
    private AuditCastService auditCastService;
    @Autowired
    private MbrAuditFraudMapper auditFraudMapper;
    @Autowired
    private MbrAuditHistoryMapper auditHistoryMapper;
    @Autowired
    private DepotWalletService depotWalletService;
    @Autowired
    private TGmApiService gmApiService;
    @Autowired
    private MbrWalletMapper mbrWalletMapper;
    @Autowired
    private FundAuditMapper fundAuditMapper;
    @Autowired
    private MbrGroupMapper groupMapper;
    @Autowired
    private TGmDepotMapper depotMapper;
    @Autowired
    private MbrBillDetailMapper billDetailMapper;
    @Autowired
    private MbrAccountLogService mbrAccountLogService;
    @Autowired
    private MbrDepotWalletMapper depotWalletMapper;
    @Autowired
    private FundMapper fundMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private SysSettingService sysSettingService;
    @Autowired
    private FundMerchantPayMapper merchantPayMapper;


    public AuditInfoDto immediatelyAudit(String loginName) {
        // 校验：是否存在会员
        MbrAccount account = new MbrAccount();
        account.setLoginName(loginName);
        MbrAccount mbrAccount = accountMapper.selectOne(account);
        if (isNull(mbrAccount)) {
            throw new R200Exception("无此会员，请核实会员名");
        }
        // 查询会员未取款的稽核，时间倒序
        List<AuditDetailDto> detailDtos =
                auditMapper.findAuditAccountList
                        (mbrAccount.getId(), null, null, Constants.EVNumber.zero);

        if (Collections3.isNotEmpty(detailDtos)) {
            // 获得即时稽核数据
            AuditInfoDto auditInfoDto = casAuditInfoDto(detailDtos);

            // 返回前台：状态2 为输光通过
            auditInfoDto.getAuditDetailDtos().stream().forEach(ds -> {
                BigDecimal validBet = ds.getRemainValidBet().add(ds.getValidBet());
                if (ds.getIsOut() == Constants.EVNumber.one
                        && validBet.compareTo(ds.getValidBet()) == -1) {    // ?? 此处不会执行 ??
                    ds.setStatus(Constants.EVNumber.two);
                }
            });
            return auditInfoDto;
        }
        return null;
    }

    public WithdrawAuditDto withdrawAudit(String loginName) {
        MbrAccount account = new MbrAccount();
        account.setLoginName(loginName);
        MbrAccount mbrAccount = accountMapper.selectOne(account);
        if (nonNull(mbrAccount)) {
            List<AuditDetailDto> detailDtos =
                    auditMapper.findAuditAccountList
                            (mbrAccount.getId(), null, null, Constants.EVNumber.two);
            if (detailDtos.size() > 0) {
                AccWithdraw accWithdraw = fundMapper.findWithdrawAudit(mbrAccount.getId());
                if (nonNull(accWithdraw)) {
                    WithdrawAuditDto auditDto = new WithdrawAuditDto();
                    auditDto.setAuditDetailDtos(detailDtos);
                    auditDto.setStatus(accWithdraw.getStatus());
                    auditDto.setOrderNo(accWithdraw.getOrderPrefix() + accWithdraw.getOrderNo());
                    return auditDto;
                }
            }
        }
        return null;
    }

    public PageUtils auditHistoryList(String loginName, Integer pageNo, Integer pageSize) {
        MbrAccount account = new MbrAccount();
        account.setLoginName(loginName);
        MbrAccount mbrAccount = accountMapper.selectOne(account);
        if (nonNull(mbrAccount)) {
            PageHelper.startPage(pageNo, pageSize);
            MbrAuditHistory auditHistory = new MbrAuditHistory();
            auditHistory.setAccountId(mbrAccount.getId());
            auditHistory.setIsSign(Constants.EVNumber.one);
            List<MbrAuditHistory> auditHistories = auditMapper.findMbrAuditHistory(auditHistory);
            auditHistories.stream().forEach(as -> {
                List<AuditDetailDto> detailDtos =
                        auditMapper.findAuditAccountList(
                                mbrAccount.getId(), as.getStartTime(), as.getEndTime(), Constants.EVNumber.one);
                if (Collections3.isNotEmpty(detailDtos)) {
                    detailDtos.stream().forEach(ds -> {
                        BigDecimal validBet = ds.getRemainValidBet().add(ds.getValidBet());
                        if (ds.getIsOut() == Constants.EVNumber.one
                                && validBet.compareTo(ds.getValidBet()) == -1) {
                            ds.setStatus(Constants.EVNumber.two);
                        }
                    });
                }
                as.setAuditInfoDto(casAuditInfoDto(detailDtos));
            });
            return BeanUtil.toPagedResult(auditHistories);
        }
        return new PageUtils();
    }

    public Map<String, Object> findAccouGroupByName(String loginName) {
        MbrAccount account = new MbrAccount();
        account.setLoginName(loginName);
        account = accountMapper.selectOne(account);
        if (isNull(account)) {
            throw new R200Exception("无此会员，请核实会员名");
        }
        MbrGroup group = groupMapper.selectByPrimaryKey(account.getGroupId());
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("GroupName", group.getGroupName());
        MbrWithdrawalCond withdrawalCond = getMbrWithdrawalCond(account.getId());
        if (Objects.nonNull(withdrawalCond)) {
            objectMap.put("OverFee", Objects.nonNull(withdrawalCond.getOverFee())
                    ? withdrawalCond.getOverFee() : BigDecimal.ZERO);
        }
        objectMap.put("TagencyId", account.getTagencyId());
        return objectMap;
    }

    public List<AuditDetailDto> auditHistoryBonusList(String loginName) {
        MbrAccount account = new MbrAccount();
        account.setLoginName(loginName);
        MbrAccount mbrAccount = accountMapper.selectOne(account);
        if (nonNull(mbrAccount)) {
            MbrAuditHistory auditHistory = new MbrAuditHistory();
            auditHistory.setAccountId(mbrAccount.getId());
            auditHistory.setIsSign(Constants.EVNumber.zero);
            List<MbrAuditHistory> auditHistories = auditMapper.findMbrAuditHistory(auditHistory);
            if (Collections3.isNotEmpty(auditHistories)) {
                return auditMapper.findAuditAccountList(mbrAccount.getId(),
                        auditHistories.get(0).getStartTime(),
                        getCurrentDate(FORMAT_18_DATE_TIME), Constants.EVNumber.one);
            }
        }
        return null;
    }

    public MbrAuditBonus auditBonusInfo(Integer auditBonusId) {
        MbrAuditBonus auditBonus = auditBonusMapper.selectByPrimaryKey(auditBonusId);
        if (nonNull(auditBonus) && nonNull(auditBonus.getOrderNo())) {
            MbrAuditFraud auditFraud = new MbrAuditFraud();
            auditFraud.setOrderNo(auditBonus.getOrderNo());
            auditBonus.setAuditFrauds(auditFraudMapper.select(auditFraud));
        }
        return auditBonus;
    }


    public Map<String, Object> isWithdrawal(Integer accountId) {
        List<MbrAuditAccount> accountAudits = auditMapper.finAuditByLoginNameList(accountId);
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("isPassed", Boolean.TRUE);
        if (Collections3.isNotEmpty(accountAudits)) {
            Optional<BigDecimal> depositValidBet = accountAudits.stream()
                    .filter(p -> nonNull(p.getAuditAmount()))
                    .map(MbrAuditAccount::getAuditAmount).reduce(BigDecimal::add);

            Optional<BigDecimal> completedValidBet = accountAudits.stream()
                    .filter(p -> nonNull(p.getValidBet()))
                    .map(MbrAuditAccount::getValidBet).reduce(BigDecimal::add);

            hashMap.put("depositValidBet", depositValidBet.isPresent() ? depositValidBet.get() : BigDecimal.ZERO);
            hashMap.put("completedValidBet", completedValidBet.isPresent() ? completedValidBet.get() : BigDecimal.ZERO);
            hashMap.put("isPassed", Boolean.FALSE);
        }

        // 判断是否开启支付宝取款 产品提出只需要大开关，测试提出来要与原有逻辑一直，故执行双逻辑
        FundMerchantPay pay = new FundMerchantPay();
        pay.setAvailable(Constants.EVNumber.one);
        pay.setMethodType(Constants.EVNumber.two);
        List<FundMerchantPay> list = merchantPayMapper.select(pay);
        PaySet paySet = sysSettingService.queryPaySet();
        if (Objects.nonNull(list) && list.size() > 0) {
            Integer alipayEnable = paySet.getAlipayEnable();
            // 如果支付宝提款的大开关是关闭的，那么返回不允许支付宝提款
            if (nonNull(alipayEnable) && Constants.EVNumber.zero == alipayEnable) {
                hashMap.put("alipayEnable", Constants.EVNumber.zero);
            } else {
                hashMap.put("alipayEnable", Constants.EVNumber.one);
            }
        } else {
            hashMap.put("alipayEnable", 0);
        }

        // 判断是否开启极速取款
        Integer fastWithdrawEnable = paySet.getFastWithdrawEnable();
        if (nonNull(fastWithdrawEnable)) {
            hashMap.put("fastWithdrawEnable", fastWithdrawEnable);
        }

        Example example = new Example(FundMerchantPay.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("methodType", Constants.EVNumber.three);
        criteria.andEqualTo("available", Constants.EVNumber.one);
        criteria.andIn("currencyCode", Arrays.asList("EBPAY","TOPAY"));
        List<FundMerchantPay> list2 = merchantPayMapper.selectByExample(example);

        // 判断是否开启ebpay，不能这样判断，需要对
        Integer ebPayWithdrawEnable = list2.stream().anyMatch(pTmp -> "EBPAY".equals(pTmp.getCurrencyCode()))
                ? Constants.EVNumber.one : Constants.EVNumber.zero;
        if (nonNull(ebPayWithdrawEnable)) {
            hashMap.put("ebPayWithdrawEnable", ebPayWithdrawEnable);
        }

        // 判断是否开启topay
        Integer toPayWithdrawEnable = list2.stream().anyMatch(pTmp -> "TOPAY".equals(pTmp.getCurrencyCode()))
                ? Constants.EVNumber.one : Constants.EVNumber.zero;
        if (nonNull(toPayWithdrawEnable)) {
            hashMap.put("toPayWithdrawEnable", toPayWithdrawEnable);
        }

        return hashMap;
    }

    // 查询即时稽核数据
    public Map<String, Object> immediatelyAudit(Integer accountId){
        // 查询即时稽核信息
        Map<String, Object> hashMap = new HashMap<>();
        List<AuditDetailDto> detailDtos = auditMapper.findAuditAccountList
                (accountId, null, null, Constants.EVNumber.zero);
        if (Collections3.isNotEmpty(detailDtos)) {
            AuditInfoDto auditInfoDto = casAuditInfoDto(detailDtos);
            auditInfoDto.getAuditDetailDtos().stream().forEach(ds -> {
                BigDecimal validBet = ds.getRemainValidBet().add(ds.getValidBet());
                if (ds.getIsOut() == Constants.EVNumber.one
                        && validBet.compareTo(ds.getValidBet()) == -1) {
                    ds.setStatus(Constants.EVNumber.two);
                }
            });
            hashMap.put("depositResidueValidBet",auditInfoDto.getDepositResidueValidBet()); // 存款剩余流水
            hashMap.put("depositValidBet",auditInfoDto.getDepositValidBet());               // 存款所需流水
        }

        return hashMap;
    }

    public List<MbrAuditAccount> getMbrAuditAccounts(Integer accountId) {
        MbrAuditAccount accountAudit = new MbrAuditAccount();
        accountAudit.setIsDrawings(Constants.EVNumber.zero);
        accountAudit.setAccountId(accountId);
        return auditMapper.finAuditList(accountAudit);
    }

    /**
     * 获得即时稽核返回数据
     *
     * @param detailDtos 会员未取款稽核liest
     * @return 稽核汇总数据和列表明细
     */
    private AuditInfoDto casAuditInfoDto(List<AuditDetailDto> detailDtos) {
        AuditInfoDto infoDto = new AuditInfoDto();

        if (Collections3.isNotEmpty(detailDtos)) {
            // 存款稽核：获取总流水/总存款/总剩余流水等数据
            castDepositValue(infoDto, detailDtos);
            // 优惠稽核：废弃
            castBounsValue(infoDto, detailDtos);

            // 未通过稽核的存款稽核 条数
            long DepositCount = detailDtos.stream().filter(
                    as -> Constants.EVNumber.zero == as.getStatus()
                            && as.getAuditType() == Constants.EVNumber.zero)
                    .map(AuditDetailDto::getId).count();
            // 未通过稽核的优惠稽核 条数
            long bounsCount = detailDtos.stream().filter(
                    as -> Constants.EVNumber.zero == as.getStatus()
                            && as.getAuditType() == Constants.EVNumber.one)
                    .map(AuditDetailDto::getId).count();
            // 存款稽核未通过
            if (DepositCount > 0) {
                infoDto.setDepositSucceed(Boolean.FALSE);
            }
            // 优惠稽核未通过
            if (bounsCount > 0) {
                infoDto.setDbounsSucceed(Boolean.FALSE);
            }
            // 稽核列表明细
            infoDto.setAuditDetailDtos(detailDtos);
        }

        return infoDto;
    }

    private void castDepositValue(AuditInfoDto infoDto, List<AuditDetailDto> detailDtos) {
        // 计算所有稽核的总存款：属于存款不属于转账的
        Optional<BigDecimal> depositTotal = detailDtos.stream()
                .filter(p -> p.getAuditType() == Constants.EVNumber.zero
                        && nonNull(p.getDepositAmount())
                        && p.getDepositAmount().compareTo(BigDecimal.ZERO) == 1
                        && p.getDiscardAmount().compareTo(BigDecimal.ZERO) == 0)    // discardAmount 为0表示是存款不是转账
                .map(AuditDetailDto::getDepositAmount).reduce(BigDecimal::add);
        // 计算所有稽核的总转账：属于转账的不属于存款的
        Optional<BigDecimal> depositDiscardAmount = detailDtos.stream()
                .filter(p -> p.getAuditType() == Constants.EVNumber.zero
                        && nonNull(p.getDiscardAmount())
                        && p.getDiscardAmount().compareTo(BigDecimal.ZERO) == 1)
                .map(AuditDetailDto::getDiscardAmount).reduce(BigDecimal::add);
        // 计算所有稽核的总流水要求
        Optional<BigDecimal> depositValidBet = detailDtos.stream()
                .filter(p -> p.getAuditType() == Constants.EVNumber.zero
                        && nonNull(p.getAuditAmount()))
                .map(AuditDetailDto::getAuditAmount).reduce(BigDecimal::add);
        // 计算所有稽核的总有效投注
        Optional<BigDecimal> currentDepositValidBet = detailDtos.stream()
                .filter(p -> p.getAuditType() == Constants.EVNumber.zero
                        && nonNull(p.getValidBet()))
                .map(AuditDetailDto::getValidBet).reduce(BigDecimal::add);


        BigDecimal bigDecimalDeposit = depositTotal.isPresent() ? depositTotal.get() : BigDecimal.ZERO;                         // 总存款
        BigDecimal bigDecimalDiscard = depositDiscardAmount.isPresent() ? depositDiscardAmount.get() : BigDecimal.ZERO;         // 总转账

        infoDto.setDepositTotal(bigDecimalDeposit.add(bigDecimalDiscard));                                                      // 稽核总存款 = 总存款 + 总转账
        infoDto.setDepositValidBet(depositValidBet.isPresent() ? depositValidBet.get() : BigDecimal.ZERO);                     // 总流水要求
        infoDto.setCurrentDepositValidBet(currentDepositValidBet.isPresent() ? currentDepositValidBet.get() : BigDecimal.ZERO); // 总有效投注

        // 处理获得存款稽核list
        List<MbrAuditAccount> auditAccounts = detailDtos.stream().filter(a ->
                a.getAuditType() == Constants.EVNumber.zero)
                .collect(Collectors.toList()).stream().map(d -> {
                    MbrAuditAccount auditAccount = new MbrAuditAccount();
                    auditAccount.setId(d.getId());
                    auditAccount.setStatus(d.getStatus());
                    auditAccount.setIsOut(d.getIsOut());
                    auditAccount.setValidBet(d.getValidBet());
                    auditAccount.setAuditAmount(d.getAuditAmount());
                    return auditAccount;
                }).collect(Collectors.toList());
        // 获取存款剩余流水
        BigDecimal depositResidueValidBet = getResidueValidBet(auditAccounts);

        infoDto.setDepositResidueValidBet(depositResidueValidBet);
    }

    private void castBounsValue(AuditInfoDto infoDto, List<AuditDetailDto> detailDtos) {
        Optional<BigDecimal> transferTotal = detailDtos.stream()
                .filter(p -> p.getAuditType() == Constants.EVNumber.one &&
                        nonNull(p.getDepositAmount()))
                .map(AuditDetailDto::getDepositAmount).reduce(BigDecimal::add);
        Optional<BigDecimal> bonusAmount = detailDtos.stream()
                .filter(p -> p.getAuditType() == Constants.EVNumber.one &&
                        nonNull(p.getDiscountAmount()))
                .map(AuditDetailDto::getDiscountAmount).reduce(BigDecimal::add);
        Optional<BigDecimal> transferValidBet = detailDtos.stream()
                .filter(p -> p.getAuditType() == Constants.EVNumber.one &&
                        nonNull(p.getAuditAmount()))
                .map(AuditDetailDto::getAuditAmount).reduce(BigDecimal::add);
        Optional<BigDecimal> currentTransferValidBet = detailDtos.stream()
                .filter(p -> p.getAuditType() == Constants.EVNumber.one &&
                        nonNull(p.getValidBet()))
                .map(AuditDetailDto::getValidBet).reduce(BigDecimal::add);
        infoDto.setTransferTotal(transferTotal.isPresent()
                ? transferTotal.get() : BigDecimal.ZERO);
        infoDto.setBonusAmount(bonusAmount.isPresent()
                ? bonusAmount.get() : BigDecimal.ZERO);
        infoDto.setTransferValidBet(transferValidBet.isPresent()
                ? transferValidBet.get() : BigDecimal.ZERO);
        infoDto.setCurrentTransferValidBet(currentTransferValidBet.isPresent()
                ? currentTransferValidBet.get() : BigDecimal.ZERO);

        Optional<BigDecimal> bonusNotAuditAmount = detailDtos.stream()
                .filter(p -> p.getAuditType() == Constants.EVNumber.one
                        && p.getStatus() == Constants.EVNumber.zero
                        && nonNull(p.getAuditAmount()))
                .map(AuditDetailDto::getAuditAmount).reduce(BigDecimal::add);

        Optional<BigDecimal> bonusNotValidBet = detailDtos.stream()
                .filter(p -> p.getAuditType() == Constants.EVNumber.one
                        && p.getStatus() == Constants.EVNumber.zero
                        && nonNull(p.getValidBet()))
                .map(AuditDetailDto::getValidBet).reduce(BigDecimal::add);

        infoDto.setBonusResidueValidBet(bonusNotAuditAmount.isPresent()
                && bonusNotValidBet.isPresent()
                ? bonusNotAuditAmount.get().subtract(bonusNotValidBet.get())
                : BigDecimal.ZERO);
    }

    public void updateAccountAuditList(List<AuditDetailDto> detailDtos, String userName, String ip) {
        detailDtos.stream().forEach(audit -> {
            if (audit.getAuditType() == Constants.EVNumber.zero || audit.getAuditType() == Constants.EVNumber.two) {
                MbrAuditAccount accountAudit = auditAccountMapper.selectByPrimaryKey(audit.getId());
                if (accountAudit.getStatus() == Constants.EVNumber.zero) {
                    accountAudit.setModifyUser(userName);
                    accountAudit.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
                    accountAudit.setAuditAmount(nonNull(audit.getAuditAmount())
                            ? audit.getAuditAmount() : BigDecimal.ZERO);
                    auditAccountMapper.updateByPrimaryKey(accountAudit);

                    //操作日志
                    mbrAccountLogService.updateAccountAuditInfo(accountAudit, userName, ip);
                }
            }
            if (audit.getAuditType() == Constants.EVNumber.one) {
                MbrAuditBonus auditBonus = auditBonusMapper.selectByPrimaryKey(audit.getId());
                if (auditBonus.getIsValid() == Constants.EVNumber.one
                        && auditBonus.getStatus() == Constants.EVNumber.zero
                        && auditBonus.getIsDrawings() == Constants.EVNumber.zero) {
                    auditBonus.setModifyUser(userName);
                    auditBonus.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
                    auditBonus.setAuditAmount(nonNull(audit.getAuditAmount())
                            ? audit.getAuditAmount() : BigDecimal.ZERO);
                    auditBonusMapper.updateByPrimaryKey(auditBonus);

                    //操作日志
                    mbrAccountLogService.updateAccountAuditBonusInfo(auditBonus, userName, ip);
                }
            }
        });
    }

    public void clearAccountAudit(String loginName, String userName, String siteCode, String memo, String ip) {
        MbrAccount account = new MbrAccount();
        account.setLoginName(loginName);
        MbrAccount mbrAccount = accountMapper.selectOne(account);
        if (nonNull(mbrAccount)) {
            MbrAuditAccount accountAudit = new MbrAuditAccount();
            accountAudit.setAccountId(mbrAccount.getId());
            accountAudit.setIsDrawings(Constants.EVNumber.zero);
            accountAudit.setStatus(Constants.EVNumber.zero);
            List<MbrAuditAccount> audits = auditAccountMapper.select(accountAudit);
            audits.stream().forEach(audit -> {
                audit.setModifyUser(userName);
                audit.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
                audit.setAuditAmount(BigDecimal.ZERO);
                audit.setStatus(Constants.EVNumber.one);
                audit.setPassTime(getCurrentDate(FORMAT_18_DATE_TIME));
                if (StringUtils.isNotEmpty(memo)) {
                    audit.setMemo(memo);
                }
                auditAccountMapper.updateByPrimaryKey(audit);

                //添加操作日志
                accountAudit.setLoginName(mbrAccount.getLoginName());
                mbrAccountLogService.cleanAccountAuditInfo(accountAudit, userName, ip);
            });

            MbrAuditBonus auditBonus = new MbrAuditBonus();
            auditBonus.setAccountId(mbrAccount.getId());
            auditBonus.setIsDrawings(Constants.EVNumber.zero);
            auditBonus.setStatus(Constants.EVNumber.zero);
            List<MbrAuditBonus> auditBonuses = auditBonusMapper.select(auditBonus);
            auditBonuses.stream().forEach(bonus -> {
                if (bonus.getIsValid() == Constants.EVNumber.one
                        && bonus.getIsDrawings() == Constants.EVNumber.zero) {
                    bonus.setModifyUser(userName);
                    bonus.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
                    bonus.setAuditAmount(BigDecimal.ZERO);
                    bonus.setStatus(Constants.EVNumber.one);
                    if (StringUtils.isNotEmpty(memo)) {
                        bonus.setMemo(memo);
                    }
                    auditBonusMapper.updateByPrimaryKey(bonus);

                    //添加操作日志
                    bonus.setLoginName(mbrAccount.getLoginName());
                    mbrAccountLogService.cleanAccountAuditBonusInfo(bonus, userName, ip);
                }
            });
//            List<String> sitePrefix = auditCastService.getSitePrefix(siteCode);
//            auditCastService.doingCronAuditAccount(siteCode, mbrAccount.getId(), sitePrefix, Boolean.FALSE);
        }
    }

    public AuditBonusDto auditDetail(Integer accountId) {
        AuditBonusDto dto = new AuditBonusDto();
        List<MbrAuditAccount> auditAccounts = getMbrAuditAccounts(accountId);
        if (Collections3.isNotEmpty(auditAccounts)) {
            long count = auditAccounts.stream().filter(
                    as -> Constants.EVNumber.zero == as.getStatus())
                    .map(MbrAuditAccount::getId).count();
            if (count > 0) {
                dto.setIsSucceed(Boolean.FALSE);
                Optional<BigDecimal> totalValidBet = auditAccounts.stream()
                        .filter(p -> nonNull(p.getAuditAmount()))
                        .map(MbrAuditAccount::getAuditAmount).reduce(BigDecimal::add);
                dto.setTotalValidBet(totalValidBet.get());
                dto.setResidueValidBet(getResidueValidBet(auditAccounts));
            }
        }
        return dto;
    }

    // 获取存款剩余流水
    private BigDecimal getResidueValidBet(List<MbrAuditAccount> auditAccounts) {
        // 时间正序排序
        Collections.sort(auditAccounts, Comparator.comparing(MbrAuditAccount::getId));

        BigDecimal bigDecimal = BigDecimal.ZERO;
        Boolean isCast = Boolean.FALSE;

        for (MbrAuditAccount auditAccount : auditAccounts) {
            // 稽核不通过或者非第一条输光：时间正序第一条输光通过的，这一条是不计算还剩余流水的，因为不会再往下溢了
            if (auditAccount.getStatus() == Constants.EVNumber.zero
                    || (isCast && auditAccount.getIsOut() == Constants.EVNumber.one)) {
                if (auditAccount.getAuditAmount() == null) {
                    auditAccount.setAuditAmount(BigDecimal.ZERO);
                }
                BigDecimal bigDecimal1 = auditAccount.getAuditAmount().subtract(auditAccount.getValidBet());          // 该条剩余流水 = 该条流水需求-有效投注
                bigDecimal = bigDecimal.add(bigDecimal1);
                isCast = Boolean.TRUE;
            }
        }
        return bigDecimal;
    }

    /**
     * 新增稽核
     *
     * @param accountId      会员id
     * @param depositAmount  稽核本金
     * @param depositId      存款表id：不绑定存款表为null
     * @param auditMultiple  指定稽核倍数：null 使用会员组稽核倍数
     * @param auditAmount    流水需求 ： 当isSign为4时，直接使用该流水需求，稽核倍数为0
     * @param discountAmount 优惠金额
     * @param ruleId         活动id：  不绑定活动为null
     * @param isSign         1：存款稽核(指定稽核本金+会员组稽核倍数)/指定稽核(指定稽核倍数和稽核本金)
     *                       2：使用稽核倍数：计算规则为 ： 活动/任务的优惠稽核(（稽核本金+稽核优惠）* 指定稽核倍数-稽核本金)
     *                       4：使用入参流水需求
     *                       5. 使用入参稽核本金做存款，流水需求做流水
     *                       6. 使用稽核倍数和稽核金额
     * @return
     */
    public MbrAuditAccount insertAccountAudit(Integer accountId, BigDecimal depositAmount, Integer depositId,
                                              BigDecimal auditMultiple, BigDecimal auditAmount, BigDecimal discountAmount,
                                              Integer ruleId, Integer isSign) {
        MbrAuditAccount accountAudit = new MbrAuditAccount();

        // 会员组取款条件
        MbrWithdrawalCond withdrawalCond = getMbrWithdrawalCond(accountId);
        Integer depositAudit = Constants.EVNumber.zero;     // 会员组的稽核倍数
        BigDecimal depositOutBalance = BigDecimal.ZERO;     // 放款额度
        if (Objects.nonNull(withdrawalCond)) {
            depositAudit = Objects.nonNull(withdrawalCond.getWithDrawalAudit()) ?
                    withdrawalCond.getWithDrawalAudit() : withdrawalCond.getWithDrawalAudit();
            depositOutBalance = Objects.nonNull(withdrawalCond.getOverFee()) ?
                    withdrawalCond.getOverFee() : BigDecimal.ZERO;
        }
        // 使用会员组的稽核倍数
        if (isNull(auditMultiple) && isSign == Constants.EVNumber.one) {
            accountAudit.setDepositAudit(new BigDecimal(depositAudit));     // 稽核倍数
            accountAudit.setAuditAmount(CommonUtil.adjustScale(
                    new BigDecimal(depositAudit).multiply(depositAmount))); // 流水需求 = 稽核倍数 * 稽核本金

        }
        // 使用入参的稽核倍数
        if (nonNull(auditMultiple) && isSign == Constants.EVNumber.one) {
            accountAudit.setDepositAudit(auditMultiple);    // 稽核倍数
            accountAudit.setAuditAmount(CommonUtil.adjustScale(
                    auditMultiple.multiply(depositAmount))); // 流水需求 = 稽核倍数 * 稽核本金

        }
        // 使用入参的流水需求，稽核倍数为0
        if (isSign == Constants.EVNumber.four || isSign == Constants.EVNumber.five) {
            accountAudit.setDepositAudit(BigDecimal.ZERO);  // 稽核倍数
            accountAudit.setAuditAmount(auditAmount);       // 流水需求
        }
        // 活动/任务的优惠稽核(（稽核本金+稽核优惠）* 指定稽核倍数-稽核本金)
        if (isSign == Constants.EVNumber.two) {
            accountAudit.setDepositAudit(auditMultiple);    // 稽核倍数

            depositAmount = nonNull(depositAmount) ? depositAmount : BigDecimal.ZERO;
            discountAmount = nonNull(discountAmount) ? discountAmount : BigDecimal.ZERO;
            BigDecimal amount = CommonUtil.adjustScale(
                    (depositAmount.add(discountAmount)).multiply(accountAudit.getDepositAudit()).subtract(depositAmount));

            accountAudit.setAuditAmount(amount);            // 流水需求
        }

        if (isSign == Constants.EVNumber.six||isSign == Constants.EVNumber.eight) {
            accountAudit.setDepositAudit(auditMultiple);
            accountAudit.setAuditAmount(auditAmount);
        }

        if(isSign == Constants.EVNumber.seven){
            accountAudit.setDepositAudit(auditMultiple);
            accountAudit.setAuditAmount(CommonUtil.adjustScale(auditMultiple.multiply(depositAmount)));
            accountAudit.setAuditType(2);
        }
        // 存款金额
        if (isSign == Constants.EVNumber.two || isSign == Constants.EVNumber.four || isSign == Constants.EVNumber.six) {
            accountAudit.setDepositAmount(discountAmount);  // 活动/任务/指定流水->优惠金额为存款金额；
        } else {    // 入参稽核本金为存款金额
            accountAudit.setDepositAmount(nonNull(depositAmount) ? depositAmount : BigDecimal.ZERO);
        }
        // 优惠金额
        accountAudit.setDiscountAmount(nonNull(discountAmount) ? discountAmount : BigDecimal.ZERO);




        MbrAccount account = accountMapper.selectByPrimaryKey(accountId);
        accountAudit.setAccountId(accountId);                       // 会员id
        accountAudit.setLoginName(account.getLoginName());          // 会员姓名
        accountAudit.setRuleId(ruleId);                             // 规则表id
        accountAudit.setDepositId(depositId);                       // 存款表id
        accountAudit.setDepositOutBalance(depositOutBalance);       // 放宽额度
        accountAudit.setTime(getCurrentDate(FORMAT_18_DATE_TIME));  // 创建时间

        accountAudit.setDepositBalance(BigDecimal.ZERO);            // 存款时余额 -派彩
        accountAudit.setIsDrawings(Constants.EVNumber.zero);        // 是否提款 0否 1是 2提款中
        accountAudit.setPayOut(BigDecimal.ZERO);                    // 派彩
        accountAudit.setIsOut(Constants.EVNumber.zero);             // 是输光 0否 1是
        accountAudit.setStatus(Constants.EVNumber.zero);            // 稽核状态 0 不通过 1通过
        accountAudit.setValidBet(BigDecimal.ZERO);                  // 有效投注
        accountAudit.setRemainValidBet(BigDecimal.ZERO);            // 累计剩余有效投注额:溢出投注
        accountAudit.setBonusRemainValidBet(BigDecimal.ZERO);       // 优惠溢出有效投注 ：废弃
        accountAudit.setReduceAuditAmount(BigDecimal.ZERO);         //
        accountAudit.setDiscardAmount(BigDecimal.ZERO);             //
        auditAccountMapper.insert(accountAudit);
        return accountAudit;
    }

    public BigDecimal getAddAuditAmount(BigDecimal auditMultiple, BigDecimal depositAmount, BigDecimal discountAmount) {
        auditMultiple = nonNull(auditMultiple) ? auditMultiple : BigDecimal.ZERO;
        depositAmount = nonNull(depositAmount) ? depositAmount : BigDecimal.ZERO;
        discountAmount = nonNull(discountAmount) ? discountAmount : BigDecimal.ZERO;
        BigDecimal amount = CommonUtil.adjustScale(
                (depositAmount.add(discountAmount))
                        .multiply(auditMultiple).subtract(depositAmount));
        return amount;
    }
    
    /**
     * 	首存送，存就送流水计算逻辑：本金*（n-1）+优惠*n，   n是优惠流水倍水
     * 
     * @param auditMultiple
     * @param depositAmount
     * @param discountAmount
     * @return
     */
    public BigDecimal getAddAuditAmountForDeposit(BigDecimal auditMultiple, BigDecimal depositAmount, BigDecimal discountAmount) {
    	auditMultiple = nonNull(auditMultiple) ? auditMultiple : BigDecimal.ONE;
    	depositAmount = nonNull(depositAmount) ? depositAmount : BigDecimal.ZERO;
    	discountAmount = nonNull(discountAmount) ? discountAmount : BigDecimal.ZERO;
    	
    	BigDecimal discountAudit = discountAmount.multiply(auditMultiple);
    	BigDecimal depositAudit = depositAmount.multiply(auditMultiple.subtract(BigDecimal.ONE));
    	return discountAudit.add(depositAudit);
    }

    public BigDecimal getPayOffAddAuditAmount(BigDecimal auditMultiple, BigDecimal amount,BigDecimal moneyMultiple,BigDecimal money) {
        auditMultiple = nonNull(auditMultiple) ? auditMultiple : BigDecimal.ZERO;
        amount = nonNull(amount) ? amount : BigDecimal.ZERO;
        moneyMultiple = nonNull(moneyMultiple) ? moneyMultiple : BigDecimal.ZERO;
        money = nonNull(money) ? money : BigDecimal.ZERO;
        BigDecimal retAmount = CommonUtil.adjustScale(
                (auditMultiple.multiply(amount))
                        .add(moneyMultiple.multiply(money)));
        return retAmount;
    }

    public MbrWithdrawalCond getMbrWithdrawalCond(int accountId) {
        MbrAccount account = accountMapper.selectByPrimaryKey(accountId);
        MbrWithdrawalCond withdrawalCond = new MbrWithdrawalCond();
        withdrawalCond.setGroupId(account.getGroupId());
        return withdrawalCondMapper.selectOne(withdrawalCond);
    }


    public AuditBonusDto getDepotAuditDto(Integer accountId, Integer depotId) {
        MbrAccount account = accountMapper.selectByPrimaryKey(accountId);
        AuditBonusDto bonusDto = outAuditBonus(account, depotId);
        return nonNull(bonusDto) ? bonusDto : new AuditBonusDto();
    }


    public Boolean isBounsOut(Integer accountId, Integer depotId) {
        List<MbrAuditBonus> auditBonuses = auditCastService.
                findMbrAuditBonusByAccountId(accountId, depotId, null);
        if (Collections3.isNotEmpty(auditBonuses)) {
            MbrAuditBonus auditBonus = auditBonuses.get(0);
            long count = auditBonuses.stream().filter(p ->
                    Constants.EVNumber.zero == p.getStatus())
                    .map(MbrAuditBonus::getId).count();
            if (auditBonus.getIsValid() == Constants.EVNumber.zero) {
                return Boolean.FALSE;
            }
            if (count > 0) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    /**
     * 转入 状态待处理 即冻结service
     * * @param amount 转账金额
     */
    public void accountBonusFreeze(OprActBonus bonus, Integer billManageId, BigDecimal amount) {
        bonus.setStatus(Constants.EVNumber.one);
        bonus.setBillDetailId(billManageId);
        bonus.setTransferAmount(amount);
        actBonusMapper.updateByPrimaryKey(bonus);
    }

    /**
     * 会员转账手动刷新 更新优惠service
     * * @param amount 转账金额
     */
    public void accountBonusFreeze(MbrBillManage mbrBillManage) {
        OprActBonus bonus = actBonusMapper.selectByPrimaryKey(mbrBillManage.getBonusId());
        if (mbrBillManage.getStatus() == Constants.EVNumber.one) {
            MbrAccount account = new MbrAccount();
            account.setId(mbrBillManage.getAccountId());
            account.setLoginName(mbrBillManage.getLoginName());
            accountUseBonus(bonus, account, mbrBillManage.getAmount(), mbrBillManage.getId(),
                    mbrBillManage.getDepotId(), mbrBillManage.getCatId());
        }
        if (mbrBillManage.getStatus() == Constants.EVNumber.two) {
            bonus.setStatus(Constants.EVNumber.three);
            bonus.setBillDetailId(mbrBillManage.getId());
            bonus.setTransferAmount(mbrBillManage.getAmount());
            actBonusMapper.updateByPrimaryKey(bonus);
        }
    }


    /**
     * 转入成功
     *
     * @param bonus
     * @param depotId
     * @param catId
     */
    public void accountUseBonus(OprActBonus bonus, MbrAccount account, BigDecimal amount,
                                Integer billManageId, Integer depotId, Integer catId) {
        MbrAuditBonus bonusAudit = new MbrAuditBonus();
        bonusAudit.setAccountId(account.getId());
        bonusAudit.setLoginName(account.getLoginName());
        bonusAudit.setTime(getCurrentDate(FORMAT_18_DATE_TIME));
        bonusAudit.setValidBet(BigDecimal.ZERO);
        BigDecimal outBalance = BigDecimal.ZERO;
        MbrWithdrawalCond withdrawalCond = getMbrWithdrawalCond(account.getId());
        if (Objects.nonNull(withdrawalCond)) {
            outBalance = Objects.nonNull(withdrawalCond.getOverFee())
                    ? withdrawalCond.getOverFee() : BigDecimal.ZERO;
        }
        TGmDepot depot = depotMapper.selectByPrimaryKey(depotId);
        bonusAudit.setDepotId(depotId);
        bonusAudit.setDepotName(depot.getDepotName());
        bonusAudit.setDepotCode(depot.getDepotCode());
        bonusAudit.setOutBalance(outBalance);
        bonusAudit.setDepositAmount(nonNull(amount) ? amount : BigDecimal.ZERO);
        bonusAudit.setDiscountAmount(bonus.getBonusAmount());
        BigDecimal auditAmount = bonusAudit.getDepositAmount()
                .add(bonusAudit.getDiscountAmount());
        bonusAudit.setAuditAmount(CommonUtil.adjustScale(
                bonus.getDiscountAudit().multiply(auditAmount)));
        bonusAudit.setRemainValidBet(BigDecimal.ZERO);
        bonusAudit.setScope(bonus.getScope());
        bonusAudit.setDiscountBalance(BigDecimal.ZERO);
        bonusAudit.setStatus(Constants.EVNumber.zero);
        bonusAudit.setIsValid(Constants.EVNumber.one);
        bonusAudit.setBillManageId(billManageId);
        bonusAudit.setCatId(catId);
        bonusAudit.setPayOut(BigDecimal.ZERO);
        bonusAudit.setIsDrawings(Constants.EVNumber.zero);
        bonusAudit.setActivityId(bonus.getActivityId());
        bonusAudit.setIsDispose(Constants.EVNumber.zero);
        bonusAudit.setIsOut(Constants.EVNumber.zero);
        bonusAudit.setIsClean(Constants.EVNumber.zero);
        auditBonusMapper.insert(bonusAudit);
        MbrBillDetail mbrBillDetail = addMbrBillDetail(bonus);
        bonus.setStatus(Constants.EVNumber.one);
        bonus.setBillDetailId(mbrBillDetail.getId());
        bonus.setTransferAmount(amount);
        bonus.setDepotId(depotId);
        bonus.setCatId(catId);
        actBonusMapper.updateByPrimaryKey(bonus);
        setDiscardAmount(account, amount);
    }

    private MbrBillDetail addMbrBillDetail(OprActBonus bonus) {
        MbrBillDetail billDetail = new MbrBillDetail();
        billDetail.setLoginName(bonus.getLoginName());
        billDetail.setAccountId(bonus.getAccountId());
        billDetail.setFinancialCode(OrderConstants.ACTIVITY_AC);
        billDetail.setOrderNo(bonus.getOrderNo().toString());
        billDetail.setOrderTime(getCurrentDate(FORMAT_25_DATE_TIME));
        billDetail.setDepotId(Constants.SYS_DEPOT_ID);
        billDetail.setAmount(bonus.getBonusAmount());
        MbrWallet entity = new MbrWallet();
        entity.setAccountId(bonus.getAccountId());
        entity = walletService.queryObjectCond(entity);
        entity.setBalance(entity.getBalance().add(bonus.getBonusAmount()));
        billDetail.setOrderTime(getCurrentDate(FORMAT_25_DATE_TIME));
        billDetail.setOpType(MbrBillDetail.OpTypeStatus.income);
        billDetail.setAfterBalance(entity.getBalance());
        billDetail.setBeforeBalance(
                BigDecimalMath.round(BigDecimalMath.sub(entity.getBalance(), billDetail.getAmount()), 2));
        billDetailMapper.insert(billDetail);
        return billDetail;
    }

    private void setDiscardAmount(MbrAccount account, BigDecimal amount) {
        MbrAuditAccount auditAccount = new MbrAuditAccount();
        auditAccount.setIsDrawings(Constants.EVNumber.zero);
        auditAccount.setAccountId(account.getId());
        List<MbrAuditAccount> auditAccounts = auditMapper.finAuditList(auditAccount);
        if (Collections3.isNotEmpty(auditAccounts)) {
            for (MbrAuditAccount mbrAuditAccount : auditAccounts) {
                Integer discardSign = mbrAuditAccount.getDepositAmount().compareTo(amount);
                if (mbrAuditAccount.getDepositAmount().compareTo(BigDecimal.ZERO) == 0) {
                    continue;
                }
                if (discardSign == 0) {
                    if (mbrAuditAccount.getDiscardAmount().compareTo(BigDecimal.ZERO) == 0) {
                        mbrAuditAccount.setDiscardAmount(mbrAuditAccount.getDepositAmount());
                    }
                    mbrAuditAccount.setDepositAmount(BigDecimal.ZERO);
                    mbrAuditAccount.setAuditAmount(BigDecimal.ZERO);
                    updateDiscardAmount(mbrAuditAccount);
                    break;
                }
                if (discardSign == 1) {
                    if (mbrAuditAccount.getDiscardAmount().compareTo(BigDecimal.ZERO) == 0) {
                        mbrAuditAccount.setDiscardAmount(mbrAuditAccount.getDepositAmount());
                    }
                    mbrAuditAccount.setDepositAmount(mbrAuditAccount.getDepositAmount().subtract(amount));
                    mbrAuditAccount.setAuditAmount(mbrAuditAccount.getDepositAudit().multiply(mbrAuditAccount.getDepositAmount()));
                    updateDiscardAmount(mbrAuditAccount);
                    break;
                }
                if (discardSign == -1) {
                    amount = amount.subtract(mbrAuditAccount.getDepositAmount());
                    if (mbrAuditAccount.getDiscardAmount().compareTo(BigDecimal.ZERO) == 0) {
                        mbrAuditAccount.setDiscardAmount(mbrAuditAccount.getDepositAmount());
                    }
                    mbrAuditAccount.setDepositAmount(BigDecimal.ZERO);
                    mbrAuditAccount.setAuditAmount(BigDecimal.ZERO);
                    updateDiscardAmount(mbrAuditAccount);
                }
            }
        }
    }

    private void updateDiscardAmount(MbrAuditAccount auditAccount) {
        auditAccountMapper.updateByPrimaryKey(auditAccount);
    }


    /**
     * 转账 转出判断优惠是否通过违规优惠稽核
     */
    public AuditBonusDto outAuditBonus(MbrAccount account, Integer depotId) {
        List<MbrAuditBonus> auditBonuses = auditCastService.findMbrAuditBonusByAccountId(account.getId(), depotId, null);
        AuditBonusDto dto = new AuditBonusDto();
        // 没有优惠稽核，下述逻辑废除，AuditBonusDto 默认值为true，所以setIsFraud，setIsSucceed 都为true
        if (Collections3.isNotEmpty(auditBonuses)) {
            MbrAuditBonus auditBonus = auditBonuses.get(0);
            long count = auditBonuses.stream().filter(p ->
                    Constants.EVNumber.zero == p.getStatus())
                    .map(MbrAuditBonus::getId).count();
            if (auditBonus.getIsValid() == Constants.EVNumber.zero) {
                dto.setIsFraud(Boolean.FALSE);
                return dto;
            }
            if (count > 0) {
                dto.setIsSucceed(Boolean.FALSE);
                Optional<BigDecimal> totalValidBet = auditBonuses.stream()
                        .filter(p -> nonNull(p.getAuditAmount()))
                        .map(MbrAuditBonus::getAuditAmount).reduce(BigDecimal::add);
                Optional<BigDecimal> validBet = auditBonuses.stream()
                        .filter(p -> nonNull(p.getValidBet()))
                        .map(MbrAuditBonus::getValidBet).reduce(BigDecimal::add);
                dto.setTotalValidBet(totalValidBet.get());
                dto.setResidueValidBet(dto.getTotalValidBet().subtract(validBet.get()));
                return dto;
            }
        }
        return dto;
    }

    /**
     * 转账成功
     */
    public void succeedAuditBonus(MbrAccount account, Integer depotId) {
        MbrAuditBonus auditBonus = new MbrAuditBonus();
        auditBonus.setAccountId(account.getId());
        auditBonus.setDepotId(depotId);
        auditBonus.setIsDrawings(Constants.EVNumber.zero);
        List<MbrAuditBonus> auditBonuses = auditBonusMapper.select(auditBonus);
        if (Collections3.isNotEmpty(auditBonuses)) {
            auditBonuses.forEach(as -> {
                as.setStatus(Constants.EVNumber.one);
                as.setIsDrawings(Constants.EVNumber.one);
                as.setTransferTime(getCurrentDate(FORMAT_18_DATE_TIME));
                auditBonusMapper.updateByPrimaryKey(as);
            });
            addOrUpdateMbrAuditHistory(auditBonuses.get(0), auditBonuses.get(auditBonuses.size() - 1).getTime());
        }
    }

    public void addOrUpdateMbrAuditHistory(MbrAuditBonus bonus, String time) {
        MbrAuditHistory auditHistory = findMbrAuditHistory(bonus.getAccountId());
        if (isNull(auditHistory)) {
            MbrAuditHistory history = new MbrAuditHistory();
            history.setAccountId(bonus.getAccountId());
            history.setLoginName(bonus.getLoginName());
            history.setStartTime(bonus.getTime());
            history.setIsSign(Constants.EVNumber.zero);
            history.setEndTime(time);
            auditHistoryMapper.insert(history);
        } else {
            if (bonus.getTime().compareTo(auditHistory.getStartTime()) < 1) {
                auditHistory.setStartTime(bonus.getTime());
            }
            if (time.compareTo(auditHistory.getEndTime()) > 0) {
                auditHistory.setEndTime(time);
            }
            auditHistoryMapper.updateByPrimaryKeySelective(auditHistory);
        }
    }

    public void addOrUpdateHistoryByDeposit(MbrAuditAccount auditAccount, String time) {
        MbrAuditHistory auditHistory = findMbrAuditHistory(auditAccount.getAccountId());
        if (isNull(auditHistory)) {
            MbrAuditHistory auditHistory1 = new MbrAuditHistory();
            auditHistory1.setAccountId(auditAccount.getAccountId());
            auditHistory1.setLoginName(auditAccount.getLoginName());
            auditHistory1.setStartTime(auditAccount.getTime());
            auditHistory1.setIsSign(Constants.EVNumber.one);
            auditHistory1.setEndTime(time);
            auditHistoryMapper.insert(auditHistory1);
        } else {
            if (StringUtils.isNotEmpty(auditAccount.getTime())
                    && auditAccount.getTime().compareTo(auditHistory.getStartTime()) < 1) {
                auditHistory.setStartTime(auditAccount.getTime());
            }
            if (time.compareTo(auditHistory.getEndTime()) > 0) {
                auditHistory.setStartTime(time);
            }
            auditHistory.setIsSign(Constants.EVNumber.one);
            auditHistoryMapper.updateByPrimaryKeySelective(auditHistory);
        }
    }

    private MbrAuditHistory findMbrAuditHistory(Integer accountId) {
        MbrAuditHistory history = new MbrAuditHistory();
        history.setAccountId(accountId);
        history.setIsSign(Constants.EVNumber.zero);
        return auditHistoryMapper.selectOne(history);
    }

    public void updateNormal(MbrAuditBonus mbrAuditBonus, String userName, String ip) {
        MbrAuditBonus auditBonus = auditBonusMapper.selectByPrimaryKey(mbrAuditBonus.getId());
        auditBonus.setMemo(mbrAuditBonus.getMemo());
        auditBonus.setDisposeType(Constants.EVNumber.one);
        updateAuditBonusById(auditBonus, userName);

        //增加操作日志
        mbrAccountLogService.updateAccountAuditBonusInfo(auditBonus, userName, ip);
    }

    public void addAuditBonus(MbrAuditBonus mbrAuditBonus, String userName, String ip) {
        MbrAuditBonus auditBonus = auditBonusMapper.selectByPrimaryKey(mbrAuditBonus.getId());
        auditBonus.setMemo(mbrAuditBonus.getMemo());
        auditBonus.setDisposeType(Constants.EVNumber.two);
        auditBonus.setDisposeAmout(mbrAuditBonus.getAuditAmount());
        auditBonus.setAuditAmount(mbrAuditBonus.getAuditAmount().add(auditBonus.getAuditAmount()));
        updateAuditBonusById(auditBonus, userName);

        //增加操作日志
        mbrAccountLogService.addAuditBonus(auditBonus, userName, ip);
    }

    private void updateAuditBonusById(MbrAuditBonus auditBonus, String userName) {
        if (nonNull(auditBonus) && auditBonus.getIsValid() == Constants.EVNumber.zero
                && auditBonus.getIsDrawings() == Constants.EVNumber.zero) {
            auditBonus.setIsValid(Constants.EVNumber.one);
            auditBonus.setUpdateAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
            auditBonus.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
            auditBonus.setModifyUser(userName);
            auditBonus.setIsDispose(Constants.EVNumber.one);
            auditBonusMapper.updateByPrimaryKey(auditBonus);
        }
    }

    public void clearAuditBonus(MbrAuditBonus mbrAuditBonus, String userName, String ip) {
        MbrAuditBonus auditBonus = auditBonusMapper.selectByPrimaryKey(mbrAuditBonus.getId());
        if (nonNull(auditBonus) && auditBonus.getIsValid() == Constants.EVNumber.zero) {
            MbrAuditBonus auditBonus1 = new MbrAuditBonus();
            auditBonus1.setAccountId(auditBonus.getAccountId());
            auditBonus1.setCatId(auditBonus1.getCatId());
            auditBonus1.setDepotId(auditBonus1.getDepotId());
            auditBonus1.setIsValid(auditBonus.getIsValid());
            auditBonus1.setIsDrawings(Constants.EVNumber.zero);
            List<MbrAuditBonus> auditBonuses = auditBonusMapper.select(auditBonus1);
            if (Collections3.isNotEmpty(auditBonuses)) {
                auditBonuses.stream().forEach(as -> {
                    as.setMemo(mbrAuditBonus.getMemo());
                    as.setIsClean(Constants.EVNumber.one);
                    as.setStatus(Constants.EVNumber.one);
                    as.setAuditAmount(BigDecimal.ZERO);
                    as.setIsDrawings(Constants.EVNumber.one);
                    as.setTransferTime(getCurrentDate(FORMAT_18_DATE_TIME));
                    as.setDisposeType(Constants.EVNumber.three);
                    updateAuditBonusById(as, userName);

                    //增加操作日志
                    mbrAccountLogService.cleanIllegalAudit(as, userName, ip);
                });
                addOrUpdateMbrAuditHistory(auditBonuses.get(0), auditBonuses.get(auditBonuses.size() - 1).getTime());
            }
        }
    }

    public BigDecimal findAuditAccountBalance(MbrAuditBonus bonus, String siteCode) {
        MbrAuditBonus auditBonus = auditBonusMapper.selectByPrimaryKey(bonus.getId());
        if (nonNull(auditBonus)) {
            return getBalance(auditBonus, siteCode);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal getBalance(MbrAuditBonus bonus, String siteCode) {
        TGmApi gmApi = gmApiService.queryApiObject(bonus.getDepotId(), siteCode);
        UserBalanceResponseDto responseDto = depotWalletService.queryDepotBalance(bonus.getAccountId(), gmApi);
        return responseDto.getBalance();
    }

    public void auditCharge(Integer auditBonusId, BigDecimal amount, String memo, String ip, String siteCode, String userName) {
        MbrAuditBonus auditBonus = auditBonusMapper.selectByPrimaryKey(auditBonusId);
        if (nonNull(auditBonus) && auditBonus.getIsDrawings() == Constants.EVNumber.zero) {
            MbrWallet wallet = new MbrWallet();
            wallet.setAccountId(auditBonus.getAccountId());
            MbrWallet mbrWallet = mbrWalletMapper.selectOne(wallet);
            BigDecimal balance = getBalance(auditBonus, siteCode);
            if (mbrWallet.getBalance().add(balance).compareTo(amount) == -1) {
                throw new R200Exception("余额不足");
            }
            MbrDepotWallet wallet1 = new MbrDepotWallet();
            wallet1.setDepotId(auditBonus.getDepotId());
            MbrDepotWallet depotWallet = depotWalletMapper.selectOne(wallet1);

            DepotFailDtosDto depotFailDtosDto = new DepotFailDtosDto();
            depotFailDtosDto.setDepotWallets(Lists.newArrayList(depotWallet));
            depotFailDtosDto.setIp(ip);
            depotFailDtosDto.setDev("PC");
            depotFailDtosDto.setTransferSource((byte) 0);
            depotFailDtosDto.setUserId(depotWallet.getAccountId());
            depotFailDtosDto.setLoginName(mbrWallet.getLoginName());
            depotFailDtosDto.setSiteCode(siteCode);
            List<DepotFailDto> depotFailDtos = walletService.getDepotFailDtos(depotFailDtosDto);
            if (Collections3.isEmpty(depotFailDtos) || Boolean.FALSE.equals(depotFailDtos.get(0).getFailError())) {
                throw new R200Exception("扣款，转账失败");
            }
            FundAudit fundAudit = new FundAudit();
            fundAudit.setAccountId(auditBonus.getAccountId());
            fundAudit.setAmount(amount);
            fundAudit.setMemo(memo);
            fundAudit.setFinancialCode(OrderConstants.FUND_ORDER_CODE_AM);
            fundAudit.setLoginName(auditBonus.getLoginName());
            fundAudit.setStatus(Constants.EVNumber.one);
            fundAudit.setOrderNo(new SnowFlake().nextId() + "");
            fundAudit.setOrderPrefix(OrderConstants.FUND_ORDER_AUDIT);
            fundAudit.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
            fundAudit.setCreateUser(userName);
            fundAudit.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
            fundAudit.setModifyUser(userName);
            fundAudit.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
            fundAudit.setAuditUser(userName);
            MbrBillDetail mbrBillDetail = walletService.castWalletAndBillDetail(fundAudit.getLoginName(),
                    fundAudit.getAccountId(), fundAudit.getFinancialCode(), fundAudit.getAmount(),
                    fundAudit.getOrderNo(), Boolean.FALSE, null, null);
            fundAudit.setBillDetailId(mbrBillDetail.getId());
            fundAuditMapper.insert(fundAudit);
            auditChargeClean(auditBonus, userName, amount);

            //增加操作日志
            mbrAccountLogService.auditCharge(auditBonus, userName, ip);
        }
    }

    private void auditChargeClean(MbrAuditBonus bonus, String userName, BigDecimal amount) {
        MbrAuditBonus auditBonus = new MbrAuditBonus();
        auditBonus.setAccountId(bonus.getAccountId());
        auditBonus.setDepotId(bonus.getDepotId());
        List<MbrAuditBonus> auditBonuses = auditBonusMapper.select(auditBonus);
        if (Collections3.isNotEmpty(auditBonuses)) {
            auditBonuses.stream().forEach(as -> {
                as.setMemo(bonus.getMemo());
                as.setAuditAmount(BigDecimal.ZERO);
                as.setModifyUser(userName);
                as.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
                as.setStatus(Constants.EVNumber.one);
                as.setIsDrawings(Constants.EVNumber.one);
                as.setIsValid(Constants.EVNumber.one);
                as.setTransferTime(getCurrentDate(FORMAT_18_DATE_TIME));
                as.setIsDispose(Constants.EVNumber.one);
                as.setDisposeType(Constants.EVNumber.four);
                if (as.getId().equals(bonus.getId())) {
                    as.setDisposeAmout(amount);
                }
                auditBonusMapper.updateByPrimaryKey(as);
            });
            addOrUpdateMbrAuditHistory(auditBonuses.get(0), auditBonuses.get(auditBonuses.size() - 1).getTime());
        }
    }

    public void updateAuditAccount(Integer accountId, BigDecimal transAmount, String siteCode) {
        String key = RedisConstants.AUDIT_ACCOUNT + siteCode + accountId;
        while (true) {
            Boolean isExpired = redisService.setRedisExpiredTimeBo(key, accountId, 10, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(isExpired)) {
                castReduceAuditAmount(accountId, transAmount);
                redisService.del(key);
                break;
            }
        }
    }

    public void castReduceAuditAmount(Integer accountId, BigDecimal transAmount) {
        Map<String, Object> objectMap = isWithdrawal(accountId);
        if (Boolean.FALSE.equals(objectMap.get("isPassed"))) {
            MbrAuditAccount accountAudit = new MbrAuditAccount();
            accountAudit.setAccountId(accountId);
            List<MbrAuditAccount> auditAccounts = auditMapper.finAuditList(accountAudit);
            for (int i = 0; i < auditAccounts.size(); i++) {
                MbrAuditAccount auditAccount = auditAccounts.get(i);

                MbrAuditAccount mbrAuditAccount = new MbrAuditAccount();
                mbrAuditAccount.setId(auditAccount.getId());

                if (auditAccount.getAuditAmount().compareTo(BigDecimal.ZERO) == 1) {
                    if (transAmount.compareTo(auditAccount.getAuditAmount()) != 1) {
                        mbrAuditAccount.setAuditAmount(auditAccount.getAuditAmount().subtract(transAmount));
                        mbrAuditAccount.setReduceAuditAmount(auditAccount.getReduceAuditAmount().add(transAmount));
                        auditAccountMapper.updateByPrimaryKeySelective(mbrAuditAccount);
                        break;
                    } else {
                        if (i == auditAccounts.size() - 1) {
                            mbrAuditAccount.setReduceAuditAmount(auditAccount.getReduceAuditAmount().add(transAmount));
                        } else {
                            mbrAuditAccount.setReduceAuditAmount(auditAccount.getReduceAuditAmount().add(auditAccount.getAuditAmount()));
                        }
                        transAmount = transAmount.subtract(auditAccount.getAuditAmount());
                        mbrAuditAccount.setAuditAmount(BigDecimal.ZERO);
                        auditAccountMapper.updateByPrimaryKeySelective(mbrAuditAccount);
                    }
                }
            }
        }
    }
}
