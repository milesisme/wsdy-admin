package com.wsdy.saasops.modules.operate.dao;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.operate.entity.AdvBanner;
import com.wsdy.saasops.modules.operate.entity.OprAdv;
import com.wsdy.saasops.modules.operate.entity.OprHelpCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


@Mapper
public interface OprHelpCategoryMapper extends MyMapper<OprHelpCategory> {
	void updatCategoryAvailable(OprHelpCategory oprHelpCategory);
	void deleteCategory(OprHelpCategory oprHelpCategory);
	List<OprHelpCategory> selectCategoryList(OprHelpCategory oprHelpCategory);
}
