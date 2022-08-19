package com.wsdy.saasops.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Configuration

public class RedisConfigProperties {
    @Resource
    private Environment environment;

    public String getValue(String key) {
        return environment.getProperty(key);
    }
}
