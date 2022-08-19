package com.wsdy.saasops.agapi.modulesV2.service;

import com.wsdy.saasops.agapi.modulesV2.dao.LogAgyloginMapper;
import com.wsdy.saasops.agapi.modulesV2.dto.AgentV2LoginLogDto;
import com.wsdy.saasops.agapi.modulesV2.entity.LogAgyLogin;
import com.wsdy.saasops.agapi.modulesV2.mapper.AgentV2LoginLogMapper;
import com.wsdy.saasops.agapi.modulesV2.mapper.AgentV2WinLoseMapper;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.member.service.IpService;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import com.wsdy.saasops.modules.sys.service.SysFileExportRecordService;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AgentV2LoginLogService extends BaseService<LogAgyloginMapper, LogAgyLogin> {
    @Autowired
    private AgentV2LoginLogMapper aentV2LoginLogMapper;
    @Autowired
    private IpService ipService;
    @Autowired
    private AgentV2WinLoseMapper winLoseMapper;
    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;
    @Autowired
    private JsonUtil jsonUtil;

    public void saveLoginLog(AgentAccount entity) {
        LogAgyLogin logAgyLogin = new LogAgyLogin();
        logAgyLogin.setAccountId(entity.getId());
        logAgyLogin.setLoginName(entity.getAgyAccount());
        logAgyLogin.setLoginIp(entity.getLoginIp());
        logAgyLogin.setLoginUrl(entity.getRegisterUrl());
        logAgyLogin.setLoginTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        String ip = logAgyLogin.getLoginIp();
        if (StringUtils.isEmpty(ip) && ip.length() > 15) {
            if (ip.indexOf(",") > 0) {
                ip = ip.substring(0, ip.indexOf(","));
            }
        }
        logAgyLogin.setLoginArea(ipService.getIpArea(ip));
        save(logAgyLogin);
    }

    public void setLoginOffTime(String loginName) {
        LogAgyLogin logAgyLogin = aentV2LoginLogMapper.findMemberLoginLastOne(loginName);
        if (logAgyLogin != null && StringUtils.isEmpty(logAgyLogin.getLogoutTime())) {
            aentV2LoginLogMapper.updateLoginTime(logAgyLogin.getId());
        }
    }


    public List<AgentV2LoginLogDto> getLoginLogList(AgentV2LoginLogDto agentV2LoginLogDto){
        // 查询数据
        List<AgentV2LoginLogDto> list = new ArrayList<>();
        if("agent".equals(agentV2LoginLogDto.getUserType())){       // 查询代理
            list = aentV2LoginLogMapper.getLoginLogListAgent(agentV2LoginLogDto);
        }else if("mbr".equals(agentV2LoginLogDto.getUserType())) {  // 查询会员
            list = aentV2LoginLogMapper.getLoginLogListMbr(agentV2LoginLogDto);
        }
        if(Objects.isNull(list)){
            return new ArrayList<>();
        }
        return list;
    }

    public PageUtils getLoginLogListPage(AgentV2LoginLogDto agentV2LoginLogDto){
        PageHelper.startPage(agentV2LoginLogDto.getPageNo(), agentV2LoginLogDto.getPageSize());
        // 查询数据
        List<AgentV2LoginLogDto> list = getLoginLogList(agentV2LoginLogDto);
        return BeanUtil.toPagedResult(list);
    }

    public AgentV2LoginLogDto getWinLosePayout(AgentV2LoginLogDto agentV2LoginLogDto) {
        AgentV2LoginLogDto returnDto = new AgentV2LoginLogDto();
        if("agent".equals(agentV2LoginLogDto.getUserType())){       // 查询代理
            returnDto = winLoseMapper.getWinLosePayoutAgent(agentV2LoginLogDto);
        }else if("mbr".equals(agentV2LoginLogDto.getUserType())) {  // 查询会员
            returnDto = winLoseMapper.getWinLosePayoutMbr(agentV2LoginLogDto);
        }
        return returnDto;
    }

    public SysFileExportRecord exportLoginLogList(AgentV2LoginLogDto agentV2LoginLogDto, Long userId, String module, String templatePath){
        // 生成文件导出记录
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);

        if (null != record) {
            List<AgentV2LoginLogDto>  dtoList =  getLoginLogList(agentV2LoginLogDto);
//            if (dtoList.size() > 10000) {
//                throw new R200Exception("导出数量超过1W条，请更新搜索条件后再进行导出！");
//            }
            List<Map<String, Object>> list = dtoList.stream().map(e -> {
                Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
                return entityMap;
            }).collect(Collectors.toList());
            String siteCode = CommonUtil.getSiteCode();

            Map<String,Object> map = new HashMap<>(8);
            map.put("startTime",agentV2LoginLogDto.getStartTime());
            map.put("endTime",agentV2LoginLogDto.getEndTime());
            map.put("mapList",list);

            sysFileExportRecordService.exportExcel(templatePath,map,userId,module,siteCode);//异步执行
        }
        return record;
    }
}
