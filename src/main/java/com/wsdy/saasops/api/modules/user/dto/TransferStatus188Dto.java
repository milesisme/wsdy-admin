package com.wsdy.saasops.api.modules.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferStatus188Dto {
	private String referenceNo;
	

	@Override
    public String toString() {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		sBuffer.append("<Request Method=\"GetTransferStatus\">");
		sBuffer.append(" <ReferenceNo>"+referenceNo+"</ReferenceNo>");
		sBuffer.append(" </Request>");
		return sBuffer.toString();
	}
}
