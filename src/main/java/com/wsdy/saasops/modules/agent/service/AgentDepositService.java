package com.wsdy.saasops.modules.agent.service;

import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.modules.agent.dao.AgentAccountMapper;
import com.wsdy.saasops.modules.agent.dao.AgentDepositMapper;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.entity.AgentDeposit;
import com.wsdy.saasops.modules.agent.entity.AgyWallet;
import com.wsdy.saasops.modules.agent.mapper.DepositMapper;
import com.wsdy.saasops.modules.fund.entity.FundDeposit;
import com.wsdy.saasops.modules.system.pay.mapper.PayMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

import static com.wsdy.saasops.common.constants.Constants.SYSTEM_USER;
import static com.wsdy.saasops.common.utils.DateUtil.*;

@Service
@Transactional
public class AgentDepositService {

    @Autowired
    private AgentDepositMapper agentDepositMapper;
    @Autowired
    private AgentWalletService walletService;
    @Autowired
    private DepositMapper depositMapper;
    @Autowired
    private PayMapper payMapper;
    @Autowired
    private AgentAccountMapper agentAccountMapper;

    public AgentDeposit queryObject(Integer id) {
        AgentDeposit agentDeposit = new AgentDeposit();
        agentDeposit.setId(id);
        Optional<AgentDeposit> optional = Optional.ofNullable(
                depositMapper.findDepositList(agentDeposit)
                        .stream().findAny()).get();
        if (optional.isPresent()) {
            AgentDeposit deposit = optional.get();
            FundDeposit sit = new FundDeposit();
            sit.setMark(deposit.getMark());
            sit.setStatus(Constants.IsStatus.succeed);
            sit.setAccountId(deposit.getAccountId());
            int depositCount = depositMapper.findDepositCount(sit);
            deposit.setDepositCount(depositCount);
            return deposit;
        }
        return null;
    }

    public PageUtils queryListPage(AgentDeposit agentDeposit, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<AgentDeposit> list = depositMapper.findDepositList(agentDeposit);
        return BeanUtil.toPagedResult(list);
    }

    public AgentDeposit depositSumStatistic(AgentDeposit agentDeposit) {
        AgentDeposit allTotal = depositMapper.findDepositListSum(agentDeposit);
        return allTotal;
    }

    public Double findSumDepositAmount(AgentDeposit agentDeposit) {
        agentDeposit.setStatus(Constants.IsStatus.succeed);
        agentDeposit.setCreateTime(getCurrentDate(FORMAT_10_DATE));
        return depositMapper.findSumDepositAmount(agentDeposit);
    }

    @Transactional
    public AgentDeposit updateDeposit(AgentDeposit agentDeposit, String userName, String ip) {
        // ???????????????????????????????????????deposit
        AgentDeposit deposit = checkoutFund(agentDeposit);
        // ??????????????????????????????????????????,?????????update
        if (Objects.nonNull(agentDeposit.getActualArrival())) {
            deposit.setActualArrival(agentDeposit.getActualArrival());
        }
        // ????????????
        if (Constants.IsStatus.succeed.equals(agentDeposit.getStatus())) {
            updateDepositSucceed(deposit, false, true);   // ???????????????????????????????????????
        }
        deposit.setStatus(agentDeposit.getStatus());
        deposit.setAuditUser(userName);
        deposit.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
        // ??????????????????
        if (!StringUtil.isEmpty(agentDeposit.getMemo())) {
            deposit.setMemo(agentDeposit.getMemo());
        }
        deposit.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        deposit.setModifyUser(userName);
        AgentDeposit oldDeposit = agentDepositMapper.selectByPrimaryKey(deposit);
        agentDepositMapper.updateByPrimaryKey(deposit);
        return deposit;
    }

    public void updateDepositSucceed(AgentDeposit fundDeposit, boolean isUpdateStatus, boolean isActualArrival) {
        // ???????????????????????????????????????????????????
        fundDeposit.setStatus(Constants.IsStatus.succeed);
        fundDeposit.setAuditUser(SYSTEM_USER);
        fundDeposit.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
        fundDeposit.setIsPayment(Boolean.TRUE);
        int i = 0;
        if (isUpdateStatus) {
            i = depositMapper.updatePayStatus(fundDeposit);
        }
        if (i > 0 || !isUpdateStatus) {
            // ??????
            AgentAccount agentAccount = agentAccountMapper.selectByPrimaryKey(fundDeposit.getAccountId());
            AgyWallet agyWallet1 = walletService.setAgyWallet(agentAccount, fundDeposit.getActualArrival(),
                    OrderConstants.AGENT_CZ, agentAccount.getId(),
                    null, fundDeposit.getOrderNo(), agentAccount.getAgyAccount(), Constants.EVNumber.one);
            walletService.addWalletAndBillDetail(agyWallet1, Constants.EVNumber.one);

            if (Objects.nonNull(fundDeposit.getCompanyPayId())) {
                payMapper.updateDepositAmount(fundDeposit.getDepositAmount(), fundDeposit.getCompanyPayId());
            }
            if (Objects.nonNull(fundDeposit.getOnlinePayId())) {
                payMapper.updateOnlinePayAmount(fundDeposit.getDepositAmount(), fundDeposit.getOnlinePayId());
            }
            if (Objects.nonNull(fundDeposit.getQrCodeId())) {
                payMapper.updateQrCodeAmount(fundDeposit.getDepositAmount(), fundDeposit.getQrCodeId());
            }
        }
    }

    private AgentDeposit checkoutFund(AgentDeposit agentDeposit) {
        AgentDeposit deposit = agentDepositMapper.selectByPrimaryKey(agentDeposit.getId());
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
}
