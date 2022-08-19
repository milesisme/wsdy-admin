package com.wsdy.saasops.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "eveb")
@PropertySource("classpath:eveb.properties")
public class EvebConfig {

    private String url;

}
