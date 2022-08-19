package com.wsdy.saasops.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.*;

import java.time.Duration;
import java.util.Map;

/**
 * 多链接Redis配置
 */
@Configuration
public class RedisMultipleConfig {

    public static final String PREFIX = "spring.redis";

    public static final String DB_INDEX = "db%s";

    public static final String  PORT = "port";

    public static final String HOST = "host";

    public static final String PASSWORD = "password";

    public static final String TIMEOUT = "timeout";

    public static final String TYPE ="type";

    public static final String DATABASE = "database";

    public static final String MAX_ACTIVE ="pool.max-active";

    public static final String MAX_WAIT ="pool.max-wait";

    public static final String MAX_IDLE ="pool.max-idle";

    public static final String MIN_IDLE ="pool.min-idle";

    public static final String  DELIMITER =".";

    public static final int DEF_PORT = 6379;

    public static final String DEF_HOST = "127.0.0.1";

    public static  final int DEF_TIMEOUT = 6000;

    public static final int DEF_DATABASE = 0;

    public static final int DEF_MAX_ACTIVE = 1000;

    public static final int DEF_MAX_WAIT = -1;

    public static final int DEF_MAX_IDLE = 10;

    public static final int DEF_MIN_IDLE = 5;

    public static final String POOL_TYPE_JEDIS = "jedis";

    public static final String POOL_TYPE_LETTUCE = "lettuce";



    @Autowired
    private RedisConfigProperties redisConfigProperties;


    @Bean
    public StringRedisTemplate stringRedisTemplate_0() {
        return initStringRedisTemplate(0);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate_1() {
        return initStringRedisTemplate(1);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate_2() {
        return initStringRedisTemplate(2);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate_3() {
        return initStringRedisTemplate(3);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate_4() {
        return initStringRedisTemplate(4);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate_5() {
        return initStringRedisTemplate(5);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate_6() {
        return initStringRedisTemplate(6);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate_7() {
        return initStringRedisTemplate(7);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate_8() {
        return initStringRedisTemplate(8);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate_9() {
        return initStringRedisTemplate(9);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate_10() {
        return initStringRedisTemplate(10);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate_11() {
        return initStringRedisTemplate(11);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate_12() {
        return initStringRedisTemplate(12);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate_13() {
        return initStringRedisTemplate(13);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate_14() {
        return initStringRedisTemplate(14);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate_15() {
        return initStringRedisTemplate(15);
    }

    private StringRedisTemplate initStringRedisTemplate(int index){

        String gHost = redisConfigProperties.getValue(StringUtils.joinWith(DELIMITER, PREFIX, HOST));
        String gPort = redisConfigProperties.getValue(StringUtils.joinWith(DELIMITER, PREFIX, PORT));
        String gPassword = redisConfigProperties.getValue(StringUtils.joinWith(DELIMITER, PREFIX, PASSWORD));
        String gTimeout = redisConfigProperties.getValue(StringUtils.joinWith(DELIMITER, PREFIX, TIMEOUT));
        String gDatabase = redisConfigProperties.getValue(StringUtils.joinWith(DELIMITER, PREFIX, DATABASE));
        String gType = redisConfigProperties.getValue(StringUtils.joinWith(DELIMITER, PREFIX, TYPE));

        String currentHost = redisConfigProperties.getValue(String.format(StringUtils.joinWith(DELIMITER, PREFIX, DB_INDEX, HOST), index));
        String currentPort = redisConfigProperties.getValue(String.format(StringUtils.joinWith(DELIMITER, PREFIX,DB_INDEX, PORT), index));
        String currentPassword = redisConfigProperties.getValue(String.format(StringUtils.joinWith(DELIMITER, PREFIX, DB_INDEX,PASSWORD), index));
        String currentTimeout = redisConfigProperties.getValue(String.format(StringUtils.joinWith(DELIMITER, PREFIX, DB_INDEX, TIMEOUT), index));
        String currentDatabase = redisConfigProperties.getValue(String.format(StringUtils.joinWith(DELIMITER, PREFIX, DB_INDEX, DATABASE), index));
        String currentType = redisConfigProperties.getValue(String.format(StringUtils.joinWith(DELIMITER, PREFIX, DB_INDEX, TYPE), index));

        RedisStandaloneConfiguration standaloneConfiguration = new RedisStandaloneConfiguration();
        int database = StringUtils.isEmpty(currentDatabase) ? (StringUtils.isEmpty(gDatabase) ? index : Integer.parseInt(gDatabase)) : Integer.parseInt(currentDatabase);
        standaloneConfiguration.setDatabase(database);

        String host = StringUtils.isEmpty(currentHost) ?(StringUtils.isEmpty(gHost) ? DEF_HOST : gHost ) : currentHost;
        standaloneConfiguration.setHostName(host);

        int port = StringUtils.isEmpty(currentPort) ?(StringUtils.isEmpty(gPort)? DEF_PORT : Integer.parseInt(gPort)) : Integer.parseInt(currentPort);
        standaloneConfiguration.setPort(port);

        String password = StringUtils.isEmpty(currentPassword) ? gPassword : currentPassword;

        if(StringUtils.isNotEmpty(password)){
            standaloneConfiguration.setPassword(password);
        }

        String gMaxActive = redisConfigProperties.getValue(StringUtils.joinWith(DELIMITER, PREFIX, MAX_ACTIVE));
        String gMaxWait = redisConfigProperties.getValue(StringUtils.joinWith(DELIMITER, PREFIX, MAX_WAIT ));
        String gMaxIdle = redisConfigProperties.getValue(StringUtils.joinWith(DELIMITER, PREFIX, MAX_IDLE));
        String gMinIdle = redisConfigProperties.getValue(StringUtils.joinWith(DELIMITER, PREFIX, MIN_IDLE));

        String currentMaxActive = redisConfigProperties.getValue(String.format(StringUtils.joinWith(DELIMITER, PREFIX,DB_INDEX, MAX_ACTIVE), index));
        String currentMaxWait = redisConfigProperties.getValue(String.format(StringUtils.joinWith(DELIMITER, PREFIX, DB_INDEX, MAX_WAIT), index ));
        String currentMaxIdle = redisConfigProperties.getValue(String.format(StringUtils.joinWith(DELIMITER, PREFIX, DB_INDEX, MAX_IDLE), index));
        String currentMinIdle = redisConfigProperties.getValue(String.format(StringUtils.joinWith(DELIMITER, PREFIX, DB_INDEX,MIN_IDLE), index));

        int maxActive = StringUtils.isEmpty(currentMaxActive) ?(StringUtils.isEmpty(gMaxActive)? DEF_MAX_ACTIVE : Integer.parseInt(gMaxActive)) : Integer.parseInt(currentMaxActive);
        int maxWait = StringUtils.isEmpty(currentMaxWait) ?(StringUtils.isEmpty(gMaxWait)? DEF_MAX_WAIT : Integer.parseInt(gMaxWait)) : Integer.parseInt(currentMaxWait);
        int maxIdle = StringUtils.isEmpty(currentMaxIdle) ?(StringUtils.isEmpty(gMaxIdle)? DEF_MAX_IDLE : Integer.parseInt(gMaxIdle)) : Integer.parseInt(currentMaxIdle);
        int minIdle = StringUtils.isEmpty(currentMinIdle) ?(StringUtils.isEmpty(gMinIdle)? DEF_MIN_IDLE : Integer.parseInt(gMinIdle)) : Integer.parseInt(currentMinIdle);

        GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
        genericObjectPoolConfig.setMaxIdle(maxIdle);
        genericObjectPoolConfig.setMinIdle(minIdle);
        genericObjectPoolConfig.setMaxTotal(maxActive);
        genericObjectPoolConfig.setMaxWaitMillis(maxWait);

        int timeout =  StringUtils.isEmpty(currentTimeout) ?(StringUtils.isEmpty(gTimeout)? DEF_TIMEOUT : Integer.parseInt(gTimeout)) : Integer.parseInt(currentTimeout);
        String poolType = StringUtils.isEmpty(currentType) ? gType : currentType;

        RedisConnectionFactory redisConnectionFactory = null;
        if(StringUtils.isNotEmpty(poolType) && POOL_TYPE_JEDIS.equals(poolType.trim())){
            JedisConnectionFactory connectionFactory = jedisConnectionFactory(standaloneConfiguration,genericObjectPoolConfig, timeout );
            connectionFactory.afterPropertiesSet();
            redisConnectionFactory = connectionFactory;
        }else {
            //default lettuce pool

            LettuceConnectionFactory connectionFactory = lettuceConnectionFactory(standaloneConfiguration,genericObjectPoolConfig, timeout);
            connectionFactory.afterPropertiesSet();
            redisConnectionFactory = connectionFactory;
        }



        StringRedisTemplate stringRedisTemplate = createRedisTemplate(redisConnectionFactory);
        return stringRedisTemplate;
    }


    private StringRedisTemplate createRedisTemplate(RedisConnectionFactory redisConnectionFactory){
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(redisConnectionFactory);
        return stringRedisTemplate;
    }

    private LettuceConnectionFactory lettuceConnectionFactory(RedisStandaloneConfiguration standaloneConfiguration, GenericObjectPoolConfig genericObjectPoolConfig, long timeout) {
        LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder builder = LettucePoolingClientConfiguration.builder();
        builder.poolConfig(genericObjectPoolConfig);
        builder.commandTimeout(Duration.ofSeconds(timeout));
        return new LettuceConnectionFactory(standaloneConfiguration, builder.build());
    }

    private JedisConnectionFactory jedisConnectionFactory(RedisStandaloneConfiguration standaloneConfiguration, GenericObjectPoolConfig genericObjectPoolConfig, long timeout){
        JedisClientConfiguration.DefaultJedisClientConfigurationBuilder builder = (JedisClientConfiguration.DefaultJedisClientConfigurationBuilder) JedisClientConfiguration
                .builder();
        builder.connectTimeout(Duration.ofSeconds(timeout));
        builder.usePooling();
        builder.poolConfig(genericObjectPoolConfig);
        return new JedisConnectionFactory(standaloneConfiguration, builder.build());
    }

}
