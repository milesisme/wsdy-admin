package com.wsdy.saasops.modules.log.service;

import java.util.List;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.modules.member.entity.MbrAccountDevice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.log.dao.LogMbrloginMapper;
import com.wsdy.saasops.modules.log.entity.LogMbrLogin;
import com.wsdy.saasops.modules.log.mapper.LogMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.service.IpService;
import com.github.pagehelper.PageHelper;

@Slf4j
@Service
public class LogMbrloginService extends BaseService<LogMbrloginMapper, LogMbrLogin> {
    @Autowired
    private LogMapper logMapper;
    @Autowired
    private IpService ipService;

    public PageUtils queryListPage(LogMbrLogin logMbrlogin, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        PageHelper.orderBy(" loginTime desc");
        List<LogMbrLogin> list = queryListCond(logMbrlogin);
        list.forEach(log -> {
            if (!StringUtils.isEmpty(log.getOnlineTime())) {
                log.setOnlineTimeStr(StringUtil.formatOnlineTime(log.getOnlineTime()));
            } else {
                log.setOnlineTimeStr(Constants.SYSTEM_NONE);
            }
        });
        return BeanUtil.toPagedResult(list);
    }

    public LogMbrLogin findMemberLoginLastOne(MbrAccount mbrAccount) {
        return logMapper.findMemberLoginLastOne(mbrAccount.getLoginName());
    }

    public PageUtils findLogMemberLoginLastOne(LogMbrLogin logMbrlogin, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        PageHelper.orderBy(" loginTime desc");
        List<LogMbrLogin> list = queryListCond(logMbrlogin);
        return BeanUtil.toPagedResult(list);
    }


    public LogMbrLogin saveLoginLog(MbrAccount entity) {
        LogMbrLogin logMbrLogin = new LogMbrLogin();
        logMbrLogin.setAccountId(entity.getId());
        logMbrLogin.setLoginName(entity.getLoginName());
        logMbrLogin.setLoginIp(entity.getLoginIp());
        logMbrLogin.setLoginUrl(entity.getRegisterUrl());
        logMbrLogin.setCheckip(entity.getCheckip());
        logMbrLogin.setLoginTime(entity.getLoginTime());
        logMbrLogin.setLoginType(entity.getLoginType());
        String ip = logMbrLogin.getLoginIp();
        if (StringUtils.isEmpty(ip) && ip.length() > 15) {
            if (ip.indexOf(",") > 0) {
                ip = ip.substring(0, ip.indexOf(","));
            }
        }
        logMbrLogin.setLoginArea(ipService.getIpArea(ip));
        save(logMbrLogin);
        return logMbrLogin;
    }

    public void setLoginOffTime(String loginName) {
        LogMbrLogin logMbrLogin = logMapper.findMemberLoginLastOne(loginName);
        if (logMbrLogin != null && StringUtils.isEmpty(logMbrLogin.getLogoutTime())) {
            logMapper.updateLoginTime(logMbrLogin.getId());
        }
    }

    public void  updateLastLoginDeviceUuid(MbrAccountDevice qryDto){
        LogMbrLogin logMbrLogin = logMapper.findMemberLoginLastOne(qryDto.getLoginName());
        if (logMbrLogin != null && StringUtils.isEmpty(logMbrLogin.getDeviceUuid())) {
            logMbrLogin.setDeviceUuid(qryDto.getDeviceUuid());
            super.update(logMbrLogin);
        }
    }
}
