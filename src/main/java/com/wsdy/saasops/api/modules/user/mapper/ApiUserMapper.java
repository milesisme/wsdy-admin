package com.wsdy.saasops.api.modules.user.mapper;


import org.apache.ibatis.annotations.Mapper;
import com.wsdy.saasops.api.modules.user.entity.FindPwEntity;

@Mapper
public interface ApiUserMapper {

	int insertFindPwd(FindPwEntity entity);
}
