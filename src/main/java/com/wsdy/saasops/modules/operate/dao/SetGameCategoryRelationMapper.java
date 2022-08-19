package com.wsdy.saasops.modules.operate.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.operate.entity.SetGameCategoryRelation;

import tk.mybatis.mapper.common.IdsMapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author ${author}
 * @since 2022-01-05
 */
@Mapper
public interface SetGameCategoryRelationMapper extends MyMapper<SetGameCategoryRelation>, IdsMapper<SetGameCategoryRelation> {

	List<SetGameCategoryRelation> selectByCategoryId(Integer id);

}
