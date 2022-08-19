package com.wsdy.saasops.modules.agent.service;

import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.modules.agent.dao.AgyDomainMapper;
import com.wsdy.saasops.modules.agent.dto.*;
import com.wsdy.saasops.modules.agent.entity.AgyDomain;
import com.wsdy.saasops.modules.agent.mapper.AgentCommMapper;
import com.wsdy.saasops.modules.member.service.MbrAccountLogService;
import com.wsdy.saasops.modules.operate.entity.TGmDepot;
import com.wsdy.saasops.modules.operate.mapper.OperateMapper;
import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static com.google.common.collect.Lists.newArrayList;

@Service
@Transactional
public class CommissionService {

    @Autowired
    private OperateMapper operateMapper;
    @Autowired
    private SysSettingService settingService;
    @Autowired
    private AgentCommMapper agentCommMapper;
    @Autowired
    private AgyDomainMapper agyDomainMapper;
    @Autowired
    private MbrAccountLogService mbrAccountLogService;


    public List<CommDepotListDto> findDpotList(String siteCode) {
        List<TGmDepot> depotList = operateMapper.findCatOrDepotListBySiteCode(Boolean.FALSE, siteCode);
        if (Collections3.isNotEmpty(depotList)) {
            List<CommDepotListDto> dtoList = newArrayList();
            Map<Integer, List<TGmDepot>> depotGroupingBy =
                    depotList.stream().collect(
                            Collectors.groupingBy(
                                    TGmDepot::getDepotId));
            for (Integer depotIdKey : depotGroupingBy.keySet()) {
                List<TGmDepot> depots = depotGroupingBy.get(depotIdKey);
                CommDepotListDto dto = new CommDepotListDto();
                dto.setDepotId(depotIdKey);
                dto.setDepotCode(depots.get(0).getDepotCode());
                dto.setDepotName(depots.get(0).getDepotName());
                List<CommCatDetailsDto> detailsDtoList = newArrayList();
                depots.stream().forEach(ds -> {
                    CommCatDetailsDto detailsDto = new CommCatDetailsDto();
                    detailsDto.setCatId(ds.getCatId());
                    detailsDto.setCatName(ds.getCatName());
                    detailsDtoList.add(detailsDto);
                });
                dto.setDetailsDtoList(detailsDtoList);
                dtoList.add(dto);
            }
            return dtoList;
        }
        return null;
    }

    public SettingAgentDto agentInfo() {
        SettingAgentDto settingAgentDto = settingService.agentInfo();
        return settingAgentDto;
    }

    public void agentRegister(SettingAgentDto settingAgentDto, String userName, String ip) {
        settingService.agentRegister(settingAgentDto);

        //添加操作日志
        mbrAccountLogService.updateAgentRegisterSetLog(settingAgentDto, userName, ip);
    }


    public PageUtils agentDomainList(AgyDomain agyDomain, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<AgyDomain> agyDomains = agentCommMapper.findAgyDomainList(agyDomain);
        return BeanUtil.toPagedResult(agyDomains);
    }

    public void agentDomainDelete(Integer id, String username) {
        AgyDomain agyDomain = agyDomainMapper.selectByPrimaryKey(id);
        agyDomain.setIsDel(Constants.EVNumber.one);
        agyDomain.setAvailable(Constants.EVNumber.zero);
        agyDomain.setModifyUser(username);
        agyDomain.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        agyDomainMapper.updateByPrimaryKeySelective(agyDomain);
    }

    public void updateDomainAvailable(AgyDomain domain, String username, String ip) {
        AgyDomain agyDomain = agyDomainMapper.selectByPrimaryKey(domain.getId());
        agyDomain.setAvailable(domain.getAvailable());
        agyDomain.setModifyUser(username);
        agyDomain.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        agyDomainMapper.updateByPrimaryKeySelective(agyDomain);

        //添加操作日志
        mbrAccountLogService.updateDomainAvailableLog(domain, username, ip);
    }

    public void updateDomainAudit(AgyDomain domain, String username) {
        AgyDomain agyDomain = agyDomainMapper.selectByPrimaryKey(domain.getId());
        agyDomain.setAvailable(Constants.EVNumber.one);
        agyDomain.setModifyUser(username);
        agyDomain.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        agyDomain.setStatus(domain.getStatus());
        agyDomain.setAvailable(domain.getStatus() == Constants.EVNumber.zero
                ? Constants.EVNumber.zero : Constants.EVNumber.one);
        agyDomainMapper.updateByPrimaryKeySelective(agyDomain);
    }

    public void domainSave(AgyDomain agyDomain, String username) {
        AgyDomain domain = new AgyDomain();
        domain.setIsDel(Constants.EVNumber.zero);
        domain.setDomainUrl(agyDomain.getDomainUrl());
        int count = agyDomainMapper.selectCount(domain);
        if (count > 0) {
            throw new R200Exception("域名已经存在");
        }
        agyDomain.setStatus(Constants.EVNumber.two);
        agyDomain.setAvailable(Constants.EVNumber.one);
        agyDomain.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        agyDomain.setCreateUser(username);
        agyDomain.setIsDel(Constants.EVNumber.zero);
        agyDomainMapper.insert(agyDomain);
    }

    public void domainUpdate(AgyDomain agyDomain, String username) {
        int count = agentCommMapper.findCountDomain(agyDomain.getDomainUrl(), agyDomain.getId());
        if (count > 0) {
            throw new R200Exception("域名已经存在");
        }
        AgyDomain domain = agyDomainMapper.selectByPrimaryKey(agyDomain.getId());
        domain.setDomainUrl(agyDomain.getDomainUrl());
        domain.setMemo(agyDomain.getMemo());
        domain.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        domain.setModifyUser(username);
        agyDomainMapper.updateByPrimaryKeySelective(domain);
    }
}

