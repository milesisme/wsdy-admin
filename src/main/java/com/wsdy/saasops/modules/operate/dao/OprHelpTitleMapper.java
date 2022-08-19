package com.wsdy.saasops.modules.operate.dao;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.operate.entity.OprHelpCategory;
import com.wsdy.saasops.modules.operate.entity.OprHelpTitle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface OprHelpTitleMapper extends MyMapper<OprHelpTitle> {
	void updatTitleAvailable(OprHelpTitle oprHelpTitle);
	int deleteTitle(OprHelpTitle oprHelpTitle);
}
