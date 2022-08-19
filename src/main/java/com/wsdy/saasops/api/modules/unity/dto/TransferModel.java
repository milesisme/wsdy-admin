package com.wsdy.saasops.api.modules.unity.dto;

import com.wsdy.saasops.api.modules.apisys.entity.TGmApi;
import lombok.Data;

@Data
public class TransferModel{

    private Integer depotId;
    private String depotName;
    private String siteCode;
    private String userName;
    private TGmApi tGmApi;
    private String orderNo;
    private Double amount;
    private String password;
}
