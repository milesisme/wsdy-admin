package com.wsdy.saasops.modules.member.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckIpDto {
    @ApiModelProperty(value = "ip")
    private String ip;
    @ApiModelProperty(value = "国家编码 如PH")
    private String countryCode;
    @ApiModelProperty(value = "国家名称")
    private String countryName;
    @ApiModelProperty(value = "asn")
    private String asn;
    @ApiModelProperty(value = "isp")
    private String isp;
    @ApiModelProperty(value = "0低风险 1高风险 2中风险 10 自定义的异常未查询到等")
    private String block;
    @ApiModelProperty(value = "域名")
    private String hostname;
}
