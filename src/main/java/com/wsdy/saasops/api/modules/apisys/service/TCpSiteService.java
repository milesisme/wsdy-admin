package com.wsdy.saasops.api.modules.apisys.service;

import com.alibaba.fastjson.JSON;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.apisys.dao.TCpSiteMapper;
import com.wsdy.saasops.api.modules.apisys.dao.TcpSiteurlMapper;
import com.wsdy.saasops.api.modules.apisys.dto.SiteUrlDto;
import com.wsdy.saasops.api.modules.apisys.entity.TCpSite;
import com.wsdy.saasops.api.modules.apisys.entity.TcpSiteurl;
import com.wsdy.saasops.api.modules.apisys.mapper.ApiSysMapper;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.StringUtil;
import com.wsdy.saasops.modules.base.service.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Slf4j
@Service
public class TCpSiteService extends BaseService<TCpSiteMapper, TCpSite> {

    /**
     * <schemaName,siteCode>
     */
    public static Map<String, String> schemaName = new ConcurrentHashMap<>();

    /**
     * <siteCode,schemaName>
     */
    public static Map<String, String> siteCode = new ConcurrentHashMap<>();

    @Autowired
    ApiSysMapper apiSysMapper;
    @Autowired
    TcpSiteurlMapper tcpSiteurlMapper;
    @Autowired
    TschemaService tschemaService;

    public TCpSite queryOneCond(String url) {
        // 去除端口
        url =  CommonUtil.getUrlWithoutPort(url);
        TCpSite tCpPreciseSite = apiSysMapper.findPreciseSiteOne(url);
        if (Objects.nonNull(tCpPreciseSite)) {
            return tCpPreciseSite;
        }

        /** 新匹配逻辑 **/
        // 先判断是否有完全相等的域名
        log.info("getSiteCode使用域名{}开始进行匹配", CommonUtil.getDomainForUrlEx(url));
        TCpSite tCpSiteEquals = apiSysMapper.findCpSiteOneEquals(CommonUtil.getDomainForUrlEx(url));
        log.info("getSiteCode使用域名{}进行equals匹配结果{}", CommonUtil.getDomainForUrlEx(url), JSON.toJSONString(tCpSiteEquals));
        if (Objects.nonNull(tCpSiteEquals) && StringUtils.isNotBlank(tCpSiteEquals.getSiteCode())) {
            return tCpSiteEquals;
        }
        // 再判断like出来的域名包含几个站点
        List<TCpSite> tCpSiteList = apiSysMapper.findCpSiteLike(CommonUtil.getDomainForUrlEx(url));
        List<String> siteCodes = tCpSiteList.stream().map(TCpSite::getSiteCode).distinct().collect(Collectors.toList());
        // 多个站点提示配置错误
        if (siteCodes.size() >= 2) {
            log.info("getSiteCode使用域名{}匹配到包含多个站点:{}", CommonUtil.getDomainForUrlEx(url), siteCodes);
            throw new R200Exception("站点配置错误，返回多个!");
        }
        if (Objects.isNull(tCpSiteList) || tCpSiteList.size() <= 0) {
            throw new R200Exception("不存在该站点!");
        }
        return tCpSiteList.get(0);
        /** 新匹配逻辑 **/

        /*TCpSite tCpSite = apiSysMapper.findCpSiteOne(CommonUtil.getDomainForUrlEx(url));
        if (Objects.isNull(tCpSite)) {
            throw new R200Exception("不存在该站点!");
        }
        return tCpSite;*/
    }

    public List<TcpSiteurl> queryDomain(String siteCode) {
        TcpSiteurl siteurl = new TcpSiteurl();
        siteurl.setSiteCode(siteCode);
        return apiSysMapper.findCpSiteUrlBySiteCode(siteurl);
    }

    @Cacheable(cacheNames = ApiConstants.REDIS_GAME_SITECODE_CACHE_KEY, key = "#siteCode")
    public TCpSite queryPreOneCond(String siteCode) {
        TCpSite cpSite = new TCpSite();
        cpSite.setSiteCode(siteCode);
        return apiSysMapper.getCpSiteBySiteCode(siteCode);
    }

    public TCpSite queryPreOneCondNoCach(String siteCode) {
        TCpSite cpSite = new TCpSite();
        cpSite.setSiteCode(siteCode);
        return apiSysMapper.getCpSiteBySiteCode(siteCode);
    }

    @Cacheable(cacheNames = ApiConstants.SITE_CODE, key = "#SchemaName")
    public String getSiteCode(String SchemaName) {
        TCpSite cpSite = new TCpSite();
        cpSite.setSchemaName(SchemaName);
        cpSite = super.queryObjectCond(cpSite);
        return cpSite.getSiteCode();
    }


    /**
     * 初始化SchemaName 和SiteCode 的对应关系 key=schemaName , value =siteCode
     * 初始化SiteCode 和SchemaName 的对应关系 key=siteCode , value =schemaName
     *
     * @return
     */
    @Bean
    public Map<String, String> initSchemaName() {
        List<TCpSite> tCpSites = apiSysMapper.findCpSite();
        schemaName.clear();
        siteCode.clear();
        for (TCpSite tCpSite : tCpSites) {
            schemaName.put(tCpSite.getSchemaName(), tCpSite.getSiteCode());
            siteCode.put(tCpSite.getSiteCode(), tCpSite.getSchemaName());
        }
        return schemaName;
    }


    private TcpSiteurl getTcpSiteurl(TCpSite tCpSite) {
        TcpSiteurl tcpSiteurl = new TcpSiteurl();
        tcpSiteurl.setSiteId(tCpSite.getId());
        tcpSiteurl.setSiteCode(tCpSite.getSiteCode());
        tcpSiteurl.setSiteUrl(tCpSite.getSiteUrl());
        return tcpSiteurl;
    }

    public String getSchemaName(String siteCode){
        //String schemaName = TCpSiteService.siteCode.get(siteCode);
       /* if(null == schemaName){
            initSchemaName();
        }*/
        return TCpSiteService.siteCode.get(siteCode);
    }


    /**
     * 传站点url给北京前端
     * @param siteCode
     * @return
     */
    public List<SiteUrlDto> getSiteurl(String siteCode) {
        // 查库获取数据
        List<TcpSiteurl>  siteUrls = apiSysMapper.getSiteurl(siteCode);

        // 处理返回数据
        List<SiteUrlDto> siteUrlListResp = new ArrayList<>();
        if(Objects.isNull(siteUrls) || siteUrls.size() == 0){
            log.info("getSiteurl==无查询结果");
            return siteUrlListResp;
        }
        // 分组处理
        Map<String, List<TcpSiteurl>> siteUrlGroupingBy =
                siteUrls.stream().collect(
                        Collectors.groupingBy(
                                TcpSiteurl::getSiteCode));

        for (String code : siteUrlGroupingBy.keySet()) {
            List<TcpSiteurl> siteUrlList = siteUrlGroupingBy.get(code);
            // 去除null及""的数据并用","拼接
            String likeUrl = siteUrlList.stream().filter(e-> StringUtil.isNotEmpty(e.getSiteUrl())).map(e->e.getSiteUrl()).collect(Collectors.joining(","));
            String url = siteUrlList.stream().filter(e-> StringUtil.isNotEmpty(e.getPreciseSiteUrl())).map(e->e.getPreciseSiteUrl()).collect(Collectors.joining(","));

            SiteUrlDto siteUrlDto = new SiteUrlDto();
            siteUrlDto.setSiteCode(code);
            siteUrlDto.setLikeUrl(likeUrl);
            siteUrlDto.setUrl(url);
            siteUrlListResp.add(siteUrlDto);
        }
        log.info("getSiteurl==" + JSON.toJSON(siteUrlListResp));
        return siteUrlListResp;
    }
}
