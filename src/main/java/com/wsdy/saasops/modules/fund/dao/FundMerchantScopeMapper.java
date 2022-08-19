package com.wsdy.saasops.modules.fund.dao;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.fund.entity.FundMerchantScope;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.IdsMapper;


@Component
@Mapper
public interface FundMerchantScopeMapper extends MyMapper<FundMerchantScope>, IdsMapper<FundMerchantScope> {

}
