package com.wsdy.saasops.modules.system.systemsetting.dao;

import com.wsdy.saasops.api.modules.apisys.entity.TcpSiteurl;
import com.wsdy.saasops.modules.system.systemsetting.entity.SysSetting;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import com.wsdy.saasops.modules.system.systemsetting.dto.StationSet;

import java.util.List;

@Component
@Mapper
public interface StationSetMapper {
	
	/**
	 * 查询会员获取数据默认天数
	 * @return
	 */
	StationSet queryConfigDaysAndScope();

	int insertSysSetting(SysSetting sysSetting);

	TcpSiteurl getPromotionUrl();

	int batchInsertSysSetting(List<SysSetting> sysSettingList);

	List<SysSetting> getSysSettingList(List<String> keys);
}
