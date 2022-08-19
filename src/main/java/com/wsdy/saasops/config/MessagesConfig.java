package com.wsdy.saasops.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;

@Configuration
@PropertySource("classpath:message.properties")
public class MessagesConfig {

    @Resource
    private Environment environment;

    public String getValue(String key) {
        return environment.getProperty(key);
    }
}
