package com.wsdy.saasops.modules.agent.service;

import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.modules.agent.dao.AgentAccountMapper;
import com.wsdy.saasops.modules.agent.dao.AgyCommissionMapper;
import com.wsdy.saasops.modules.agent.dao.AgyCommissionProfitMapper;
import com.wsdy.saasops.modules.agent.dto.AgentReportDto;
import com.wsdy.saasops.modules.agent.dto.DepotCostDto;
import com.wsdy.saasops.modules.agent.entity.*;
import com.wsdy.saasops.modules.agent.mapper.AgentCommMapper;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static java.util.Objects.nonNull;


@Service
@Transactional
public class CommissionReportService {

    @Autowired
    private AgentCommMapper agentCommMapper;
    @Autowired
    private AgyCommissionMapper agyCommissionMapper;
    @Autowired
    private AgentWalletService walletService;
    @Autowired
    private AgentAccountMapper agentAccountMapper;
    @Autowired
    private AgyCommissionProfitMapper commissionProfitMapper;


    public PageUtils agentReportList(String startTime, String endTime, Integer accountId, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<AgentReportDto> reportDtos = agentCommMapper.findReportList(startTime, endTime, accountId);
        return BeanUtil.toPagedResult(reportDtos);
    }


    public PageUtils commissionReviewList(AgyCommission commission, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        commission.setType(Constants.EVNumber.zero);
        List<AgyCommission> reportDtos = agentCommMapper.commissionReviewList(commission);
        return BeanUtil.toPagedResult(reportDtos);
    }

    public AgyCommission commissionDetails(AgyCommission commission) {
        AgyCommission agyCommission = agyCommissionMapper.selectByPrimaryKey(commission.getId());
        agyCommission.setDetailsDtos(agentCommMapper.findCommissionDetails(agyCommission.getTime(), agyCommission.getAgyAccount()));
        return agyCommission;
    }

    public void updateReviewStatus(AgyCommission commission, String userName) {
        AgyCommission commission1 = agyCommissionMapper.selectByPrimaryKey(commission.getId());
        if (nonNull(commission1)) {
            updateAgyCommission(commission1, commission, userName);

            AgyCommission agyCommission = new AgyCommission();
            agyCommission.setTime(commission1.getTime());
            agyCommission.setType(Constants.EVNumber.one);
            agyCommission.setSubAgyaccount(commission1.getAgyAccount());
            List<AgyCommission> agyCommissionList = agyCommissionMapper.select(agyCommission);
            for (AgyCommission commission2 : agyCommissionList) {
                updateAgyCommission(commission2, commission, userName);
            }
        }
    }

    private void updateAgyCommission(AgyCommission commission1, AgyCommission commission, String userName) {
        commission1.setReviewStatus(commission.getReviewStatus());
        commission1.setIssuestatus(Constants.EVNumber.two);
        commission1.setModifyUser(userName);
        commission1.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        commission1.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
        commission1.setAuditUser(userName);
        commission1.setMemo(commission.getMemo());
        agyCommissionMapper.updateByPrimaryKeySelective(commission1);
    }

    public PageUtils commissionFreedList(AgyCommission commission, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        commission.setReviewStatus(Constants.EVNumber.one);
        commission.setType(Constants.EVNumber.zero);
        List<AgyCommission> reportDtos = agentCommMapper.commissionReviewList(commission);
        return BeanUtil.toPagedResult(reportDtos);
    }
    
    /**
     * 	代理佣金所有下级列表
     * 
     * @param commission
     * @param pageNo
     * @param pageSize
     * @return
     */
    public PageUtils commissionAllSubList(AgyCommission commission, Integer pageNo, Integer pageSize) {
    	PageHelper.startPage(pageNo, pageSize);
    	commission.setType(Constants.EVNumber.one);
    	List<AgyCommission> reportDtos = agentCommMapper.commissionAllSubList(commission);
    	return BeanUtil.toPagedResult(reportDtos);
    }

    public void updateFreedStatus(AgyCommission commission, String userName) {
        AgyCommission commission1 = agyCommissionMapper.selectByPrimaryKey(commission.getId());
        if (nonNull(commission1) && commission1.getIssuestatus() == 2) {
            addCommission(commission1, commission, userName, Boolean.TRUE);

            AgyCommission agyCommission = new AgyCommission();
            agyCommission.setTime(commission1.getTime());
            agyCommission.setType(Constants.EVNumber.one);
            agyCommission.setSubAgyaccount(commission1.getAgyAccount());
            List<AgyCommission> agyCommissionList = agyCommissionMapper.select(agyCommission);
            for (AgyCommission commission2 : agyCommissionList) {
                commission.setAdjustedAmount(BigDecimal.ZERO);
                addCommission(commission2, commission, userName, Boolean.FALSE);
            }
        }
    }

    private void addCommission(AgyCommission commission1, AgyCommission commission, String userName, Boolean isSign) {
        AgyCommissionProfit profit = new AgyCommissionProfit();
        profit.setAgentId(commission1.getAgentId());
        AgyCommissionProfit commissionProfit = commissionProfitMapper.selectOne(profit);

        if (nonNull(commission.getIsClean()) && commission.getIsClean() == 1 && isSign) {
            commissionProfit.setNetwinlose(BigDecimal.ZERO);
            commissionProfitMapper.updateByPrimaryKeySelective(commissionProfit);
        }

        if (commission.getIssuestatus() == 1) {
            BigDecimal bigDecimal = commission1.getCommission();
            if (nonNull(commission.getAdjustedAmount())) {
                bigDecimal = bigDecimal.add(commission.getAdjustedAmount());
            }
            String financialCode = OrderConstants.AGENT_ORDER_CODE_AYH;
            if (commission1.getType() == 1) {
                financialCode = OrderConstants.AGENT_ORDER_CODE_AXJ;
            }
            AgentAccount agentAccount = agentAccountMapper.selectByPrimaryKey(commission1.getAgentId());
            AgyWallet wallet = walletService.setAgyWallet(agentAccount,
                    bigDecimal, financialCode, null, null,
                    commission1.getOrderNo(), agentAccount.getAgyAccount(),
                    Constants.EVNumber.zero);
            AgyBillDetail billDetail = walletService.addWalletAndBillDetail(
                    wallet, Constants.EVNumber.one);
            commission1.setBillDetailId(billDetail.getId());
        }
        commission1.setAdjustedAmount(commission.getAdjustedAmount());
        commission1.setIssuestatus(commission.getIssuestatus());
        commission1.setModifyUser(userName);
        commission1.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        commission1.setPassTime(getCurrentDate(FORMAT_18_DATE_TIME));
        commission1.setPassUser(userName);
        commission1.setFinanceMemo(commission.getFinanceMemo());
        agyCommissionMapper.updateByPrimaryKeySelective(commission1);
    }

    public PageUtils commissionReport(AgyCommission commission, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<AgyCommission> reportDtos = agentCommMapper.commissionReport(commission);
        return BeanUtil.toPagedResult(reportDtos);
    }

    public PageUtils depotCostList(DepotCostDto dto, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<DepotCostDto> dtoList = agentCommMapper.depotCostList(dto);
        return BeanUtil.toPagedResult(dtoList);
    }

    public DepotCostDto sumDepotCost(DepotCostDto dto) {
        DepotCostDto depotCostDto = agentCommMapper.sumDepotCost(dto);
        return depotCostDto;
    }

    public List<DepotCostDto> depotCostDetail(DepotCostDto dto) {
        List<DepotCostDto> dtoList = agentCommMapper.depotCostDetail(dto);
        return dtoList;
    }
}

