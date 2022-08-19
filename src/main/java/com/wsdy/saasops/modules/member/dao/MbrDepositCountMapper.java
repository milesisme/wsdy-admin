package com.wsdy.saasops.modules.member.dao;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.member.entity.MbrDepositCount;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface MbrDepositCountMapper extends MyMapper<MbrDepositCount> {
    Integer updateCount(MbrDepositCount count);
    Integer resetDepositLockNum(MbrDepositCount count);
}
