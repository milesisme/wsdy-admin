package com.wsdy.saasops.modules.operate.dao;

import com.wsdy.saasops.modules.operate.entity.OprNotice;
import org.apache.ibatis.annotations.Mapper;
import com.wsdy.saasops.modules.base.mapper.MyMapper;
import tk.mybatis.mapper.common.IdsMapper;


@Mapper
public interface OprNoticeMapper extends MyMapper<OprNotice>, IdsMapper<OprNotice> {

}
