package com.wsdy.saasops.modules.member.dao;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.member.entity.MbrDepositLockLog;
import com.wsdy.saasops.modules.member.entity.MbrFriendTransDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.IdsMapper;

import java.util.List;


@Mapper
public interface MbrDepositLockLogMapper extends MyMapper<MbrDepositLockLog> {

    List<MbrDepositLockLog> listPage(MbrDepositLockLog entity);

    List<MbrDepositLockLog> listDepositLockLog(@Param("accountId")Integer accountId, @Param("startTime") String startTime,
                                               @Param("endTime")String endTime);

    List<MbrDepositLockLog> listLock(MbrDepositLockLog entity);

    MbrDepositLockLog getLastLock(MbrDepositLockLog entity);

    List<MbrDepositLockLog> getLock(@Param("accountId")Integer accountId);

    List<MbrDepositLockLog> getUnfinishLockLog(@Param("accountId")Integer accountId);

    Integer getAllUnpayOrder(@Param("accountId")Integer accountId, @Param("startTime")String startTime,
                                   @Param("endTime")String endTime);

    Integer getUnpayCompanyidOrder(@Param("accountId")Integer accountId, @Param("startTime")String startTime,
                                   @Param("endTime")String endTime);


    Integer getUnpayOnlinepayidOrder(@Param("accountId")Integer accountId, @Param("startTime")String startTime,
                                   @Param("endTime")String endTime, @Param("payIds") List<Integer> payIds);
}
