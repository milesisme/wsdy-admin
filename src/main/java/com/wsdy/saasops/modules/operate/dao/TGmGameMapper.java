package com.wsdy.saasops.modules.operate.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.operate.entity.TGmCat;
import com.wsdy.saasops.modules.operate.entity.TGmDepot;
import com.wsdy.saasops.modules.operate.entity.TGmGame;


@Mapper
public interface TGmGameMapper extends MyMapper<TGmGame> {

	List<TGmCat> queryCatList(TGmDepot tGmDepot);

	List<TGmGame> getBySubCatId(Integer subCatId, Integer gamelogoid);

}
