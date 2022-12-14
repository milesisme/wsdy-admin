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

    // ??????module???excel??????path
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
     *  ????????????
     */
    public AgentComReportDto totalInfo(AgentComReportDto model, Long userId) {
        AgentComReportDto result = agentComReportMapper.totalInfoFromReport(model); // ????????????report??????
        
        ReportParamsDto dto = new ReportParamsDto();
        dto.setStartTime(model.getStartTime());
        dto.setEndTime(model.getEndTime());
        dto.setIsCagency(1);
        dto.setDepartmentid(model.getDepartmentid());
        // ??????????????????????????????????????????
        CostTotalDto costTotalDto = finaceReportService.depotCostTotal(dto);
		result.setCost(costTotalDto.getCost());
		result.setServiceCost(costTotalDto.getServiceCost());
        return result;
    }

    /***
     * ????????????
     */
    public PageUtils totalListByDay(AgentComReportDto model, Long userId) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());

        List<AgentComReportDto> resultList = agentComReportMapper.totalListByDayFromReport(model); // ????????????report??????
        PageUtils p = BeanUtil.toPagedResult(resultList);
        return p;
    }

    /**
     * (??????/??????/??????)??????/??????????????????
     */
    public PageUtils tagencyList(AgentComReportDto model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        Long logNow = System.currentTimeMillis();
        model.setGroubyAgent(true);
        List<AgentComReportDto> resultList = agentComReportMapper.tagencyListFromReport(model); // ????????????report??????
        
        // ?????? ?????? ??????????????????????????? ????????????????????????????????????
        if (model.getIsCagency() != 0) {
	        ReportParamsDto dto = new ReportParamsDto();
	        dto.setStartTime(model.getStartTime());
	        dto.setEndTime(model.getEndTime());
	        dto.setIsCagency(1);
	        dto.setDepartmentid(model.getDepartmentid());
	        dto.setAgentLevel(model.getAgentLevel());
	        // ???????????????????????????????????????????????????
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
            //????????????
            resultList.add(getSubtotal(resultList, Constants.EVNumber.one));
            log.info("tagencyList==??????" + JSON.toJSONString(model) + "==??????????????????????????????????????????"+ (System.currentTimeMillis() - logNow) +"??????");
            model.setIsNotIncludeSelf(true);
            //????????????
            resultList.add(getTotalOnlyOne(model, Constants.EVNumber.one, false));
            log.info("tagencyList==??????" + JSON.toJSONString(model) + "==??????????????????????????????????????????"+ (System.currentTimeMillis() - logNow) +"??????");
        }
        PageUtils p = BeanUtil.toPagedResult(resultList);
        return p;
    }

    /**
     * ???????????????(??????)??????
     */
    public PageUtils categoryList(AgentComReportDto model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        Long logNow = System.currentTimeMillis();
        log.info("categoryList==??????" + JSON.toJSONString(model) + "==??????" );
        List<AgentComReportDto> resultList = agentComReportMapper.categoryListFromReport(model); // ????????????report??????
        log.info("categoryList==??????" + JSON.toJSONString(model) + "==??????????????????????????????????????????"+ (System.currentTimeMillis() - logNow) +"??????");
        if (resultList.size() != 0) {
            //????????????
            resultList.add(getSubtotal(resultList, Constants.EVNumber.three));
            log.info("categoryList==??????" + JSON.toJSONString(model) + "==??????????????????????????????????????????"+ (System.currentTimeMillis() - logNow) +"??????");
            //????????????
            resultList.add(getTotalOnlyOne(model, Constants.EVNumber.three, false));
            log.info("categoryList==??????" + JSON.toJSONString(model) + "==??????????????????????????????????????????"+ (System.currentTimeMillis() - logNow) +"??????");
        }
        PageUtils p = BeanUtil.toPagedResult(resultList);
        return p;
    }

    /**
     * ????????????--??????????????????????????????
     * 
     */
    public List<AgentComReportDto> subAgentTotalViewList(AgentComReportDto model) {
        List<AgentComReportDto> resultList = new ArrayList<>();
        // ????????????
        model.setIsNotIncludeSelf(true);
        resultList.add(getTotalOnlyOne(model, Constants.EVNumber.one, true));
        // ????????????
        resultList.add(getTotalOnlyOne(model, Constants.EVNumber.two, true));
        return resultList;
    }

    /**
     * ????????????
     */
    public PageUtils agentMemberTotalView(AgentComReportDto model) {
        List<AgentComReportDto> resultList = new ArrayList<>();
        Long logNow = System.currentTimeMillis();
        log.info("agentMemberTotalView==??????" + JSON.toJSONString(model) + "==??????" );
        // ?????????????????????????????????????????????
        resultList = getMemberTotalOnlyOne(model, Constants.EVNumber.two, true);
        log.info("agentMemberTotalView==??????" + JSON.toJSONString(model) + "==????????????????????????????????????"+ (System.currentTimeMillis() - logNow) +"??????");
        PageUtils p = BeanUtil.toPagedResult(resultList);
        return p;
    }


    /**
     * ????????????--??????????????????
     */
    public PageUtils memberList(AgentComReportDto model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        Long logNow = System.currentTimeMillis();
        log.info("memberList==??????" + JSON.toJSONString(model) + "==??????" );
        // ????????????????????????????????????
        List<AgentComReportDto> resultList = agentComReportMapper.memberListFromReport(model); // ????????????report??????
        
        log.info("memberList==??????" + JSON.toJSONString(model) + "==??????????????????????????????????????????"+ (System.currentTimeMillis() - logNow) +"??????");
        if (resultList.size() != 0) {
            //????????????
            resultList.add(getSubtotal(resultList, Constants.EVNumber.two));
            log.info("memberList==??????" + JSON.toJSONString(model) + "==??????????????????????????????????????????"+ (System.currentTimeMillis() - logNow) +"??????");
            //????????????
            resultList.add(getTotalOnlyOne(model, Constants.EVNumber.two, false));
            log.info("memberList==??????" + JSON.toJSONString(model) + "==??????????????????????????????????????????"+ (System.currentTimeMillis() - logNow) +"??????");
        }
        return BeanUtil.toPagedResult(resultList);
    }

    /**
     * ?????????????????????????????????
     *
     * @param reportModelDto
     * @param userId
     * @return
     */
    public Integer agentLineReportExportCount(AgentComReportDto reportModelDto, Long userId) {
        // ??????????????????????????? ?????? ??????????????????????????????????????????????????????
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
        if (grade == Constants.EVNumber.zero) { // ??????
            searchGrade = reportModelDto.getAgentLineExportTypes();
        } else if (grade == Constants.EVNumber.one) { // ??????
            for (Integer type : reportModelDto.getAgentLineExportTypes()) {
                if (type > Constants.EVNumber.one) {
                    searchGrade.add(type-1);
                }
            }
        } else if (grade == Constants.EVNumber.two) { // ????????????
            for (Integer type : reportModelDto.getAgentLineExportTypes()) {
                if (type > Constants.EVNumber.two) {
                    searchGrade.add(type-2);
                }
            }
        }
        return searchGrade;
    }

    /**
     * ???????????????
     */
    private void agentLineListExport(AgentComReportDto reportModelDto, Long userId, String siteCode) {
        reportModelDto.setIsCagency(99); // 99??????????????????
        // ???????????????????????????????????????
        List<AgentAccount> checkAgyList = agentMapper.findAgyAccountAndGrade(new AgentAccount(){{setAgyAccount(reportModelDto.getAgyAccount());}});
        if (checkAgyList == null || checkAgyList.size() <= 0) {
            throw new R200Exception("????????????????????????????????????!");
        }
        AgentAccount checkAgy = checkAgyList.get(0);
        if (checkAgy.getGrade() > Constants.EVNumber.two) {
            throw new R200Exception("????????????????????????????????????!");
        }

        // ????????????????????????????????? 1?????? 2???????????? 3????????????
        List<AgentComReportDto> resultList = new ArrayList<>();
        List<Integer> searchGrade = getAgentLineExportSearchGrade(reportModelDto, checkAgy);
        if (searchGrade == null || searchGrade.size() <= 0) {
            throw new R200Exception("????????????????????????????????????!");
        }
        for (Integer grade : searchGrade) { // ????????????????????????????????????
            reportModelDto.setAgentLevel(grade);
            // ??????????????????list
            reportModelDto.setGroubyAgent(true);
            List<AgentComReportDto> tempList = agentComReportMapper.tagencyListFromReport(reportModelDto); // ????????????report??????
            resultList.addAll(tempList);
        }

        // ??????????????????????????????????????????
        if (reportModelDto.getIsCagency() != 0) {
            ReportParamsDto reportParamsDto = new ReportParamsDto();
            reportParamsDto.setStartTime(reportModelDto.getStartTime());
            reportParamsDto.setEndTime(reportModelDto.getEndTime());
            reportParamsDto.setIsCagency(1);
            // ????????????????????????????????????????????????????????????
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
            //????????????
            resultList.add(getSubtotal(resultList, Constants.EVNumber.one));
            //????????????
            //resultList.add(getTotalOnlyOne(reportModelDto, Constants.EVNumber.one, false));
            // ???????????????????????????
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
        // list?????????map
        List<Map<String, Object>> mapList = resultList.stream().map(e -> {
            dealAndTransData(e);
            Map<String, Object> entityMap = jsonUtil.Entity2Map(e);

            return entityMap;
        }).collect(Collectors.toList());

        // excel???????????????
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

        // ?????????????????????excel??????
        sysFileExportRecordService.exportExcel(agentLineListExportPath, map, userId, reportModelDto.getModule(), siteCode);
    }

    /**
     * ????????????????????????
     */
    private void tagencyListByDayExport(AgentComReportDto reportModelDto, Long userId, String siteCode) {
        // ??????????????????list
        reportModelDto.setGroubyAgent(true);
        // ?????????????????????????????????SQL?????????????????????
        List<String> dateList = DateUtil.getListDateBetween(reportModelDto.getStartTime(), reportModelDto.getEndTime());
        log.info("????????????????????????{}", dateList);
        List<AgentComReportDto> resultList = new ArrayList<>();
        // ????????????????????????
        String str = JSON.toJSONString(reportModelDto);
        AgentComReportDto searchDto = JSON.parseObject(str, AgentComReportDto.class);
        for (String next : dateList) {
            searchDto.setStartTime(next.concat(" 00:00:00"));
            searchDto.setEndTime(next.concat(" 23:59:59"));

            List<AgentComReportDto> tmpList = agentComReportMapper.tagencyListFromReport(searchDto); // ????????????report??????
            tmpList.stream().forEach(e->e.setCreateTime(next));
            resultList.addAll(tmpList);
            tmpList = null;
        }


        // ?????? ?????? ??????????????????????????? ????????????????????????????????????
        if (reportModelDto.getIsCagency() != 0) {
            ReportParamsDto reportParamsDto = new ReportParamsDto();
            reportParamsDto.setStartTime(reportModelDto.getStartTime());
            reportParamsDto.setEndTime(reportModelDto.getEndTime());
            reportParamsDto.setIsCagency(1);
            // ????????????????????????????????????????????????????????????
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
            //????????????
            resultList.add(getSubtotal(resultList, Constants.EVNumber.one));
        }
        // list?????????map
        List<Map<String, Object>> mapList = resultList.stream().map(e -> {
            dealAndTransData(e);
            Map<String, Object> entityMap = jsonUtil.Entity2Map(e);

            return entityMap;
        }).collect(Collectors.toList());

        // excel???????????????
        Map<String, Object> map = new HashMap<>(8);
        // ????????????parent
        if (Objects.nonNull(reportModelDto.getIsCagency()) && reportModelDto.getIsCagency().equals(Constants.EVNumber.zero)) {    // ??????
            map.put("parent", "??????");
        }
        if (Objects.nonNull(reportModelDto.getIsCagency()) && reportModelDto.getIsCagency().equals(Constants.EVNumber.one)) {     // ?????????
            AgentAccount parent = agentAccountMapper.selectByPrimaryKey(reportModelDto.getAgyId());
            if (Objects.nonNull(parent)) {
                map.put("parent", parent.getAgyAccount());
            }
        }
        map.put("startTime", reportModelDto.getStartTime());
        map.put("endTime", reportModelDto.getEndTime());
        map.put("downloadTime", DateUtil.getCurrentDate(FORMAT_18_DATE_TIME));
        map.put("mapList", mapList);

        // ?????????????????????excel??????
        sysFileExportRecordService.exportExcel(tagencyListByDayExportPath, map, userId, reportModelDto.getModule(), siteCode);
    }

    /**
     * ????????????
     *
     * @param reportModelDto
     * @param userId
     * @return
     */
    public SysFileExportRecord comReportExport(AgentComReportDto reportModelDto, Long userId) {
        // ????????????????????????
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, reportModelDto.getModule());

        // ??????????????????????????????????????????????????????
        if (null != record) {
            // ??????????????????
            String siteCode = CommonUtil.getSiteCode();
            CompletableFuture.runAsync(() -> {
                ThreadLocalCache.setSiteCodeAsny(siteCode);
                log.info("exportExcel==??????==start=={}=={}", reportModelDto.getModule(), siteCode);
                if (totalListByDayModule.equals(reportModelDto.getModule())) {  // ??????????????????
                    totalListByDayExport(reportModelDto, userId, siteCode);
                }
                if (tagencyListModule.equals(reportModelDto.getModule())) {    // ???????????????????????????/??????????????????
                    log.info("exportExcel==??????==????????????=={}=={}", tagencyListModule, siteCode);
                    tagencyListExport(reportModelDto, userId, siteCode);
                }
                if (categoryListModule.equals(reportModelDto.getModule())) {   // ???????????????(??????)??????
                    categoryListExport(reportModelDto, userId, siteCode);
                }
                if (memberListModule.equals(reportModelDto.getModule())) {     // ??????????????????
                    memberListExport(reportModelDto, userId, siteCode);
                }
                if (cagencyMemberTotalListModule.equals(reportModelDto.getModule())) {     // ??????????????????
                    cagencyMemberTotalListExport(reportModelDto, userId, siteCode);
                }
                if (agentLineListModel.equals(reportModelDto.getModule())) { // ???????????????
                    agentLineListExport(reportModelDto, userId, siteCode);
                }
                if (tagencyListByDayModule.equals(reportModelDto.getModule())) { // ??????????????????
                    tagencyListByDayExport(reportModelDto, userId, siteCode);
                }
            });
            /*CompletableFuture.runAsync(() -> {

            });*/
        }
        return record;
    }

    /**
     * ?????????????????????????????????
     */
    public R checkFile(String module, Long userId) {
        // ???????????????module????????????
        SysFileExportRecord record = sysFileExportRecordService.getAsynFileExportRecordByUserId(userId, module);
        if (null != record) {
            String fileName = "";
            if (totalListByDayModule.equals(module)) {  // ??????????????????
                fileName = totalListByDayExportPath.substring(totalListByDayExportPath.lastIndexOf("/") + 1, totalListByDayExportPath.length());
            }
            if (tagencyListModule.equals(module)) {    // ??????????????????/??????????????????
                fileName = tagencyListExportPath.substring(tagencyListExportPath.lastIndexOf("/") + 1, tagencyListExportPath.length());
            }
            if (categoryListModule.equals(module)) {   // ???????????????(??????)??????
                fileName = categoryListExportPath.substring(categoryListExportPath.lastIndexOf("/") + 1, categoryListExportPath.length());
            }
            if (memberListModule.equals(module)) {     // ??????????????????
                fileName = memberListExportPath.substring(memberListExportPath.lastIndexOf("/") + 1, memberListExportPath.length());
            }
            if (cagencyMemberTotalListModule.equals(module)) {     // ????????????
                fileName = cagencyMemberTotalListExportPath.substring(cagencyMemberTotalListExportPath.lastIndexOf("/") + 1, cagencyMemberTotalListExportPath.length());
            }
            if (agentLineListModel.equals(module)) {     // ???????????????
                fileName = agentLineListExportPath.substring(agentLineListExportPath.lastIndexOf("/") + 1, agentLineListExportPath.length());
            }
            if (tagencyListByDayModule.equals(module)) {     // ??????????????????
                fileName = tagencyListByDayExportPath.substring(tagencyListByDayExportPath.lastIndexOf("/") + 1, tagencyListByDayExportPath.length());
            }

            record.setDownloadFileName(fileName);
            return R.ok().put(record);
        }
        return R.ok(false);
    }

    /**
     * ??????????????????--?????????
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
     * ????????????
     *
     * @param list
     * @param flag 1  ????????????  2 ???????????? 3 ??????????????????
     * @return
     */
    private AgentComReportDto getSubtotal(List<AgentComReportDto> list, int flag) {
        AgentComReportDto subTotal = new AgentComReportDto();
        BigDecimal totalProfit = BigDecimal.ZERO;               // ?????????
        BigDecimal totalDepositBalance = BigDecimal.ZERO;       // ????????????
        BigDecimal totalActualarrival = BigDecimal.ZERO;       // ????????????????????????
        BigDecimal totalDepositBalanceNum = BigDecimal.ZERO;    // ????????????
        BigDecimal totalDrawAmount = BigDecimal.ZERO;           // ????????????
        BigDecimal totalDrawAmountNum = BigDecimal.ZERO;        // ????????????
        BigDecimal totalBonusAmount = BigDecimal.ZERO;          // ????????????
        BigDecimal totalBonusAmountNum = BigDecimal.ZERO;       // ????????????
        BigDecimal totalPayout = BigDecimal.ZERO;               // ????????????
        BigDecimal totalActiveMbrs = BigDecimal.ZERO;           // ????????????
        BigDecimal totalNewMbrs = BigDecimal.ZERO;              // ????????????
        BigDecimal totalNewDeposits = BigDecimal.ZERO;          // ????????????
        BigDecimal totalNewDepositAmount = BigDecimal.ZERO;    // ????????????
        BigDecimal totalValidBets = BigDecimal.ZERO;            // ????????????
        BigDecimal totalSubAgentNum = BigDecimal.ZERO;          // ??????????????????
        BigDecimal totalSubMbrNum = BigDecimal.ZERO;            // ??????????????????
        BigDecimal totalCost = BigDecimal.ZERO;                 // ???????????????
        BigDecimal totalServiceCost = BigDecimal.ZERO;          // ???????????????
        Integer betCount = 0;          							// ????????????
        BigDecimal totalBetMbrs = BigDecimal.ZERO;          	// ????????????
        BigDecimal calculateProfit = BigDecimal.ZERO;          	// ????????????

        for (AgentComReportDto model : list) {
            if (model.getTotalProfit() != null) {   // ?????????
                totalProfit = totalProfit.add(model.getTotalProfit());
            }
            if (model.getTotalDepositBalance() != null) {   // ????????????
                totalDepositBalance = totalDepositBalance.add(model.getTotalDepositBalance());
            }
            if (model.getTotalActualarrival() != null) {   // ??????????????????
                totalActualarrival = totalActualarrival.add(model.getTotalActualarrival());
            }
            if (model.getTotalDepositBalanceNum() != null) {    // ????????????
                totalDepositBalanceNum = totalDepositBalanceNum.add(model.getTotalDepositBalanceNum());
            }
            if (model.getTotalDrawAmount() != null) {   // ????????????
                totalDrawAmount = totalDrawAmount.add(model.getTotalDrawAmount());
            }
            if (model.getTotalDrawAmountNum() != null) {   // ????????????
                totalDrawAmountNum = totalDrawAmountNum.add(model.getTotalDrawAmountNum());
            }
            if (model.getTotalBonusAmount() != null) {  // ????????????
                totalBonusAmount = totalBonusAmount.add(model.getTotalBonusAmount());
            }
            if (model.getTotalBonusAmountNum() != null) {  // ????????????
                totalBonusAmountNum = totalBonusAmountNum.add(model.getTotalBonusAmountNum());
            }
            if (model.getTotalPayout() != null) {    // ????????????
                totalPayout = totalPayout.add(model.getTotalPayout());
            }
            if (model.getTotalActiveMbrs() != null) {   // ????????????
                totalActiveMbrs = totalActiveMbrs.add(model.getTotalActiveMbrs());
            }
            if (model.getTotalNewMbrs() != null) { // ????????????
                totalNewMbrs = totalNewMbrs.add(model.getTotalNewMbrs());
            }
            if (model.getTotalNewDeposits() != null) { // ????????????
                totalNewDeposits = totalNewDeposits.add(model.getTotalNewDeposits());
            }
            if (model.getTotalNewDepositAmount() != null) { // ????????????
                totalNewDepositAmount = totalNewDepositAmount.add(model.getTotalNewDepositAmount());
            }
            if (model.getTotalValidBets() != null) {   // ????????????
                totalValidBets = totalValidBets.add(model.getTotalValidBets());
            }
            if (model.getTotalSubAgentNum() != null) {   // ??????????????????
                totalSubAgentNum = totalSubAgentNum.add(model.getTotalSubAgentNum());
            }
            if (model.getTotalSubMbrNum() != null) {   // ??????????????????
                totalSubMbrNum = totalSubMbrNum.add(model.getTotalSubMbrNum());
            }
            if (model.getCost() != null) {   // ???????????????
                totalCost = totalCost.add(model.getCost());
            }
            if (model.getServiceCost() != null) {   // ???????????????
                totalServiceCost = totalServiceCost.add(model.getServiceCost());
            }
            if (model.getBetCount() != null) {   // ????????????
            	betCount = betCount + model.getBetCount();
            }
            if (model.getTotalBetMbrs() != null) {   // ????????????
            	totalBetMbrs = totalBetMbrs.add(model.getTotalBetMbrs());
            }
            if (model.getCalculateProfit() != null) {   // ????????????
            	calculateProfit = calculateProfit.add(model.getCalculateProfit());
            }
        }


        if (Constants.EVNumber.one == flag) {
            subTotal.setAgyAccount("??????");
        } else if (Constants.EVNumber.two == flag) {
            subTotal.setLoginName("??????");
        } else if (Constants.EVNumber.three == flag) {
            subTotal.setCateGory("??????");
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
        //?????????????????????????????????
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
     * ????????????
     *
     * @param dto
     * @param flag 1  ????????????  2 ???????????? 3 ??????????????????
     * @param view true ???????????????????????? false ????????????
     * @return
     */
    private AgentComReportDto getTotal(AgentComReportDto dto, int flag, Boolean view) {
        AgentComReportDto allTotal = new AgentComReportDto();
        List<AgentComReportDto> secondList;
        if (Constants.EVNumber.one == flag) {
            dto.setGroubyAgent(true);
            //secondList = agentComReportMapper.tagencyList(dto);
            secondList = agentComReportMapper.tagencyListFromReport(dto); // ????????????report??????
//            for (AgentComReportDto dto2 : secondList){
//                if (dto2.getTotalProfit().compareTo(BigDecimal.ZERO) == -1) {
//                    dto2.setTotalProfit((dto2.getTotalProfit().negate().add(dto2.getCost())).negate());
//                } else {
//                    dto2.setTotalProfit(dto2.getTotalProfit().subtract(dto2.getCost()));
//                }
//            }
        } else if (Constants.EVNumber.two == flag) {
            //secondList = agentComReportMapper.memberList(dto);
            secondList = agentComReportMapper.memberListFromReport(dto); // ????????????report??????
        } else if (Constants.EVNumber.three == flag) {
            //secondList = agentComReportMapper.categoryList(dto);
            secondList = agentComReportMapper.categoryListFromReport(dto); // ????????????report??????
        } else {
            return allTotal;
        }
        BigDecimal totalProfit = BigDecimal.ZERO;               // ?????????
        BigDecimal totalDepositBalance = BigDecimal.ZERO;       // ????????????
        BigDecimal totalActualarrival = BigDecimal.ZERO;       // ????????????????????????
        BigDecimal totalDepositBalanceNum = BigDecimal.ZERO;    // ????????????
        BigDecimal totalDrawAmount = BigDecimal.ZERO;           // ????????????
        BigDecimal totalDrawAmountNum = BigDecimal.ZERO;        // ????????????
        BigDecimal totalBonusAmount = BigDecimal.ZERO;          // ????????????
        BigDecimal totalBonusAmountNum = BigDecimal.ZERO;       // ????????????
        BigDecimal totalPayout = BigDecimal.ZERO;               // ????????????
        BigDecimal totalActiveMbrs = BigDecimal.ZERO;           // ????????????
        BigDecimal totalNewMbrs = BigDecimal.ZERO;              // ????????????
        BigDecimal totalNewDeposits = BigDecimal.ZERO;          // ????????????
        BigDecimal totalNewDepositAmount = BigDecimal.ZERO;     // ????????????
        BigDecimal totalValidBets = BigDecimal.ZERO;            // ????????????
        BigDecimal totalSubAgentNum = BigDecimal.ZERO;          // ??????????????????
        BigDecimal totalSubMbrNum = BigDecimal.ZERO;            // ??????????????????
        BigDecimal costAll = BigDecimal.ZERO;            		// ?????????
        BigDecimal serviceCost = BigDecimal.ZERO;            	// ?????????


        for (AgentComReportDto model : secondList) {
            if (model.getTotalProfit() != null) {   // ?????????
                totalProfit = totalProfit.add(model.getTotalProfit());
            }
            if (model.getTotalDepositBalance() != null) {   // ????????????
                totalDepositBalance = totalDepositBalance.add(model.getTotalDepositBalance());
            }
            if (model.getTotalActualarrival() != null) {   // ????????????????????????
                totalActualarrival = totalActualarrival.add(model.getTotalActualarrival());
            }
            if (model.getTotalDepositBalanceNum() != null) {    // ????????????
                totalDepositBalanceNum = totalDepositBalanceNum.add(model.getTotalDepositBalanceNum());
            }
            if (model.getTotalDrawAmount() != null) {   // ????????????
                totalDrawAmount = totalDrawAmount.add(model.getTotalDrawAmount());
            }
            if (model.getTotalDrawAmountNum() != null) {   // ????????????
                totalDrawAmountNum = totalDrawAmountNum.add(model.getTotalDrawAmountNum());
            }
            if (model.getTotalBonusAmount() != null) {  // ????????????
                totalBonusAmount = totalBonusAmount.add(model.getTotalBonusAmount());
            }
            if (model.getTotalBonusAmountNum() != null) {  // ????????????
                totalBonusAmountNum = totalBonusAmountNum.add(model.getTotalBonusAmountNum());
            }
            if (model.getTotalPayout() != null) {    // ????????????
                totalPayout = totalPayout.add(model.getTotalPayout());
            }
            if (model.getTotalActiveMbrs() != null) {   // ????????????
                totalActiveMbrs = totalActiveMbrs.add(model.getTotalActiveMbrs());
            }
            if (model.getTotalNewMbrs() != null) { // ????????????
                totalNewMbrs = totalNewMbrs.add(model.getTotalNewMbrs());
            }
            if (model.getTotalNewDeposits() != null) { // ????????????
                totalNewDeposits = totalNewDeposits.add(model.getTotalNewDeposits());
            }
            if (model.getTotalNewDepositAmount() != null) { // ????????????
                totalNewDepositAmount = totalNewDepositAmount.add(model.getTotalNewDepositAmount());
            }
            if (model.getTotalValidBets() != null) {   // ????????????
                totalValidBets = totalValidBets.add(model.getTotalValidBets());
            }
            if (model.getTotalSubAgentNum() != null) {   // ??????????????????
                totalSubAgentNum = totalSubAgentNum.add(model.getTotalSubAgentNum());
            }
            if (model.getTotalSubMbrNum() != null) {   // ??????????????????
                totalSubMbrNum = totalSubMbrNum.add(model.getTotalSubMbrNum());
            }
            if (model.getCost() != null) {   // ?????????
            	costAll = costAll.add(model.getCost());
            }
            if (model.getServiceCost() != null) {   // ?????????
            	serviceCost = serviceCost.add(model.getServiceCost());
            }
        }

        if (view) {
            if (Constants.EVNumber.one == flag) {
                allTotal.setAgyAccount("????????????");
            } else if (Constants.EVNumber.two == flag) {
                allTotal.setAgyAccount("????????????");
            }
        } else {
            if (Constants.EVNumber.one == flag) {
                allTotal.setAgyAccount("??????");
            } else if (Constants.EVNumber.two == flag) {
                allTotal.setLoginName("??????");
            } else if (Constants.EVNumber.three == flag) {
                allTotal.setCateGory("??????");
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
        // flag 1 ????????????  2 ???????????? 3 ??????????????????
        if (Constants.EVNumber.two == flag) {   // ????????????
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
        // ?????? ???????????????????????????
        allTotal.setCtRatio(null);
        allTotal.setLsRatio(null);
        allTotal.setSyRatio(null);
        allTotal.setYhRatio(null);
        allTotal.setCtDiffer(null);
        return allTotal;
    }

    /**
     * ????????????
     *
     * @param dto
     * @param flag 1  ???????????? ?????????????????????????????????  2 ???????????? 3 ??????????????????
     * @param view true ???????????????????????? false ????????????
     * @return
     */
    private AgentComReportDto getTotalOnlyOne(AgentComReportDto dto, int flag, Boolean view) {
        AgentComReportDto allTotal = new AgentComReportDto();
        List<AgentComReportDto> secondList;
        BigDecimal costDepot = BigDecimal.ZERO;
        BigDecimal serviceCostDepot = BigDecimal.ZERO;
        		
		if (Constants.EVNumber.one == flag) {
			 // ?????? ?????? ??????????????????????????? ????????????????????????????????????
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
			secondList = agentComReportExtendMapper.tagencyTotalFromReport(dto); // ????????????report??????
		} else if (Constants.EVNumber.two == flag) {
			
            secondList = agentComReportExtendMapper.memberTotalFromReport(dto); // ????????????report??????
        } else if (Constants.EVNumber.three == flag) {
            secondList = agentComReportExtendMapper.categoryTotalFromReport(dto); // ????????????report??????
        } else {
            return allTotal;
        }
        
        if (view) {
            if (Constants.EVNumber.one == flag) {
                allTotal.setAgyAccount("????????????");
            } else if (Constants.EVNumber.two == flag) {
                allTotal.setAgyAccount("????????????");
            }
        } else {
            if (Constants.EVNumber.one == flag) {
                allTotal.setAgyAccount("??????");
            } else if (Constants.EVNumber.two == flag) {
                allTotal.setLoginName("??????");
            } else if (Constants.EVNumber.three == flag) {
                allTotal.setCateGory("??????");
            }
        }
        if (CollectionUtil.isEmpty(secondList)) {
        	return allTotal;
        }
        AgentComReportDto onlyOne = secondList.get(0);
        // ???????????? ???????????????
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
        // ?????????????????????
        allTotal.setCost(costDepot);
    	allTotal.setServiceCost(serviceCostDepot);
    	allTotal.setTotalBetMbrs(onlyOne.getTotalBetMbrs());
    	allTotal.setBetCount(onlyOne.getBetCount()); // ????????????
    	allTotal.setCalculateProfit(onlyOne.getCalculateProfit()); // ????????????
    	allTotal.setWinloseLastTime(onlyOne.getWinloseLastTime()); // ?????????????????????

    	// ?????????????????????????????????????????????
        if (Constants.EVNumber.two == flag && view) {   // ????????????
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
            // ?????????????????????0
            if (allTotal.getTotalProfit().compareTo(BigDecimal.ZERO) == -1) {
                allTotal.setTotalProfit((allTotal.getTotalProfit().negate().add(cost).add(serviceCost)).negate());
            } else {
            	// ?????????0 ???????????????????????????
                allTotal.setTotalProfit(allTotal.getTotalProfit().subtract(cost).subtract(serviceCost));
            }
            allTotal.setNetwinlose(getNetwinlose(dto.getAgyId()));
        }
        
        // ?????? ???????????????????????????
        allTotal.setCtRatio(null);
        allTotal.setLsRatio(null);
        allTotal.setSyRatio(null);
        allTotal.setYhRatio(null);
        allTotal.setCtDiffer(null);
        return allTotal;
    }
    
    /**
     * ????????????????????????
     */
    private void totalListByDayExport(AgentComReportDto reportModelDto, Long userId, String siteCode) {
        // ??????????????????list
        //List<AgentComReportDto> resultList = agentComReportMapper.totalListByDay(reportModelDto);
        List<AgentComReportDto> resultList = agentComReportMapper.totalListByDayFromReport(reportModelDto); // ????????????report??????
        if (resultList.size() > 10000) {
            throw new R200Exception("??????????????????1W????????????????????????????????????????????????");
        }
        for (AgentComReportDto agentComReportDto : resultList) {
        	ReportParamsDto dto = new ReportParamsDto();
        	try {
    			dto.setStartTime(DateUtil.yyyyMMddToStrStart(agentComReportDto.getCreateTime()));
    			dto.setEndTime(DateUtil.yyyyMMddToStrEnd(agentComReportDto.getCreateTime()));
			} catch (Exception e) {
				log.error("totalListByDayExport--?????????????????????,createTime" + agentComReportDto.getCreateTime(), e);
				continue;
			}
        	dto.setEndTime(reportModelDto.getEndTime());
        	dto.setIsCagency(1);
        	dto.setDepartmentid(reportModelDto.getDepartmentid());
        	// ??????????????????????????????????????????
        	CostTotalDto costTotalDto = finaceReportService.depotCostTotal(dto);
        	agentComReportDto.setCost(costTotalDto.getCost());
        	agentComReportDto.setServiceCost(costTotalDto.getServiceCost());
        }

        // list?????????map
        List<Map<String, Object>> mapList = resultList.stream().map(e -> {
            dealAndTransData(e);
            Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
            return entityMap;
        }).collect(Collectors.toList());

        // excel???????????????
        Map<String, Object> map = new HashMap<>(8);
        map.put("startTime", reportModelDto.getStartTime());
        map.put("endTime", reportModelDto.getEndTime());
        map.put("downloadTime", DateUtil.getCurrentDate(FORMAT_18_DATE_TIME));
        map.put("mapList", mapList);

        // ?????????????????????excel??????
        sysFileExportRecordService.exportExcel(totalListByDayExportPath, map, userId, reportModelDto.getModule(), siteCode);
    }

    /**
     * ??????????????????/????????????????????????
     */
    private void tagencyListExport(AgentComReportDto reportModelDto, Long userId, String siteCode) {
        // ??????????????????list
        log.info("exportExcel==tagencyList==getdata=start");
    	reportModelDto.setIsCagency(1);
        reportModelDto.setGroubyAgent(true);
        List<AgentComReportDto> resultList = agentComReportMapper.tagencyListFromReport(reportModelDto); // ????????????report??????
        log.info("exportExcel==tagencyList==getdata=end");

        // ?????? ?????? ??????????????????????????? ????????????????????????????????????
        if (reportModelDto.getIsCagency() != 0) {
	        ReportParamsDto reportParamsDto = new ReportParamsDto();
	        reportParamsDto.setStartTime(reportModelDto.getStartTime());
	        reportParamsDto.setEndTime(reportModelDto.getEndTime());
	        reportParamsDto.setIsCagency(1);
	        // ????????????????????????????????????????????????????????????
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
            //????????????
            resultList.add(getSubtotal(resultList, Constants.EVNumber.one));
            //????????????
            resultList.add(getTotalOnlyOne(reportModelDto, Constants.EVNumber.one, false));
        }
        // list?????????map
        List<Map<String, Object>> mapList = resultList.stream().map(e -> {
            dealAndTransData(e);
            Map<String, Object> entityMap = jsonUtil.Entity2Map(e);

            return entityMap;
        }).collect(Collectors.toList());
        log.info("exportExcel==tagencyList==total=end");

        // excel???????????????
        Map<String, Object> map = new HashMap<>(8);
        // ????????????parent
        if (Objects.nonNull(reportModelDto.getIsCagency()) && reportModelDto.getIsCagency().equals(Constants.EVNumber.zero)) {    // ??????
            map.put("parent", "??????");
        }
        if (Objects.nonNull(reportModelDto.getIsCagency()) && reportModelDto.getIsCagency().equals(Constants.EVNumber.one)) {     // ?????????
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
        // ?????????????????????excel??????
        sysFileExportRecordService.exportExcel(tagencyListExportPath, map, userId, reportModelDto.getModule(), siteCode);
    }

    /**
     * ???????????????(??????)????????????
     */
    private void categoryListExport(AgentComReportDto reportModelDto, Long userId, String siteCode) {
        // ??????????????????list
        //List<AgentComReportDto> resultList = agentComReportMapper.categoryList(reportModelDto);
        List<AgentComReportDto> resultList = agentComReportMapper.categoryListFromReport(reportModelDto); // ????????????report??????
        if (resultList.size() != 0) {
            //????????????
            resultList.add(getSubtotal(resultList, Constants.EVNumber.three));
            //????????????
            resultList.add(getTotal(reportModelDto, Constants.EVNumber.three, false));
        }
//        if (resultList.size() > 10000) {
//            throw new R200Exception("??????????????????1W????????????????????????????????????????????????");
//        }

        // list?????????map
        List<Map<String, Object>> mapList = resultList.stream().map(e -> {
            dealAndTransDataEx(e);
            Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
            return entityMap;
        }).collect(Collectors.toList());

        // excel???????????????
        Map<String, Object> map = new HashMap<>(8);
        map.put("startTime", reportModelDto.getStartTime());
        map.put("endTime", reportModelDto.getEndTime());
        map.put("downloadTime", DateUtil.getCurrentDate(FORMAT_18_DATE_TIME));
        map.put("mapList", mapList);

        // ?????????????????????excel??????
        sysFileExportRecordService.exportExcel(categoryListExportPath, map, userId, reportModelDto.getModule(), siteCode);
    }

    /**
     * ????????????????????????
     */
	private void memberListExport(AgentComReportDto reportModelDto, Long userId, String siteCode) {
		// ??????????????????list
		List<AgentComReportDto> resultList = agentComReportMapper.memberListFromReport(reportModelDto); // ????????????report??????

		if (resultList.size() != 0) {
			for (int i = 0; i < resultList.size(); i++) {
				if (StringUtil.isEmpty(resultList.get(i).getCateGory())) {
					resultList.get(i).setCateGory("??????");
				}
			}
			// ????????????
			resultList.add(getSubtotal(resultList, Constants.EVNumber.two));
			// ????????????
			resultList.add(getTotalOnlyOne(reportModelDto, Constants.EVNumber.two, false));
		}
//        if (resultList.size() > 10000) {
//            throw new R200Exception("??????????????????1W????????????????????????????????????????????????");
//        }

		// list?????????map
		List<Map<String, Object>> mapList = resultList.stream().map(e -> {
			dealAndTransData(e);
			Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
			return entityMap;
		}).collect(Collectors.toList());

		// excel???????????????
		Map<String, Object> map = new HashMap<>(8);
		map.put("startTime", reportModelDto.getStartTime());
		map.put("endTime", reportModelDto.getEndTime());
		map.put("downloadTime", DateUtil.getCurrentDate(FORMAT_18_DATE_TIME));
		map.put("mapList", mapList);

		// ?????????????????????excel??????
		sysFileExportRecordService.exportExcel(memberListExportPath, map, userId, reportModelDto.getModule(), siteCode);
	}

    /**
     * ??????????????????
     */
    private void dealAndTransDataEx(AgentComReportDto dto) {
        if (StringUtils.isEmpty(dto.getCateGory())) {     // ??????
            dto.setCateGory("??????");
        }
    }

    private void dealAndTransData(AgentComReportDto dto) {
    }

    /**
     * ????????????????????????????????????
     * @return
     */
    private List<AgentComReportDto> getMemberTotalOnlyOne(AgentComReportDto dto, int flag, Boolean view) {
    	AgentAccount agentByAccount = agentMapper.getAgentByAccount(dto.getParentAgyAccount());
    	if (agentByAccount != null) {
    		dto.setAgyId(agentByAccount.getId());
    	}
    	PageHelper.startPage(dto.getPageNo(), dto.getPageSize());
        List<AgentComReportDto> secondList = agentComReportExtendMapper.cagencyMemberTotalFromReport(dto); // ????????????report??????

        ReportParamsDto dtoCost = new ReportParamsDto();
        dtoCost.setStartTime(dto.getStartTime());
        dtoCost.setEndTime(dto.getEndTime());
			
        for (AgentComReportDto acrDto : secondList) {
        	// ???????????????????????????????????????????????????
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
     * ??????????????????????????????????????????
     */
    private void cagencyMemberTotalListExport(AgentComReportDto reportModelDto, Long userId, String siteCode) {
        // ??????????????????list
        List<AgentComReportDto> resultList = agentComReportExtendMapper.cagencyMemberTotalFromReport(reportModelDto); // ????????????report??????

        ReportParamsDto dtoCost = new ReportParamsDto();
        dtoCost.setStartTime(reportModelDto.getStartTime());
        dtoCost.setEndTime(reportModelDto.getEndTime());
        
        // ??????????????????????????????
        for (AgentComReportDto acrDto : resultList) {
        	// ???????????????????????????????????????????????????
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
                    resultList.get(i).setCateGory("??????");
                }
            }
            //????????????
            //resultList.add(getSubtotal(resultList, Constants.EVNumber.two));
            //????????????
            //resultList.add(getTotal(reportModelDto, Constants.EVNumber.two, false));
        }

        // list?????????map
        List<Map<String, Object>> mapList = resultList.stream().map(e -> {
            dealAndTransData(e);
            Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
            return entityMap;
        }).collect(Collectors.toList());

        // excel???????????????
        Map<String, Object> map = new HashMap<>(8);
        map.put("startTime", reportModelDto.getStartTime());
        map.put("endTime", reportModelDto.getEndTime());
        map.put("downloadTime", DateUtil.getCurrentDate(FORMAT_18_DATE_TIME));
        map.put("mapList", mapList);

        // ?????????????????????excel??????
        sysFileExportRecordService.exportExcel(cagencyMemberTotalListExportPath, map, userId, reportModelDto.getModule(), siteCode);
    }
}
