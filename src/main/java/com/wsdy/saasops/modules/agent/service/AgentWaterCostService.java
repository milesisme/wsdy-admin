package com.wsdy.saasops.modules.agent.service;

import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.modules.agent.dto.CostReportViewDto;
import com.wsdy.saasops.modules.agent.dto.DepotCostDto;
import com.wsdy.saasops.modules.agent.mapper.WaterCostMapper;
import org.apache.lucene.search.ConstantScoreQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentWaterCostService {

    @Autowired
    private WaterCostMapper waterCostMapper;


    /**
     * 	平台费报表
     * 
     * 
     * @param dto
     * @param pageNo
     * @param pageSize
     * @return
     */
    public PageUtils findCostReportViewAgent(DepotCostDto dto, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        dto.setGroubyAgent(Boolean.TRUE);
        List<DepotCostDto> reportDtos = waterCostMapper.findCostReportViewAgent(dto);
        if (reportDtos.size() > 0) {
            reportDtos.add(waterCostMapper.findAgentCostSum(dto));
        }
        return BeanUtil.toPagedResult(reportDtos);
    }

    public CostReportViewDto findCostLostReportView(DepotCostDto reportModelDto) {
        CostReportViewDto ret = new CostReportViewDto();
        // 下级代理
        reportModelDto.setGroubyAgent(Boolean.TRUE);
        reportModelDto.setIsSign(String.valueOf(Constants.EVNumber.one));
        DepotCostDto sub = waterCostMapper.findCostLostReportView(reportModelDto);
        ret.setSubordinateAgent(sub);
        // 直属会员
        reportModelDto.setGroubyAgent(Boolean.FALSE);
        reportModelDto.setIsSign(String.valueOf(Constants.EVNumber.two));
        DepotCostDto account = waterCostMapper.findCostLostReportView(reportModelDto);
        ret.setDirectAccount(account);
        // 所有下级总计
        reportModelDto.setGroubyAgent(Boolean.TRUE);
        reportModelDto.setIsSign(String.valueOf(Constants.EVNumber.three));
        DepotCostDto all = waterCostMapper.findCostLostReportView(reportModelDto);
        ret.setAllSubordinates(all);
        return ret;
    }

    /**	
     * 	直属会员列表
     * @param reportModelDto
     * @param pageNo
     * @param pageSize
     * @return
     */
    public PageUtils findCostListLevel(DepotCostDto reportModelDto, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        reportModelDto.setAccountAgyAccount(reportModelDto.getAgyAccount());
        reportModelDto.setTopAgyAccount(reportModelDto.getAgyAccount());
        reportModelDto.setAgyAccount(null);
        List<DepotCostDto> reportDtos = waterCostMapper.findCostListLevel(reportModelDto);
        if (reportDtos.size() > 0) {
            reportDtos.add(waterCostMapper.findCostListLevelSum(reportModelDto));
        }
        return BeanUtil.toPagedResult(reportDtos);
    }

    public List<DepotCostDto> findCostAccountDetails(DepotCostDto reportModelDto) {
        return waterCostMapper.findCostAccountDetails(reportModelDto);
    }
}

