package com.wsdy.saasops.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "okhttp")
@PropertySource("classpath:okhttp.properties")
@ApiModel(value = "OKHTTP 默认参数设置")
public class OkHttpConfig {
    @ApiModelProperty(value = "默认连接值 分钟计时")
    private Integer connectTimeout;
    @ApiModelProperty(value = "默认读取 分钟计时")
    private Integer readTimeout;
    @ApiModelProperty(value = "默认写  分钟计时")
    private Integer writeTimeout;
    @ApiModelProperty(value = "默认写  分钟计时")
    private String proxyGroup;
    @ApiModelProperty(value = "最大连接数")
    private Integer maxIdleConnections;
    @ApiModelProperty(value = "持继期数")
    private Long keepAliveDuration;
}
