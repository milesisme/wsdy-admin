package com.wsdy.saasops.modules.operate.dto;

import com.wsdy.saasops.common.constants.Constants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "JWaterRebatesDto", description = "返水优惠")
public class JWaterRebatesDto {

    @ApiModelProperty(value = "会员范围 0全部会员 1层级会员")
    private Integer scope = Constants.EVNumber.one;

    @ApiModelProperty(value = "结算周期 0日结 1周结")
    private Integer period;

    @ApiModelProperty(value = "规则")
    private JWaterRebatesNeDto rebatesNeDto;
}
