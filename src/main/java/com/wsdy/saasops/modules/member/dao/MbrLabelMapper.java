package com.wsdy.saasops.modules.member.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.member.entity.MbrLabel;

/**
 * <p>
 * 会员标签表 Mapper 接口
 * </p>
 *
 * @author ${author}
 * @since 2022-05-06
 */
@Mapper
public interface MbrLabelMapper extends MyMapper<MbrLabel> {

	List<MbrLabel> listPage(MbrLabel mbrLabel);

	Integer updateAvailable(Integer id, Boolean isAvailable);

	int checkNameCount(String name, Integer id);

	int setMbrLabel(Integer labelid, Iterable<String> userList);

}
