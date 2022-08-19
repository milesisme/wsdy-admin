package com.wsdy.saasops.agapi.modules.service;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wsdy.saasops.agapi.modules.dto.DirectMemberDto;
import com.wsdy.saasops.agapi.modules.dto.DirectMemberParamDto;
import com.wsdy.saasops.agapi.modules.dto.ReportParamsDto;
import com.wsdy.saasops.agapi.modules.dto.ReportResultDto;
import com.wsdy.saasops.agapi.modules.dto.SubAgentListDto;
import com.wsdy.saasops.agapi.modules.mapper.FinaceReportMapper;
import com.wsdy.saasops.agapi.modules.mapper.TeamMapper;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.modules.agent.dao.AgentAccountMapper;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.service.AgentComReportService;
import com.wsdy.saasops.modules.agent.service.AgentExportService;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import com.wsdy.saasops.modules.sys.service.SysFileExportRecordService;


@Service
public class AgentExportExcelService {

    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private TeamMapper teamMapper;
    @Autowired
    private AgentTeamService agentTeamService;
    @Autowired
    private AgentAccountMapper agentAccountMapper;
    @Autowired
    private AgentExportService exportService;
    @Autowired
    private FinaceReportMapper reportMapper;
    @Autowired
    private AgentNewService agentNewService;
    @Autowired
    private AgentComReportService agentComReportService;

    public SysFileExportRecord directMemberExport(DirectMemberParamDto paramDto, Long userId, String templatePath, String module) {
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);
        if (null != record) {
            List<DirectMemberDto> dtoList = teamMapper.directMember(paramDto);
            agentTeamService.setDirectMemberDto(dtoList);
            List<Map<String, Object>> list = dtoList.stream().map(e -> {
                e.setLoginTime(exportService.getTime(e.getLoginTime()));
                e.setRegisterTime(exportService.getTime(e.getRegisterTime()));
                Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
                return entityMap;
            }).collect(Collectors.toList());
            String siteCode = CommonUtil.getSiteCode();
            Map<String, Object> map = new HashMap<>(8);
            map.put("mapList", list);
            sysFileExportRecordService.exportExcel(templatePath, map, userId, module, siteCode);//异步执行
        }
        return record;
    }

    /**
     * 	下级代理导出
     * 
     * @param paramDto
     * @param userId
     * @param templatePath
     * @param module
     * @return
     */
    public SysFileExportRecord subAgentListExport(DirectMemberParamDto paramDto, Long userId, String templatePath, String module) {
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);
        if (null != record) {
            if (StringUtils.isNotEmpty(paramDto.getSubAgyAccount())) {
                AgentAccount agentAccount = new AgentAccount();
                agentAccount.setAgyAccount(paramDto.getSubAgyAccount());
                AgentAccount agentAccount1 = agentAccountMapper.selectOne(agentAccount);
                if (isNull(agentAccount1)) {
                    return null;
                }
                paramDto.setSubAgentId(agentAccount1.getId());
            }
            String time = DateUtil.getLastMonthByTime(paramDto.getStartTime());
            paramDto.setTime(time);
            paramDto.setGroubyAgent(true);
            List<SubAgentListDto> dtoList = teamMapper.subAgentList(paramDto);
            List<Map<String, Object>> list = dtoList.stream().map(e -> {
            	e.setNetwinlose(e.getTotalProfit());

                e.setRegisterTime(exportService.getTime(e.getRegisterTime()));
                Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
                return entityMap;
            }).collect(Collectors.toList());
            String siteCode = CommonUtil.getSiteCode();
            Map<String, Object> map = new HashMap<>(8);
            map.put("mapList", list);
            sysFileExportRecordService.exportExcel(templatePath, map, userId, module, siteCode);//异步执行
        }
        return record;
    }

    public SysFileExportRecord superiorCloneExport(DirectMemberParamDto paramDto, Long userId, String templatePath, String module) {
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);
        if (null != record) {
            List<SubAgentListDto> dtoList = teamMapper.superiorCloneList(paramDto);
            List<Map<String, Object>> list = dtoList.stream().map(e -> {
                Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
                return entityMap;
            }).collect(Collectors.toList());
            String siteCode = CommonUtil.getSiteCode();
            Map<String, Object> map = new HashMap<>(8);
            map.put("mapList", list);
            sysFileExportRecordService.exportExcel(templatePath, map, userId, module, siteCode);//异步执行
        }
        return record;
    }

    public SysFileExportRecord subAgentAccountExport(DirectMemberParamDto paramDto, Long userId, String templatePath, String module) {
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);
        if (null != record) {
            if (isNull(paramDto.getCagencyId())) {
                AgentAccount agentAccount = new AgentAccount();
                agentAccount.setAgyAccount(paramDto.getAgyAccount());
                AgentAccount agentAccount1 = agentAccountMapper.selectOne(agentAccount);
                if (nonNull(agentAccount1) && agentAccount1.getAttributes() == 1) {
                    paramDto.setSubCagencyId(agentAccount1.getId());
                    paramDto.setAgyAccount(null);
                }
            }
            List<DirectMemberDto> dtoList = teamMapper.directMember(paramDto);
            agentTeamService.setDirectMemberDto(dtoList);
            List<Map<String, Object>> list = dtoList.stream().map(e -> {
                e.setLoginTime(exportService.getTime(e.getLoginTime()));
                e.setRegisterTime(exportService.getTime(e.getRegisterTime()));
                Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
                return entityMap;
            }).collect(Collectors.toList());
            String siteCode = CommonUtil.getSiteCode();
            Map<String, Object> map = new HashMap<>(8);
            map.put("mapList", list);
            sysFileExportRecordService.exportExcel(templatePath, map, userId, module, siteCode);//异步执行
        }
        return record;
    }

    public SysFileExportRecord finaceReportExport(ReportParamsDto dto, Long userId, String templatePath, String module) {
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);
        if (null != record) {
            List<ReportResultDto> dtoList = reportMapper.agentFinanceReportList(dto);
            List<Map<String, Object>> list = dtoList.stream().map(e -> {
                Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
                return entityMap;
            }).collect(Collectors.toList());
            String siteCode = CommonUtil.getSiteCode();
            Map<String, Object> map = new HashMap<>(8);
            map.put("mapList", list);
            sysFileExportRecordService.exportExcel(templatePath, map, userId, module, siteCode);//异步执行
        }
        return record;
    }
}
