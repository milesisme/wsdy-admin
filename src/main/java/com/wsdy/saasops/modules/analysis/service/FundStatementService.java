package com.wsdy.saasops.modules.analysis.service;

import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.service.AgentAccountService;
import com.wsdy.saasops.modules.analysis.dto.*;
import com.wsdy.saasops.modules.analysis.entity.FundStatementModel;
import com.wsdy.saasops.modules.analysis.mapper.FundStatementMapper;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import com.wsdy.saasops.modules.sys.service.SysFileExportRecordService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FundStatementService {

    @Autowired
    private FundStatementMapper fundStatementMapper;

    @Autowired
    private AgentAccountService accountService;

    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;
    
    @Autowired
    private JsonUtil jsonUtil;

    /***
     * 按天查询资金报表
     */
    public PageUtils findFundReportPage(FundStatementModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<FundStatementModel> list = fundStatementMapper.findFundReportPage(model);
        PageUtils p = BeanUtil.toPagedResult(list);
        return p;
    }

    public PageUtils findFundReportMbrPage(FundStatementModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<FundStatementModel> list = fundStatementMapper.findFundReportPage(model);
        PageUtils p = BeanUtil.toPagedResult(list);
        return p;
    }

    /***
     *  查询总体详情
     */
    public FundStatementModel findFundTotalInfo(FundStatementModel model) {
        FundStatementModel resultList = fundStatementMapper.findFundTotalInfo(model);
        return resultList;
    }

    public List<FundStatementModel> findDepotPayoutList(FundStatementModel model){
        List<FundStatementModel> resultList = fundStatementMapper.findDepotPayoutList(model);
        return resultList;
    }

    public PageUtils findTagencyList(FundStatementModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<FundStatementModel> resultList = fundStatementMapper.findTagencyList(model);
        if(resultList.size() != 0){
            //获取小计
            resultList.add(getSubtotal(resultList, true));
            //获取总计
            resultList.add(getTotal(model, true, false));
        }
        PageUtils p = BeanUtil.toPagedResult(resultList);
        return p;
    }


    public PageUtils tagencyFundList(FundStatementModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<FundAgentStatementDto> resultList = fundStatementMapper.tagencyFundList(model);
        PageUtils p = BeanUtil.toPagedResult(resultList);
        return p;
    }

    public PageUtils agentList(FundStatementModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<FundStatementDto> resultList = fundStatementMapper.agentList(model);
        //FundStatementDto fundStatementDto = fundStatementMapper.agentListSum(model);
       // resultList.add(fundStatementDto);
        PageUtils p = BeanUtil.toPagedResult(resultList);
        return p;
    }

    public List<FundStatementModel> totalList(FundStatementModel model){
        List<FundStatementModel> resultList = new ArrayList<>();
        // 下级代理
        resultList.add(getTotal(model, true, true));
        // 直属会员
        resultList.add(getTotal(model, false, true));
        return resultList;
    }

    public PageUtils findMemberList(FundStatementModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<FundStatementModel> resultList = fundStatementMapper.findMemberList(model);
        // 无限会员层级则这里的总计小计没有意义，是重复计算值
//        if(resultList.size() != 0){
//            //获取小计
//            resultList.add(getSubtotal(resultList, false));
//            //获取总计
//            resultList.add(getTotal(model, false, false));
//        }
        PageUtils p = BeanUtil.toPagedResult(resultList);
        return p;
    }

    public PageUtils agentSubMbrList(FundStatementModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<FundStatementModel> resultList = fundStatementMapper.agentSubMbrList(model);
        PageUtils p = BeanUtil.toPagedResult(resultList);
        return p;
    }
    
    /**
     * 	会员资金报表
     * 
     * @param model
     * @return
     */
    public PageUtils fundMbrList(FundStatementModel model) {
    	PageHelper.startPage(model.getPageNo(), model.getPageSize());
    	List<FundStatementModel> resultList = fundStatementMapper.fundMbrList(model);
    	PageUtils p = BeanUtil.toPagedResult(resultList);
    	return p;
    }

    public List<FundStatementModel> totalMbrList(FundStatementModel model){
        List<FundStatementModel> resultList = new ArrayList<>();
        List<FundStatementModel> subMbr = new ArrayList<>();
        List<FundStatementModel> subMbrAgent = new ArrayList<>();

        // 查询会员直属下级数据
        List<FundStatementModel> qryList = fundStatementMapper.mbrSubList(model);

        if(!Collections3.isEmpty(qryList) && qryList.get(0) != null){
            // 分组计算
            Map<Integer, List<FundStatementModel>> groupBy =
                    qryList.stream().collect(
                            Collectors.groupingBy(
                                    FundStatementModel::getAgyFlag));

            subMbr = groupBy.get(Constants.EVNumber.zero);
            subMbrAgent = groupBy.get(Constants.EVNumber.one);
        }

        // 下级代理会员
        resultList.add(getTotalMbr(subMbr,true));
        // 下级非代理会员
        resultList.add(getTotalMbr(subMbrAgent,false));
        return resultList;
    }

    public PageUtils mbrSubMbrList(FundStatementModel model){
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<FundStatementModel> resultList = fundStatementMapper.mbrSubList(model);
        PageUtils p = BeanUtil.toPagedResult(resultList);
        return p;
    }


    private FundStatementModel getTotalMbr(List<FundStatementModel> secondList, Boolean view){
        FundStatementModel allTotal = new FundStatementModel();

        BigDecimal totalProfit = BigDecimal.ZERO;
        BigDecimal totalDepositBalance = BigDecimal.ZERO;
        BigDecimal fundAdjust = BigDecimal.ZERO;
        BigDecimal totalRebate = BigDecimal.ZERO;
        BigDecimal totalDrawAmount = BigDecimal.ZERO;
        BigDecimal totalBonusAmount = BigDecimal.ZERO;
        BigDecimal totalPayout = BigDecimal.ZERO;
        BigDecimal totalJackpotPayout = BigDecimal.ZERO;
        BigDecimal friendsTransAmountTotal = BigDecimal.ZERO;
        BigDecimal friendsRecepitAmountTotal = BigDecimal.ZERO;
        BigDecimal totalTaskBonus = BigDecimal.ZERO;
        if(!Collections3.isEmpty(secondList)){
            for (FundStatementModel item : secondList) {
                if (item.getTotalProfit() != null) {
                    totalProfit = totalProfit.add(item.getTotalProfit());
                }
                if (item.getTotalDepositBalance() != null) {
                    totalDepositBalance = totalDepositBalance.add(item.getTotalDepositBalance());
                }
                if (item.getFundAdjust() != null) {
                    fundAdjust = fundAdjust.add(item.getFundAdjust());
                }
                if (item.getTotalRebate() != null) {
                    totalRebate = totalRebate.add(item.getTotalRebate());
                }
                if (item.getTotalDrawAmount() != null) {
                    totalDrawAmount = totalDrawAmount.add(item.getTotalDrawAmount());
                }
                if (item.getTotalBonusAmount() != null) {
                    totalBonusAmount = totalBonusAmount.add(item.getTotalBonusAmount());
                }
                if (item.getTotalPayout() != null) {
                    totalPayout = totalPayout.add(item.getTotalPayout());
                }
                if (item.getTotalJackpotPayout() != null) {
                    totalJackpotPayout = totalJackpotPayout.add(item.getTotalJackpotPayout());
                }
                if(item.getFriendsRecepitAmountTotal() !=null){
                    friendsRecepitAmountTotal = friendsRecepitAmountTotal.add(item.getFriendsRecepitAmountTotal());
                }
                if(item.getFriendsTransAmountTotal() !=null){
                    friendsTransAmountTotal = friendsTransAmountTotal.add(item.getFriendsTransAmountTotal());
                }
                if(item.getTotalTaskBonus() !=null){
                    totalTaskBonus = totalTaskBonus.add(item.getTotalTaskBonus());
                }
            }
        }
        if(view){
            allTotal.setAgyAccount("下级代理会员");
        } else {
            allTotal.setAgyAccount("直属会员");
        }
        allTotal.setTotalProfit(totalProfit);
        allTotal.setTotalDepositBalance(totalDepositBalance);
        allTotal.setFundAdjust(fundAdjust);
        allTotal.setTotalRebate(totalRebate);
        allTotal.setTotalDrawAmount(totalDrawAmount);
        allTotal.setTotalBonusAmount(totalBonusAmount);
        allTotal.setTotalPayout(totalPayout);
        allTotal.setTotalJackpotPayout(totalJackpotPayout);
        allTotal.setFriendsRecepitAmountTotal(friendsRecepitAmountTotal);
        allTotal.setFriendsTransAmountTotal(friendsTransAmountTotal);
        allTotal.setTotalTaskBonus(totalTaskBonus);
        return allTotal;
    }

    private FundStatementModel getTotal(FundStatementModel model, Boolean flag, Boolean view){
        FundStatementModel allTotal = new FundStatementModel();
        List<FundStatementModel> secondList;
        if(flag){
            secondList = fundStatementMapper.findTagencyList(model);
        } else {
            secondList = fundStatementMapper.findMemberList(model);
        }
        BigDecimal totalProfit = BigDecimal.ZERO;
        BigDecimal totalDepositBalance = BigDecimal.ZERO;
        BigDecimal fundAdjust = BigDecimal.ZERO;
        BigDecimal totalRebate = BigDecimal.ZERO;
        BigDecimal totalDrawAmount = BigDecimal.ZERO;
        BigDecimal totalBonusAmount = BigDecimal.ZERO;
        BigDecimal totalPayout = BigDecimal.ZERO;
        BigDecimal totalJackpotPayout = BigDecimal.ZERO;
        BigDecimal friendsTransAmountTotal = BigDecimal.ZERO;
        BigDecimal friendsRecepitAmountTotal = BigDecimal.ZERO;
        BigDecimal totalTaskBonus = BigDecimal.ZERO;
        BigDecimal totalBonusAmountOnline = BigDecimal.ZERO;
        BigDecimal totalBonusAmountOffline = BigDecimal.ZERO;
        if(!Collections3.isEmpty(secondList)){
            for (FundStatementModel item : secondList) {
                if (item.getTotalProfit() != null) {
                    totalProfit = totalProfit.add(item.getTotalProfit());
                }
                if (item.getTotalDepositBalance() != null) {
                    totalDepositBalance = totalDepositBalance.add(item.getTotalDepositBalance());
                }
                if (item.getFundAdjust() != null) {
                    fundAdjust = fundAdjust.add(item.getFundAdjust());
                }
                if (item.getTotalRebate() != null) {
                    totalRebate = totalRebate.add(item.getTotalRebate());
                }
                if (item.getTotalDrawAmount() != null) {
                    totalDrawAmount = totalDrawAmount.add(item.getTotalDrawAmount());
                }
                if (item.getTotalBonusAmount() != null) {
                    totalBonusAmount = totalBonusAmount.add(item.getTotalBonusAmount());
                }
                if (item.getTotalPayout() != null) {
                    totalPayout = totalPayout.add(item.getTotalPayout());
                }
                if (item.getTotalJackpotPayout() != null) {
                    totalJackpotPayout = totalJackpotPayout.add(item.getTotalJackpotPayout());
                }
                if(item.getFriendsRecepitAmountTotal() !=null){
                    friendsRecepitAmountTotal = friendsRecepitAmountTotal.add(item.getFriendsRecepitAmountTotal());
                }
                if(item.getFriendsTransAmountTotal() !=null){
                    friendsTransAmountTotal = friendsTransAmountTotal.add(item.getFriendsTransAmountTotal());
                }
                if(item.getTotalTaskBonus() !=null){
                	totalTaskBonus = totalTaskBonus.add(item.getTotalTaskBonus());
                }
                if(item.getTotalBonusAmountOnline() !=null){
                	totalBonusAmountOnline = totalBonusAmountOnline.add(item.getTotalBonusAmountOnline());
                }
                if(item.getTotalBonusAmountOffline() !=null){
                	totalBonusAmountOffline = totalBonusAmountOffline.add(item.getTotalBonusAmountOffline());
                }
            }
        }
        if(view){
            if(flag){
                allTotal.setAgyAccount("下级代理");
            } else {
                allTotal.setAgyAccount("直属会员");
            }
        }else{
            if(flag){
                allTotal.setAgyAccount("总计");
            } else {
                allTotal.setLoginName("总计");
            }
        }
        allTotal.setTotalProfit(totalProfit);
        allTotal.setTotalDepositBalance(totalDepositBalance);
        allTotal.setFundAdjust(fundAdjust);
        allTotal.setTotalRebate(totalRebate);
        allTotal.setTotalDrawAmount(totalDrawAmount);
        allTotal.setTotalBonusAmount(totalBonusAmount);
        allTotal.setTotalPayout(totalPayout);
        allTotal.setTotalJackpotPayout(totalJackpotPayout);
        allTotal.setFriendsRecepitAmountTotal(friendsRecepitAmountTotal);
        allTotal.setFriendsTransAmountTotal(friendsTransAmountTotal);
        allTotal.setTotalTaskBonus(totalTaskBonus);
        allTotal.setTotalBonusAmountOnline(totalBonusAmountOnline);
        allTotal.setTotalBonusAmountOffline(totalBonusAmountOffline);;
        return allTotal;
    }

    /**
     * 返回小计
     */
    private FundStatementModel getSubtotal(List<FundStatementModel> list, Boolean flag) {
        FundStatementModel subTotal = new FundStatementModel();
        BigDecimal totalProfit = BigDecimal.ZERO;
        BigDecimal totalDepositBalance = BigDecimal.ZERO;
        BigDecimal fundAdjust = BigDecimal.ZERO;
        BigDecimal totalRebate = BigDecimal.ZERO;
        BigDecimal totalDrawAmount = BigDecimal.ZERO;
        BigDecimal totalBonusAmount = BigDecimal.ZERO;
        BigDecimal totalPayout = BigDecimal.ZERO;
        BigDecimal totalJackpotPayout = BigDecimal.ZERO;
        BigDecimal friendsTransAmountTotal = BigDecimal.ZERO;
        BigDecimal friendsRecepitAmountTotal = BigDecimal.ZERO;
        BigDecimal totalTaskBonus = BigDecimal.ZERO;
        BigDecimal totalBonusAmountOnline = BigDecimal.ZERO;
        BigDecimal totalBonusAmountOffline = BigDecimal.ZERO;
        if(!Collections3.isEmpty(list)){
            for (FundStatementModel model : list) {
                if (model.getTotalProfit() != null) {
                    totalProfit = totalProfit.add(model.getTotalProfit());
                }
                if (model.getTotalDepositBalance() != null) {
                    totalDepositBalance = totalDepositBalance.add(model.getTotalDepositBalance());
                }
                if (model.getFundAdjust() != null) {
                    fundAdjust = fundAdjust.add(model.getFundAdjust());
                }
                if (model.getTotalRebate() != null) {
                    totalRebate = totalRebate.add(model.getTotalRebate());
                }
                if (model.getTotalDrawAmount() != null) {
                    totalDrawAmount = totalDrawAmount.add(model.getTotalDrawAmount());
                }
                if (model.getTotalBonusAmount() != null) {
                    totalBonusAmount = totalBonusAmount.add(model.getTotalBonusAmount());
                }
                if (model.getTotalPayout() != null) {
                    totalPayout = totalPayout.add(model.getTotalPayout());
                }
                if (model.getTotalJackpotPayout() != null) {
                    totalJackpotPayout = totalJackpotPayout.add(model.getTotalJackpotPayout());
                }
                if(model.getFriendsRecepitAmountTotal() !=null){
                    friendsRecepitAmountTotal = friendsRecepitAmountTotal.add(model.getFriendsRecepitAmountTotal());
                }
                if(model.getFriendsTransAmountTotal() !=null){
                    friendsTransAmountTotal = friendsTransAmountTotal.add(model.getFriendsTransAmountTotal());
                }
                if(model.getTotalTaskBonus() !=null){
                    totalTaskBonus = totalTaskBonus.add(model.getTotalTaskBonus());
                }
                if(model.getTotalBonusAmountOnline() !=null){
                	totalBonusAmountOnline = totalBonusAmountOnline.add(model.getTotalBonusAmountOnline());
                }
                if(model.getTotalBonusAmountOffline() !=null){
                	totalBonusAmountOffline = totalBonusAmountOffline.add(model.getTotalBonusAmountOffline());
                }
            }
        }
        if(flag){
            subTotal.setAgyAccount("小计");
        } else {
            subTotal.setLoginName("小计");
        }
        subTotal.setTotalProfit(totalProfit);
        subTotal.setTotalDepositBalance(totalDepositBalance);
        subTotal.setFundAdjust(fundAdjust);
        subTotal.setTotalRebate(totalRebate);
        subTotal.setTotalDrawAmount(totalDrawAmount);
        subTotal.setTotalBonusAmount(totalBonusAmount);
        subTotal.setTotalPayout(totalPayout);
        subTotal.setTotalJackpotPayout(totalJackpotPayout);
        subTotal.setFriendsRecepitAmountTotal(friendsRecepitAmountTotal);
        subTotal.setFriendsTransAmountTotal(friendsTransAmountTotal);
        subTotal.setTotalTaskBonus(totalTaskBonus);
        subTotal.setTotalBonusAmountOnline(totalBonusAmountOnline);
        subTotal.setTotalBonusAmountOffline(totalBonusAmountOffline);
        
        return subTotal;
    }

    public AgentAccount judgeTagency(String agyAccount){
        AgentAccount agentInfo = accountService.findSubAgency(agyAccount);
        Assert.isNull(agentInfo, "代理名有误");
        AgentAccount ret = new AgentAccount();
        ret.setId(agentInfo.getId());
        ret.setAgyAccount(agentInfo.getAgyAccount());
        ret.setParentId(agentInfo.getParentId());
        ret.setDepartmentid(agentInfo.getDepartmentid());
        return ret;
    }

    public FundStatementModel findFundTotalInfoMbrSelf(FundStatementModel model) {
        FundStatementModel resultList = fundStatementMapper.findFundTotalInfo(model);
        return resultList;
    }


    public SysFileExportRecord exportTagencyList(FundStatementModel model, Long userId, String module) {
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);
        if (null != record) {
            String siteCode = CommonUtil.getSiteCode();
            List<Map<String, Object>> sheetsList = new ArrayList<>();
            List<FundStatementModel> resultList = fundStatementMapper.findTagencyList(model);
            ExportParams exportParams = new ExportParams();


            List<ExportTagencyDto> data = new ArrayList<>();
            for (FundStatementModel fundStatementModel : resultList) {
                ExportTagencyDto exportTagencyDto = new ExportTagencyDto();
                BeanUtils.copyProperties(fundStatementModel, exportTagencyDto);
                data.add(exportTagencyDto);
            }

            Map<String, Object> map = new HashMap<>();
            map.put("data", data);
            map.put("entity", ExportTagencyDto.class);
            map.put("title", exportParams);
            sheetsList.add(map);
            sysFileExportRecordService.exportMilSheet(sheetsList,  userId,  module,  siteCode);
        }
        return record;
    }

    public SysFileExportRecord exportList(FundStatementModel model, Long userId, String module) {
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);
        if (null != record) {
            String siteCode = CommonUtil.getSiteCode();
            List<Map<String, Object>> sheetsList = new ArrayList<>();
            List<FundStatementModel> resultList = fundStatementMapper.findFundReportPage(model);
            ExportParams exportParams = new ExportParams();


            List<ExportListDto> data = new ArrayList<>();
            for (FundStatementModel fundStatementModel : resultList) {
                ExportListDto exportListDto = new ExportListDto();
                BeanUtils.copyProperties(fundStatementModel, exportListDto);
                data.add(exportListDto);
            }

            Map<String, Object> map = new HashMap<>();
            map.put("data", data);
            map.put("entity", ExportListDto.class);
            map.put("title", exportParams);
            sheetsList.add(map);
            sysFileExportRecordService.exportMilSheet(sheetsList,  userId,  module,  siteCode);
        }
        return record;
    }
    
    /**
     * @param model
     * @param userId
     * @param module
     * @param mbrAccountExcelTempPath 
     * @return
     */
    public SysFileExportRecord exportListMbr(FundStatementModel model, Long userId, String module, String mbrAccountExcelTempPath) {
    	SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);
    	if (null != record) {
    		String siteCode = CommonUtil.getSiteCode();
    		List<FundStatementModel> resultList = fundStatementMapper.fundMbrList(model);
    		 List<Map<String, Object>> list = resultList.stream().map(e -> {
                 Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
                 return entityMap;
             }).collect(Collectors.toList());
           sysFileExportRecordService.exportExcel(mbrAccountExcelTempPath, list, userId, module, siteCode);
    	}
    	return record;
    }

    public SysFileExportRecord exportAgentSubMbrList(FundStatementModel model, Long userId, String module) {
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);
        if (null != record) {
            String siteCode = CommonUtil.getSiteCode();
            List<Map<String, Object>> sheetsList = new ArrayList<>();
            List<FundStatementModel> resultList = fundStatementMapper.agentSubMbrList(model);
            ExportParams exportParams = new ExportParams();

            List<ExportAgentSubMbrDto> data = new ArrayList<>();
            for (FundStatementModel fundStatementModel : resultList) {
                ExportAgentSubMbrDto exportAgentSubMbrDto = new ExportAgentSubMbrDto();
                BeanUtils.copyProperties(fundStatementModel, exportAgentSubMbrDto);
                data.add(exportAgentSubMbrDto);
            }
            Map<String, Object> map = new HashMap<>();
            map.put("data", data);
            map.put("entity", ExportAgentSubMbrDto.class);
            map.put("title", exportParams);
            sheetsList.add(map);
            sysFileExportRecordService.exportMilSheet(sheetsList,  userId,  module,  siteCode);
        }
        return record;
    }


    public SysFileExportRecord exportAgentList(FundStatementModel fundStatementModel, Long userId, String module) {
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);
        if (null != record) {
            String siteCode = CommonUtil.getSiteCode();
            List<Map<String, Object>> sheetsList = new ArrayList<>();
            List<FundStatementDto> resultList = fundStatementMapper.agentList(fundStatementModel);
           // FundStatementDto fundStatementDtoSum = fundStatementMapper.agentListSum(fundStatementModel);
            //resultList.add(fundStatementDtoSum);
            ExportParams exportParams = new ExportParams();

            List<ExportAgentListDto> data = new ArrayList<>();
            for (FundStatementDto fundStatementDto : resultList) {
                ExportAgentListDto exportAgentListDto = new ExportAgentListDto();
                BeanUtils.copyProperties(fundStatementDto, exportAgentListDto);
                data.add(exportAgentListDto);
            }
            Map<String, Object> map = new HashMap<>();
            map.put("data", data);
            map.put("entity", ExportAgentListDto.class);
            map.put("title", exportParams);
            sheetsList.add(map);
            sysFileExportRecordService.exportMilSheet(sheetsList,  userId,  module,  siteCode);
        }
        return record;
    }

    public SysFileExportRecord exportTagencyFundList(FundStatementModel model, Long userId, String module) {
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);
        if (null != record) {
            String siteCode = CommonUtil.getSiteCode();
            List<Map<String, Object>> sheetsList = new ArrayList<>();
            List<FundAgentStatementDto> resultList = fundStatementMapper.tagencyFundList(model);
            ExportParams exportParams = new ExportParams();
            List<ExportFundAgentDto> data = new ArrayList<>();
            for (FundAgentStatementDto fundStatementModel : resultList) {
                ExportFundAgentDto exportListDto = new ExportFundAgentDto();
                BeanUtils.copyProperties(fundStatementModel, exportListDto);
                data.add(exportListDto);
            }

            Map<String, Object> map = new HashMap<>();
            map.put("data", data);
            map.put("entity", ExportFundAgentDto.class);
            map.put("title", exportParams);
            sheetsList.add(map);
            sysFileExportRecordService.exportMilSheet(sheetsList,  userId,  module,  siteCode);
        }
        return record;
    }

}
