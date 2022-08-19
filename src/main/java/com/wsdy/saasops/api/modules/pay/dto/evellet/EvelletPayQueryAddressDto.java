package com.wsdy.saasops.api.modules.pay.dto.evellet;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EvelletPayQueryAddressDto {
    @ApiModelProperty(value = "地址")
    private String address;
    @ApiModelProperty(value = "类型 TRC,ERC")
    private String type;
}
