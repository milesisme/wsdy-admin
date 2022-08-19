package com.wsdy.saasops.api.modules.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class RedisDelService {
    @Resource(name = "redisTemplate")
    RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private RedisService redisService;
    /**
     * 使用redis模糊清除缓存
     */
    public void redisCache() {
        redisTemplate.delete(redisTemplate.keys("SaasopsV2:gameSiteCodeCache:*" + "*"));
        redisTemplate.delete(redisTemplate.keys("SaasopsV2:gameCompanyCache:*" + "*"));
        redisTemplate.delete(redisTemplate.keys("SaasopsV2:gameApiCache:*" + "*"));
        redisTemplate.delete(redisTemplate.keys("SaasopsV2:SysRoleMenuTree:*" + "*"));
        redisTemplate.delete(redisTemplate.keys("SaasopsV2:loginPngTokenCache:*" + "*"));
        redisTemplate.delete(redisTemplate.keys("SaasopsV2:redisProxyCache:*" + "*"));
        redisTemplate.delete(redisTemplate.keys("SaasopsV2:loginPt2TokenCache:*" + "*"));
        redisTemplate.delete(redisTemplate.keys("SaasopsV2:loginNtTokenCache:*" + "*"));
        redisTemplate.delete(redisTemplate.keys("SaasopsV2:loginPngTokenCache:*" + "*"));
        redisTemplate.delete(redisTemplate.keys("SaasopsV2:SysRoleMenuTree:*" + "*"));
        redisTemplate.delete(redisTemplate.keys("SaasopsV2:mailsetCache:*" + "*"));
        redisTemplate.delete(redisTemplate.keys("SaasopsV2:StationsetCache:*" + "*"));
        redisTemplate.delete(redisTemplate.keys("SaasopsV2:redisProxyCache:*" + "*"));
        redisTemplate.delete(redisTemplate.keys("SaasopsV2:accountSite_:*" + "*"));
        redisTemplate.delete(redisTemplate.keys("batchdispatcherTask_*"));
        redisTemplate.delete(redisTemplate.keys("auditAccountSign_*"));
        redisTemplate.delete(redisTemplate.keys("excel_export_*"));     // 导出
        redisTemplate.delete(redisTemplate.keys("qiniuDomainOfBucketV2"));

    }

    public void redisCacheByKey(String key){
        redisService.del(key);
    }

    public void runRedisCache() {
        redisTemplate.delete(redisTemplate.keys("auditAccountSign_*"));
        redisTemplate.delete(redisTemplate.keys("auditAccount_*"));
        redisTemplate.delete(redisTemplate.keys("accountVerifySercret_*"));
        redisTemplate.delete(redisTemplate.keys("batchdispatcherTask_*"));
    }
}
