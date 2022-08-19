package com.wsdy.saasops.aff.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.wsdy.saasops.aff.dto.*;
import com.wsdy.saasops.api.modules.apisys.dao.TcpSiteurlMapper;
import com.wsdy.saasops.api.modules.apisys.entity.TCpSite;
import com.wsdy.saasops.api.modules.apisys.entity.TcpSiteurl;
import com.wsdy.saasops.api.modules.apisys.mapper.ApiSysMapper;
import com.wsdy.saasops.api.modules.user.mapper.SdyDataMapper;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.modules.agent.dao.AgentAccountMapper;
import com.wsdy.saasops.modules.agent.dao.AgyDomainMapper;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.entity.AgyDomain;
import com.wsdy.saasops.modules.agent.entity.AgyTree;
import com.wsdy.saasops.modules.agent.mapper.AgentMapper;
import com.wsdy.saasops.modules.agent.service.AgentAccountService;
import com.wsdy.saasops.modules.analysis.entity.GameReportQueryModel;
import com.wsdy.saasops.modules.analysis.service.AnalysisService;
import com.wsdy.saasops.modules.fund.dao.FundAuditMapper;
import com.wsdy.saasops.modules.fund.dao.FundDepositMapper;
import com.wsdy.saasops.modules.fund.entity.FundAudit;
import com.wsdy.saasops.modules.fund.entity.FundDeposit;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrBillDetail;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.member.service.AuditAccountService;
import com.wsdy.saasops.modules.member.service.MbrVerifyService;
import com.wsdy.saasops.modules.member.service.MbrWalletService;
import com.wsdy.saasops.modules.operate.dao.OprActBonusMapper;
import com.wsdy.saasops.modules.operate.entity.OprActActivity;
import com.wsdy.saasops.modules.operate.entity.OprActBonus;
import com.wsdy.saasops.modules.operate.entity.TOpActtmpl;
import com.wsdy.saasops.modules.operate.mapper.OperateActivityMapper;
import com.wsdy.saasops.modules.operate.service.OprActActivityCastService;
import com.wsdy.saasops.modules.sdyExcel.service.SdyExcelService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static com.wsdy.saasops.common.constants.Constants.SYSTEM_USER;
import static com.wsdy.saasops.common.utils.DateUtil.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@Transactional
public class SdyDataService {

    @Autowired
    private AgentAccountMapper agentAccountMapper;
    @Autowired
    private MbrAccountMapper accountMapper;
    @Autowired
    private SdyExcelService sdyExcelService;
    @Autowired
    private AgentAccountService agentAccountService;
    @Autowired
    private SdyDataMapper sdyDataMapper;
    @Autowired
    private AnalysisService analysisService;
    @Autowired
    private MbrWalletService walletService;
    @Autowired
    private FundAuditMapper fundAuditMapper;
    @Autowired
    private AuditAccountService auditAccountService;
    @Autowired
    private AgyDomainMapper agyDomainMapper;
    @Autowired
    private AgentMapper agentMapper;
    @Autowired
    private TcpSiteurlMapper tcpSiteurlMapper;
    @Autowired
    private ApiSysMapper apiSysMapper;
    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    private FundDepositMapper fundDepositMapper;
    @Autowired
    private OprActActivityCastService actActivityCastService;
    @Autowired
    private OperateActivityMapper activityMapper;
    @Autowired
    private OprActBonusMapper actBonusMapper;
    @Autowired
    private MbrVerifyService verifyService;


    public void createAffiliate(CreateUserDto dto) {
        MbrAccount account = new MbrAccount();
        account.setLoginName(dto.getMembercode());
        MbrAccount mbrAccount = accountMapper.selectOne(account);
        if (isNull(mbrAccount)) {
            throw new R200Exception("会员不存在");
        }
        AgentAccount agentAccount1 = new AgentAccount();
        agentAccount1.setAgyAccount(dto.getMembercode());
        int agentCount = agentAccountMapper.selectCount(agentAccount1);
        if (agentCount > 0) {
            throw new R200Exception("代理已经存在");
        }
        AgentAccount agentAccount = new AgentAccount();
        String salt = RandomStringUtils.randomAlphanumeric(20);
        if (StringUtils.isNotEmpty(dto.getPass())) {
            agentAccount.setAgyPwd(new Sha256Hash(dto.getPass(), salt).toHex());
            agentAccount.setSecurePwd(new Sha256Hash(dto.getPass(), salt).toHex());
        }
        agentAccount.setParentId(1);
        if (StringUtils.isNotEmpty(dto.getSuperiorAgent())) {
            AgentAccount agentAccount2 = new AgentAccount();
            agentAccount2.setAgyAccount(dto.getSuperiorAgent().trim());
            AgentAccount agentAccount3 = agentAccountMapper.selectOne(agentAccount2);
            if (isNull(agentAccount3)) {
                throw new R200Exception("上级代理不存在");
            }
            agentAccount.setParentId(agentAccount3.getId());
        }

        agentAccount.setSalt(salt);
        agentAccount.setAgyAccount(dto.getMembercode());
        agentAccount.setAvailable(Constants.EVNumber.one);
        agentAccount.setEmail(mbrAccount.getEmail());
        agentAccount.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        agentAccount.setIp(mbrAccount.getRegisterIp());
        agentAccount.setStatus(Constants.EVNumber.one);
        agentAccount.setRegisterSign(Constants.EVNumber.one);

        agentAccount.setRealName(account.getRealName());
        agentAccount.setMobile(account.getMobile());
        agentAccount.setGroupId(sdyExcelService.getGroupId(null));
        if (StringUtils.isNotEmpty(dto.getAffiliatecode())) {
            agentAccount.setSpreadCode(dto.getAffiliatecode().trim());
        } else {
            agentAccount.setSpreadCode(agentAccountService.getSpreadCode());
        }
        agentAccount.setAttributes(0);
        agentAccount.setCommissionId(null);
        agentAccountMapper.insert(agentAccount);
        agentAccountService.addAgentWalletAndTree(agentAccount);
        agentAccountService.saveUserRole(agentAccount);
        log.info(dto.getMembercode() + "变更代理成功" + JSON.toJSONString(dto));
        if (StringUtils.isNotEmpty(dto.getDomain())) {
            String[] str = dto.getDomain().split(",");
            for (int i = 0; i < str.length; i++) {
                agyDomainAudit(agentAccount, str[i], dto.getSiteCode());
            }
        }
    }

    public void updateAgent(CreateUserDto dto) {
        AgentAccount agentAccount = new AgentAccount();
        agentAccount.setAgyAccount(dto.getMembercode());
        AgentAccount account = agentAccountMapper.selectOne(agentAccount);
        if (isNull(account)) {
            throw new R200Exception("代理不存在");
        }
        if (StringUtils.isNotEmpty(dto.getAffiliatecode())) {
            account.setSpreadCode(dto.getAffiliatecode().trim());
        }
        if (StringUtils.isNotEmpty(dto.getRemarks())) {
            account.setMemo(dto.getRemarks().trim());
        }
        agentAccountMapper.updateByPrimaryKeySelective(account);
        if (StringUtils.isNotEmpty(dto.getDomain())) {
            AgyDomain agyDomain = new AgyDomain();
            agyDomain.setAgyAccount(dto.getMembercode());
            agyDomainMapper.delete(agyDomain);
            if (!"-".equals(dto.getDomain())) {
                String[] str = dto.getDomain().split(",");
                for (int i = 0; i < str.length; i++) {
                    agyDomainAudit(account, str[i], dto.getSiteCode());
                }
            }
        }
    }

    public void agyDomainAudit(AgentAccount agentAccount, String domain, String siteCode) {
        domain = domain.replace("www.", "");
        int agyDomains = agentMapper.findAgyCommitDomainCount(domain);
        if (agyDomains > 0) {
            log.info(domain + "已被申请!");
            return;
        }
        AgyDomain agyDomain = new AgyDomain();
        agyDomain.setDomainUrl(domain);
        agyDomain.setAgyAccount(agentAccount.getAgyAccount());
        agyDomain.setAccountId(agentAccount.getId());
        agyDomain.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        agyDomain.setCreateUser(Constants.SYSTEM_USER);
        agyDomain.setStatus(Constants.EVNumber.one);
        agyDomain.setIsDel(1);  //是否可以删除 0否 1是
        agyDomain.setAvailable(1); //开启

        agyDomain.setModifyUser(Constants.SYSTEM_USER);
        agyDomain.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));

        //查询主域名是否已配置
        String mainDomainUrl = CommonUtil.getDomainForUrl(agyDomain.getDomainUrl());
        String agyDomainss = agentMapper.findAgyDomain(1);
        if (org.apache.commons.lang.StringUtils.isNotEmpty(agyDomainss)) {
            if (agyDomainss.contains(agyDomain.getDomainUrl())) {
                log.info(agyDomain.getDomainUrl() + "已被绑定!");
                return;
            }
        }
        TcpSiteurl siteUrl = new TcpSiteurl();
        siteUrl.setSiteUrl(mainDomainUrl);
        siteUrl.setAvailable(1);
        siteUrl.setSiteCode(siteCode);
        List<TcpSiteurl> list = tcpSiteurlMapper.select(siteUrl);
        if (Collections3.isEmpty(list)) {
            TCpSite site = apiSysMapper.getCpSiteBySiteCode(siteCode);
            siteUrl.setSiteId(site.getId());
            siteUrl.setSiteCode(site.getSiteCode());
            siteUrl.setSiteUrl(mainDomainUrl);
            siteUrl.setClientType(3);  //pc代理
            siteUrl.setAvailable(1);
            apiSysMapper.insertCpSiteUrlInfo(siteUrl);
        }
        agyDomainMapper.insert(agyDomain);
    }

    public void updateAccountAgent(AccountAgentDto dto) {
        MbrAccount account = new MbrAccount();
        account.setLoginName(dto.getMembercode());
        MbrAccount mbrAccount = accountMapper.selectOne(account);
        if (isNull(mbrAccount)) {
            throw new R200Exception("会员不存在");
        }
        AgentAccount agentAccount = new AgentAccount();
        agentAccount.setAgyAccount(dto.getParentmembercode());
        AgentAccount agentAccount1 = agentAccountMapper.selectOne(agentAccount);
        if (isNull(agentAccount1)) {
            throw new R200Exception("代理不存在");
        }
        int count = mbrMapper.findAccoutnSubCount(mbrAccount.getId());
        if (count > 0) {
            throw new R200Exception("有下级会员不可变更");
        }
        Integer parentid = mbrMapper.findsubAccountParentid(mbrAccount.getId());
        if (nonNull(parentid)) {
            throw new R200Exception("已经存在推荐人时，不可变更代理");
        }
        if (agentAccount1.getParentId() == Constants.EVNumber.zero) {
            throw new R200Exception("不可选择总代");
        }
        MbrAccount account1 = new MbrAccount();
        AgentAccount tAgent = agentMapper.getTagentByCagent(agentAccount1);
        account1.setTagencyId(tAgent.getId());
        account1.setCagencyId(agentAccount1.getId());
        account1.setId(mbrAccount.getId());
        accountMapper.updateByPrimaryKeySelective(account1);
    }

    public void updateAgentSuperior(AccountAgentDto dto) {
        AgentAccount agentAccount = new AgentAccount();
        agentAccount.setAgyAccount(dto.getMembercode());
        AgentAccount agentAccount1 = agentAccountMapper.selectOne(agentAccount);
        if (isNull(agentAccount1)) {
            throw new R200Exception("代理不存在");
        }
        agentAccount.setAgyAccount(dto.getParentmembercode());
        AgentAccount agentAccount2 = agentAccountMapper.selectOne(agentAccount);
        if (isNull(agentAccount2)) {
            throw new R200Exception("上级代理不存在");
        }
        updateAgentTree(agentAccount1.getId(), agentAccount2.getId());
    }

    public void updateAgentTree(Integer agentId, Integer parentAgentId) {
        List<AgyTree> mbrTreeList = agentMapper.findAgyTreeByparentId(agentId);
        List<AgyTree> agyTrees = Lists.newArrayList();
        for (AgyTree tree : mbrTreeList) {
            AgyTree agyTree = new AgyTree();
            if (tree.getChildNodeId().equals(agentId)) {
                agyTree.setParentId(parentAgentId);
                agyTree.setChildNodeId(agentId);
            } else {
                AgyTree agyTree1 = agentMapper.findAgyParentId(tree.getChildNodeId());
                agyTree.setParentId(agyTree1.getParentId());
                agyTree.setChildNodeId(tree.getChildNodeId());
            }
            agyTrees.add(agyTree);
        }
        for (AgyTree agyTree : agyTrees) {
            if (agyTree.getChildNodeId().equals(agentId)) {
                agentMapper.updateParentId(agyTree.getParentId(), agyTree.getChildNodeId());
            }
            agentMapper.deleteAgyTreeByparentId(agyTree.getChildNodeId());
            agentMapper.addAgentNode(agyTree.getParentId(), agyTree.getChildNodeId());

            AgentAccount agentAccount1 = new AgentAccount();
            agentAccount1.setId(agyTree.getChildNodeId());
            AgentAccount tAgent = agentMapper.getTagentByCagent(agentAccount1);

            agentMapper.updateAccountTagencyId(tAgent.getId(), agyTree.getChildNodeId());
        }
    }


    public PageUtils accountList(Integer pageNo, Integer pageSize, AccountListRequestDto dto) {
        PageHelper.startPage(pageNo, pageSize);
        List<MbrAccount> accounts = sdyDataMapper.findAcocountList(dto);
        List<AccountListResponseDto> responseDtos = Lists.newArrayList();
        if (Collections3.isNotEmpty(accounts)) {
            accounts.stream().forEach(as -> {
                AccountListResponseDto responseDto = new AccountListResponseDto();
                responseDto.setMembercode(as.getLoginName());
                responseDto.setStatus(Integer.valueOf(as.getAvailable()));
                responseDto.setEmail(as.getEmail());
                responseDto.setFullname(as.getRealName());
                responseDto.setContact(as.getMobile());
                responseDto.setRegisterip(as.getRegisterIp());
                responseDto.setLastlogindate(as.getLoginTime());
                responseDto.setJoineddate(as.getRegisterTime());
                responseDto.setAffiliatemembercode(as.getAgyAccount());
                responseDto.setCountsamefp(0);
                responseDto.setCountsameip(0);
                responseDto.setLoginTime(as.getLoginTime());
                if (StringUtils.isNotEmpty(as.getRegisterIp())) {
                    responseDto.setCountsameip(sdyDataMapper.findAccountIpCount(as.getId(), as.getRegisterIp()));
                }
                responseDtos.add(responseDto);
            });
        }
        return BeanUtil.toPagedResult(responseDtos);
    }

    public PageUtils depositAndWithdrawalList(Integer pageNo, Integer pageSize, AccountDepositRequestDto dto) {
        PageHelper.startPage(pageNo, pageSize);
        List<AccountDepositResponseDto> responseDtos = sdyDataMapper.depositAndWithdrawalList(dto);
        return BeanUtil.toPagedResult(responseDtos);
    }

    public PageUtils auditAndBonusList(Integer pageNo, Integer pageSize, AccountDepositRequestDto dto) {
        PageHelper.startPage(pageNo, pageSize);
        List<AuditBonusResponseDto> responseDtos = sdyDataMapper.auditAndBonusList(dto);
        return BeanUtil.toPagedResult(responseDtos);
    }

    public PageUtils getRptBetListPage(Integer pageNo, Integer pageSize, GameReportQueryModel model) {
        if (StringUtils.isNotEmpty(model.getAgyAccount())) {
            AgentAccount agentAccount = new AgentAccount();
            agentAccount.setAgyAccount(model.getAgyAccount());
            AgentAccount agentAccount1 = agentAccountMapper.selectOne(agentAccount);
            if (nonNull(agentAccount1)) {
                model.setAgentid(agentAccount1.getId());
            }
        }
        model.setIsSdyNet(Boolean.TRUE);
        return analysisService.getRptBetListPage(pageNo, pageSize, model);
    }

    public void addBalance(AddBalanceDto dto) {
        log.info(dto.getMembercode() + "会员开始上分" + JSON.toJSONString(dto));
        MbrAccount account = new MbrAccount();
        account.setLoginName(dto.getMembercode());
        MbrAccount mbrAccount = accountMapper.selectOne(account);
        if (isNull(mbrAccount)) {
            throw new R200Exception("会员不存在");
        }
        if (StringUtils.isNotEmpty(dto.getRemarks()) && dto.getRemarks().length() > 1) {
            String remarks = dto.getRemarks();
            String cj = remarks.substring(0, 2);
            if ("cj".equalsIgnoreCase(cj)) {
                addBalanceBouns(dto.getAmount(), mbrAccount, dto.getRemarks());
                return;
            }
        }
        String orderNo = String.valueOf(new SnowFlake().nextId());
        addBalanceDeposit(dto.getAmount(), mbrAccount, orderNo);
    }

    private void addBalanceBouns(BigDecimal amount, MbrAccount account, String memo) {
        OprActActivity activity = new OprActActivity();
        activity.setTmplCode(TOpActtmpl.affActivityCode);
        OprActActivity actActivity = activityMapper.findAffActivity(activity);
        OprActBonus bonus = actActivityCastService.setOprActBonus(account.getId(), account.getLoginName(),
                actActivity.getId(), null, null, actActivity.getRuleId());
        bonus.setMemo(memo);
        bonus.setDiscountAudit(new BigDecimal(1));
        bonus.setBonusAmount(amount);
        bonus.setSource(1);
        bonus.setAuditAmount(auditAccountService.getAddAuditAmount(bonus.getDiscountAudit(), bonus.getDepositedAmount(), bonus.getBonusAmount()));
        bonus.setCreateUser(Constants.SYSTEM_USER);
        actBonusMapper.insert(bonus);
        bonus.setAuditUser(Constants.SYSTEM_USER);
        bonus.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
        actActivityCastService.auditOprActBonus(bonus, OrderConstants.ACTIVITY_AFF, actActivity.getActivityName(), Boolean.FALSE);
    }

    /**
     * 彩金钱包上分
     * @param amount
     * @param account
     * @param memo
     */
    public void addBalanceBounsByPayOffWallet(BigDecimal amount, MbrAccount account, String memo,Integer auditMultiple,String remarks,Integer moneyMultiple,BigDecimal money) {
        OprActActivity activity = new OprActActivity();
        activity.setTmplCode(TOpActtmpl.affActivityCode);
        OprActActivity actActivity = activityMapper.findAffActivity(activity);
        OprActBonus bonus = actActivityCastService.setOprActBonus(account.getId(), account.getLoginName(),
                actActivity.getId(), null, null, actActivity.getRuleId());
        bonus.setApplicationMemo(remarks);
        bonus.setMemo(memo);
        bonus.setDiscountAudit(new BigDecimal(auditMultiple));
        bonus.setBonusAmount(amount);
        bonus.setSource(1);
        //彩金钱包上分稽核流水 稽核流水=本金金额*本金稽核倍数+上分金额*上分稽核倍数
        bonus.setAuditAmount(auditAccountService.getPayOffAddAuditAmount(new BigDecimal(auditMultiple), amount,new BigDecimal(moneyMultiple),money));
        bonus.setDepositedAmount(money);
        bonus.setCreateUser(Constants.SYSTEM_USER);
        actBonusMapper.insert(bonus);
        bonus.setAuditUser(Constants.SYSTEM_USER);
        bonus.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
        actActivityCastService.auditOprActBonus(bonus, OrderConstants.AGENT_MSF, actActivity.getActivityName(), Boolean.FALSE);
    }

    public void addBalanceDeposit(BigDecimal amount, MbrAccount account, String orderNo) {
        FundDeposit deposit = new FundDeposit();
        deposit.setOrderNo(orderNo);
        deposit.setMark(FundDeposit.Mark.affPay);
        deposit.setStatus(FundDeposit.Status.suc);
        deposit.setIsPayment(FundDeposit.PaymentStatus.pay);
        deposit.setDepositAmount(CommonUtil.adjustScale(amount));    // 截断存款2位数
        deposit.setHandlingCharge(BigDecimal.ZERO);   // 手续费为空
        deposit.setHandingback(Constants.Available.disable);
        deposit.setActualArrival(deposit.getDepositAmount());

        deposit.setOrderPrefix(OrderConstants.FUND_ORDER_COMPANYDEPOSIT);
        deposit.setDepositUser(account.getLoginName());
        deposit.setCreateUser(account.getLoginName());
        deposit.setAccountId(account.getId());
        deposit.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        deposit.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));

        deposit.setAuditUser(SYSTEM_USER);
        deposit.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
        deposit.setMemo("代理充值");

        fundDepositMapper.insert(deposit);
        verifyService.addMbrVerifyDeposit(deposit, CommonUtil.getSiteCode());


        auditAccountService.insertAccountAudit(
                deposit.getAccountId(), deposit.getDepositAmount(),
                deposit.getId(), null, null,
                null, null, Constants.EVNumber.one);

        MbrBillDetail billDetail = walletService.castWalletAndBillDetail(account.getLoginName(),
                deposit.getAccountId(), deposit.getOrderPrefix(),
                deposit.getActualArrival(), deposit.getOrderNo(), Boolean.TRUE, null, null);

        deposit.setBillDetailId(billDetail.getId());
        FundDeposit depositBill = new FundDeposit();
        depositBill.setId(deposit.getId());
        depositBill.setBillDetailId(billDetail.getId());
        fundDepositMapper.updateByPrimaryKeySelective(depositBill);
    }


    public FundAudit addBalanceAccount(BigDecimal amount, MbrAccount supAccount) {
        FundAudit fundAudit = new FundAudit();
        fundAudit.setAccountId(supAccount.getId());
        fundAudit.setAmount(amount);
        fundAudit.setMemo("api充值");
        fundAudit.setFinancialCode(OrderConstants.FUND_ORDER_CODE_AA);
        fundAudit.setLoginName(supAccount.getLoginName());
        fundAudit.setStatus(Constants.EVNumber.one);
        fundAudit.setOrderNo(String.valueOf(new SnowFlake().nextId()));
        fundAudit.setOrderPrefix(OrderConstants.FUND_ORDER_AUDIT);
        fundAudit.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        fundAudit.setCreateUser(Constants.SYSTEM_USER);
        fundAudit.setModifyTime(fundAudit.getCreateTime());
        fundAudit.setModifyUser(Constants.SYSTEM_USER);
        fundAudit.setAuditTime(fundAudit.getCreateTime());
        fundAudit.setAuditUser(Constants.SYSTEM_USER);
        fundAudit.setAuditAddType(Constants.EVNumber.four);
        MbrBillDetail mbrBillDetail = walletService.castWalletAndBillDetail(fundAudit.getLoginName(),
                fundAudit.getAccountId(), fundAudit.getFinancialCode(), fundAudit.getAmount(),
                fundAudit.getOrderNo(), Boolean.TRUE, null, null);
        fundAudit.setBillDetailId(mbrBillDetail.getId());
        fundAuditMapper.insert(fundAudit);

        auditAccountService.insertAccountAudit(
                fundAudit.getAccountId(), amount,
                null, null, null,
                null, null, Constants.EVNumber.one);
        return fundAudit;
    }


    public PageUtils findAccountBet(AccountBetRequestDto dto, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<AccountBetResponseDto> responseDtos = sdyDataMapper.findAccountBet(dto);
        if (responseDtos.size() > 0) {
            responseDtos.stream().forEach(rs -> {
                String str = new AccountEncryption().accountMobieEncrypt(rs.getPhoneNumber());
                rs.setPhoneNumber(str);
                rs.setLastDepositDate(sdyDataMapper.maxDpAudittime(rs.getMembercode()));
                rs.setLastWdDate(sdyDataMapper.maxWdAudittime(rs.getMembercode()));
                AccountBetResponseDto betResponseDto = sdyDataMapper.maxRptTime(rs.getMembercode());
                if (nonNull(betResponseDto)) {
                    rs.setTotalTurnover(betResponseDto.getTotalTurnover());
                    rs.setLastBetDate(betResponseDto.getLastBetDate());
                }
            });
        }
        return BeanUtil.toPagedResult(responseDtos);
    }

    public PageUtils findAdjustBonus(AccountDepositRequestDto dto, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<BonusWriteOffDto> responseDtos = sdyDataMapper.findAdjustBonus(dto.getMembercode(), dto.getStartTime(), dto.getEndTime());
        return BeanUtil.toPagedResult(responseDtos);
    }
}
