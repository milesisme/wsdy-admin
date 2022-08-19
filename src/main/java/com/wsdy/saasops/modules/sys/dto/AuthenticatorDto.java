package com.wsdy.saasops.modules.sys.dto;

import lombok.Data;

@Data
public class AuthenticatorDto {

    private String secret;
    private String url;
}
