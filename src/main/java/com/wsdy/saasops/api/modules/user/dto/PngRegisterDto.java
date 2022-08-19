package com.wsdy.saasops.api.modules.user.dto;

import org.springframework.util.StringUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PngRegisterDto {

	private String externalUserId;
	private String username;
	private String nickname;
	private String currency;
	private String country;
	private String birthdate;
	private String registration;
	private String brandId;
	private String language;
	private String ip;
	private String locked;
	private String gender;

	@Override
    public String toString() {
		StringBuffer sb = new StringBuffer();
		// sb.append("<?xml version=\"1.0\"?>");
		sb.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" ");
		sb.append("xmlns:v1=\"http://playngo.com/v1\">");
		sb.append("<soapenv:Header/>");
		sb.append("<soapenv:Body>");
		sb.append("<v1:RegisterUser>");
		sb.append("<v1:UserInfo>");
		sb.append("<v1:ExternalUserId>" + externalUserId + "</v1:ExternalUserId>");
		sb.append("<v1:Username>" + username + "</v1:Username>");
		sb.append("<v1:Nickname>" + nickname + "</v1:Nickname>");
		sb.append("<v1:Currency>" + currency + "</v1:Currency>");
		sb.append("<v1:Country>" + country + "</v1:Country>");
		sb.append("<v1:Birthdate>" + birthdate + "</v1:Birthdate>");
		sb.append("<v1:Registration>" + registration + "</v1:Registration>");
		sb.append("<v1:BrandId>" + getBrandIdVal() + "</v1:BrandId>");
		sb.append("<v1:Language>" + language + "</v1:Language>");
		sb.append("<v1:IP>" + ip + "</v1:IP>");
		sb.append("<v1:Locked>" + locked + "</v1:Locked>");
		sb.append("<v1:Gender>" + gender + "</v1:Gender>");
		sb.append("</v1:UserInfo>");
		sb.append("</v1:RegisterUser>");
		sb.append("</soapenv:Body>");
		sb.append("</soapenv:Envelope>");
		return sb.toString();
	}

	public String getBrandIdVal() {
		if (StringUtils.isEmpty(brandId)) {
			return "";
		} else {
			return brandId;
		}
	}

}
