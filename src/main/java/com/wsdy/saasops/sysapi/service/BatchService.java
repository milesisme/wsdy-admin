package com.wsdy.saasops.sysapi.service;

import com.wsdy.saasops.agapi.modules.service.AgentPaymentService;
import com.wsdy.saasops.api.modules.pay.service.PaymentService;
import com.wsdy.saasops.modules.agent.entity.AgentDeposit;
import com.wsdy.saasops.modules.agent.mapper.DepositMapper;
import com.wsdy.saasops.modules.fund.entity.FundDeposit;
import com.wsdy.saasops.modules.fund.mapper.FundMapper;
import com.wsdy.saasops.modules.fund.service.FundDepositService;
import com.wsdy.saasops.modules.member.service.MbrGroupService;
import com.wsdy.saasops.modules.system.pay.mapper.PayMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
public class BatchService {

    @Autowired
    private FundDepositService fundDepositService;
    @Autowired
    private PayMapper payMapper;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private FundMapper fundMapper;
    @Autowired
    private MbrGroupService groupService;
    @Autowired
    private AgentPaymentService agentPaymentService;
    @Autowired
    private DepositMapper depositMapper;


    public void updateDepositAmount() {
        payMapper.clearDepositAmount();
        payMapper.clearOnlinePayAmount();

        groupService.updateRebateShow();
    }

    public void updateFundDeposit() {
        FundDeposit fundDeposit = new FundDeposit();
        fundDeposit.setMemo("未支付");
        paymentService.setFundDeposit(fundDeposit);
        fundMapper.updatePayStatus(fundDeposit);
    }

    public void updateAgentFundDeposit() {
        AgentDeposit fundDeposit = new AgentDeposit();
        fundDeposit.setMemo("未支付");
        agentPaymentService.setFundDeposit(fundDeposit);
        depositMapper.updatePayStatus(fundDeposit);
    }

    public List<FundDeposit> getFundDepositList() {
        return fundDepositService.fundFundDepositByTime();
    }
}
