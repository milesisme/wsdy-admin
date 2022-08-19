package com.wsdy.saasops.modules.member.dao;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccountDevice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper
public interface MbrAccountDeviceMapper extends MyMapper<MbrAccountDevice>{
    List<MbrAccountDevice> getSameDeviceMbrList(@Param("num") int num);

    void batchUpdateMbrGroup(@Param("groups") List<Integer> groups,@Param("groupId") Integer groupId);
    void batchUpdateMbrDevice(@Param("groups") List<Integer> groups);
    Integer getCountByLoginNameAndDevice(@Param("loginName")String loginName,@Param("device")String device);

}
