package com.wsdy.saasops.api.modules.user.dto;

import org.springframework.util.StringUtils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel(value="NT api接口参数")
@Getter
@Setter
public class NtDto {

	@ApiModelProperty(value="代理ID")
	private String brandId;
	@ApiModelProperty(value="代理密码")
	private String brandPassword;
	@ApiModelProperty(value="会员UUID")
	private String uuid;
	@ApiModelProperty(value="会员登陆的SESSION id")
	private String sessionKey;
	@ApiModelProperty(value="会员登陆名")
	private String loginName;
	@ApiModelProperty(value="货币")
	private String currency;
	@ApiModelProperty(value="平台代码  NETENT_CAS")
	private String platform;
	@ApiModelProperty(value="游戏ID")
	private String gameId;
	@ApiModelProperty(value="真钱玩家或试玩")
	private boolean playForReal;
	@ApiModelProperty(value="是否为手机玩家")
	private boolean isMobile;
	
	@Override
    public String toString() {
		StringBuffer buffer = new StringBuffer();
		if (!StringUtils.isEmpty(brandId)) {
            buffer.append("brandId=").append(brandId).append("&");
        }
		
		if (!StringUtils.isEmpty(brandPassword)) {
            buffer.append("brandPassword=").append(brandPassword).append("&");
        }

		if (!StringUtils.isEmpty(uuid)) {
            buffer.append("uuid=").append(uuid).append("&");
        }
		
		if (!StringUtils.isEmpty(sessionKey)) {
            buffer.append("sessionKey=").append(sessionKey).append("&");
        }
		
		if (!StringUtils.isEmpty(loginName)) {
            buffer.append("loginName=").append(loginName).append("&");
        }
		
		if (!StringUtils.isEmpty(currency)) {
            buffer.append("currency=").append(currency).append("&");
        }
		
		if (!StringUtils.isEmpty(platform)) {
            buffer.append("platform=").append(platform).append("&");
        }
		
		if (!StringUtils.isEmpty(gameId)) {
            buffer.append("gameId=").append(gameId).append("&");
        }
		
		if (!StringUtils.isEmpty(playForReal)) {
            buffer.append("playForReal=").append(playForReal).append("&");
        }
		
		if (!StringUtils.isEmpty(isMobile)) {
            buffer.append("isMobile=").append(isMobile).append("&");
        }
		
		if (buffer.length() > 0) {
			buffer.insert(0, "?");
			buffer.setLength(buffer.length() - 1);
		}
		return buffer.toString();
	}
}
