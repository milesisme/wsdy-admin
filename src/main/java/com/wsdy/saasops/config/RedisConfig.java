package com.wsdy.saasops.config;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.wsdy.saasops.api.annotation.CacheDuration;
import com.wsdy.saasops.api.listener.RedisExpiredListener;
import com.wsdy.saasops.common.constants.Constants;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

/**
 * Redis配置+ springboot缓存配置
 */
@Configuration
@EnableCaching
@Slf4j
public class RedisConfig extends CachingConfigurerSupport {

    @Autowired
    private RedisConnectionFactory factory;

    @Bean
    public CacheManager cacheManager(RedisTemplate<String, Object> template) {
        RedisCacheConfiguration defaultCacheConfiguration =
                RedisCacheConfiguration
                        .defaultCacheConfig()
                        // 设置key为String
                        .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(template.getStringSerializer()))
                        // 设置value 为自动转Json的Object
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(template.getValueSerializer()))
                        // 不缓存null
                        .disableCachingNullValues()
                        //设置默认过期时间
                        .entryTtl(Duration.ofSeconds(43200 * 2 * 30))
                        //前缀
                        .computePrefixWith(cacheName -> Constants.PROJECT_NAME + Constants.REDIS_SPACE_SPACING+ cacheName + Constants.REDIS_SPACE_SPACING);

        Map<String, RedisCacheConfiguration> configurationMap = new HashMap<>();
        configurationMap.put("authority", defaultCacheConfiguration.entryTtl(Duration.ofSeconds(1000L))); //缓存过期时间
        Map<String, Long> cacheExpires =  getCacheExpires();
        log.info("load cache expires: {} ", JSON.toJSONString(cacheExpires));
        //设置缓存过期时间
        for (Map.Entry<String, Long> entry : cacheExpires.entrySet()) {
            configurationMap.put(entry.getKey(), defaultCacheConfiguration.entryTtl(Duration.ofSeconds(entry.getValue())));
        }
        //构造缓存管理器
        RedisCacheManager redisCacheManager =
                RedisCacheManager.RedisCacheManagerBuilder
                        // Redis 连接工厂
                        .fromConnectionFactory(template.getConnectionFactory())
                        // 缓存配置
                        .cacheDefaults(defaultCacheConfiguration)
                        .withInitialCacheConfigurations(configurationMap)
                        // 配置同步修改或删除 put/evict
                        .transactionAware()
                        .build();
        return redisCacheManager;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
        template.setKeySerializer(new StringRedisSerializer());

        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        template.setValueSerializer(jackson2JsonRedisSerializer);

        template.setConnectionFactory(factory);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public static ConfigureRedisAction configureRedisAction() {
        return ConfigureRedisAction.NO_OP;
    }

    @Bean
    public RedisMessageListenerContainer listenerContainer(RedisConnectionFactory factory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.addMessageListener(new RedisExpiredListener(), new PatternTopic("__keyevent@0__:expired"));
        return container;
    }

    @Bean
    public HashOperations<String, String, Object> hashOperations(RedisTemplate<String, Object> redisTemplate) {
        return redisTemplate.opsForHash();
    }

    @Bean
    public ValueOperations<String, Object> valueOperations(RedisTemplate<String, Object> redisTemplate) {
        return redisTemplate.opsForValue();
    }

    @Bean
    public ListOperations<String, Object> listOperations(RedisTemplate<String, Object> redisTemplate) {
        return redisTemplate.opsForList();
    }

    @Bean
    public SetOperations<String, Object> setOperations(RedisTemplate<String, Object> redisTemplate) {
        return redisTemplate.opsForSet();
    }

    @Bean
    public ZSetOperations<String, Object> zSetOperations(RedisTemplate<String, Object> redisTemplate) {
        return redisTemplate.opsForZSet();
    }

    private Map<String, Long> getCacheExpires() {
        Map<String, Long> cacheExpires = new HashMap<>();
        Reflections reflections = new Reflections(   new ConfigurationBuilder().forPackage("com.wsdy.saasops.").setClassLoaders(new ClassLoader[]{this.getClass().getClassLoader()}));
        Set<Class<?>>  classes = reflections.getTypesAnnotatedWith(Service.class);
        for(Class<?> clz : classes){
            Method[] methods =  clz.getMethods();
            for(int i = 0; i < methods.length; i ++ ){
                Method method = methods[i];
                CacheDuration cacheDuration = findAnnotation(method, CacheDuration.class);
                if(cacheDuration == null){
                    continue;
                }
                long  duration = cacheDuration.duration();
                Cacheable cacheable = findAnnotation(method, Cacheable.class);
                CacheConfig cacheConfig = findAnnotation(clz, CacheConfig.class);
                if(cacheable!= null){
                    Set<String> cns = findCacheNames(cacheConfig, cacheable);
                    for (String cn : cns) {
                        cacheExpires.put(cn, duration);
                    }
                }
                CachePut cachePut =  findAnnotation(method, CachePut.class);
                if(cachePut != null){
                    Set<String> cns = findCachePutNames(cacheConfig, cachePut);
                    for (String cn : cns) {
                        cacheExpires.put(cn, duration);
                    }
                }
            }
        }
        return cacheExpires;
    }

    private Set<String> findCacheNames(CacheConfig cacheConfig, Cacheable cacheable) {
        return  cacheable.value() == null || cacheable.value().length < 0 ?
                newHashSet(cacheConfig.cacheNames()) : newHashSet(cacheable.value());
    }

    private Set<String> findCachePutNames(CacheConfig cacheConfig, CachePut cacheable) {
        return  cacheable.value() == null || cacheable.value().length < 0 ?
                newHashSet(cacheConfig.cacheNames()) : newHashSet(cacheable.value());
    }
}
