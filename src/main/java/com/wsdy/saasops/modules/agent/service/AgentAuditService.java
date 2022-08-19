package com.wsdy.saasops.modules.agent.service;

import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.utils.SnowFlake;
import com.wsdy.saasops.modules.agent.dao.AgentAccountMapper;
import com.wsdy.saasops.modules.agent.dao.AgentAuditMapper;
import com.wsdy.saasops.modules.agent.entity.*;
import com.wsdy.saasops.modules.agent.mapper.AgyAuditMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static java.util.Objects.isNull;

@Slf4j
@Service
@Transactional
public class AgentAuditService {

    @Autowired
    private AgentAccountMapper agentAccountMapper;
    @Autowired
    private AgentAuditMapper agentAuditMapper;
    @Autowired
    private AgentWalletService walletService;
    @Autowired
    private AgyAuditMapper agyAuditMapper;

    public void auditSave(AgentAudit agentAudit) {
        String orderNo = String.valueOf(new SnowFlake().nextId());
        AgentAccount agentAccount = new AgentAccount();
        agentAccount.setAgyAccount(agentAudit.getAgyAccount());
        AgentAccount account = agentAccountMapper.selectOne(agentAccount);
        if (isNull(account)) {
            throw new R200Exception("代理不存在");
        }
        agentAudit.setAgentId(account.getId());
        agentAudit.setAgyAccount(account.getAgyAccount());
        agentAudit.setStatus(Constants.EVNumber.one);
        agentAudit.setOrderNo(orderNo);
        agentAudit.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        agentAudit.setModifyTime(agentAudit.getCreateTime());

        if (OrderConstants.AGENT_ORDER_CODE_AM.equals(agentAudit.getFinancialCode())) {
            AgyWallet wallet = walletService.setAgyWallet(account,
                    agentAudit.getAmount(), OrderConstants.AGENT_ORDER_CODE_AM,
                    null, null, orderNo, account.getAgyAccount(),
                    agentAudit.getWalletType());
            AgyBillDetail billDetail = walletService.reduceWalletAndBillDetail(
                    wallet, Constants.EVNumber.zero);
            if (Objects.isNull(billDetail)) {
                throw new R200Exception("人工减少余额不足");
            }
            agentAudit.setBilldetailId(billDetail.getId());
            agentAudit.setAfterBalance(billDetail.getAfterBalance());
            agentAudit.setBeforeBalance(billDetail.getBeforeBalance());
        }
        if (OrderConstants.AGENT_ORDER_CODE_AA.equals(agentAudit.getFinancialCode())) {
            AgyWallet wallet = walletService.setAgyWallet(account,
                    agentAudit.getAmount(), OrderConstants.AGENT_ORDER_CODE_AA,
                    agentAudit.getId(), null, orderNo, account.getAgyAccount(),
                    agentAudit.getWalletType());
            AgyBillDetail billDetail = walletService.addWalletAndBillDetail(
                    wallet, Constants.EVNumber.one);
            agentAudit.setBilldetailId(billDetail.getId());
            agentAudit.setAfterBalance(billDetail.getAfterBalance());
            agentAudit.setBeforeBalance(billDetail.getBeforeBalance());
        }
        agentAuditMapper.insert(agentAudit);
    }

    public void auditUpdateStatus(AgentAudit fundAudit, String siteCode, String ip) {
        AgentAudit audit = agentAuditMapper.selectByPrimaryKey(fundAudit.getId());
        if (!audit.getStatus().equals(Constants.IsStatus.pending)) {
            throw new R200Exception("订单已处理完成，不可再次审核");
        }
        AgentAccount account = agentAccountMapper.selectByPrimaryKey(fundAudit.getAgentId());
        audit.setStatus(fundAudit.getStatus());
        audit.setMemo(fundAudit.getMemo());
        audit.setAuditUser(fundAudit.getModifyUser());
        audit.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
        audit.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        audit.setModifyUser(fundAudit.getModifyUser());
        if (fundAudit.getStatus() == Constants.EVNumber.one) {
            if (OrderConstants.AGENT_ORDER_CODE_AA.equals(audit.getFinancialCode())) {
                AgyWallet wallet = walletService.setAgyWallet(account,
                        audit.getAmount(), OrderConstants.AGENT_ORDER_CODE_AA,
                        null, null, audit.getOrderNo(), audit.getAgyAccount(),
                        audit.getWalletType());
                AgyBillDetail billDetail = walletService.addWalletAndBillDetail(
                        wallet, Constants.EVNumber.one);
                audit.setBilldetailId(billDetail.getId());
                audit.setAfterBalance(billDetail.getAfterBalance());
                audit.setBeforeBalance(billDetail.getBeforeBalance());
            }
        } else if (fundAudit.getStatus() == Constants.EVNumber.zero) {
            if (OrderConstants.AGENT_ORDER_CODE_AM.equals(audit.getFinancialCode())) {
                AgyWallet wallet = walletService.setAgyWallet(account,
                        audit.getAmount(), OrderConstants.AGENT_ORDER_CODE_AM,
                        null, null, audit.getOrderNo(),
                        audit.getAgyAccount(), audit.getWalletType());
                 walletService.addWalletAndBillDetail(wallet, Constants.EVNumber.one);
            }
        }
        agentAuditMapper.updateByPrimaryKey(audit);
    }

    public PageUtils auditList(AgentAudit audit, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<AgentAudit> list = agyAuditMapper.auditList(audit);
        return BeanUtil.toPagedResult(list);
    }
}

