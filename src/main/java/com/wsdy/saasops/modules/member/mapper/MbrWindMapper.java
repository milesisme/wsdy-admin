package com.wsdy.saasops.modules.member.mapper;

import com.wsdy.saasops.modules.member.dto.MbrWindDto;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper
public interface MbrWindMapper {
    List<MbrAccount> getMbrAccountList(MbrAccount mbrAccount);
    MbrAccount mbrInfoByAccount(Integer accountId);
    Integer queryAccountIPNum(@Param("accountId") Integer accountId);
    Integer queryAccountDeviceNum(@Param("accountId") Integer accountId);
    Integer queryAccountIPProfNum(@Param("accountId") Integer accountId);
    Integer queryAccountDeviceProfNum(@Param("accountId") Integer accountId);

    List<MbrWindDto> activitymbrIP(@Param("accountId") Integer accountId);
    List<MbrWindDto> activitymbrDevice(@Param("accountId") Integer accountId);
    List<MbrWindDto> mbrwithIPPrefNum(@Param("accountId") Integer accountId);
    List<MbrWindDto> mbrwithDevicePrefNum(@Param("accountId") Integer accountId);

    List<MbrWindDto> getBonusList(@Param("accountId") Integer accountId);
}
