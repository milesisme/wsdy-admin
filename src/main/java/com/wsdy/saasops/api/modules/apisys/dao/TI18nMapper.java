package com.wsdy.saasops.api.modules.apisys.dao;

import com.wsdy.saasops.api.modules.apisys.entity.TI18n;
import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.sys.dao.SysI18nDto;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@Mapper
public interface TI18nMapper extends MyMapper<TI18n> {

    int insertI18nBatch(List<SysI18nDto> list);

}