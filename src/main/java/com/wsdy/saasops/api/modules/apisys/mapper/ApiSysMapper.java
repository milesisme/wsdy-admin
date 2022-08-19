package com.wsdy.saasops.api.modules.apisys.mapper;


import com.wsdy.saasops.api.modules.apisys.entity.*;
import org.apache.ibatis.annotations.Mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApiSysMapper {

	TCpSite findCpSiteOneEquals(@Param("siteUrl") String siteUrl);

	TCpSite findCpSiteOne(@Param("siteUrl") String siteUrl);

	List<TCpSite> findCpSiteLike(@Param("siteUrl") String siteUrl);

	TCpSite findPreciseSiteOne(@Param("url") String url);

	List<TCpSite> findCpSite();

	TGmApi findGmApiOne(@Param("depotId") Integer depotId, @Param("siteCode") String siteCode);

	Tschema selectTschemaOne();

    List<SsysConfig> listSysConfig(@Param("groups") String groups);

    String queryGiniuyunUrl(@Param("param") String param);

    String getCpSiteCode(@Param("siteCode") String siteCode);

	TCpSite getCpSiteBySiteCode(@Param("siteCode") String siteCode);

	List<TcpSiteurl> findCpSiteUrlBySiteCode(TcpSiteurl siteurl);

	int insertCpSiteUrlInfo(TcpSiteurl siteurl);

	int updateCpSiteUrlClientType(TcpSiteurl siteurl);

	List<TcpSiteurl> getSiteurl(@Param("siteCode") String siteCode);
}
