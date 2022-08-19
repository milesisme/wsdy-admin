package com.wsdy.saasops.api.modules.user.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferV2Req {
	private String brandId;
	
	private String brandPassword;
	
	private String uuid;

	private BigDecimal amount;

	private String iso3Currency;

	private String platformTranId;
	
	@Override
    public String toString() {
		StringBuffer sb = new StringBuffer();  
        sb.append("<?xml version=\"1.0\"?>");  
        sb.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" ");  
        sb.append("xmlns:sec=\"http://secondarywallet.connect.omega.com/\">");
        sb.append("<soapenv:Header/>");  
        sb.append("<soapenv:Body>");
        sb.append("<sec:transferV2Req>");  
        sb.append("<brandId>"+getBrandId()+"</brandId>");  
        sb.append("<brandPassword>"+getBrandPassword()+"</brandPassword>");
        sb.append("<uuid>"+getUuid()+"</uuid>");
        sb.append("<amount>"+getAmount()+"</amount>");
        sb.append("<iso3Currency>"+getIso3Currency()+"</iso3Currency>");
        sb.append("<platformTranId>"+getPlatformTranId()+"</platformTranId>");
        sb.append("</sec:transferV2Req>");  
        sb.append("</soapenv:Body>");  
        sb.append("</soapenv:Evelope>");  
        return sb.toString();  
	}
}