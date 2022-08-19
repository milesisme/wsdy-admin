package com.wsdy.saasops.api.modules.pay.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class DongDongResponseDto {


    private String resultCode;
    private String resultMsg;
    private String success;
    private Map<String,Object> data;


}
