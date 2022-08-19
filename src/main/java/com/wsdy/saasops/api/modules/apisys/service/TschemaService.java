package com.wsdy.saasops.api.modules.apisys.service;

import com.wsdy.saasops.api.modules.apisys.dao.TschemaMapper;
import com.wsdy.saasops.api.modules.apisys.entity.Tschema;
import com.wsdy.saasops.api.modules.apisys.mapper.ApiSysMapper;
import com.wsdy.saasops.modules.base.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TschemaService  extends BaseService<TschemaMapper, Tschema> {

    @Autowired
    TschemaMapper tschemaMapper;
    @Autowired
    ApiSysMapper apiSysMapper;

    public Tschema selectOne(Tschema tschema){
        return tschemaMapper.selectOne(tschema);
    }

    @Override
    public int selectCount(Tschema tschema){
        return super.selectCount(tschema);
    }
}
