package com.wsdy.saasops.modules.member.dto;

import org.springframework.util.StringUtils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "ip地址")
public class IpDto {

	private String ip;
	@ApiModelProperty(value = "国家")
	private String country;
	@ApiModelProperty(value = "区域")
	private String area;
	@ApiModelProperty(value = "省份")
	private String region;
	@ApiModelProperty(value = "城市")
	private String city;
	@ApiModelProperty(value = "")
	private String county;
	@ApiModelProperty(value = "运营商")
	private String isp;

	@Override
    public String toString(){
		StringBuffer buffer = new StringBuffer();
		if (!StringUtils.isEmpty(country)) {
            buffer.append(country).append(" ");
        }
		if (!StringUtils.isEmpty(area)) {
            buffer.append(area).append(" ");
        }
		if (!StringUtils.isEmpty(region)) {
            buffer.append(region).append(" ");
        }
		if (!StringUtils.isEmpty(city)) {
            buffer.append(city).append(" ");
        }
		if (!StringUtils.isEmpty(isp)) {
            buffer.append(isp).append(" ");
        }
		return buffer.toString();
	}
}
