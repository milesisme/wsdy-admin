package com.wsdy.saasops.api.modules.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterMember188Dto {
	private String loginName;
	private String currencyCode;
	private String oddsTypeId;
	private String langCode;
	private String timeZone;
	private String memberStatus;

	@Override
    public String toString() {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		sBuffer.append("<Request Method=\"RegisterMember\">");
		sBuffer.append(" <LoginName>"+loginName+"</LoginName>");
		sBuffer.append(" <CurrencyCode>"+currencyCode+"</CurrencyCode>");
		sBuffer.append(" <OddsTypeId>"+oddsTypeId+"</OddsTypeId>");
		sBuffer.append(" <LangCode>"+langCode+"</LangCode>");
		sBuffer.append(" <TimeZone>"+timeZone+"</TimeZone>");
		sBuffer.append(" <MemberStatus>"+memberStatus+"</MemberStatus>");
		sBuffer.append(" </Request>");
		return sBuffer.toString();
	}


}
