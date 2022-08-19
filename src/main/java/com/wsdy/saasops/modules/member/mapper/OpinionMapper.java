package com.wsdy.saasops.modules.member.mapper;


import com.wsdy.saasops.modules.member.entity.MbrOpinion;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OpinionMapper {

    List<MbrOpinion> finOpinionList(MbrOpinion opinion);
}
