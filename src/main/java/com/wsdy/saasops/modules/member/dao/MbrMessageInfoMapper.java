package com.wsdy.saasops.modules.member.dao;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.member.entity.MbrMessage;
import com.wsdy.saasops.modules.member.entity.MbrMessageInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MbrMessageInfoMapper extends MyMapper<MbrMessageInfo>{
    // 查询指定会员指定类型的消息
    List<MbrMessageInfo>  selectMbrMessageInfo(MbrMessageInfo mbrMessageInfo);

    List<MbrMessageInfo> fineMbrMessageV2Info(MbrMessageInfo mbrMessageInfo);

    // 更新管家消息状态
    int updateMessageList(MbrMessageInfo info);

    int setMessageMbrRead(MbrMessageInfo info);

    int batchInsertMbrMessage(@Param("groups") List<MbrMessage> groups);
    int batchUpdateMbrMessage(MbrMessage mbrMessage);
    int batchInsertMbrMessageInfo(@Param("groups") List<MbrMessageInfo> groups);

    int messageDeleteExpiration();
}
