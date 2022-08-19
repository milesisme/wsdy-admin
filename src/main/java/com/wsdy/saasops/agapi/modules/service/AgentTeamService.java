package com.wsdy.saasops.agapi.modules.service;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.aff.service.SdyDataService;
import com.wsdy.saasops.agapi.modules.dto.DirectMemberDto;
import com.wsdy.saasops.agapi.modules.dto.DirectMemberParamDto;
import com.wsdy.saasops.agapi.modules.dto.SubAgentListDto;
import com.wsdy.saasops.agapi.modules.mapper.TeamMapper;
import com.wsdy.saasops.api.modules.pay.dto.DepositListDto;
import com.wsdy.saasops.api.modules.user.dto.DepotManageDto;
import com.wsdy.saasops.api.modules.user.service.DepotOperatService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.AccountEncryption;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.utils.SnowFlake;
import com.wsdy.saasops.modules.agent.dao.AgentAccountMapper;
import com.wsdy.saasops.modules.agent.dao.AgentAccountMemoMapper;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.entity.AgentAccountMemo;
import com.wsdy.saasops.modules.agent.entity.AgyBillDetail;
import com.wsdy.saasops.modules.agent.entity.AgyWallet;
import com.wsdy.saasops.modules.agent.service.AgentWalletService;
import com.wsdy.saasops.modules.fund.entity.AccWithdraw;
import com.wsdy.saasops.modules.fund.entity.FundDeposit;
import com.wsdy.saasops.modules.fund.mapper.FundMapper;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.dao.MbrWalletMapper;
import com.wsdy.saasops.modules.member.dto.BillRecordDto;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrWallet;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class AgentTeamService {

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private AgentAccountMemoMapper accountMemoMapper;

    @Autowired
    private MbrAccountMapper accountMapper;

    @Autowired
    private FundMapper fundMapper;

    @Autowired
    private AgentAccountMapper agentAccountMapper;

    @Autowired
    private AgentWalletService walletService;

    @Autowired
    private SdyDataService sdyDataService;

    @Autowired
    private MbrWalletMapper mbrWalletMapper;

    @Autowired
    private MbrMapper mbrMapper;

    @Autowired
    private DepotOperatService depotOperatService;

    public PageUtils directMember(AgentAccount agentAccount, DirectMemberParamDto paramDto, Integer pageNo, Integer pageSize) {
        if (agentAccount.getAttributes() == Constants.EVNumber.one) {
            paramDto.setSubCagencyId(agentAccount.getId());
        } else if (agentAccount.getAttributes() == Constants.EVNumber.four) {
            paramDto.setCagencyId(agentAccount.getAgentId());
        } else {
            paramDto.setCagencyId(agentAccount.getId());
        }
        PageHelper.startPage(pageNo, pageSize);
        List<DirectMemberDto> dtos = teamMapper.directMember(paramDto);
        setDirectMemberDto(dtos);
        return BeanUtil.toPagedResult(dtos);
    }

    /**
     * 	直属会员头部
     * 
     * @param agentAccount
     * @param paramDto
     * @return
     */
    public Map directMemberHead(AgentAccount agentAccount, DirectMemberParamDto paramDto) {
        if (agentAccount.getAttributes() == Constants.EVNumber.one) {
            paramDto.setSubCagencyId(agentAccount.getId());
        } else if (agentAccount.getAttributes() == Constants.EVNumber.four) {
            paramDto.setCagencyId(agentAccount.getAgentId());
        } else {
            paramDto.setCagencyId(agentAccount.getId());
        }
        Map<String, Object> objectMap = new HashMap<>(16);
        List<DirectMemberDto> dtos = teamMapper.directMember(paramDto);
        BigDecimal depositAmount =  BigDecimal.ZERO; //存款
        BigDecimal withdrawAmount =  BigDecimal.ZERO; //取款
        BigDecimal ctDiffer =  BigDecimal.ZERO; //存提差
        BigDecimal validBet =  BigDecimal.ZERO; //有效投注
        BigDecimal betTotal =  BigDecimal.ZERO; //投注金额
        BigDecimal payout =  BigDecimal.ZERO;//总输赢
        BigDecimal bonusAmount =  BigDecimal.ZERO;//红利
        BigDecimal auditAmAmount =  BigDecimal.ZERO;//人工扣减
        BigDecimal calculateProfit =  BigDecimal.ZERO;//人工调整
        
        for (DirectMemberDto dto:dtos){
            if (dto.getDepositAmount()!=null) {
                depositAmount = depositAmount.add(dto.getDepositAmount());
            }
            if (dto.getWithdrawAmount()!=null) {
                withdrawAmount = withdrawAmount.add(dto.getWithdrawAmount());
            }
            if (dto.getValidBet()!=null) {
                validBet = validBet.add(dto.getValidBet());
            }
            if (dto.getBetTotal()!=null) {
                betTotal = betTotal.add(dto.getBetTotal());
            }
            if (dto.getPayout()!=null) {
                payout = payout.add(dto.getPayout());
            }
            if (dto.getBonusamountAll()!=null) {
                bonusAmount = bonusAmount.add(dto.getBonusamountAll());
            }
            if (dto.getAuditAmAmount() != null) {
            	auditAmAmount = auditAmAmount.add(dto.getAuditAmAmount());
            }
            if (dto.getCalculateProfit() != null) {
            	calculateProfit = calculateProfit.add(dto.getCalculateProfit());
            }
        }
        ctDiffer = depositAmount.subtract(withdrawAmount);
        objectMap.put("depositAmount", depositAmount);
        objectMap.put("withdrawAmount", withdrawAmount);
        objectMap.put("ctDiffer", ctDiffer);
        objectMap.put("validBet", validBet);
        objectMap.put("betTotal", betTotal);
        objectMap.put("payout", payout);
        objectMap.put("bonusAmount", bonusAmount);
        objectMap.put("auditAmAmount", auditAmAmount);
        objectMap.put("calculateProfit", calculateProfit);

        return objectMap;
    }

    public void accountTransfer(AgentAccount agentAccount, DirectMemberParamDto dto) {
        AgyWallet agyWallet = walletService.findAgyWallet(agentAccount.getId());
        if (agyWallet.getRechargeWallet().compareTo(dto.getAmount()) == -1) {
            throw new R200Exception("余额不足");
        }
        String financialCode = OrderConstants.AGENT_ADC;
        if (agentAccount.getAttributes() == Constants.EVNumber.two
                || agentAccount.getAttributes() == Constants.EVNumber.three) {
            financialCode = OrderConstants.AGENT_ASF;
        }
        String orderNo = String.valueOf(new SnowFlake().nextId());
        AgyWallet wallet = walletService.setAgyWallet(agentAccount,
                dto.getAmount(), financialCode, agentAccount.getId(),
                dto.getAccountId(), orderNo, agentAccount.getAgyAccount(),
                Constants.EVNumber.one);
        AgyBillDetail billDetail = walletService.reduceWalletAndBillDetail(wallet, Constants.EVNumber.zero);
        if (isNull(billDetail)) {
            throw new R200Exception("余额不足");
        }
        MbrAccount mbrAccount = accountMapper.selectByPrimaryKey(dto.getAccountId());
        sdyDataService.addBalanceDeposit(dto.getAmount(), mbrAccount, orderNo);
    }


    public void updateAccountMemo(AgentAccountMemo accountMemo) {
        AgentAccountMemo agentAccountMemo = new AgentAccountMemo();
        agentAccountMemo.setAccountId(accountMemo.getAccountId());
        AgentAccountMemo memo = accountMemoMapper.selectOne(agentAccountMemo);
        MbrAccount account = accountMapper.selectByPrimaryKey(accountMemo.getAccountId());
        if (isNull(memo)) {
            accountMemo.setLogiNname(account.getLoginName());
            accountMemoMapper.insert(accountMemo);
        } else {
            memo.setMemoType(accountMemo.getMemoType());
            memo.setNumbering(accountMemo.getNumbering());
            accountMemoMapper.updateByPrimaryKeySelective(memo);
        }
    }

    /**
     * 	直属会员-详情-上
     * 
     * @param account
     * @param loginName
     * @return
     */
    public DirectMemberDto memberDetails(AgentAccount account, String loginName) {
        DirectMemberParamDto paramDto = new DirectMemberParamDto();
        paramDto.setLoginName(loginName);
        if (account.getAttributes() == Constants.EVNumber.one) {
            paramDto.setSubCagencyId(account.getId());
        } else if (account.getAttributes() == Constants.EVNumber.four) {
            paramDto.setCagencyId(account.getAgentId());
        } else {
            paramDto.setCagencyId(account.getId());
        }
        List<DirectMemberDto> dtos = teamMapper.directMember(paramDto);
        setDirectMemberDto(dtos);
        if (dtos.size() > 0) {
            DirectMemberDto directMemberDto = dtos.get(0);
            directMemberDto.setValidBet(teamMapper.findValidbet(loginName));
            directMemberDto.setBonusAmount(teamMapper.findBonusAmount(loginName));

            MbrAccount mbrAccount = mbrMapper.getMbrListByLoginNames(Arrays.asList(loginName)).get(0);
            // 各个游戏子钱包余额之和
            List<DepotManageDto> depotList = depotOperatService.getDepotList(mbrAccount.getId());
            List<DepotManageDto> dtoList = depotList.stream().filter(dt -> nonNull(dt) && nonNull(dt.getBalance())).collect(Collectors.toList());
            BigDecimal reduce = dtoList.stream().map(DepotManageDto::getBalance).reduce(BigDecimal.ZERO, BigDecimal::add);
            MbrWallet wallet = new MbrWallet();
            wallet.setAccountId(mbrAccount.getId());
            // 账户余额
            MbrWallet mbrWallet = mbrWalletMapper.selectOne(wallet);
            directMemberDto.setBalance(reduce.add(mbrWallet.getBalance()).setScale(2, RoundingMode.DOWN));
            return directMemberDto;
        }
        return null;
    }

    public PageUtils memberBillRecordList(DirectMemberParamDto paramDto, Integer pageNo, Integer pageSize) {
        BillRecordDto billRecordDto = new BillRecordDto();
        billRecordDto.setAccountName(paramDto.getLoginName());
        billRecordDto.setEndTime(paramDto.getEndTime());
        billRecordDto.setStartTime(paramDto.getStartTime());
        billRecordDto.setOrderBy(paramDto.getOrderBy());

        MbrAccount account = new MbrAccount();
        account.setLoginName(paramDto.getLoginName());
        MbrAccount account1 = accountMapper.selectOne(account);
        if (isNull(account1)) {
            return null;
        }
        PageHelper.startPage(pageNo, pageSize);
        billRecordDto.setAccountId(account.getId());
        List<BillRecordDto> list = fundMapper.findBillRecordList(billRecordDto);
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils subAgentList(DirectMemberParamDto paramDto, Integer pageNo, Integer pageSize) {
        if (StringUtils.isNotEmpty(paramDto.getSubAgyAccount())) {
            AgentAccount agentAccount = new AgentAccount();
            agentAccount.setAgyAccount(paramDto.getSubAgyAccount());
            AgentAccount agentAccount1 = agentAccountMapper.selectOne(agentAccount);
            if (isNull(agentAccount1)) {
                return null;
            }
            paramDto.setSubAgentId(agentAccount1.getId());
        }
        PageHelper.startPage(pageNo, pageSize);
        String time =DateUtil.getLastMonthByTime(paramDto.getStartTime());
        paramDto.setTime(time);
        paramDto.setGroubyAgent(true);
        List<SubAgentListDto> subAgentList = teamMapper.subAgentList(paramDto);
        if (Collections3.isNotEmpty(subAgentList)) {
            subAgentList.stream().forEach(st -> {
                st.setNetwinlose(st.getTotalProfit());
            });
        }
        return BeanUtil.toPagedResult(subAgentList);
    }
    
    /**
     * 	直属会员-下级代理头部
     * 
     * @param paramDto
     * @return
     */
    public Map subAgentHead(DirectMemberParamDto paramDto) {
        if (StringUtils.isNotEmpty(paramDto.getSubAgyAccount())) {
            AgentAccount agentAccount = new AgentAccount();
            agentAccount.setAgyAccount(paramDto.getSubAgyAccount());
            AgentAccount agentAccount1 = agentAccountMapper.selectOne(agentAccount);
            if (isNull(agentAccount1)) {
                return null;
            }
            paramDto.setSubAgentId(agentAccount1.getId());
        }

        String time =DateUtil.getLastMonthByTime(paramDto.getStartTime());
        paramDto.setTime(time);
        paramDto.setGroubyAgent(true);
        List<SubAgentListDto> subAgentList = teamMapper.subAgentList(paramDto);

        // 返回值
        Map<String, Object> objectMap = new HashMap<>(32);
        Integer accountNum =0;//直属会员
        Integer activeAccountNum=0;//活跃会员
        BigDecimal totalNewDeposits = BigDecimal.ZERO;//首存人数
        BigDecimal totalNewDepositAmount = BigDecimal.ZERO;//首存金额
        BigDecimal depositBalance = BigDecimal.ZERO;//存款
        BigDecimal drawAmount = BigDecimal.ZERO;//提款
        BigDecimal ctDiffer = BigDecimal.ZERO;//存提差
        BigDecimal validBet = BigDecimal.ZERO;//有效投注
        BigDecimal betTotal = BigDecimal.ZERO;//总投注
        BigDecimal totalBonusAmount = BigDecimal.ZERO;//总红利
        BigDecimal payout = BigDecimal.ZERO;//总输赢
        BigDecimal netwinlose = BigDecimal.ZERO;//净输赢
        BigDecimal auditAmAmount =  BigDecimal.ZERO;//人工扣减
        BigDecimal cost =  BigDecimal.ZERO;//平台费
        BigDecimal serviceCost =  BigDecimal.ZERO;//服务费
        BigDecimal calculateProfit =  BigDecimal.ZERO;//资金调整费用

        if (Collections3.isNotEmpty(subAgentList)) {
            subAgentList.stream().forEach(st -> {
                st.setNetwinlose(st.getTotalProfit());
            });
        }
        for (SubAgentListDto dto:subAgentList) {
            if (!isNull(dto.getAccountNum())){
                accountNum=accountNum+dto.getAccountNum();
            }
            if (!isNull(dto.getActiveAccountNum())){
                activeAccountNum=activeAccountNum+dto.getActiveAccountNum();
            }
            if (!isNull(dto.getTotalNewDeposits())){
                totalNewDeposits=totalNewDeposits.add(dto.getTotalNewDeposits());
            }
            if (!isNull(dto.getTotalNewDepositAmount())){
                totalNewDepositAmount=totalNewDepositAmount.add(dto.getTotalNewDepositAmount());
            }
            if (!isNull(dto.getDepositBalance())){
                depositBalance=depositBalance.add(dto.getDepositBalance());
            }
            if (!isNull(dto.getDrawAmount())){
                drawAmount=drawAmount.add(dto.getDrawAmount());
            }
            if (!isNull(dto.getValidBet())){
                validBet=validBet.add(dto.getValidBet());
            }
            if (!isNull(dto.getBetTotal())){
                betTotal=betTotal.add(dto.getBetTotal());
            }
            if (!isNull(dto.getTotalBonusAmount())){
                totalBonusAmount=totalBonusAmount.add(dto.getTotalBonusAmount());
            }
            if (!isNull(dto.getPayout())){
                payout=payout.add(dto.getPayout());
            }
            if (!isNull(dto.getNetwinlose())){
                netwinlose=netwinlose.add(dto.getNetwinlose());
            }
            if (!isNull(dto.getAuditAmAmount())){
            	auditAmAmount=auditAmAmount.add(dto.getAuditAmAmount());
            }
            if (!isNull(dto.getCost())){
            	cost=cost.add(dto.getCost());
            }
            if (!isNull(dto.getServiceCost())){
            	serviceCost=serviceCost.add(dto.getServiceCost());
            }
            if (!isNull(dto.getCalculateProfit())){
            	calculateProfit=calculateProfit.add(dto.getCalculateProfit());
            }
        }
        
        ctDiffer = depositBalance.subtract(drawAmount);
        objectMap.put("ctDiffer",ctDiffer);
        objectMap.put("accountNum",accountNum);
        objectMap.put("activeAccountNum",activeAccountNum);
        objectMap.put("totalNewDeposits",totalNewDeposits);
        objectMap.put("totalNewDepositAmount",totalNewDepositAmount);
        objectMap.put("depositBalance",depositBalance);
        objectMap.put("drawAmount",drawAmount);
        objectMap.put("validBet",validBet);
        objectMap.put("betTotal",betTotal);
        objectMap.put("totalBonusAmount",totalBonusAmount);
        objectMap.put("payout",payout);
        objectMap.put("netwinlose",netwinlose);
        objectMap.put("auditAmAmount",auditAmAmount);
        objectMap.put("cost",cost);
        objectMap.put("serviceCost",serviceCost);
        objectMap.put("calculateProfit",calculateProfit);
        return objectMap;
    }

    public PageUtils aentAccountList(DirectMemberParamDto paramDto, Integer pageNo, Integer pageSize) {
        if (isNull(paramDto.getCagencyId())) {
            AgentAccount agentAccount = new AgentAccount();
            agentAccount.setAgyAccount(paramDto.getAgyAccount());
            AgentAccount agentAccount1 = agentAccountMapper.selectOne(agentAccount);
            if (nonNull(agentAccount1) && agentAccount1.getAttributes() == 1) {
                paramDto.setSubCagencyId(agentAccount1.getId());
                paramDto.setAgyAccount(null);
            }
        }
        PageHelper.startPage(pageNo, pageSize);
        List<DirectMemberDto> dtos = teamMapper.directMember(paramDto);
        setDirectMemberDto(dtos);
        return BeanUtil.toPagedResult(dtos);
    }

    public PageUtils superiorCloneList(DirectMemberParamDto paramDto, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<SubAgentListDto> subAgentList = teamMapper.superiorCloneList(paramDto);
        return BeanUtil.toPagedResult(subAgentList);
    }
    
    /**
     * 	分线代理头部
     * 
     * @param paramDto
     * @return
     */
    public Map superiorCloneHead(DirectMemberParamDto paramDto) {
        List<SubAgentListDto> subAgentList = teamMapper.superiorCloneList(paramDto);
        Map<String, Object> objectMap = new HashMap<>(16);
        
        Integer accountNum =0;//直属会员
        Integer activeAccountNum=0;//活跃会员
        BigDecimal totalNewDeposits = BigDecimal.ZERO;//首存人数
        BigDecimal totalNewDepositAmount = BigDecimal.ZERO;//首存金额
        BigDecimal depositBalance = BigDecimal.ZERO;//存款
        BigDecimal drawAmount = BigDecimal.ZERO;//提款
        BigDecimal ctDiffer = BigDecimal.ZERO;//存提差
        BigDecimal validBet = BigDecimal.ZERO;//有效投注
        BigDecimal betTotal = BigDecimal.ZERO;//投注金额
        BigDecimal totalBonusAmount = BigDecimal.ZERO;//总红利
        BigDecimal payout = BigDecimal.ZERO;//总输赢
        BigDecimal auditAmAmount =  BigDecimal.ZERO;//人工扣减
        BigDecimal calculateProfit =  BigDecimal.ZERO;//人工调整金额

        for (SubAgentListDto dto:subAgentList) {
            if (!isNull(dto.getAccountNum())){
                accountNum=accountNum+dto.getAccountNum();
            }
            if (!isNull(dto.getActiveAccountNum())){
                activeAccountNum=activeAccountNum+dto.getActiveAccountNum();
            }
            if (!isNull(dto.getTotalNewDeposits())){
                totalNewDeposits=totalNewDeposits.add(dto.getTotalNewDeposits());
            }
            if (!isNull(dto.getTotalNewDepositAmount())){
                totalNewDepositAmount=totalNewDepositAmount.add(dto.getTotalNewDepositAmount());
            }
            if (!isNull(dto.getDepositBalance())){
                depositBalance=depositBalance.add(dto.getDepositBalance());
            }
            if (!isNull(dto.getDrawAmount())){
                drawAmount=drawAmount.add(dto.getDrawAmount());
            }
            if (!isNull(dto.getValidBet())){
                validBet=validBet.add(dto.getValidBet());
            }
            if (!isNull(dto.getBetTotal())){
                betTotal=betTotal.add(dto.getBetTotal());
            }
            if (!isNull(dto.getTotalBonusAmount())){
                totalBonusAmount=totalBonusAmount.add(dto.getTotalBonusAmount());
            }
            if (!isNull(dto.getPayout())){
                payout=payout.add(dto.getPayout());
            }
            if (!isNull(dto.getAuditAmAmount())){
            	auditAmAmount = auditAmAmount.add(dto.getAuditAmAmount());
            }
            if (!isNull(dto.getCalculateProfit())){
            	calculateProfit = calculateProfit.add(dto.getCalculateProfit());
            }

        }
        ctDiffer = depositBalance.subtract(drawAmount);
        objectMap.put("ctDiffer",ctDiffer);
        objectMap.put("accountNum",accountNum);
        objectMap.put("activeAccountNum",activeAccountNum);
        objectMap.put("totalNewDeposits",totalNewDeposits);
        objectMap.put("totalNewDepositAmount",totalNewDepositAmount);
        objectMap.put("depositBalance",depositBalance);
        objectMap.put("drawAmount",drawAmount);
        objectMap.put("validBet",validBet);
        objectMap.put("betTotal",betTotal);
        objectMap.put("totalBonusAmount",totalBonusAmount);
        objectMap.put("payout",payout);
        objectMap.put("auditAmAmount",auditAmAmount);
        objectMap.put("calculateProfit",calculateProfit);
        return objectMap;
    }

    public PageUtils superiorCloneAccountList(DirectMemberParamDto paramDto, Integer pageNo, Integer pageSize) {
        AgentAccount agentAccount = new AgentAccount();
        agentAccount.setAgyAccount(paramDto.getAgyAccount());
        AgentAccount agentAccount1 = agentAccountMapper.selectOne(agentAccount);
        paramDto.setSubCagencyId(agentAccount1.getId());
        paramDto.setAgyAccount(null);
        PageHelper.startPage(pageNo, pageSize);
        List<DirectMemberDto> dtos = teamMapper.directMember(paramDto);
        setDirectMemberDto(dtos);
        return BeanUtil.toPagedResult(dtos);
    }

    public void setDirectMemberDto(List<DirectMemberDto> dtos) {
        if (dtos.size() > 0) {
            dtos.stream().forEach(ds -> {
                String str = new AccountEncryption().accountMobieEncrypt(ds.getMobile());
                if (StringUtils.isNotBlank(str)) {
                    if (str.length() > 7) {
                        StringBuilder sb = new StringBuilder(str);
                        sb.replace(3, 7, "****");
                        str = sb.toString();
                    }
                }
                ds.setMobile(str);
            });
        }
    }

    /**
     * 	直属会员-详情-存款记录
     * 
     * @param dto
     * @param pageNo
     * @param pageSize
     * @return
     */
    public PageUtils accountDepositList(DirectMemberParamDto dto, Integer pageNo, Integer pageSize) {
        MbrAccount account = new MbrAccount();
        account.setLoginName(dto.getLoginName());
        MbrAccount mbrAccount = accountMapper.selectOne(account);
        if (isNull(mbrAccount)) {
            return null;
        }
        DepositListDto fundDeposit = new DepositListDto();
        fundDeposit.setAccountId(mbrAccount.getId());
//        fundDeposit.setStatus(Constants.EVNumber.one);
        fundDeposit.setOrderBy(dto.getOrderBy());
        PageHelper.startPage(pageNo, pageSize);
        List<FundDeposit> depositList = fundMapper.findDepositListApi(fundDeposit);
        return BeanUtil.toPagedResult(depositList);
    }

    public PageUtils accountWithdrawList(DirectMemberParamDto dto, Integer pageNo, Integer pageSize) {
        MbrAccount account = new MbrAccount();
        account.setLoginName(dto.getLoginName());
        MbrAccount mbrAccount = accountMapper.selectOne(account);
        if (isNull(mbrAccount)) {
            return null;
        }
        PageHelper.startPage(pageNo, pageSize);
        List<AccWithdraw> list = fundMapper.findFixateAccWithdraw(dto.getStartTime(),
                dto.getEndTime(), mbrAccount.getId(), Constants.EVNumber.one, dto.getOrderBy());
        return BeanUtil.toPagedResult(list);
    }

}
