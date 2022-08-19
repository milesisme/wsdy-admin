package com.wsdy.saasops.modules.agent.service;

import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.modules.agent.dao.AgentContracttMapper;
import com.wsdy.saasops.modules.agent.entity.AgentContract;
import com.wsdy.saasops.modules.agent.mapper.ContractMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;

@Slf4j
@Service
@Transactional
public class AgentContractService {

    @Autowired
    private AgentContracttMapper contracttMapper;
    @Autowired
    private ContractMapper contractMapper;

    public List<AgentContract> allContractList() {
        return contracttMapper.select(null);
    }

    public PageUtils contractList(Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<AgentContract> list = contractMapper.contractList();
        return BeanUtil.toPagedResult(list);
    }

    public AgentContract contractInfo(AgentContract contract) {
        return contracttMapper.selectByPrimaryKey(contract.getId());
    }

    public void addContract(AgentContract contract) {
        contract.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        contracttMapper.insert(contract);
    }

    public void updateContract(AgentContract contract) {
        contract.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        contracttMapper.updateByPrimaryKeySelective(contract);
    }
}

