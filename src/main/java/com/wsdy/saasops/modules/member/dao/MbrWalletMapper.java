package com.wsdy.saasops.modules.member.dao;

import org.apache.ibatis.annotations.Mapper;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.member.entity.MbrWallet;


@Mapper
public interface MbrWalletMapper extends MyMapper<MbrWallet> {

	int updateAdjustment(MbrWallet mbrWallet);

}
