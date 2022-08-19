package com.wsdy.saasops.modules.agent.service;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static java.util.Objects.nonNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.agapi.modules.dto.ReportParamsDto;
import com.wsdy.saasops.agapi.modules.service.AgentFinaceReportService;
import com.wsdy.saasops.agapi.modules.service.AgentNewService;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.utils.StringUtil;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.modules.agent.dao.AgentAccountMapper;
import com.wsdy.saasops.modules.agent.dao.AgyCommissionProfitMapper;
import com.wsdy.saasops.modules.agent.dto.AgentComReportDto;
import com.wsdy.saasops.modules.agent.dto.CostTotalDto;
import com.wsdy.saasops.modules.agent.dto.DepotCostDto;
import com.wsdy.saasops.modules.agent.dto.ParentAgentDto;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.entity.AgyCommissionProfit;
import com.wsdy.saasops.modules.agent.mapper.AgentComReportExtendMapper;
import com.wsdy.saasops.modules.agent.mapper.AgentComReportMapper;
import com.wsdy.saasops.modules.agent.mapper.AgentMapper;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import com.wsdy.saasops.modules.sys.service.SysFileExportRecordService;

import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AgentComReportService {

    @Autowired
    private AgentComReportMapper agentComReportMapper;
    @Autowired
    private AgentComReportExtendMapper agentComReportExtendMapper;
    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private AgentAccountMapper agentAccountMapper;
    @Autowired
    private AgentMapper agentMapper;
    @Autowired
    private AgentNewService agentNewService;
    @Autowired
    private AgyCommissionProfitMapper commissionProfitMapper;
    @Autowired
    private AgentFinaceReportService finaceReportService;

    // 导出module和excel文件path
    @Value("${agent.report.comviewexport.totalListByDayExport.path}")
    private String totalListByDayExportPath;
    @Value("${agent.report.comviewexport.tagencyListExport.path}")
    private String tagencyListExportPath;
    @Value("${agent.report.comviewexport.categoryListExport.path}")
    private String categoryListExportPath;
    @Value("${agent.report.comviewexport.memberListExport.path}")
    private String memberListExportPath;
    @Value("${agent.report.comviewexport.cagencyMemberTotalListExport.path}")
    private String cagencyMemberTotalListExportPath;
    @Value("${agent.report.comviewexport.agentLineListExportPath.path}")
    private String agentLineListExportPath;
    @Value("${agent.report.comviewexport.tagencyListByDayExport.path}")
    private String tagencyListByDayExportPath;
    private final String totalListByDayModule = "totalListByDay";
    private final String tagencyListModule = "tagencyList";
    private final String categoryListModule = "categoryList";
    private final String memberListModule = "memberList";
    private final String cagencyMemberTotalListModule = "cagencyMemberTotalList";
    private final String agentLineListModel = "agentLineListModel";
    private final String tagencyListByDayModule = "tagencyListByDay";

    /***
     *  总览详情
     */
    public AgentComReportDto totalInfo(AgentComReportDto model, Long userId) {
        AgentComReportDto result = agentComReportMapper.totalInfoFromReport(model); // 新的查询report方法
        
        ReportParamsDto dto = new ReportParamsDto();
        dto.setStartTime(model.getStartTime());
        dto.setEndTime(model.getEndTime());
        dto.setIsCagency(1);
        dto.setDepartmentid(model.getDepartmentid());
        // 查询时间范围的平台费，服务费
        CostTotalDto costTotalDto = finaceReportService.depotCostTotal(dto);
		result.setCost(costTotalDto.getCost());
		result.setServiceCost(costTotalDto.getServiceCost());
        return result;
    }

    /***
     * 按天查询
     */
    public PageUtils totalListByDay(AgentComReportDto model, Long userId) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());

        List<AgentComReportDto> resultList = agentComReportMapper.totalListByDayFromReport(model); // 新的查询report方法
        PageUtils p = BeanUtil.toPagedResult(resultList);
        return p;
    }

    /**
     * (股东/总代/全选)视图/下级代理列表
     */
    public PageUtils tagencyList(AgentComReportDto model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        Long logNow = System.currentTimeMillis();
        model.setGroubyAgent(true);
        List<AgentComReportDto> resultList = agentComReportMapper.tagencyListFromReport(model); // 新的查询report方法
        
        // 总代 或者 单纯只查询代理等级 不需要查询平台费，服务费
        if (model.getIsCagency() != 0) {
	        ReportParamsDto dto = new ReportParamsDto();
	        dto.setStartTime(model.getStartTime());
	        dto.setEndTime(model.getEndTime());
	        dto.setIsCagency(1);
	        dto.setDepartmentid(model.getDepartmentid());
	        dto.setAgentLevel(model.getAgentLevel());
	        // 获取每个代理最下级的平台费，服务费
			for (AgentComReportDto agentComReportDto : resultList) {
				dto.setAgyId(agentComReportDto.getAgyId());
				CostTotalDto costTotalDto = finaceReportService.depotCostTotal(dto);
				agentComReportDto.setCost(costTotalDto.getCost());
				agentComReportDto.setServiceCost(costTotalDto.getServiceCost());
				BigDecimal totalProfit = agentComReportDto.getTotalProfit();
				agentComReportDto.setTotalProfit(totalProfit.subtract(costTotalDto.getCost()).subtract(costTotalDto.getServiceCost()));
			}
        }
        if (resultList.size() != 0) {
            //获取小计
            resultList.add(getSubtotal(resultList, Constants.EVNumber.one));
            log.info("tagencyList==参数" + JSON.toJSONString(model) + "==获取小计结束，目前所用时间："+ (System.currentTimeMillis() - logNow) +"毫秒");
            model.setIsNotIncludeSelf(true);
            //获取总计
            resultList.add(getTotalOnlyOne(model, Constants.EVNumber.one, false));
            log.info("tagencyList==参数" + JSON.toJSONString(model) + "==获取总计结束，目前所用时间："+ (System.currentTimeMillis() - logNow) +"毫秒");
        }
        PageUtils p = BeanUtil.toPagedResult(resultList);
        return p;
    }

    /**
     * 总代下部门(类别)视图
     */
    public PageUtils categoryList(AgentComReportDto model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        Long logNow = System.currentTimeMillis();
        log.info("categoryList==参数" + JSON.toJSONString(model) + "==开始" );
        List<AgentComReportDto> resultList = agentComReportMapper.categoryListFromReport(model); // 新的查询report方法
        log.info("categoryList==参数" + JSON.toJSONString(model) + "==查询数据结束，目前所用时间："+ (System.currentTimeMillis() - logNow) +"毫秒");
        if (resultList.size() != 0) {
            //获取小计
            resultList.add(getSubtotal(resultList, Constants.EVNumber.three));
            log.info("categoryList==参数" + JSON.toJSONString(model) + "==获取小计结束，目前所用时间："+ (System.currentTimeMillis() - logNow) +"毫秒");
            //获取总计
            resultList.add(getTotalOnlyOne(model, Constants.EVNumber.three, false));
            log.info("categoryList==参数" + JSON.toJSONString(model) + "==获取总计结束，目前所用时间："+ (System.currentTimeMillis() - logNow) +"毫秒");
        }
        PageUtils p = BeanUtil.toPagedResult(resultList);
        return p;
    }

    /**
     * 综合报表--子代代理视图汇总表头
     * 
     */
    public List<AgentComReportDto> subAgentTotalViewList(AgentComReportDto model) {
        List<AgentComReportDto> resultList = new ArrayList<>();
        // 下级代理
        model.setIsNotIncludeSelf(true);
        resultList.add(getTotalOnlyOne(model, Constants.EVNumber.one, true));
        // 直属会员
        resultList.add(getTotalOnlyOne(model, Constants.EVNumber.two, true));
        return resultList;
    }

    /**
     * 代理总览
     */
    public PageUtils agentMemberTotalView(AgentComReportDto model) {
        List<AgentComReportDto> resultList = new ArrayList<>();
        Long logNow = System.currentTimeMillis();
        log.info("agentMemberTotalView==参数" + JSON.toJSONString(model) + "==开始" );
        // 查询直属会员总计并根据代理分组
        resultList = getMemberTotalOnlyOne(model, Constants.EVNumber.two, true);
        log.info("agentMemberTotalView==参数" + JSON.toJSONString(model) + "==代理总览，目前所用时间："+ (System.currentTimeMillis() - logNow) +"毫秒");
        PageUtils p = BeanUtil.toPagedResult(resultList);
        return p;
    }


    /**
     * 综合报表--下级会员列表
     */
    public PageUtils memberList(AgentComReportDto model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        Long logNow = System.currentTimeMillis();
        log.info("memberList==参数" + JSON.toJSONString(model) + "==开始" );
        // 净盈利减去平台费，服务费
        List<AgentComReportDto> resultList = agentComReportMapper.memberListFromReport(model); // 新的查询report方法
        
        log.info("memberList==参数" + JSON.toJSONString(model) + "==查询数据结束，目前所用时间："+ (System.currentTimeMillis() - logNow) +"毫秒");
        if (resultList.size() != 0) {
            //获取小计
            resultList.add(getSubtotal(resultList, Constants.EVNumber.two));
            log.info("memberList==参数" + JSON.toJSONString(model) + "==获取小计结束，目前所用时间："+ (System.currentTimeMillis() - logNow) +"毫秒");
            //获取总计
            resultList.add(getTotalOnlyOne(model, Constants.EVNumber.two, false));
            log.info("memberList==参数" + JSON.toJSONString(model) + "==获取总计结束，目前所用时间："+ (System.currentTimeMillis() - logNow) +"毫秒");
        }
        return BeanUtil.toPagedResult(resultList);
    }

    /**
     * 代理线导出统计代理总数
     *
     * @param reportModelDto
     * @param userId
     * @return
     */
    public Integer agentLineReportExportCount(AgentComReportDto reportModelDto, Long userId) {
        // 先判断用户是否股东 总代 一级代理，只有这几个代理有导出代理线
        List<AgentAccount> checkAgyList = agentMapper.findAgyAccountAndGrade(new AgentAccount(){{setAgyAccount(reportModelDto.getAgyAccount());}});
        if (checkAgyList == null || checkAgyList.size() <= 0) {
            return Constants.EVNumber.zero;
        }
        AgentAccount checkAgy = checkAgyList.get(0);
        if (checkAgy.getGrade() > Constants.EVNumber.two) {
            return Constants.EVNumber.zero;
        }
        List<Integer> searchGrade = getAgentLineExportSearchGrade(reportModelDto, checkAgy);
        if (searchGrade == null || searchGrade.size() <= 0) {
            return Constants.EVNumber.zero;
        }
        return agentComReportExtendMapper.countAgentLine(reportModelDto.getAgyAccount(), searchGrade).size();
    }

    public List<Integer> getAgentLineExportSearchGrade(AgentComReportDto reportModelDto, AgentAccount checkAgy) {
        Integer grade = checkAgy.getGrade();
        List<Integer> searchGrade = new ArrayList<>();
        if (grade == Constants.EVNumber.zero) { // 股东
            searchGrade = reportModelDto.getAgentLineExportTypes();
        } else if (grade == Constants.EVNumber.one) { // 总代
            for (Integer type : reportModelDto.getAgentLineExportTypes()) {
                if (type > Constants.EVNumber.one) {
                    searchGrade.add(type-1);
                }
            }
        } else if (grade == Constants.EVNumber.two) { // 一级代理
            for (Integer type : reportModelDto.getAgentLineExportTypes()) {
                if (type > Constants.EVNumber.two) {
                    searchGrade.add(type-2);
                }
            }
        }
        return searchGrade;
    }

    /**
     * 代理线导出
     */
    private void agentLineListExport(AgentComReportDto reportModelDto, Long userId, String siteCode) {
        reportModelDto.setIsCagency(99); // 99为导出代理线
        // 判断是否有下级代理可供导出
        List<AgentAccount> checkAgyList = agentMapper.findAgyAccountAndGrade(new AgentAccount(){{setAgyAccount(reportModelDto.getAgyAccount());}});
        if (checkAgyList == null || checkAgyList.size() <= 0) {
            throw new R200Exception("您输入的代理无下级可导出!");
        }
        AgentAccount checkAgy = checkAgyList.get(0);
        if (checkAgy.getGrade() > Constants.EVNumber.two) {
            throw new R200Exception("您输入的代理无下级可导出!");
        }

        // 需要导出的下级代理数组 1总代 2一级代理 3二级代理
        List<AgentComReportDto> resultList = new ArrayList<>();
        List<Integer> searchGrade = getAgentLineExportSearchGrade(reportModelDto, checkAgy);
        if (searchGrade == null || searchGrade.size() <= 0) {
            throw new R200Exception("您输入的代理无下级可导出!");
        }
        for (Integer grade : searchGrade) { // 循环查询可导出的下级代理
            reportModelDto.setAgentLevel(grade);
            // 查询导出数据list
            reportModelDto.setGroubyAgent(true);
            List<AgentComReportDto> tempList = agentComReportMapper.tagencyListFromReport(reportModelDto); // 新的查询report方法
            resultList.addAll(tempList);
        }

        // 总代不需要查询平台费，服务费
        if (reportModelDto.getIsCagency() != 0) {
            ReportParamsDto reportParamsDto = new ReportParamsDto();
            reportParamsDto.setStartTime(reportModelDto.getStartTime());
            reportParamsDto.setEndTime(reportModelDto.getEndTime());
            reportParamsDto.setIsCagency(1);
            // 循环查找列表代理的直属会员平台费与服务费
            for (AgentComReportDto agentdto : resultList) {
                reportParamsDto.setAgyId(agentdto.getAgyId());
                CostTotalDto costTotalDto = finaceReportService.depotCostTotal(reportParamsDto);
                BigDecimal cost = costTotalDto.getCost();
                BigDecimal serviceCost = costTotalDto.getServiceCost();
                BigDecimal totalProfit = agentdto.getTotalProfit();
                agentdto.setCost(cost);
                agentdto.setServiceCost(serviceCost);
                agentdto.setTotalProfit(totalProfit.subtract(cost).subtract(serviceCost));
            }
        }

        if (resultList.size() != 0) {
            //获取小计
            resultList.add(getSubtotal(resultList, Constants.EVNumber.one));
            //获取总计
            //resultList.add(getTotalOnlyOne(reportModelDto, Constants.EVNumber.one, false));
            // 获取用户的直线上级
            List<String> agtAccount = resultList.stream().map(e -> e.getAgyAccount()).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(agtAccount)) {
                List<ParentAgentDto> paList = agentMapper.findParentAgent(agtAccount);
                resultList.forEach(r -> {
                    Integer agyId = r.getAgyId();
                    if (agyId == null) {
                        return;
                    }
                    Optional<ParentAgentDto> any = paList.stream().filter(p -> agyId.equals(p.getAgyId())).findAny();
                    if (any.isPresent()) {
                        String parentAccount = any.get().getParentAccount();
                        r.setParentAgyAccount(parentAccount);
                    }
                });
            }
        }
        // list处理为map
        List<Map<String, Object>> mapList = resultList.stream().map(e -> {
            dealAndTransData(e);
            Map<String, Object> entityMap = jsonUtil.Entity2Map(e);

            return entityMap;
        }).collect(Collectors.toList());

        // excel第一行数据
        Map<String, Object> map = new HashMap<>(8);

        if (Objects.nonNull(reportModelDto.getIsCagency())) {
            AgentAccount parent = agentAccountMapper.selectByPrimaryKey(reportModelDto.getAgyId());
            if (Objects.nonNull(parent)) {
                map.put("parent", parent.getAgyAccount());
            }

        }
        map.put("startTime", reportModelDto.getStartTime());
        map.put("endTime", reportModelDto.getEndTime());
        map.put("downloadTime", DateUtil.getCurrentDate(FORMAT_18_DATE_TIME));
        map.put("mapList", mapList);

        // 异步生成并上传excel文件
        sysFileExportRecordService.exportExcel(agentLineListExportPath, map, userId, reportModelDto.getModule(), siteCode);
    }

    /**
     * 日期渠道表单导出
     */
    private void tagencyListByDayExport(AgentComReportDto reportModelDto, Long userId, String siteCode) {
        // 查询导出数据list
        reportModelDto.setGroubyAgent(true);
        // 根据日期循环查询，不在SQL中使用日期分组
        List<String> dateList = DateUtil.getListDateBetween(reportModelDto.getStartTime(), reportModelDto.getEndTime());
        log.info("日期渠道导出时间{}", dateList);
        List<AgentComReportDto> resultList = new ArrayList<>();
        // 复制一个查询对象
        String str = JSON.toJSONString(reportModelDto);
        AgentComReportDto searchDto = JSON.parseObject(str, AgentComReportDto.class);
        for (String next : dateList) {
            searchDto.setStartTime(next.concat(" 00:00:00"));
            searchDto.setEndTime(next.concat(" 23:59:59"));

            List<AgentComReportDto> tmpList = agentComReportMapper.tagencyListFromReport(searchDto); // 新的查询report方法
            tmpList.stream().forEach(e->e.setCreateTime(next));
            resultList.addAll(tmpList);
            tmpList = null;
        }


        // 总代 或者 单纯只查询代理等级 不需要查询平台费，服务费
        if (reportModelDto.getIsCagency() != 0) {
            ReportParamsDto reportParamsDto = new ReportParamsDto();
            reportParamsDto.setStartTime(reportModelDto.getStartTime());
            reportParamsDto.setEndTime(reportModelDto.getEndTime());
            reportParamsDto.setIsCagency(1);
            // 循环查找列表代理的直属会员平台费与服务费
            for (AgentComReportDto agentdto : resultList) {
                reportParamsDto.setAgyId(agentdto.getAgyId());
                CostTotalDto costTotalDto = finaceReportService.depotCostTotal(reportParamsDto);
                BigDecimal cost = costTotalDto.getCost();
                BigDecimal serviceCost = costTotalDto.getServiceCost();
                BigDecimal totalProfit = agentdto.getTotalProfit();
                agentdto.setCost(cost);
                agentdto.setServiceCost(serviceCost);
                agentdto.setTotalProfit(totalProfit.subtract(cost).subtract(serviceCost));
            }
        }

        if (resultList.size() != 0) {
            //获取小计
            resultList.add(getSubtotal(resultList, Constants.EVNumber.one));
        }
        // list处理为map
        List<Map<String, Object>> mapList = resultList.stream().map(e -> {
            dealAndTransData(e);
            Map<String, Object> entityMap = jsonUtil.Entity2Map(e);

            return entityMap;
        }).collect(Collectors.toList());

        // excel第一行数据
        Map<String, Object> map = new HashMap<>(8);
        // 特殊处理parent
        if (Objects.nonNull(reportModelDto.getIsCagency()) && reportModelDto.getIsCagency().equals(Constants.EVNumber.zero)) {    // 股东
            map.put("parent", "股东");
        }
        if (Objects.nonNull(reportModelDto.getIsCagency()) && reportModelDto.getIsCagency().equals(Constants.EVNumber.one)) {     // 非股东
            AgentAccount parent = agentAccountMapper.selectByPrimaryKey(reportModelDto.getAgyId());
            if (Objects.nonNull(parent)) {
                map.put("parent", parent.getAgyAccount());
            }
        }
        map.put("startTime", reportModelDto.getStartTime());
        map.put("endTime", reportModelDto.getEndTime());
        map.put("downloadTime", DateUtil.getCurrentDate(FORMAT_18_DATE_TIME));
        map.put("mapList", mapList);

        // 异步生成并上传excel文件
        sysFileExportRecordService.exportExcel(tagencyListByDayExportPath, map, userId, reportModelDto.getModule(), siteCode);
    }

    /**
     * 报表导出
     *
     * @param reportModelDto
     * @param userId
     * @return
     */
    public SysFileExportRecord comReportExport(AgentComReportDto reportModelDto, Long userId) {
        // 生成文件导出记录
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, reportModelDto.getModule());

        // 不存在导出文件，则生成并上传导出文件
        if (null != record) {
            // 异步查询数据
            String siteCode = CommonUtil.getSiteCode();
            CompletableFuture.runAsync(() -> {
                ThreadLocalCache.setSiteCodeAsny(siteCode);
                log.info("exportExcel==异步==start=={}=={}", reportModelDto.getModule(), siteCode);
                if (totalListByDayModule.equals(reportModelDto.getModule())) {  // 按天汇总视图
                    totalListByDayExport(reportModelDto, userId, siteCode);
                }
                if (tagencyListModule.equals(reportModelDto.getModule())) {    // 股东、总代列表视图/下级代理视图
                    log.info("exportExcel==异步==进入判断=={}=={}", tagencyListModule, siteCode);
                    tagencyListExport(reportModelDto, userId, siteCode);
                }
                if (categoryListModule.equals(reportModelDto.getModule())) {   // 股东下部门(类别)视图
                    categoryListExport(reportModelDto, userId, siteCode);
                }
                if (memberListModule.equals(reportModelDto.getModule())) {     // 下级会员列表
                    memberListExport(reportModelDto, userId, siteCode);
                }
                if (cagencyMemberTotalListModule.equals(reportModelDto.getModule())) {     // 下级会员列表
                    cagencyMemberTotalListExport(reportModelDto, userId, siteCode);
                }
                if (agentLineListModel.equals(reportModelDto.getModule())) { // 代理线导出
                    agentLineListExport(reportModelDto, userId, siteCode);
                }
                if (tagencyListByDayModule.equals(reportModelDto.getModule())) { // 日期渠道表单
                    tagencyListByDayExport(reportModelDto, userId, siteCode);
                }
            });
            /*CompletableFuture.runAsync(() -> {

            });*/
        }
        return record;
    }

    /**
     * 统一查询文件是否可下载
     */
    public R checkFile(String module, Long userId) {
        // 查询用户的module下载记录
        SysFileExportRecord record = sysFileExportRecordService.getAsynFileExportRecordByUserId(userId, module);
        if (null != record) {
            String fileName = "";
            if (totalListByDayModule.equals(module)) {  // 按天汇总视图
                fileName = totalListByDayExportPath.substring(totalListByDayExportPath.lastIndexOf("/") + 1, totalListByDayExportPath.length());
            }
            if (tagencyListModule.equals(module)) {    // 股东列表视图/下级代理视图
                fileName = tagencyListExportPath.substring(tagencyListExportPath.lastIndexOf("/") + 1, tagencyListExportPath.length());
            }
            if (categoryListModule.equals(module)) {   // 股东下部门(类别)视图
                fileName = categoryListExportPath.substring(categoryListExportPath.lastIndexOf("/") + 1, categoryListExportPath.length());
            }
            if (memberListModule.equals(module)) {     // 下级会员列表
                fileName = memberListExportPath.substring(memberListExportPath.lastIndexOf("/") + 1, memberListExportPath.length());
            }
            if (cagencyMemberTotalListModule.equals(module)) {     // 代理总览
                fileName = cagencyMemberTotalListExportPath.substring(cagencyMemberTotalListExportPath.lastIndexOf("/") + 1, cagencyMemberTotalListExportPath.length());
            }
            if (agentLineListModel.equals(module)) {     // 代理线导出
                fileName = agentLineListExportPath.substring(agentLineListExportPath.lastIndexOf("/") + 1, agentLineListExportPath.length());
            }
            if (tagencyListByDayModule.equals(module)) {     // 日期渠道表单
                fileName = tagencyListByDayExportPath.substring(tagencyListByDayExportPath.lastIndexOf("/") + 1, tagencyListByDayExportPath.length());
            }

            record.setDownloadFileName(fileName);
            return R.ok().put(record);
        }
        return R.ok(false);
    }

    /**
     * 修改代理部门--测试用
     *
     * @param reportModelDto
     */
    public void modifyAgentCateGory(AgentComReportDto reportModelDto) {
        AgentAccount agent = new AgentAccount();
        agent.setAgyAccount(reportModelDto.getAgyAccount());
        agent.setDepartmentid(reportModelDto.getDepartmentid());
        agentMapper.modifyAgentCateGory(agent);
    }

    /**
     * 返回小计
     *
     * @param list
     * @param flag 1  查询代理  2 查询会员 3 查询代理类别
     * @return
     */
    private AgentComReportDto getSubtotal(List<AgentComReportDto> list, int flag) {
        AgentComReportDto subTotal = new AgentComReportDto();
        BigDecimal totalProfit = BigDecimal.ZERO;               // 净盈利
        BigDecimal totalDepositBalance = BigDecimal.ZERO;       // 存款金额
        BigDecimal totalActualarrival = BigDecimal.ZERO;       // 存款实际到账金额
        BigDecimal totalDepositBalanceNum = BigDecimal.ZERO;    // 存款人数
        BigDecimal totalDrawAmount = BigDecimal.ZERO;           // 取款金额
        BigDecimal totalDrawAmountNum = BigDecimal.ZERO;        // 取款人数
        BigDecimal totalBonusAmount = BigDecimal.ZERO;          // 优惠金额
        BigDecimal totalBonusAmountNum = BigDecimal.ZERO;       // 优惠人数
        BigDecimal totalPayout = BigDecimal.ZERO;               // 派彩金额
        BigDecimal totalActiveMbrs = BigDecimal.ZERO;           // 活跃人数
        BigDecimal totalNewMbrs = BigDecimal.ZERO;              // 新增会员
        BigDecimal totalNewDeposits = BigDecimal.ZERO;          // 首存人数
        BigDecimal totalNewDepositAmount = BigDecimal.ZERO;    // 首存金额
        BigDecimal totalValidBets = BigDecimal.ZERO;            // 有效投注
        BigDecimal totalSubAgentNum = BigDecimal.ZERO;          // 下级代理个数
        BigDecimal totalSubMbrNum = BigDecimal.ZERO;            // 下级会员个数
        BigDecimal totalCost = BigDecimal.ZERO;                 // 小计平台费
        BigDecimal totalServiceCost = BigDecimal.ZERO;          // 小计服务费
        Integer betCount = 0;          							// 注单数量
        BigDecimal totalBetMbrs = BigDecimal.ZERO;          	// 投注人数
        BigDecimal calculateProfit = BigDecimal.ZERO;          	// 资金调整

        for (AgentComReportDto model : list) {
            if (model.getTotalProfit() != null) {   // 净盈利
                totalProfit = totalProfit.add(model.getTotalProfit());
            }
            if (model.getTotalDepositBalance() != null) {   // 存款金额
                totalDepositBalance = totalDepositBalance.add(model.getTotalDepositBalance());
            }
            if (model.getTotalActualarrival() != null) {   // 存款到账金额
                totalActualarrival = totalActualarrival.add(model.getTotalActualarrival());
            }
            if (model.getTotalDepositBalanceNum() != null) {    // 存款人数
                totalDepositBalanceNum = totalDepositBalanceNum.add(model.getTotalDepositBalanceNum());
            }
            if (model.getTotalDrawAmount() != null) {   // 取款金额
                totalDrawAmount = totalDrawAmount.add(model.getTotalDrawAmount());
            }
            if (model.getTotalDrawAmountNum() != null) {   // 取款人数
                totalDrawAmountNum = totalDrawAmountNum.add(model.getTotalDrawAmountNum());
            }
            if (model.getTotalBonusAmount() != null) {  // 优惠金额
                totalBonusAmount = totalBonusAmount.add(model.getTotalBonusAmount());
            }
            if (model.getTotalBonusAmountNum() != null) {  // 优惠人数
                totalBonusAmountNum = totalBonusAmountNum.add(model.getTotalBonusAmountNum());
            }
            if (model.getTotalPayout() != null) {    // 派彩金额
                totalPayout = totalPayout.add(model.getTotalPayout());
            }
            if (model.getTotalActiveMbrs() != null) {   // 活跃人数
                totalActiveMbrs = totalActiveMbrs.add(model.getTotalActiveMbrs());
            }
            if (model.getTotalNewMbrs() != null) { // 新增会员
                totalNewMbrs = totalNewMbrs.add(model.getTotalNewMbrs());
            }
            if (model.getTotalNewDeposits() != null) { // 首存人数
                totalNewDeposits = totalNewDeposits.add(model.getTotalNewDeposits());
            }
            if (model.getTotalNewDepositAmount() != null) { // 首存金额
                totalNewDepositAmount = totalNewDepositAmount.add(model.getTotalNewDepositAmount());
            }
            if (model.getTotalValidBets() != null) {   // 有效投注
                totalValidBets = totalValidBets.add(model.getTotalValidBets());
            }
            if (model.getTotalSubAgentNum() != null) {   // 下级代理个数
                totalSubAgentNum = totalSubAgentNum.add(model.getTotalSubAgentNum());
            }
            if (model.getTotalSubMbrNum() != null) {   // 下级会员个数
                totalSubMbrNum = totalSubMbrNum.add(model.getTotalSubMbrNum());
            }
            if (model.getCost() != null) {   // 小计平台费
                totalCost = totalCost.add(model.getCost());
            }
            if (model.getServiceCost() != null) {   // 小计服务费
                totalServiceCost = totalServiceCost.add(model.getServiceCost());
            }
            if (model.getBetCount() != null) {   // 注单数量
            	betCount = betCount + model.getBetCount();
            }
            if (model.getTotalBetMbrs() != null) {   // 投注人数
            	totalBetMbrs = totalBetMbrs.add(model.getTotalBetMbrs());
            }
            if (model.getCalculateProfit() != null) {   // 资金调整
            	calculateProfit = calculateProfit.add(model.getCalculateProfit());
            }
        }


        if (Constants.EVNumber.one == flag) {
            subTotal.setAgyAccount("小计");
        } else if (Constants.EVNumber.two == flag) {
            subTotal.setLoginName("小计");
        } else if (Constants.EVNumber.three == flag) {
            subTotal.setCateGory("小计");
        }

        subTotal.setTotalProfit(totalProfit);
        subTotal.setTotalDepositBalance(totalDepositBalance);
        subTotal.setTotalActualarrival(totalActualarrival);
        subTotal.setTotalDepositBalanceNum(totalDepositBalanceNum);
        subTotal.setTotalDrawAmount(totalDrawAmount);
        subTotal.setTotalDrawAmountNum(totalDrawAmountNum);
        subTotal.setTotalBonusAmount(totalBonusAmount);
        subTotal.setTotalBonusAmountNum(totalBonusAmountNum);
        subTotal.setTotalPayout(totalPayout);
        subTotal.setTotalActiveMbrs(totalActiveMbrs);
        subTotal.setTotalNewMbrs(totalNewMbrs);
        subTotal.setTotalNewDeposits(totalNewDeposits);
        subTotal.setTotalNewDepositAmount(totalNewDepositAmount);
        subTotal.setTotalValidBets(totalValidBets);
        subTotal.setTotalSubAgentNum(totalSubAgentNum);
        subTotal.setTotalSubMbrNum(totalSubMbrNum);
        subTotal.setSumDepositAndWithdrawal(totalDrawAmount.add(totalDepositBalance));
        subTotal.setCost(totalCost);
        subTotal.setServiceCost(totalServiceCost);
        subTotal.setBetCount(betCount);
        subTotal.setTotalBetMbrs(totalBetMbrs);
        subTotal.setCalculateProfit(calculateProfit);
        //总计小计不显示统计数据
        subTotal.setCtRatio(null);
        subTotal.setLsRatio(null);
        subTotal.setSyRatio(null);
        subTotal.setYhRatio(null);
        subTotal.setCtDiffer(null);
        return subTotal;
    }

    public BigDecimal getNetwinlose(Integer agentId) {
        AgyCommissionProfit agyCommissionProfit = new AgyCommissionProfit();
        agyCommissionProfit.setAgentId(agentId);

        AgyCommissionProfit commissionProfit = commissionProfitMapper.selectOne(agyCommissionProfit);
        if (nonNull(commissionProfit)) {
            return commissionProfit.getNetwinlose();
        }
        return BigDecimal.ZERO;
    }


    /**
     * 返回总计
     *
     * @param dto
     * @param flag 1  查询代理  2 查询会员 3 查询代理类别
     * @param view true 代理表头视图汇总 false 普通汇总
     * @return
     */
    private AgentComReportDto getTotal(AgentComReportDto dto, int flag, Boolean view) {
        AgentComReportDto allTotal = new AgentComReportDto();
        List<AgentComReportDto> secondList;
        if (Constants.EVNumber.one == flag) {
            dto.setGroubyAgent(true);
            //secondList = agentComReportMapper.tagencyList(dto);
            secondList = agentComReportMapper.tagencyListFromReport(dto); // 新的查询report方法
//            for (AgentComReportDto dto2 : secondList){
//                if (dto2.getTotalProfit().compareTo(BigDecimal.ZERO) == -1) {
//                    dto2.setTotalProfit((dto2.getTotalProfit().negate().add(dto2.getCost())).negate());
//                } else {
//                    dto2.setTotalProfit(dto2.getTotalProfit().subtract(dto2.getCost()));
//                }
//            }
        } else if (Constants.EVNumber.two == flag) {
            //secondList = agentComReportMapper.memberList(dto);
            secondList = agentComReportMapper.memberListFromReport(dto); // 新的查询report方法
        } else if (Constants.EVNumber.three == flag) {
            //secondList = agentComReportMapper.categoryList(dto);
            secondList = agentComReportMapper.categoryListFromReport(dto); // 新的查询report方法
        } else {
            return allTotal;
        }
        BigDecimal totalProfit = BigDecimal.ZERO;               // 净盈利
        BigDecimal totalDepositBalance = BigDecimal.ZERO;       // 存款金额
        BigDecimal totalActualarrival = BigDecimal.ZERO;       // 存款实际到账金额
        BigDecimal totalDepositBalanceNum = BigDecimal.ZERO;    // 存款人数
        BigDecimal totalDrawAmount = BigDecimal.ZERO;           // 取款金额
        BigDecimal totalDrawAmountNum = BigDecimal.ZERO;        // 取款人数
        BigDecimal totalBonusAmount = BigDecimal.ZERO;          // 优惠金额
        BigDecimal totalBonusAmountNum = BigDecimal.ZERO;       // 优惠人数
        BigDecimal totalPayout = BigDecimal.ZERO;               // 派彩金额
        BigDecimal totalActiveMbrs = BigDecimal.ZERO;           // 活跃人数
        BigDecimal totalNewMbrs = BigDecimal.ZERO;              // 新增会员
        BigDecimal totalNewDeposits = BigDecimal.ZERO;          // 首存人数
        BigDecimal totalNewDepositAmount = BigDecimal.ZERO;     // 首存金额
        BigDecimal totalValidBets = BigDecimal.ZERO;            // 有效投注
        BigDecimal totalSubAgentNum = BigDecimal.ZERO;          // 下级代理个数
        BigDecimal totalSubMbrNum = BigDecimal.ZERO;            // 下级会员个数
        BigDecimal costAll = BigDecimal.ZERO;            		// 平台费
        BigDecimal serviceCost = BigDecimal.ZERO;            	// 服务费


        for (AgentComReportDto model : secondList) {
            if (model.getTotalProfit() != null) {   // 净盈利
                totalProfit = totalProfit.add(model.getTotalProfit());
            }
            if (model.getTotalDepositBalance() != null) {   // 存款金额
                totalDepositBalance = totalDepositBalance.add(model.getTotalDepositBalance());
            }
            if (model.getTotalActualarrival() != null) {   // 存款实际到账金额
                totalActualarrival = totalActualarrival.add(model.getTotalActualarrival());
            }
            if (model.getTotalDepositBalanceNum() != null) {    // 存款人数
                totalDepositBalanceNum = totalDepositBalanceNum.add(model.getTotalDepositBalanceNum());
            }
            if (model.getTotalDrawAmount() != null) {   // 取款金额
                totalDrawAmount = totalDrawAmount.add(model.getTotalDrawAmount());
            }
            if (model.getTotalDrawAmountNum() != null) {   // 取款人数
                totalDrawAmountNum = totalDrawAmountNum.add(model.getTotalDrawAmountNum());
            }
            if (model.getTotalBonusAmount() != null) {  // 优惠金额
                totalBonusAmount = totalBonusAmount.add(model.getTotalBonusAmount());
            }
            if (model.getTotalBonusAmountNum() != null) {  // 优惠人数
                totalBonusAmountNum = totalBonusAmountNum.add(model.getTotalBonusAmountNum());
            }
            if (model.getTotalPayout() != null) {    // 派彩金额
                totalPayout = totalPayout.add(model.getTotalPayout());
            }
            if (model.getTotalActiveMbrs() != null) {   // 活跃人数
                totalActiveMbrs = totalActiveMbrs.add(model.getTotalActiveMbrs());
            }
            if (model.getTotalNewMbrs() != null) { // 新增会员
                totalNewMbrs = totalNewMbrs.add(model.getTotalNewMbrs());
            }
            if (model.getTotalNewDeposits() != null) { // 首存人数
                totalNewDeposits = totalNewDeposits.add(model.getTotalNewDeposits());
            }
            if (model.getTotalNewDepositAmount() != null) { // 首存金额
                totalNewDepositAmount = totalNewDepositAmount.add(model.getTotalNewDepositAmount());
            }
            if (model.getTotalValidBets() != null) {   // 有效投注
                totalValidBets = totalValidBets.add(model.getTotalValidBets());
            }
            if (model.getTotalSubAgentNum() != null) {   // 下级代理个数
                totalSubAgentNum = totalSubAgentNum.add(model.getTotalSubAgentNum());
            }
            if (model.getTotalSubMbrNum() != null) {   // 下级会员个数
                totalSubMbrNum = totalSubMbrNum.add(model.getTotalSubMbrNum());
            }
            if (model.getCost() != null) {   // 平台费
            	costAll = costAll.add(model.getCost());
            }
            if (model.getServiceCost() != null) {   // 服务费
            	serviceCost = serviceCost.add(model.getServiceCost());
            }
        }

        if (view) {
            if (Constants.EVNumber.one == flag) {
                allTotal.setAgyAccount("下级代理");
            } else if (Constants.EVNumber.two == flag) {
                allTotal.setAgyAccount("直属会员");
            }
        } else {
            if (Constants.EVNumber.one == flag) {
                allTotal.setAgyAccount("总计");
            } else if (Constants.EVNumber.two == flag) {
                allTotal.setLoginName("总计");
            } else if (Constants.EVNumber.three == flag) {
                allTotal.setCateGory("总计");
            }
        }
        allTotal.setTotalProfit(totalProfit);
        allTotal.setTotalDepositBalance(totalDepositBalance);
        allTotal.setTotalActualarrival(totalActualarrival);
        allTotal.setTotalDepositBalanceNum(totalDepositBalanceNum);
        allTotal.setTotalDrawAmount(totalDrawAmount);
        allTotal.setTotalDrawAmountNum(totalDrawAmountNum);
        allTotal.setTotalBonusAmount(totalBonusAmount);
        allTotal.setTotalBonusAmountNum(totalBonusAmountNum);
        allTotal.setTotalPayout(totalPayout);
        allTotal.setTotalActiveMbrs(totalActiveMbrs);
        allTotal.setTotalNewMbrs(totalNewMbrs);
        allTotal.setTotalNewDeposits(totalNewDeposits);
        allTotal.setTotalNewDepositAmount(totalNewDepositAmount);
        allTotal.setTotalValidBets(totalValidBets);
        allTotal.setTotalSubAgentNum(totalSubAgentNum);
        allTotal.setTotalSubMbrNum(totalSubMbrNum);
        allTotal.setSumDepositAndWithdrawal(totalDepositBalance.add(totalDrawAmount));
        allTotal.setCost(costAll);
        allTotal.setServiceCost(serviceCost);
        // flag 1 查询代理  2 查询会员 3 查询代理类别
        if (Constants.EVNumber.two == flag) {   // 平台费用
            DepotCostDto costDto = agentNewService.getDepotCost(dto.getStartTime(), dto.getEndTime(), dto.getAgyId(), null);
            BigDecimal cost = costDto.getCost();
            if (nonNull(costDto.getFeeModel()) && costDto.getFeeModel() == Constants.EVNumber.two) {
                allTotal.setServiceCost(cost);
            } else {
                allTotal.setCost(cost);
            }
            if (allTotal.getTotalProfit().compareTo(BigDecimal.ZERO) == -1) {
                allTotal.setTotalProfit((allTotal.getTotalProfit().negate().add(cost)).negate());
            } else {
                allTotal.setTotalProfit(allTotal.getTotalProfit().subtract(cost));
            }
            allTotal.setNetwinlose(getNetwinlose(dto.getAgyId()));
        }
        // 总计 小计不显示统计数据
        allTotal.setCtRatio(null);
        allTotal.setLsRatio(null);
        allTotal.setSyRatio(null);
        allTotal.setYhRatio(null);
        allTotal.setCtDiffer(null);
        return allTotal;
    }

    /**
     * 返回总计
     *
     * @param dto
     * @param flag 1  查询代理 需要查询平台费，服务费  2 查询会员 3 查询代理类别
     * @param view true 代理表头视图汇总 false 普通汇总
     * @return
     */
    private AgentComReportDto getTotalOnlyOne(AgentComReportDto dto, int flag, Boolean view) {
        AgentComReportDto allTotal = new AgentComReportDto();
        List<AgentComReportDto> secondList;
        BigDecimal costDepot = BigDecimal.ZERO;
        BigDecimal serviceCostDepot = BigDecimal.ZERO;
        		
		if (Constants.EVNumber.one == flag) {
			 // 总代 或者 单纯只查询代理等级 不需要查询平台费，服务费
	        if (dto.getIsCagency() != 0) {
				ReportParamsDto reportParamsDto = new ReportParamsDto();
				reportParamsDto.setStartTime(dto.getStartTime());
				reportParamsDto.setEndTime(dto.getEndTime());
				reportParamsDto.setIsCagency(1);
				reportParamsDto.setDepartmentid(dto.getDepartmentid());
				reportParamsDto.setAgyId(dto.getAgyId());
				reportParamsDto.setIsNotIncludeSelf(dto.getIsNotIncludeSelf());
				reportParamsDto.setAgentLevel(dto.getAgentLevel());
				reportParamsDto.setDepartmentid(dto.getDepartmentid());
				CostTotalDto costTotalDto = finaceReportService.depotCostTotal(reportParamsDto);
				costDepot = costTotalDto.getCost();
				serviceCostDepot = costTotalDto.getServiceCost();
			}
			dto.setGroubyAgent(true);
			secondList = agentComReportExtendMapper.tagencyTotalFromReport(dto); // 新的查询report方法
		} else if (Constants.EVNumber.two == flag) {
			
            secondList = agentComReportExtendMapper.memberTotalFromReport(dto); // 新的查询report方法
        } else if (Constants.EVNumber.three == flag) {
            secondList = agentComReportExtendMapper.categoryTotalFromReport(dto); // 新的查询report方法
        } else {
            return allTotal;
        }
        
        if (view) {
            if (Constants.EVNumber.one == flag) {
                allTotal.setAgyAccount("下级代理");
            } else if (Constants.EVNumber.two == flag) {
                allTotal.setAgyAccount("直属会员");
            }
        } else {
            if (Constants.EVNumber.one == flag) {
                allTotal.setAgyAccount("总计");
            } else if (Constants.EVNumber.two == flag) {
                allTotal.setLoginName("总计");
            } else if (Constants.EVNumber.three == flag) {
                allTotal.setCateGory("总计");
            }
        }
        if (CollectionUtil.isEmpty(secondList)) {
        	return allTotal;
        }
        AgentComReportDto onlyOne = secondList.get(0);
        // 查询不到 直接返回空
        if (onlyOne == null) {
        	return allTotal;
        }
        allTotal.setTotalProfit(onlyOne.getTotalProfit().subtract(costDepot).subtract(serviceCostDepot));
        allTotal.setTotalDepositBalance(onlyOne.getTotalDepositBalance());
        allTotal.setTotalActualarrival(onlyOne.getTotalActualarrival());
        allTotal.setTotalDepositBalanceNum(onlyOne.getTotalDepositBalanceNum());
        allTotal.setTotalDrawAmount(onlyOne.getTotalDrawAmount());
        allTotal.setTotalDrawAmountNum(onlyOne.getTotalDrawAmountNum());
        allTotal.setTotalBonusAmount(onlyOne.getTotalBonusAmount());
        allTotal.setTotalBonusAmountNum(onlyOne.getTotalBonusAmountNum());
        allTotal.setTotalPayout(onlyOne.getTotalPayout());
        allTotal.setTotalActiveMbrs(onlyOne.getTotalActiveMbrs());
        allTotal.setTotalNewMbrs(onlyOne.getTotalNewMbrs());
        allTotal.setTotalNewDeposits(onlyOne.getTotalNewDeposits());
        allTotal.setTotalNewDepositAmount(onlyOne.getTotalNewDepositAmount());
        allTotal.setTotalValidBets(onlyOne.getTotalValidBets());
        allTotal.setTotalSubAgentNum(onlyOne.getTotalSubAgentNum());
        allTotal.setTotalSubMbrNum(onlyOne.getTotalSubMbrNum());
        allTotal.setSumDepositAndWithdrawal(onlyOne.getTotalDepositBalance().add(onlyOne.getTotalDrawAmount()));
        // 服务费，平台费
        allTotal.setCost(costDepot);
    	allTotal.setServiceCost(serviceCostDepot);
    	allTotal.setTotalBetMbrs(onlyOne.getTotalBetMbrs());
    	allTotal.setBetCount(onlyOne.getBetCount()); // 注单数量
    	allTotal.setCalculateProfit(onlyOne.getCalculateProfit()); // 资金调整
    	allTotal.setWinloseLastTime(onlyOne.getWinloseLastTime()); // 总盈亏更新时间

    	// 代理表头统计，查询下级会员总计
        if (Constants.EVNumber.two == flag && view) {   // 平台费用
            AgentAccount account = agentAccountMapper.selectByPrimaryKey(dto.getAgyId());
            ReportParamsDto reportParamsDto = new ReportParamsDto();
            reportParamsDto.setStartTime(dto.getStartTime());
            reportParamsDto.setEndTime(dto.getEndTime());
            reportParamsDto.setAgyAccount(account.getAgyAccount());
            CostTotalDto costTotalDto = finaceReportService.depotCostTotalForSingle(reportParamsDto);
            BigDecimal cost = costTotalDto.getCost();
            BigDecimal serviceCost = costTotalDto.getServiceCost();
        	allTotal.setCost(cost);
        	allTotal.setServiceCost(serviceCost);
            // 如果净盈利小于0
            if (allTotal.getTotalProfit().compareTo(BigDecimal.ZERO) == -1) {
                allTotal.setTotalProfit((allTotal.getTotalProfit().negate().add(cost).add(serviceCost)).negate());
            } else {
            	// 不小于0 减去平台费，服务费
                allTotal.setTotalProfit(allTotal.getTotalProfit().subtract(cost).subtract(serviceCost));
            }
            allTotal.setNetwinlose(getNetwinlose(dto.getAgyId()));
        }
        
        // 总计 小计不显示统计数据
        allTotal.setCtRatio(null);
        allTotal.setLsRatio(null);
        allTotal.setSyRatio(null);
        allTotal.setYhRatio(null);
        allTotal.setCtDiffer(null);
        return allTotal;
    }
    
    /**
     * 按天汇总视图导出
     */
    private void totalListByDayExport(AgentComReportDto reportModelDto, Long userId, String siteCode) {
        // 查询导出数据list
        //List<AgentComReportDto> resultList = agentComReportMapper.totalListByDay(reportModelDto);
        List<AgentComReportDto> resultList = agentComReportMapper.totalListByDayFromReport(reportModelDto); // 新的查询report方法
        if (resultList.size() > 10000) {
            throw new R200Exception("导出数量超过1W条，请更新搜索条件后再进行导出！");
        }
        for (AgentComReportDto agentComReportDto : resultList) {
        	ReportParamsDto dto = new ReportParamsDto();
        	try {
    			dto.setStartTime(DateUtil.yyyyMMddToStrStart(agentComReportDto.getCreateTime()));
    			dto.setEndTime(DateUtil.yyyyMMddToStrEnd(agentComReportDto.getCreateTime()));
			} catch (Exception e) {
				log.error("totalListByDayExport--时间格式化异常,createTime" + agentComReportDto.getCreateTime(), e);
				continue;
			}
        	dto.setEndTime(reportModelDto.getEndTime());
        	dto.setIsCagency(1);
        	dto.setDepartmentid(reportModelDto.getDepartmentid());
        	// 查询时间范围的平台费，服务费
        	CostTotalDto costTotalDto = finaceReportService.depotCostTotal(dto);
        	agentComReportDto.setCost(costTotalDto.getCost());
        	agentComReportDto.setServiceCost(costTotalDto.getServiceCost());
        }

        // list处理为map
        List<Map<String, Object>> mapList = resultList.stream().map(e -> {
            dealAndTransData(e);
            Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
            return entityMap;
        }).collect(Collectors.toList());

        // excel第一行数据
        Map<String, Object> map = new HashMap<>(8);
        map.put("startTime", reportModelDto.getStartTime());
        map.put("endTime", reportModelDto.getEndTime());
        map.put("downloadTime", DateUtil.getCurrentDate(FORMAT_18_DATE_TIME));
        map.put("mapList", mapList);

        // 异步生成并上传excel文件
        sysFileExportRecordService.exportExcel(totalListByDayExportPath, map, userId, reportModelDto.getModule(), siteCode);
    }

    /**
     * 股东列表视图/下级代理视图导出
     */
    private void tagencyListExport(AgentComReportDto reportModelDto, Long userId, String siteCode) {
        // 查询导出数据list
        log.info("exportExcel==tagencyList==getdata=start");
    	reportModelDto.setIsCagency(1);
        reportModelDto.setGroubyAgent(true);
        List<AgentComReportDto> resultList = agentComReportMapper.tagencyListFromReport(reportModelDto); // 新的查询report方法
        log.info("exportExcel==tagencyList==getdata=end");

        // 总代 或者 单纯只查询代理等级 不需要查询平台费，服务费
        if (reportModelDto.getIsCagency() != 0) {
	        ReportParamsDto reportParamsDto = new ReportParamsDto();
	        reportParamsDto.setStartTime(reportModelDto.getStartTime());
	        reportParamsDto.setEndTime(reportModelDto.getEndTime());
	        reportParamsDto.setIsCagency(1);
	        // 循环查找列表代理的直属会员平台费与服务费
	        for (AgentComReportDto agentdto : resultList) {
	        	reportParamsDto.setDepartmentid(agentdto.getDepartmentid());
	            reportParamsDto.setAgyId(agentdto.getAgyId());
	            reportParamsDto.setDepartmentid(agentdto.getDepartmentid());
	            CostTotalDto costTotalDto = finaceReportService.depotCostTotal(reportParamsDto);
	            BigDecimal cost = costTotalDto.getCost();
	            BigDecimal serviceCost = costTotalDto.getServiceCost();
	            BigDecimal totalProfit = agentdto.getTotalProfit();
	        	agentdto.setCost(cost);
	        	agentdto.setServiceCost(serviceCost);
	            agentdto.setTotalProfit(totalProfit.subtract(cost).subtract(serviceCost));
	        }
        }
        log.info("exportExcel==tagencyList==fee=end");

        if (resultList.size() != 0) {
            //获取小计
            resultList.add(getSubtotal(resultList, Constants.EVNumber.one));
            //获取总计
            resultList.add(getTotalOnlyOne(reportModelDto, Constants.EVNumber.one, false));
        }
        // list处理为map
        List<Map<String, Object>> mapList = resultList.stream().map(e -> {
            dealAndTransData(e);
            Map<String, Object> entityMap = jsonUtil.Entity2Map(e);

            return entityMap;
        }).collect(Collectors.toList());
        log.info("exportExcel==tagencyList==total=end");

        // excel第一行数据
        Map<String, Object> map = new HashMap<>(8);
        // 特殊处理parent
        if (Objects.nonNull(reportModelDto.getIsCagency()) && reportModelDto.getIsCagency().equals(Constants.EVNumber.zero)) {    // 股东
            map.put("parent", "股东");
        }
        if (Objects.nonNull(reportModelDto.getIsCagency()) && reportModelDto.getIsCagency().equals(Constants.EVNumber.one)) {     // 非股东
            AgentAccount parent = agentAccountMapper.selectByPrimaryKey(reportModelDto.getAgyId());
            if (Objects.nonNull(parent)) {
                map.put("parent", parent.getAgyAccount());
            }

        }
        map.put("startTime", reportModelDto.getStartTime());
        map.put("endTime", reportModelDto.getEndTime());
        map.put("downloadTime", DateUtil.getCurrentDate(FORMAT_18_DATE_TIME));
        map.put("mapList", mapList);

        log.info("exportExcel==tagencyList==getdata=allover");
        // 异步生成并上传excel文件
        sysFileExportRecordService.exportExcel(tagencyListExportPath, map, userId, reportModelDto.getModule(), siteCode);
    }

    /**
     * 股东下部门(类别)视图导出
     */
    private void categoryListExport(AgentComReportDto reportModelDto, Long userId, String siteCode) {
        // 查询导出数据list
        //List<AgentComReportDto> resultList = agentComReportMapper.categoryList(reportModelDto);
        List<AgentComReportDto> resultList = agentComReportMapper.categoryListFromReport(reportModelDto); // 新的查询report方法
        if (resultList.size() != 0) {
            //获取小计
            resultList.add(getSubtotal(resultList, Constants.EVNumber.three));
            //获取总计
            resultList.add(getTotal(reportModelDto, Constants.EVNumber.three, false));
        }
//        if (resultList.size() > 10000) {
//            throw new R200Exception("导出数量超过1W条，请更新搜索条件后再进行导出！");
//        }

        // list处理为map
        List<Map<String, Object>> mapList = resultList.stream().map(e -> {
            dealAndTransDataEx(e);
            Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
            return entityMap;
        }).collect(Collectors.toList());

        // excel第一行数据
        Map<String, Object> map = new HashMap<>(8);
        map.put("startTime", reportModelDto.getStartTime());
        map.put("endTime", reportModelDto.getEndTime());
        map.put("downloadTime", DateUtil.getCurrentDate(FORMAT_18_DATE_TIME));
        map.put("mapList", mapList);

        // 异步生成并上传excel文件
        sysFileExportRecordService.exportExcel(categoryListExportPath, map, userId, reportModelDto.getModule(), siteCode);
    }

    /**
     * 下级会员列表导出
     */
	private void memberListExport(AgentComReportDto reportModelDto, Long userId, String siteCode) {
		// 查询导出数据list
		List<AgentComReportDto> resultList = agentComReportMapper.memberListFromReport(reportModelDto); // 新的查询report方法

		if (resultList.size() != 0) {
			for (int i = 0; i < resultList.size(); i++) {
				if (StringUtil.isEmpty(resultList.get(i).getCateGory())) {
					resultList.get(i).setCateGory("其他");
				}
			}
			// 获取小计
			resultList.add(getSubtotal(resultList, Constants.EVNumber.two));
			// 获取总计
			resultList.add(getTotalOnlyOne(reportModelDto, Constants.EVNumber.two, false));
		}
//        if (resultList.size() > 10000) {
//            throw new R200Exception("导出数量超过1W条，请更新搜索条件后再进行导出！");
//        }

		// list处理为map
		List<Map<String, Object>> mapList = resultList.stream().map(e -> {
			dealAndTransData(e);
			Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
			return entityMap;
		}).collect(Collectors.toList());

		// excel第一行数据
		Map<String, Object> map = new HashMap<>(8);
		map.put("startTime", reportModelDto.getStartTime());
		map.put("endTime", reportModelDto.getEndTime());
		map.put("downloadTime", DateUtil.getCurrentDate(FORMAT_18_DATE_TIME));
		map.put("mapList", mapList);

		// 异步生成并上传excel文件
		sysFileExportRecordService.exportExcel(memberListExportPath, map, userId, reportModelDto.getModule(), siteCode);
	}

    /**
     * 导出字段处理
     */
    private void dealAndTransDataEx(AgentComReportDto dto) {
        if (StringUtils.isEmpty(dto.getCateGory())) {     // 部门
            dto.setCateGory("其他");
        }
    }

    private void dealAndTransData(AgentComReportDto dto) {
    }

    /**
     * 代理总览查询直属会员总计
     * @return
     */
    private List<AgentComReportDto> getMemberTotalOnlyOne(AgentComReportDto dto, int flag, Boolean view) {
    	AgentAccount agentByAccount = agentMapper.getAgentByAccount(dto.getParentAgyAccount());
    	if (agentByAccount != null) {
    		dto.setAgyId(agentByAccount.getId());
    	}
    	PageHelper.startPage(dto.getPageNo(), dto.getPageSize());
        List<AgentComReportDto> secondList = agentComReportExtendMapper.cagencyMemberTotalFromReport(dto); // 新的查询report方法

        ReportParamsDto dtoCost = new ReportParamsDto();
        dtoCost.setStartTime(dto.getStartTime());
        dtoCost.setEndTime(dto.getEndTime());
			
        for (AgentComReportDto acrDto : secondList) {
        	// 获取每个代理最下级的平台费，服务费
        	dtoCost.setAgyId(acrDto.getAgyId());
        	CostTotalDto costTotalDto = finaceReportService.depotCostTotalForSingle(dtoCost);
        	acrDto.setCost(costTotalDto.getCost());
        	acrDto.setServiceCost(costTotalDto.getServiceCost());
            
        	 if (acrDto.getTotalProfit().compareTo(BigDecimal.ZERO) == -1) {
                 acrDto.setTotalProfit((acrDto.getTotalProfit().negate().add(costTotalDto.getCost()).add(costTotalDto.getServiceCost())).negate());
             } else {
                 acrDto.setTotalProfit(acrDto.getTotalProfit().subtract(costTotalDto.getCost()).subtract(costTotalDto.getServiceCost()));
             }
            acrDto.setNetwinlose(getNetwinlose(acrDto.getAgyId()));
        }

        return secondList;
    }

    /**
     * 代理总览查询直属会员总计导出
     */
    private void cagencyMemberTotalListExport(AgentComReportDto reportModelDto, Long userId, String siteCode) {
        // 查询导出数据list
        List<AgentComReportDto> resultList = agentComReportExtendMapper.cagencyMemberTotalFromReport(reportModelDto); // 新的查询report方法

        ReportParamsDto dtoCost = new ReportParamsDto();
        dtoCost.setStartTime(reportModelDto.getStartTime());
        dtoCost.setEndTime(reportModelDto.getEndTime());
        
        // 轮询查询代理平台费用
        for (AgentComReportDto acrDto : resultList) {
        	// 获取每个代理最下级的平台费，服务费
        	dtoCost.setAgyId(acrDto.getAgyId());
        	CostTotalDto costTotalDto = finaceReportService.depotCostTotalForSingle(dtoCost);
        	acrDto.setCost(costTotalDto.getCost());
        	acrDto.setServiceCost(costTotalDto.getServiceCost());
            
        	 if (acrDto.getTotalProfit().compareTo(BigDecimal.ZERO) == -1) {
                 acrDto.setTotalProfit((acrDto.getTotalProfit().negate().add(costTotalDto.getCost()).add(costTotalDto.getServiceCost())).negate());
             } else {
                 acrDto.setTotalProfit(acrDto.getTotalProfit().subtract(costTotalDto.getCost()).subtract(costTotalDto.getServiceCost()));
             }
            acrDto.setNetwinlose(getNetwinlose(acrDto.getAgyId()));
        }

        if (resultList.size() != 0) {
            for (int i = 0; i < resultList.size(); i++) {
                if (StringUtil.isEmpty(resultList.get(i).getCateGory())) {
                    resultList.get(i).setCateGory("其他");
                }
            }
            //获取小计
            //resultList.add(getSubtotal(resultList, Constants.EVNumber.two));
            //获取总计
            //resultList.add(getTotal(reportModelDto, Constants.EVNumber.two, false));
        }

        // list处理为map
        List<Map<String, Object>> mapList = resultList.stream().map(e -> {
            dealAndTransData(e);
            Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
            return entityMap;
        }).collect(Collectors.toList());

        // excel第一行数据
        Map<String, Object> map = new HashMap<>(8);
        map.put("startTime", reportModelDto.getStartTime());
        map.put("endTime", reportModelDto.getEndTime());
        map.put("downloadTime", DateUtil.getCurrentDate(FORMAT_18_DATE_TIME));
        map.put("mapList", mapList);

        // 异步生成并上传excel文件
        sysFileExportRecordService.exportExcel(cagencyMemberTotalListExportPath, map, userId, reportModelDto.getModule(), siteCode);
    }
}
