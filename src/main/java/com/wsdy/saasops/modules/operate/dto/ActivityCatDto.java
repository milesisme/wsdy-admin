package com.wsdy.saasops.modules.operate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ActivityCatDto {

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "")
    private String catName;

    @ApiModelProperty(value = "备注")
    private String catMemo;

    @ApiModelProperty(value = "1开启，0隐藏")
    private Byte available;

    @ApiModelProperty(value = "活动列表")
    private List<Map<String,Object>> actActivities;
}
