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
@ConfigurationProperties(prefix = "pt")
@PropertySource("classpath:pt.properties")
public class PtConfig {
    @ApiModelProperty(value = "点击游戏后返给模板中的URL地址")
    private String gameUrl;
    @ApiModelProperty(value = "游戏平台跳转到第三方平台的游戏URL地址")
    private String gameRouteUrl;
    @ApiModelProperty(value = "游戏跳转中需要第三方平台的JS路径")
    private String jsUrl;
    @ApiModelProperty(value = "信运奖的URL地址")
    private String luckyJackpotUrl;
    @ApiModelProperty(value = "游戏平台跳转到第三方平台的游戏URL地址Mb")
    private String gameMbRouteUrl;

}
