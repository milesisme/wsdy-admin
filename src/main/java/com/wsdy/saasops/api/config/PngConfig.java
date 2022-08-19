package com.wsdy.saasops.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "png")
@PropertySource("classpath:png.properties")
public class PngConfig {
    @ApiModelProperty(value = "URL跳转路径")
    private String gameUrl;
    @ApiModelProperty(value = "URL平台内部游戏跳转路径")
    private String gameRouteUrl;
    @ApiModelProperty(value = "宽度与高度")
    private String withAndHight;
    @ApiModelProperty(value = "PNG 中JS 调用接口")
    private String jsUrl;
    @ApiModelProperty(value = "PNG 中手机JS 调用接口")
    private String mbUrl;
}
