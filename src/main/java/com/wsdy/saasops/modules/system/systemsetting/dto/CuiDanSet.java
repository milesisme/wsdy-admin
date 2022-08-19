package com.wsdy.saasops.modules.system.systemsetting.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CuiDanSet {
    @ApiModelProperty("催单时间")
    private Integer cuiDan;
}
