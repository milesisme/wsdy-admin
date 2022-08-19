package com.wsdy.saasops.modules.analysis.service;

import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.modules.analysis.dto.AgentGameDateDto;
import com.wsdy.saasops.modules.analysis.entity.GameReportModel;
import com.wsdy.saasops.modules.analysis.entity.RptBetTotalModel;
import com.wsdy.saasops.modules.analysis.mapper.GameDateNewMapper;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GameDateNewService {

    @Autowired
    private GameDateNewMapper gameDateNewMapper;

    public PageUtils findBetDayGroupAgentPage(GameReportModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<RptBetTotalModel> list = gameDateNewMapper.getBetDayGroupAgentList(model);
        if (Collections3.isNotEmpty(list)) {
            List<Integer> agentIds = list.stream().map(RptBetTotalModel::getAgentId).collect(Collectors.toList());
            model.setAgentIds(agentIds);
            List<RptBetTotalModel> rptBetTotalModels = gameDateNewMapper.getBetDayGroupAgentCount(model);
            if (Collections3.isNotEmpty(rptBetTotalModels)) {
                list.stream().forEach(ls -> {
                    Optional<RptBetTotalModel> rptBetTotalModel =
                            rptBetTotalModels.stream().filter(
                                    d -> d.getAgentId().equals(ls.getAgentId()))
                                    .findAny();
                    if (rptBetTotalModel.isPresent()) {
                        ls.setSubCount(rptBetTotalModel.get().getSubCount());
                    }
                });
            }
        }
        return BeanUtil.toPagedResult(list);
    }

    public AgentGameDateDto findBetDayBetAgent(GameReportModel model) {
        AgentGameDateDto agentGameDateDto = new AgentGameDateDto();
        List<RptBetTotalModel> rptBetTotalModels = gameDateNewMapper.findBetDayBetAgent(model);
        if (Collections3.isNotEmpty(rptBetTotalModels)) {
            for (RptBetTotalModel rptBetTotalModel : rptBetTotalModels) {
                if (rptBetTotalModel.getIsSign() == 1){
                    agentGameDateDto.setSubordinateAgent(rptBetTotalModel);
                }
                if (rptBetTotalModel.getIsSign() == 2){
                    agentGameDateDto.setDirectAccount(rptBetTotalModel);
                }
                if (rptBetTotalModel.getIsSign() == 3){
                    agentGameDateDto.setAllSubordinates(rptBetTotalModel);
                }
            }
        }
        return agentGameDateDto;
    }

    public PageUtils findBetDayGroupGameTypePage(GameReportModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<RptBetTotalModel> list = gameDateNewMapper.getBetDayGroupGameTypeList(model);
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils findBetDayByAgentPage(GameReportModel model) {
        return BeanUtil.toPagedResult(Collections.singletonList(gameDateNewMapper.getBetDayByAgentTotal(model)));
    }

    public PageUtils findBetDayGroupUserPage(GameReportModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<RptBetTotalModel> list = gameDateNewMapper.getBetDayGroupUserList(model);
        return BeanUtil.toPagedResult(list);
    }

}