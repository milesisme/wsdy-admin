package com.wsdy.saasops.modules.system.msgtemple.dao;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.system.msgtemple.entity.MsgModel;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;


@Mapper
@Component
public interface MsgModelMapper extends MyMapper<MsgModel>  {

}
