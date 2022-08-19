package com.wsdy.saasops.modules.fund.dao;

import com.wsdy.saasops.modules.fund.entity.FundDeposit;
import org.apache.ibatis.annotations.Mapper;
import com.wsdy.saasops.modules.base.mapper.MyMapper;
import org.springframework.stereotype.Component;


@Component
@Mapper
public interface FundDepositMapper extends MyMapper<FundDeposit> {

}
