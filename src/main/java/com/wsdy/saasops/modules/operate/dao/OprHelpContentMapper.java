package com.wsdy.saasops.modules.operate.dao;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.operate.entity.OprHelpContent;
import com.wsdy.saasops.modules.operate.entity.OprHelpTitle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface OprHelpContentMapper extends MyMapper<OprHelpContent> {
	void updatContentAvailable(OprHelpContent oprHelpContent);
	int deleteContent(OprHelpContent oprHelpContent);
}
