package com.wsdy.saasops.modules.base.mapper;

import org.apache.ibatis.annotations.SelectProvider;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * Created by William on 2017/11/24.
 */
public interface AuthorityMapper<T> extends Mapper<T> {

    @SelectProvider(type = AuthorityProvider.class,method = "dynamicSQL")
    List<T> selectInAuth(T record);

}
