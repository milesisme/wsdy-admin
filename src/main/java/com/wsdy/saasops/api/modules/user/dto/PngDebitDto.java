package com.wsdy.saasops.api.modules.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PngDebitDto {

	private String externalUserId;
	private String amount;
	private String currency;
	private String externalTransactionId;

	@Override
    public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" ");
		sb.append("xmlns:v1=\"http://playngo.com/v1\">");
		sb.append("<soapenv:Header/>");
		sb.append("<soapenv:Body>");
		sb.append("<v1:Debit>");
		sb.append("<v1:ExternalUserId>" + externalUserId + "</v1:ExternalUserId>");
		sb.append("<v1:Amount>" + amount + "</v1:Amount>");
		sb.append("<v1:Currency>" + currency + "</v1:Currency>");
		sb.append("<v1:ExternalTransactionId>" + externalTransactionId + "</v1:ExternalTransactionId>");
		sb.append("</v1:Debit>");
		sb.append("</soapenv:Body>");
		sb.append("</soapenv:Envelope>");
		return sb.toString();
	}
}
