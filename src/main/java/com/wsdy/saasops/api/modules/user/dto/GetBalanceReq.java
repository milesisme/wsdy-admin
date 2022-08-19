package com.wsdy.saasops.api.modules.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetBalanceReq {
	private String uuid;
	private String brandId;
	private String brandPassword;
	@Override
    public String toString() {
		StringBuffer sb = new StringBuffer();  
        sb.append("<?xml version=\"1.0\"?>");  
        sb.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" ");  
        sb.append("xmlns:sec=\"http://secondarywallet.connect.omega.com/\">");
        sb.append("<soapenv:Header/>");  
        sb.append("<soapenv:Body>");
        sb.append("<sec:getBalanceReq>");  
        sb.append("<brandId>"+getBrandId()+"</brandId>");  
        sb.append("<brandPassword>"+getBrandPassword()+"</brandPassword>");
        sb.append("<uuid>"+getUuid()+"</uuid>");
        sb.append("</sec:getBalanceReq>");  
        sb.append("</soapenv:Body>");  
        sb.append("</soapenv:Evelope>");  
        return sb.toString();  
	}
}
