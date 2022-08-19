package com.wsdy.saasops.api.modules.activity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class HuPengRebateInfoDto {

    @ApiModelProperty("是否显示呼朋")
    private Boolean isShowHupengRebate;

    @ApiModelProperty("推广码")
    private String codeId;

}
