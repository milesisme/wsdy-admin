package com.wsdy.saasops.modules.member.mapper;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.member.entity.MbrSysWarning;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MbrSysWarningMapper extends MyMapper<MbrSysWarning> {

    MbrSysWarning getMbrSysWarningByAccountId(Integer accountId);

    int updateChargeLockByAccountId(Integer accountId, Integer status);

}
