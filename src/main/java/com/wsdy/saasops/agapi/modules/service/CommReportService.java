package com.wsdy.saasops.agapi.modules.service;

import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.entity.AgyCommission;
import com.wsdy.saasops.modules.agent.mapper.AgentCommMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.wsdy.saasops.common.utils.DateUtil.*;


@Service
public class CommReportService {

    @Autowired
    private AgentCommMapper agentCommMapper;

    /**
     * 	代理系统 佣金报表查询
     * 
     * @param agentAccount
     * @param type
     * @return
     */
    public AgyCommission finCommission(AgentAccount agentAccount, Integer type) {
        String time = getCurrentDate(FORMAT_6_DATE);
        if (type == Constants.EVNumber.one) {
            time = getLastMonth();
        }
        Integer agentId = agentAccount.getId();
        if (agentAccount.getAttributes() == Constants.EVNumber.four) {
            agentId = agentAccount.getAgentId();
        }
        AgyCommission agyCommission = new AgyCommission();
        agyCommission.setTime(time);
        agyCommission.setAgentId(agentId);
        agyCommission.setType(Constants.EVNumber.zero);
        List<AgyCommission> agyCommissions = agentCommMapper.commissionReport(agyCommission);

        AgyCommission agyCommission1 = new AgyCommission();
        if (agyCommissions.size() > 0) {
            agyCommission1 = agyCommissions.get(0);
            agyCommission.setType(Constants.EVNumber.one);
            agyCommission1.setSubCommission(agentCommMapper.sumCommissionReport(agyCommission));
            agyCommission1.setDepotList(agentCommMapper.findCommissionDepot(agyCommission1.getOrderNo()));
            return agyCommission1;
        } else {
            AgyCommission agyCommission2 = new AgyCommission();
            agyCommission2.setType(Constants.EVNumber.one);
            agyCommission2.setTime(time);
            agyCommission2.setAgentId(agentId);
            agyCommission1.setSubCommission(agentCommMapper.sumCommissionReport(agyCommission2));
        }
        return agyCommission1;
    }

}
