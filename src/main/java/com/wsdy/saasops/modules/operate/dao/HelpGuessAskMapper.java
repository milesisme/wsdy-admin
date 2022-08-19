package com.wsdy.saasops.modules.operate.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.operate.entity.HelpGuessAsk;

import tk.mybatis.mapper.common.IdsMapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author ${author}
 * @since 2022-05-21
 */
@Mapper
public interface HelpGuessAskMapper extends MyMapper<HelpGuessAsk>, IdsMapper<HelpGuessAsk> {

	List<HelpGuessAsk> queryListPage(@Param("isOpen") Boolean isOpen);

}
