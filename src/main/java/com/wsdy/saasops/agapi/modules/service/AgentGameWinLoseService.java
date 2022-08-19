package com.wsdy.saasops.agapi.modules.service;

import com.wsdy.saasops.agapi.modules.dto.AgentListDto;
import com.wsdy.saasops.agapi.modules.dto.AgentWinLostReportDto;
import com.wsdy.saasops.agapi.modules.dto.AgentWinLostReportModelDto;
import com.wsdy.saasops.agapi.modules.mapper.AgentWinLoseMapper;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.analysis.dto.WinLostReportDto;
import com.wsdy.saasops.modules.analysis.dto.WinLostReportViewDto;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class AgentGameWinLoseService {

    @Autowired
    private AgentWinLoseMapper winLoseMapper;
    @Autowired
    private MbrMapper mbrMapper;

    public List<AgentWinLostReportDto> findWinLostReportList(AgentWinLostReportModelDto reportModelDto) {
        if (StringUtils.isNotEmpty(reportModelDto.getLoginName())) {
            String agyAccount = mbrMapper.getTagencyNameByName(reportModelDto.getLoginName());
            if(StringUtils.isNotEmpty(agyAccount) && agyAccount.contains(reportModelDto.getAgyAccount())){
                return winLoseMapper.findWinLostReportListByLoginName(reportModelDto);
            }
            return null;
        }
        if (StringUtils.isNotEmpty(reportModelDto.getAgyAccount())) {
            reportModelDto.setIsSign(winLoseMapper.findAgyAccountDepth(reportModelDto.getAgyAccount()));
            reportModelDto.setUsername(reportModelDto.getAgyAccount());
        } else {
            reportModelDto.setUsername(CommonUtil.getSiteCode());
        }
        return winLoseMapper.findWinLostReportList(reportModelDto);
    }

    public AgentWinLostReportDto findWinLostSum(AgentWinLostReportModelDto reportModelDto) {
        if (StringUtils.isNotEmpty(reportModelDto.getLoginName())) {
            String agyAccount = mbrMapper.getTagencyNameByName(reportModelDto.getLoginName());
            if(StringUtils.isNotEmpty(agyAccount) && agyAccount.contains(reportModelDto.getAgyAccount())){
                return winLoseMapper.findWinLostListSumByLoginName(reportModelDto);
            }
            return null;
        }
        return winLoseMapper.findWinLostSum(reportModelDto);
    }

    public AgentWinLostReportDto findWinLostLoginName(AgentWinLostReportModelDto reportModelDto, AgentAccount agentAccount) {
        if (StringUtils.isNotEmpty(reportModelDto.getLoginName())) {
            String agyAccountTemp = mbrMapper.getTagencyNameByName(reportModelDto.getLoginName());
            if(StringUtils.isEmpty(agyAccountTemp) || !agyAccountTemp.contains(agentAccount.getAgyAccount())){
                return null;
            }
        }

        List<AgentWinLostReportDto> reportDtos = winLoseMapper.findWinLostLoginName(reportModelDto);
        AgentWinLostReportDto reportDto = new AgentWinLostReportDto();
        reportDto.setTotal((long) Constants.EVNumber.one);
        reportDto.setUsername(reportModelDto.getLoginName());
        reportDto.setLevel("account");
        if (reportDtos.size() == 0 || Objects.isNull(reportDtos.get(0))) {
            reportDto.setTotal((long) Constants.EVNumber.zero);
        } else {
            reportDto.setBetTotal(reportDtos.get(0).getBetTotal());
            reportDto.setValidbetTotal(reportDtos.get(0).getValidbetTotal());
            reportDto.setPayoutTotal(reportDtos.get(0).getPayoutTotal());
        }
        return reportDto;
    }

    public PageUtils findWinLostListLevel(AgentWinLostReportModelDto reportModelDto, Integer pageNo, Integer pageSize, AgentAccount agentAccount) {
        if (StringUtils.isNotEmpty(reportModelDto.getLoginName())) {
            String agyAccountTemp = mbrMapper.getTagencyNameByName(reportModelDto.getLoginName());
            if(StringUtils.isEmpty(agyAccountTemp) || !agyAccountTemp.contains(agentAccount.getAgyAccount())){
                return null;
            }
        }

        if (StringUtils.isNotEmpty(reportModelDto.getAgyAccount())) {
            return findWinLostListLevelByAgyAccount(reportModelDto, pageNo, pageSize);
        }
        PageHelper.startPage(pageNo, pageSize);
        List<AgentWinLostReportDto> reportDtos = winLoseMapper.findWinLostListLevelLoginName(reportModelDto);
        return BeanUtil.toPagedResult(reportDtos);
    }

    private PageUtils findWinLostListLevelByAgyAccount(AgentWinLostReportModelDto reportModelDto, Integer pageNo, Integer pageSize) {
        List<AgentWinLostReportDto> reportDtos;
        Integer count = winLoseMapper.findAgyAccountDepthLevel(reportModelDto.getAgyAccount());
        PageHelper.startPage(pageNo, pageSize);
        if (count > 0) {
            reportDtos = winLoseMapper.findWinLostListLevelAgyAccount(reportModelDto);
        } else {
            reportDtos = winLoseMapper.findAgyAccountLevelLoginName(reportModelDto);
        }
        return BeanUtil.toPagedResult(reportDtos);
    }

    public AgentWinLostReportDto findWinLostListSumLoginName(AgentWinLostReportModelDto reportModelDto) {
        return winLoseMapper.findWinLostListSumLoginName(reportModelDto);
    }

    public PageUtils findWinLostLoginNameList(AgentWinLostReportModelDto reportModelDto, Integer pageNo, Integer pageSize, AgentAccount agentAccount) {
        PageHelper.startPage(pageNo, pageSize);
        reportModelDto.setIsGroup(Boolean.TRUE);
        if (StringUtils.isNotEmpty(reportModelDto.getLoginName())) {
            String agyAccountTemp = mbrMapper.getTagencyNameByName(reportModelDto.getLoginName());
            if(StringUtils.isEmpty(agyAccountTemp) || !agyAccountTemp.contains(agentAccount.getAgyAccount())){
                return null;
            }
        }

        List<AgentWinLostReportDto> reportDtos = winLoseMapper.findWinLostLoginName(reportModelDto);
        if (Collections3.isNotEmpty(reportDtos)) {
            reportDtos.forEach(cs -> {
                cs.setLevel("account");
                cs.setTotal((long) Constants.EVNumber.one);
            });
        }
        return BeanUtil.toPagedResult(reportDtos);
    }

    public List<AgentWinLostReportDto> findWinLostAccount(AgentWinLostReportModelDto reportModelDto) {
        return winLoseMapper.findWinLostAccount(reportModelDto);
    }

   public  List<AgentListDto>  selectAgentByParentIdList(Integer agyAccountId){
        List<AgentListDto> agyAccountList =winLoseMapper.selectAgentByParentIdList(agyAccountId);
       return agyAccountList;
    }

   public   List<String>selectMbrAccountByAgrIdAndLoginName( Integer agyAccountId, String loginName){
        return winLoseMapper.selectMbrAccountByAgrIdAndLoginName(agyAccountId,loginName);
    }

    public WinLostReportViewDto findWinLostReportView(AgentWinLostReportModelDto reportModelDto) {
        WinLostReportViewDto ret = new WinLostReportViewDto();

        // 下级代理
        reportModelDto.setIsSign(String.valueOf(Constants.EVNumber.one));
        WinLostReportDto sub = winLoseMapper.findWinLostReportView(reportModelDto);
        ret.setSubordinateAgent(sub);
        // 直属会员
        reportModelDto.setIsSign(String.valueOf(Constants.EVNumber.two));
        WinLostReportDto account = winLoseMapper.findWinLostReportView(reportModelDto);
        ret.setDirectAccount(account);
        // 所有下级
        reportModelDto.setIsSign(String.valueOf(Constants.EVNumber.three));
        WinLostReportDto all = winLoseMapper.findWinLostReportView(reportModelDto);
        ret.setAllSubordinates(all);
        return ret;
    }

    public PageUtils findWinLostReportViewAgent(AgentWinLostReportModelDto reportModelDto, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<WinLostReportDto> reportDtos =  winLoseMapper.findWinLostReportViewAgent(reportModelDto);
        if(StringUtil.isNotEmpty(reportModelDto.getOrderBy())){
            PageHelper.orderBy(reportModelDto.getOrderBy());
        }
        return BeanUtil.toPagedResult(reportDtos);
    }

}
