package com.wsdy.saasops.modules.system.msgtemple.mapper;

import com.wsdy.saasops.modules.system.msgtemple.entity.MsgModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by William on 2017/11/7.
 */
@Mapper
@Component
public interface myMsgModelMapper {
    void deleteByIds(@Param("ids") String ids);
    List<MsgModel> selectListByIds(@Param("ids") String ids);
    List<MsgModel> queryByConditions(MsgModel msgModel);
}
