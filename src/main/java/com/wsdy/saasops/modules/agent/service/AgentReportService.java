package com.wsdy.saasops.modules.agent.service;

import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.modules.agent.entity.*;
import com.wsdy.saasops.modules.agent.mapper.AgyReportMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@Service
@Transactional
public class AgentReportService {

    @Autowired
    private AgyReportMapper agyReportMapper;

    public PageUtils upperScoreRecord(AgyBillDetail billDetail, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        billDetail.setWalletType(Constants.EVNumber.one);
        List<AgyBillDetail> detailList = agyReportMapper.upperScoreRecord(billDetail);
        return BeanUtil.toPagedResult(detailList);
    }

    public PageUtils agentAccountChange(AgyBillDetail billDetail, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<AgyBillDetail> detailList = agyReportMapper.agentAccountChange(billDetail);
        return BeanUtil.toPagedResult(detailList);
    }
}

