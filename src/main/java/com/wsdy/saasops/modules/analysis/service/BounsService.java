package com.wsdy.saasops.modules.analysis.service;

import com.beust.jcommander.internal.Lists;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.modules.analysis.entity.BounsReportQueryModel;
import com.wsdy.saasops.modules.analysis.entity.RptWinLostModel;
import com.wsdy.saasops.modules.analysis.mapper.BounsMapper;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class BounsService {

    @Autowired
    private BounsMapper bounsMapper;
    @Autowired
    private AnalysisService analysisService;

    public PageUtils findSubordinateAgent(BounsReportQueryModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<RptWinLostModel> list = bounsMapper.findSubordinateAgent(model);
        list.add(analysisService.totalWinLost(list));
        list.add(bounsMapper.findSubordinateAgentListTotal(model));
        return BeanUtil.toPagedResult(list);
    }

    public List<RptWinLostModel> findSubordinateBonus(BounsReportQueryModel model) {
        model.setIsSign(Constants.EVNumber.one);
        RptWinLostModel agentRptWinLostModel = bounsMapper.findSubordinateBonus(model);
        model.setIsSign(Constants.EVNumber.two);
        RptWinLostModel accountRptWinLostModel = bounsMapper.findSubordinateBonus(model);
        List<RptWinLostModel> rptWinLostModels = Lists.newArrayList();
        rptWinLostModels.add(agentRptWinLostModel);
        rptWinLostModels.add(accountRptWinLostModel);
        return rptWinLostModels;
    }

}