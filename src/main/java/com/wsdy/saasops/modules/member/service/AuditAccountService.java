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
        // ???????????????????????????
        MbrAccount account = new MbrAccount();
        account.setLoginName(loginName);
        MbrAccount mbrAccount = accountMapper.selectOne(account);
        if (isNull(mbrAccount)) {
            throw new R200Exception("?????????????????????????????????");
        }
        // ?????????????????????????????????????????????
        List<AuditDetailDto> detailDtos =
                auditMapper.findAuditAccountList
                        (mbrAccount.getId(), null, null, Constants.EVNumber.zero);

        if (Collections3.isNotEmpty(detailDtos)) {
            // ????????????????????????
            AuditInfoDto auditInfoDto = casAuditInfoDto(detailDtos);

            // ?????????????????????2 ???????????????
            auditInfoDto.getAuditDetailDtos().stream().forEach(ds -> {
                BigDecimal validBet = ds.getRemainValidBet().add(ds.getValidBet());
                if (ds.getIsOut() == Constants.EVNumber.one
                        && validBet.compareTo(ds.getValidBet()) == -1) {    // ?? ?????????????????? ??
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
            throw new R200Exception("?????????????????????????????????");
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

        // ????????????????????????????????? ?????????????????????????????????????????????????????????????????????????????????????????????
        FundMerchantPay pay = new FundMerchantPay();
        pay.setAvailable(Constants.EVNumber.one);
        pay.setMethodType(Constants.EVNumber.two);
        List<FundMerchantPay> list = merchantPayMapper.select(pay);
        PaySet paySet = sysSettingService.queryPaySet();
        if (Objects.nonNull(list) && list.size() > 0) {
            Integer alipayEnable = paySet.getAlipayEnable();
            // ????????????????????????????????????????????????????????????????????????????????????
            if (nonNull(alipayEnable) && Constants.EVNumber.zero == alipayEnable) {
                hashMap.put("alipayEnable", Constants.EVNumber.zero);
            } else {
                hashMap.put("alipayEnable", Constants.EVNumber.one);
            }
        } else {
            hashMap.put("alipayEnable", 0);
        }

        // ??????????????????????????????
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

        // ??????????????????ebpay?????????????????????????????????
        Integer ebPayWithdrawEnable = list2.stream().anyMatch(pTmp -> "EBPAY".equals(pTmp.getCurrencyCode()))
                ? Constants.EVNumber.one : Constants.EVNumber.zero;
        if (nonNull(ebPayWithdrawEnable)) {
            hashMap.put("ebPayWithdrawEnable", ebPayWithdrawEnable);
        }

        // ??????????????????topay
        Integer toPayWithdrawEnable = list2.stream().anyMatch(pTmp -> "TOPAY".equals(pTmp.getCurrencyCode()))
                ? Constants.EVNumber.one : Constants.EVNumber.zero;
        if (nonNull(toPayWithdrawEnable)) {
            hashMap.put("toPayWithdrawEnable", toPayWithdrawEnable);
        }

        return hashMap;
    }

    // ????????????????????????
    public Map<String, Object> immediatelyAudit(Integer accountId){
        // ????????????????????????
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
            hashMap.put("depositResidueValidBet",auditInfoDto.getDepositResidueValidBet()); // ??????????????????
            hashMap.put("depositValidBet",auditInfoDto.getDepositValidBet());               // ??????????????????
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
     * ??????????????????????????????
     *
     * @param detailDtos ?????????????????????liest
     * @return ?????????????????????????????????
     */
    private AuditInfoDto casAuditInfoDto(List<AuditDetailDto> detailDtos) {
        AuditInfoDto infoDto = new AuditInfoDto();

        if (Collections3.isNotEmpty(detailDtos)) {
            // ??????????????????????????????/?????????/????????????????????????
            castDepositValue(infoDto, detailDtos);
            // ?????????????????????
            castBounsValue(infoDto, detailDtos);

            // ?????????????????????????????? ??????
            long DepositCount = detailDtos.stream().filter(
                    as -> Constants.EVNumber.zero == as.getStatus()
                            && as.getAuditType() == Constants.EVNumber.zero)
                    .map(AuditDetailDto::getId).count();
            // ?????????????????????????????? ??????
            long bounsCount = detailDtos.stream().filter(
                    as -> Constants.EVNumber.zero == as.getStatus()
                            && as.getAuditType() == Constants.EVNumber.one)
                    .map(AuditDetailDto::getId).count();
            // ?????????????????????
            if (DepositCount > 0) {
                infoDto.setDepositSucceed(Boolean.FALSE);
            }
            // ?????????????????????
            if (bounsCount > 0) {
                infoDto.setDbounsSucceed(Boolean.FALSE);
            }
            // ??????????????????
            infoDto.setAuditDetailDtos(detailDtos);
        }

        return infoDto;
    }

    private void castDepositValue(AuditInfoDto infoDto, List<AuditDetailDto> detailDtos) {
        // ???????????????????????????????????????????????????????????????
        Optional<BigDecimal> depositTotal = detailDtos.stream()
                .filter(p -> p.getAuditType() == Constants.EVNumber.zero
                        && nonNull(p.getDepositAmount())
                        && p.getDepositAmount().compareTo(BigDecimal.ZERO) == 1
                        && p.getDiscardAmount().compareTo(BigDecimal.ZERO) == 0)    // discardAmount ???0???????????????????????????
                .map(AuditDetailDto::getDepositAmount).reduce(BigDecimal::add);
        // ??????????????????????????????????????????????????????????????????
        Optional<BigDecimal> depositDiscardAmount = detailDtos.stream()
                .filter(p -> p.getAuditType() == Constants.EVNumber.zero
                        && nonNull(p.getDiscardAmount())
                        && p.getDiscardAmount().compareTo(BigDecimal.ZERO) == 1)
                .map(AuditDetailDto::getDiscardAmount).reduce(BigDecimal::add);
        // ????????????????????????????????????
        Optional<BigDecimal> depositValidBet = detailDtos.stream()
                .filter(p -> p.getAuditType() == Constants.EVNumber.zero
                        && nonNull(p.getAuditAmount()))
                .map(AuditDetailDto::getAuditAmount).reduce(BigDecimal::add);
        // ????????????????????????????????????
        Optional<BigDecimal> currentDepositValidBet = detailDtos.stream()
                .filter(p -> p.getAuditType() == Constants.EVNumber.zero
                        && nonNull(p.getValidBet()))
                .map(AuditDetailDto::getValidBet).reduce(BigDecimal::add);


        BigDecimal bigDecimalDeposit = depositTotal.isPresent() ? depositTotal.get() : BigDecimal.ZERO;                         // ?????????
        BigDecimal bigDecimalDiscard = depositDiscardAmount.isPresent() ? depositDiscardAmount.get() : BigDecimal.ZERO;         // ?????????

        infoDto.setDepositTotal(bigDecimalDeposit.add(bigDecimalDiscard));                                                      // ??????????????? = ????????? + ?????????
        infoDto.setDepositValidBet(depositValidBet.isPresent() ? depositValidBet.get() : BigDecimal.ZERO);                     // ???????????????
        infoDto.setCurrentDepositValidBet(currentDepositValidBet.isPresent() ? currentDepositValidBet.get() : BigDecimal.ZERO); // ???????????????

        // ????????????????????????list
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
        // ????????????????????????
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

                    //????????????
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

                    //????????????
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

                //??????????????????
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

                    //??????????????????
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

    // ????????????????????????
    private BigDecimal getResidueValidBet(List<MbrAuditAccount> auditAccounts) {
        // ??????????????????
        Collections.sort(auditAccounts, Comparator.comparing(MbrAuditAccount::getId));

        BigDecimal bigDecimal = BigDecimal.ZERO;
        Boolean isCast = Boolean.FALSE;

        for (MbrAuditAccount auditAccount : auditAccounts) {
            // ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            if (auditAccount.getStatus() == Constants.EVNumber.zero
                    || (isCast && auditAccount.getIsOut() == Constants.EVNumber.one)) {
                if (auditAccount.getAuditAmount() == null) {
                    auditAccount.setAuditAmount(BigDecimal.ZERO);
                }
                BigDecimal bigDecimal1 = auditAccount.getAuditAmount().subtract(auditAccount.getValidBet());          // ?????????????????? = ??????????????????-????????????
                bigDecimal = bigDecimal.add(bigDecimal1);
                isCast = Boolean.TRUE;
            }
        }
        return bigDecimal;
    }

    /**
     * ????????????
     *
     * @param accountId      ??????id
     * @param depositAmount  ????????????
     * @param depositId      ?????????id????????????????????????null
     * @param auditMultiple  ?????????????????????null ???????????????????????????
     * @param auditAmount    ???????????? ??? ???isSign???4???????????????????????????????????????????????????0
     * @param discountAmount ????????????
     * @param ruleId         ??????id???  ??????????????????null
     * @param isSign         1???????????????(??????????????????+?????????????????????)/????????????(?????????????????????????????????)
     *                       2??????????????????????????????????????? ??? ??????/?????????????????????(???????????????+???????????????* ??????????????????-????????????)
     *                       4???????????????????????????
     *                       5. ?????????????????????????????????????????????????????????
     *                       6. ?????????????????????????????????
     * @return
     */
    public MbrAuditAccount insertAccountAudit(Integer accountId, BigDecimal depositAmount, Integer depositId,
                                              BigDecimal auditMultiple, BigDecimal auditAmount, BigDecimal discountAmount,
                                              Integer ruleId, Integer isSign) {
        MbrAuditAccount accountAudit = new MbrAuditAccount();

        // ?????????????????????
        MbrWithdrawalCond withdrawalCond = getMbrWithdrawalCond(accountId);
        Integer depositAudit = Constants.EVNumber.zero;     // ????????????????????????
        BigDecimal depositOutBalance = BigDecimal.ZERO;     // ????????????
        if (Objects.nonNull(withdrawalCond)) {
            depositAudit = Objects.nonNull(withdrawalCond.getWithDrawalAudit()) ?
                    withdrawalCond.getWithDrawalAudit() : withdrawalCond.getWithDrawalAudit();
            depositOutBalance = Objects.nonNull(withdrawalCond.getOverFee()) ?
                    withdrawalCond.getOverFee() : BigDecimal.ZERO;
        }
        // ??????????????????????????????
        if (isNull(auditMultiple) && isSign == Constants.EVNumber.one) {
            accountAudit.setDepositAudit(new BigDecimal(depositAudit));     // ????????????
            accountAudit.setAuditAmount(CommonUtil.adjustScale(
                    new BigDecimal(depositAudit).multiply(depositAmount))); // ???????????? = ???????????? * ????????????

        }
        // ???????????????????????????
        if (nonNull(auditMultiple) && isSign == Constants.EVNumber.one) {
            accountAudit.setDepositAudit(auditMultiple);    // ????????????
            accountAudit.setAuditAmount(CommonUtil.adjustScale(
                    auditMultiple.multiply(depositAmount))); // ???????????? = ???????????? * ????????????

        }
        // ?????????????????????????????????????????????0
        if (isSign == Constants.EVNumber.four || isSign == Constants.EVNumber.five) {
            accountAudit.setDepositAudit(BigDecimal.ZERO);  // ????????????
            accountAudit.setAuditAmount(auditAmount);       // ????????????
        }
        // ??????/?????????????????????(???????????????+???????????????* ??????????????????-????????????)
        if (isSign == Constants.EVNumber.two) {
            accountAudit.setDepositAudit(auditMultiple);    // ????????????

            depositAmount = nonNull(depositAmount) ? depositAmount : BigDecimal.ZERO;
            discountAmount = nonNull(discountAmount) ? discountAmount : BigDecimal.ZERO;
            BigDecimal amount = CommonUtil.adjustScale(
                    (depositAmount.add(discountAmount)).multiply(accountAudit.getDepositAudit()).subtract(depositAmount));

            accountAudit.setAuditAmount(amount);            // ????????????
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
        // ????????????
        if (isSign == Constants.EVNumber.two || isSign == Constants.EVNumber.four || isSign == Constants.EVNumber.six) {
            accountAudit.setDepositAmount(discountAmount);  // ??????/??????/????????????->??????????????????????????????
        } else {    // ?????????????????????????????????
            accountAudit.setDepositAmount(nonNull(depositAmount) ? depositAmount : BigDecimal.ZERO);
        }
        // ????????????
        accountAudit.setDiscountAmount(nonNull(discountAmount) ? discountAmount : BigDecimal.ZERO);




        MbrAccount account = accountMapper.selectByPrimaryKey(accountId);
        accountAudit.setAccountId(accountId);                       // ??????id
        accountAudit.setLoginName(account.getLoginName());          // ????????????
        accountAudit.setRuleId(ruleId);                             // ?????????id
        accountAudit.setDepositId(depositId);                       // ?????????id
        accountAudit.setDepositOutBalance(depositOutBalance);       // ????????????
        accountAudit.setTime(getCurrentDate(FORMAT_18_DATE_TIME));  // ????????????

        accountAudit.setDepositBalance(BigDecimal.ZERO);            // ??????????????? -??????
        accountAudit.setIsDrawings(Constants.EVNumber.zero);        // ???????????? 0??? 1??? 2?????????
        accountAudit.setPayOut(BigDecimal.ZERO);                    // ??????
        accountAudit.setIsOut(Constants.EVNumber.zero);             // ????????? 0??? 1???
        accountAudit.setStatus(Constants.EVNumber.zero);            // ???????????? 0 ????????? 1??????
        accountAudit.setValidBet(BigDecimal.ZERO);                  // ????????????
        accountAudit.setRemainValidBet(BigDecimal.ZERO);            // ???????????????????????????:????????????
        accountAudit.setBonusRemainValidBet(BigDecimal.ZERO);       // ???????????????????????? ?????????
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
     * 	????????????????????????????????????????????????*???n-1???+??????*n???   n?????????????????????
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
     * ?????? ??????????????? ?????????service
     * * @param amount ????????????
     */
    public void accountBonusFreeze(OprActBonus bonus, Integer billManageId, BigDecimal amount) {
        bonus.setStatus(Constants.EVNumber.one);
        bonus.setBillDetailId(billManageId);
        bonus.setTransferAmount(amount);
        actBonusMapper.updateByPrimaryKey(bonus);
    }

    /**
     * ???????????????????????? ????????????service
     * * @param amount ????????????
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
     * ????????????
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
     * ?????? ????????????????????????????????????????????????
     */
    public AuditBonusDto outAuditBonus(MbrAccount account, Integer depotId) {
        List<MbrAuditBonus> auditBonuses = auditCastService.findMbrAuditBonusByAccountId(account.getId(), depotId, null);
        AuditBonusDto dto = new AuditBonusDto();
        // ??????????????????????????????????????????AuditBonusDto ????????????true?????????setIsFraud???setIsSucceed ??????true
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
     * ????????????
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

        //??????????????????
        mbrAccountLogService.updateAccountAuditBonusInfo(auditBonus, userName, ip);
    }

    public void addAuditBonus(MbrAuditBonus mbrAuditBonus, String userName, String ip) {
        MbrAuditBonus auditBonus = auditBonusMapper.selectByPrimaryKey(mbrAuditBonus.getId());
        auditBonus.setMemo(mbrAuditBonus.getMemo());
        auditBonus.setDisposeType(Constants.EVNumber.two);
        auditBonus.setDisposeAmout(mbrAuditBonus.getAuditAmount());
        auditBonus.setAuditAmount(mbrAuditBonus.getAuditAmount().add(auditBonus.getAuditAmount()));
        updateAuditBonusById(auditBonus, userName);

        //??????????????????
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

                    //??????????????????
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
                throw new R200Exception("????????????");
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
                throw new R200Exception("?????????????????????");
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

            //??????????????????
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
