package com.wsdy.saasops.api.modules.unity.dto;

import com.wsdy.saasops.api.modules.apisys.entity.TGmApi;
import lombok.Data;

@Data
public class LoginModel {

    private Integer depotId;
    private String depotName;
    private String password;
    private String siteCode;
    private String userName;
    private TGmApi tGmApi;

}
