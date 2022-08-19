package com.wsdy.saasops.api.modules.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PngBalanceOrTicketDto {
	private String externalUserId;
	private String mod;

	@Override
    public String toString() {
		StringBuffer sb = new StringBuffer();
		// sb.append("<?xml version=\"1.0\"?>");
		sb.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" ");
		sb.append("xmlns:v1=\"http://playngo.com/v1\">");
		sb.append("<soapenv:Header/>");
		sb.append("<soapenv:Body>");
		sb.append("<v1:" + mod + ">");
		sb.append("<v1:ExternalUserId>" + externalUserId + "</v1:ExternalUserId>");
		sb.append("</v1:" + mod + ">");
		sb.append("</soapenv:Body>");
		sb.append("</soapenv:Envelope>");
		return sb.toString();
	}
}
