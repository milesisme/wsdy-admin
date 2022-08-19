package com.wsdy.saasops.api.modules.apisys.service;

import java.util.List;
import java.util.Map;

import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.common.constants.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.apisys.dao.TGmApiMapper;
import com.wsdy.saasops.api.modules.apisys.dto.ProxyProperty;
import com.wsdy.saasops.api.modules.apisys.entity.SsysConfig;
import com.wsdy.saasops.api.modules.apisys.entity.TGmApi;
import com.wsdy.saasops.api.modules.apisys.mapper.ApiSysMapper;
import com.wsdy.saasops.modules.base.service.BaseService;
import com.google.gson.Gson;

import static java.util.Objects.nonNull;


@Service
public class TGmApiService extends BaseService<TGmApiMapper, TGmApi> {

    @Autowired
    ApiSysMapper apiSysMapper;
    @Autowired
    private RedisService redisService;

    private final static String qiniuDomainOfBucketV2 = "qiniuDomainOfBucketV2";

    @SuppressWarnings("unchecked")
    //@Cacheable(cacheNames = ApiConstants.REDIS_GAME_API_CACHE_KEY, key = "#siteCode+'_'+#depotId")
    public TGmApi queryApiObject(Integer depotId, String siteCode) {
        //FIXME
        TGmApi gmApi = apiSysMapper.findGmApiOne(depotId, siteCode);
        if (nonNull(gmApi) && !StringUtils.isEmpty(gmApi.getSecureCode())) {
            gmApi.setSecureCodes((Map<String, String>) JSON.parse(gmApi.getSecureCode()));
        }
        return gmApi;
    }

    //	@Cacheable(cacheNames = ApiConstants.REDIS_PROXY_CATCH)
    public List<SsysConfig> queryList(String groupApi) {
        List<SsysConfig> apiSystemList = apiSysMapper.listSysConfig(groupApi);
        apiSystemList.forEach(e -> {
            e.setProxyProperty(new Gson().fromJson(e.getValues(), ProxyProperty.class));
        });
        return apiSystemList;
    }

    public String queryGiniuyunUrl() {
        String url = apiSysMapper.queryGiniuyunUrl(qiniuDomainOfBucketV2);
        return url;
    }
}
