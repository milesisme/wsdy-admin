package com.wsdy.saasops.modules.member.service;


import com.wsdy.saasops.modules.member.entity.MbrSysWarning;
import com.wsdy.saasops.modules.member.mapper.MbrSysWarningMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MbrSysWarningService {

    @Autowired
    private MbrSysWarningMapper mbrSysWarningMapper;

   public  MbrSysWarning getMbrSysWarningByAccountId(Integer accountId){
       return mbrSysWarningMapper.getMbrSysWarningByAccountId(accountId);
    }

  public   int updateChargeLockByAccountId(Integer accountId, Integer status){
        return mbrSysWarningMapper.updateChargeLockByAccountId(accountId, status);
    }

   public  int save(MbrSysWarning mbrSysWarning){
       return mbrSysWarningMapper.insert(mbrSysWarning);
    }

}
