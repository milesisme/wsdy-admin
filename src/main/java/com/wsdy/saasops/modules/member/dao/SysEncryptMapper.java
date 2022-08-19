package com.wsdy.saasops.modules.member.dao;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.SysEncrypt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface SysEncryptMapper extends MyMapper<SysEncrypt>{

    List<MbrAccount> desAccountList(
            @Param("prefixMobile") String prefixMobile,
            @Param("isSign") Integer isSign,
            @Param("limit") Integer limit);

    int desAccountCount(@Param("prefixMobile") String prefixMobile);

    SysEncrypt findEncryptDesc(@Param("id") Integer id);

    List<SysEncrypt> findEncryptNoId(@Param("id") Integer id);

    List<SysEncrypt> findEncryptDescDel();
}
