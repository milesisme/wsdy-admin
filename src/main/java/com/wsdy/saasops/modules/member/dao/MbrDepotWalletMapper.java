package com.wsdy.saasops.modules.member.dao;

import org.apache.ibatis.annotations.Mapper;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.member.entity.MbrDepotWallet;

import tk.mybatis.mapper.common.IdsMapper;


@Mapper
public interface MbrDepotWalletMapper extends MyMapper<MbrDepotWallet>,IdsMapper<MbrDepotWallet> {

}
