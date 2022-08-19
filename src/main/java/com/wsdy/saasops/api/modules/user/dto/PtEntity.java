package com.wsdy.saasops.api.modules.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "PT key")
public class PtEntity {
    @ApiModelProperty(value = "实体KEY")
    public String entityKey;
    @ApiModelProperty(value = "实体内容")
    public String entityContext;
}
