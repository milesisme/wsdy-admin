package com.wsdy.saasops.modules.system.systemsetting.dao;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.system.systemsetting.entity.SmsConfig;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface SmsConfigMapper extends MyMapper<SmsConfig> {
    List<SmsConfig> querySmsConfig(SmsConfig smsConfig);
}
