package com.wsdy.saasops.modules.analysis.service;

import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.modules.analysis.dto.WinLostReportDto;
import com.wsdy.saasops.modules.analysis.dto.WinLostReportModelDto;
import com.wsdy.saasops.modules.analysis.mapper.WinLoseMapper;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import com.wsdy.saasops.modules.sys.service.SysFileExportRecordService;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class GameWinLoseService {

    @Autowired
    private WinLoseMapper winLoseMapper;
    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;
    @Autowired
    private AnalysisService analysisService;

    public List<WinLostReportDto> findWinLostReportList(WinLostReportModelDto reportModelDto) {
        // 精确查询会员 表头：合计自己和下级的汇总
        if (StringUtils.isNotEmpty(reportModelDto.getLoginName())) {
            return winLoseMapper.findWinLostReportListByLoginName(reportModelDto);
        }

        // 处理参数
        // 非股东
        if (StringUtils.isNotEmpty(reportModelDto.getAgyAccount())) {
            // 级别 isSign 表头： null股东 0总代 >=1 代理 account会员 4仅查询会员可使用  视图： 1 2 3
            String isSign = winLoseMapper.findAgyAccountDepth(reportModelDto.getAgyAccount());
            reportModelDto.setIsSign(isSign);
            reportModelDto.setUsername(reportModelDto.getAgyAccount()); // 非股东账号列：代理名
        } else {    // 股东 isSign 为null
            reportModelDto.setUsername(CommonUtil.getSiteCode());       // 股东账号列：siteCode
        }
        // 代理表头查询
        return winLoseMapper.findWinLostReportList(reportModelDto);
    }

    public WinLostReportDto findWinLostSum(WinLostReportModelDto reportModelDto) {
        // 会员类别汇总
        if (StringUtils.isNotEmpty(reportModelDto.getLoginName())) {
            return winLoseMapper.findWinLostListSumByLoginName(reportModelDto);
        }
        // 代理类别汇总
        return winLoseMapper.findWinLostSum(reportModelDto);
    }

    public WinLostReportDto findWinLostLoginName(WinLostReportModelDto reportModelDto) {
        // 会员-只查询自己
        List<WinLostReportDto> reportDtos = winLoseMapper.findWinLostLoginName(reportModelDto);
        WinLostReportDto reportDto = new WinLostReportDto();
        reportDto.setTotal((long) Constants.EVNumber.one);
        reportDto.setUsername(reportModelDto.getLoginName());
        reportDto.setLevel("account");
        if (reportDtos.size() == 0 || Objects.isNull(reportDtos.get(0))) {
            reportDto.setTotal((long) Constants.EVNumber.zero);
        } else {
            reportDto.setBetTotal(reportDtos.get(0).getBetTotal());
            reportDto.setValidbetTotal(reportDtos.get(0).getValidbetTotal());
            reportDto.setPayoutTotal(reportDtos.get(0).getPayoutTotal());
            reportDto.setQuantity(reportDtos.get(0).getQuantity());
        }
        return reportDto;
    }

    public PageUtils findWinLostListLevel(WinLostReportModelDto reportModelDto, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);

        // 查询代理下级-会员
        if (StringUtils.isNotEmpty(reportModelDto.getAgyAccount())) {
            List<WinLostReportDto> reportDtos = winLoseMapper.findAgyAccountLevelLoginName(reportModelDto);
            return BeanUtil.toPagedResult(reportDtos);
        }

        // 查询会员下级
        if(StringUtil.isNotEmpty(reportModelDto.getOrderBy())){
            PageHelper.orderBy(reportModelDto.getOrderBy());
        }
        List<WinLostReportDto> reportDtos = winLoseMapper.findWinLostListLevelLoginName(reportModelDto);
        return BeanUtil.toPagedResult(reportDtos);
    }


    public WinLostReportDto findWinLostListSumLoginName(WinLostReportModelDto reportModelDto) {
        return winLoseMapper.findWinLostListSumLoginName(reportModelDto);
    }

    public PageUtils findWinLostLoginNameList(WinLostReportModelDto reportModelDto, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        if(StringUtil.isNotEmpty(reportModelDto.getOrderBy())){
            PageHelper.orderBy(reportModelDto.getOrderBy());
        }
        reportModelDto.setIsGroup(Boolean.TRUE);
        List<WinLostReportDto> reportDtos = winLoseMapper.findWinLostLoginName(reportModelDto);

        // 前端的账号列和个数列处理
        if (Collections3.isNotEmpty(reportDtos)) {
            reportDtos.forEach(cs -> {
                cs.setLevel("account");
                cs.setTotal((long) Constants.EVNumber.one);
            });
        }
        return BeanUtil.toPagedResult(reportDtos);
    }

    public List<WinLostReportDto> findWinLostAccount(WinLostReportModelDto reportModelDto) {
        if(StringUtil.isNotEmpty(reportModelDto.getOrderBy())){
            PageHelper.orderBy(reportModelDto.getOrderBy());
        }
        return winLoseMapper.findWinLostAccount(reportModelDto);
    }

    public PageUtils findWinLostAccountPage(WinLostReportModelDto reportModelDto,Integer pageNo,Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        if(StringUtil.isNotEmpty(reportModelDto.getOrderBy())){
            PageHelper.orderBy(reportModelDto.getOrderBy());
        }
        List<WinLostReportDto> reportDtos = winLoseMapper.findWinLostAccount(reportModelDto);
        return BeanUtil.toPagedResult(reportDtos);
    }

    public SysFileExportRecord exportMbrWinLoseList(WinLostReportModelDto model, Long userId, String module, String templatePath){
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);

        if(null != record) {
            List<Map<String,Object>> list = winLoseMapper.findMbrWinLoseList(model);
            String siteCode = CommonUtil.getSiteCode();
            String lastBetTime="";
            try {
                lastBetTime = analysisService.getBetLastDate(siteCode);
            } catch (Exception e) {
                log.error(siteCode+"查询最后投注时间失败",e);
            }
            Map<String,Object> map = new HashMap<>(8);
            map.put("startTime",model.getStartTime());
            map.put("endTime",model.getEndTime());
            map.put("updateTime",lastBetTime);
            map.put("mapList",list);
            sysFileExportRecordService.exportExcel(templatePath,map,userId,module,siteCode);//异步执行
        }
        return record;

    }
}
