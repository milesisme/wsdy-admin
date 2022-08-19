package com.wsdy.saasops.modules.system.agencydomain.mapper;

import com.wsdy.saasops.modules.system.agencydomain.entity.SystemAgencyUrl;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by William on 2017/11/8.
 */
@Mapper
@Component
public interface MySystemAgencyUrlMapper {
    List<SystemAgencyUrl> queryConditions(SystemAgencyUrl systemAgencyUrl);
    void multiInsert(List<SystemAgencyUrl> systemAgencyUrls);
    void multiDelete(@Param("ids") String ids);
}
