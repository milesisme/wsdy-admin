package com.wsdy.saasops.modules.agent.service;

import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.modules.agent.dto.AgentChargeMDto;
import com.wsdy.saasops.modules.agent.mapper.ChargeCostMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentServiceChargService {

    @Autowired
    private ChargeCostMapper chargeCostMapper;

    public PageUtils findServiceChargAgent(AgentChargeMDto dto, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<AgentChargeMDto> reportDtos = chargeCostMapper.findServiceChargAgent(dto);
        if (reportDtos.size() > 0) {
            reportDtos.add(chargeCostMapper.sumServiceChargAgent(dto));
        }
        return BeanUtil.toPagedResult(reportDtos);
    }

    public PageUtils findServiceChargAccount(AgentChargeMDto dto, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<AgentChargeMDto> reportDtos = chargeCostMapper.findServiceChargAccount(dto);
        if (reportDtos.size() > 0) {
            reportDtos.add(chargeCostMapper.sumServiceChargAccount(dto));
        }
        return BeanUtil.toPagedResult(reportDtos);
    }
}

