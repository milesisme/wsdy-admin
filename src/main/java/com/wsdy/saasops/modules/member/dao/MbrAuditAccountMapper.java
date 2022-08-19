package com.wsdy.saasops.modules.member.dao;

import com.wsdy.saasops.modules.fund.entity.AccWithdraw;
import com.wsdy.saasops.modules.member.entity.MbrAuditAccount;
import org.apache.ibatis.annotations.Mapper;
import com.wsdy.saasops.modules.base.mapper.MyMapper;
import tk.mybatis.mapper.common.IdsMapper;


@Mapper
public interface MbrAuditAccountMapper extends MyMapper<MbrAuditAccount>,IdsMapper<AccWithdraw> {

}
