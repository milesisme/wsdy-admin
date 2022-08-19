package com.wsdy.saasops.api.modules.apisys.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "站点域名数据")
public class SiteUrlDto {
    @ApiModelProperty(value = "站点siteCode")
    private String siteCode;
    @ApiModelProperty(value = "模糊匹配域名字符串，通过','拼接")
    private String url;
    @ApiModelProperty(value = "精确匹配域名字符串，通过','拼接")
    private String likeUrl;
}
