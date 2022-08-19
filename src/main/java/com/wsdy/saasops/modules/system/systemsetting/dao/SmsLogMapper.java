package com.wsdy.saasops.modules.system.systemsetting.dao;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.system.systemsetting.entity.SmsLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface SmsLogMapper extends MyMapper<SmsLog> {
    List<SmsLog> queryFailLog(SmsLog smslog);

    void updateStatus(SmsLog smslog);

	List<SmsLog> selectList(SmsLog logQuery);
}
