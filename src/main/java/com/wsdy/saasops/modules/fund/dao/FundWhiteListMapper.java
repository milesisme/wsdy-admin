package com.wsdy.saasops.modules.fund.dao;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.fund.entity.FundWhiteList;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;


@Component
@Mapper
public interface FundWhiteListMapper extends MyMapper<FundWhiteList> {

}
