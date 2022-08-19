package com.wsdy.saasops.modules.analysis.service;

import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.utils.StringUtil;
import com.wsdy.saasops.modules.analysis.dto.*;
import com.wsdy.saasops.modules.analysis.mapper.WinLoseMapper;
import com.wsdy.saasops.modules.analysis.mapper.WinLoseNewMapper;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import com.wsdy.saasops.modules.sys.service.SysFileExportRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GameWinLoseNewService {

    @Autowired
    private WinLoseNewMapper winLoseNewMapper;

    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;

    @Autowired
    private GameWinLoseService winLoseService;

    @Autowired
    private WinLoseMapper winLoseMapper;

    public WinLostReportViewDto findWinLostReportView(WinLostReportModelDto reportModelDto) {
        WinLostReportViewDto ret = new WinLostReportViewDto();

        // 下级代理
        reportModelDto.setIsSign(String.valueOf(Constants.EVNumber.one));
        WinLostReportDto sub = winLoseNewMapper.findWinLostReportView(reportModelDto);
        ret.setSubordinateAgent(sub);
        // 直属会员
        reportModelDto.setIsSign(String.valueOf(Constants.EVNumber.two));
        WinLostReportDto account = winLoseNewMapper.findWinLostReportView(reportModelDto);
        ret.setDirectAccount(account);
        // 所有下级
        reportModelDto.setIsSign(String.valueOf(Constants.EVNumber.three));
        WinLostReportDto all = winLoseNewMapper.findWinLostReportView(reportModelDto);
        ret.setAllSubordinates(all);
        return ret;
    }

    public PageUtils findWinLostReportViewAgent(WinLostReportModelDto reportModelDto, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<WinLostReportDto> reportDtos =  winLoseNewMapper.findWinLostReportViewAgent(reportModelDto);
        if(StringUtil.isNotEmpty(reportModelDto.getOrderBy())){
            PageHelper.orderBy(reportModelDto.getOrderBy());
        }
        return BeanUtil.toPagedResult(reportDtos);
    }

    public WinLostReportViewDto findWinLostReportMbrView(WinLostReportModelDto reportModelDto) {
        WinLostReportViewDto ret = new WinLostReportViewDto();

        // 下级代理会员
        reportModelDto.setIsSign(String.valueOf(Constants.EVNumber.one));
        WinLostReportDto sub = winLoseNewMapper.findWinLostReportMbrView(reportModelDto);
        ret.setSubordinateAgent(sub);
        // 下级非代理会员
        reportModelDto.setIsSign(String.valueOf(Constants.EVNumber.two));
        WinLostReportDto account = winLoseNewMapper.findWinLostReportMbrView(reportModelDto);
        ret.setDirectAccount(account);
        // 所有下级
        reportModelDto.setIsSign(String.valueOf(Constants.EVNumber.three));
        WinLostReportDto all = winLoseNewMapper.findWinLostReportMbrView(reportModelDto);
        ret.setAllSubordinates(all);
        return ret;
    }



    public SysFileExportRecord exportAccountWinLostReport(WinLostReportModelDto reportModelDto, Long userId, String module) {
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);
        if (null != record) {
            String siteCode = CommonUtil.getSiteCode();
            List<Map<String, Object>> sheetsList = new ArrayList<>();

            // 导出玩家
            List<WinLostReportDto> winLostReportDtoList1 =  winLoseService.findWinLostReportList(reportModelDto);
            List<SelfWinLostReportExcelDto> selfData = new ArrayList<>();

            for (WinLostReportDto winLostReportDto : winLostReportDtoList1){
                SelfWinLostReportExcelDto selfWinLostReportExcelDto = new SelfWinLostReportExcelDto();
                BeanUtils.copyProperties(winLostReportDto, selfWinLostReportExcelDto);
                if(winLostReportDto.getLevel()== null){
                    selfWinLostReportExcelDto.setLevelName("股东");
                }else if(winLostReportDto.getLevel().equals("0")){
                    selfWinLostReportExcelDto.setLevelName("总代");
                }else if(winLostReportDto.getLevel().equals("account")){
                    selfWinLostReportExcelDto.setLevelName("会员");
                }else if(winLostReportDto.getLevel().equals("agentMbr")){
                    selfWinLostReportExcelDto.setLevelName("代理会员");
                }else {
                    selfWinLostReportExcelDto.setLevelName("代理");
                }
                double rt = selfWinLostReportExcelDto.getPayoutTotal().doubleValue()* 100 / selfWinLostReportExcelDto.getValidbetTotal().doubleValue();
                selfWinLostReportExcelDto.setRate(String.format("%.2f", rt) + "%");

                selfData.add(selfWinLostReportExcelDto);
            }

            ExportParams exportParams2 = new ExportParams();
            exportParams2.setSheetName("自身及下级输赢总数据");
            Map<String, Object> map2 = new HashMap<>();
            map2.put("data", selfData);
            map2.put("entity", SelfWinLostReportExcelDto.class);
            map2.put("title", exportParams2);
            sheetsList.add(map2);
            //会员导出

            if(StringUtil.isNotEmpty(reportModelDto.getOrderBy())){
                PageHelper.orderBy(reportModelDto.getOrderBy());
            }
            reportModelDto.setIsGroup(Boolean.TRUE);
            List<WinLostReportDto> winLostReportDtoList = winLoseMapper.findWinLostLoginName(reportModelDto);
            List<AccountWinLostReportExcelDto> data = new ArrayList<>();
            for (WinLostReportDto winLostReportDto : winLostReportDtoList){
                AccountWinLostReportExcelDto accountWinLostReportExcelDto = new AccountWinLostReportExcelDto();
                    BeanUtils.copyProperties(winLostReportDto, accountWinLostReportExcelDto);
                    accountWinLostReportExcelDto.setLevelName("会员");
                    accountWinLostReportExcelDto.setTotal(1L);
                    double rt = accountWinLostReportExcelDto.getPayoutTotal().doubleValue() * 100 / accountWinLostReportExcelDto.getValidbetTotal().doubleValue();
                    accountWinLostReportExcelDto.setRate(String.format("%.2f", rt) + "%");

                data.add(accountWinLostReportExcelDto);
            }

            ExportParams exportParams = new ExportParams();
            exportParams.setSheetName("会员数");
            Map<String, Object> map = new HashMap<>();
            map.put("data", data);
            map.put("entity", AccountWinLostReportExcelDto.class);
            map.put("title", exportParams);
            sheetsList.add(map);

            sysFileExportRecordService.exportMilSheet(sheetsList,  userId,  module,  siteCode);
        }
        return record;
    }


    public SysFileExportRecord exportWinLostReport(WinLostReportModelDto reportModelDto, Long userId, String module) {
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);
        if (null != record) {
            String siteCode = CommonUtil.getSiteCode();
            List<Map<String, Object>> sheetsList = new ArrayList<>();

            // 导出玩家
            List<WinLostReportDto> winLostReportDtoList1 =  winLoseService.findWinLostReportList(reportModelDto);
            List<SelfWinLostReportExcelDto> selfData = new ArrayList<>();

            for (WinLostReportDto winLostReportDto : winLostReportDtoList1){
                SelfWinLostReportExcelDto selfWinLostReportExcelDto = new SelfWinLostReportExcelDto();
                BeanUtils.copyProperties(winLostReportDto, selfWinLostReportExcelDto);
                if(winLostReportDto.getLevel()== null){
                    selfWinLostReportExcelDto.setLevelName("股东");
                }else if(winLostReportDto.getLevel().equals("0")){
                    selfWinLostReportExcelDto.setLevelName("总代");
                }else if(winLostReportDto.getLevel().equals("account")){
                    selfWinLostReportExcelDto.setLevelName("会员");
                }else if(winLostReportDto.getLevel().equals("agentMbr")){
                    selfWinLostReportExcelDto.setLevelName("代理会员");
                }else {
                    selfWinLostReportExcelDto.setLevelName("代理");
                }
                double rt = selfWinLostReportExcelDto.getPayoutTotal().doubleValue()* 100 / selfWinLostReportExcelDto.getValidbetTotal().doubleValue();
                selfWinLostReportExcelDto.setRate(String.format("%.2f", rt) + "%");

                selfData.add(selfWinLostReportExcelDto);
            }

            ExportParams exportParams2 = new ExportParams();
            exportParams2.setSheetName("自身及下级输赢总数据");
            Map<String, Object> map2 = new HashMap<>();
            map2.put("data", selfData);
            map2.put("entity", SelfWinLostReportExcelDto.class);
            map2.put("title", exportParams2);
            sheetsList.add(map2);
            //导出代理
            List<WinLostReportDto> winLostReportDtoList =  winLoseNewMapper.findWinLostReportViewAgent(reportModelDto);
            List<AgentWinLostReportExcelDto> data = new ArrayList<>();
            for (WinLostReportDto winLostReportDto : winLostReportDtoList){
                AgentWinLostReportExcelDto agentWinLostReportExcelDto = new AgentWinLostReportExcelDto();
                BeanUtils.copyProperties(winLostReportDto, agentWinLostReportExcelDto);
                if(winLostReportDto.getLevel()== null){
                    agentWinLostReportExcelDto.setLevelName("股东");
                }else if(winLostReportDto.getLevel().equals("0")){
                    agentWinLostReportExcelDto.setLevelName("总代");
                }else if(winLostReportDto.getLevel().equals("account")){
                    agentWinLostReportExcelDto.setLevelName("会员");
                }else if(winLostReportDto.getLevel().equals("agentMbr")){
                    agentWinLostReportExcelDto.setLevelName("代理会员");
                }else{
                    agentWinLostReportExcelDto.setLevelName("代理");
                }
                double rt = agentWinLostReportExcelDto.getPayoutTotal().doubleValue() * 100 / agentWinLostReportExcelDto.getValidbetTotal().doubleValue();
                agentWinLostReportExcelDto.setRate(String.format("%.2f", rt) + "%");

                data.add(agentWinLostReportExcelDto);
            }

            ExportParams exportParams = new ExportParams();
            exportParams.setSheetName("总代数");
            Map<String, Object> map = new HashMap<>();
            map.put("data", data);
            map.put("entity", AgentWinLostReportExcelDto.class);
            map.put("title", exportParams);
            sheetsList.add(map);

            sysFileExportRecordService.exportMilSheet(sheetsList,  userId,  module,  siteCode);
        }
        return record;
    }




}
