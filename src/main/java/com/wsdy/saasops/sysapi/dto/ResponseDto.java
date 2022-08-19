package com.wsdy.saasops.sysapi.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class ResponseDto {

    @ApiModelProperty(value = "错误明细")
    private String msg;

    @ApiModelProperty(value = "数据信息")
    private Map<String, Object> data;

    @ApiModelProperty("错误代码")
    private String code = "0";
}
