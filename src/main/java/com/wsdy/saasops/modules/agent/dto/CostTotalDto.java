package com.wsdy.saasops.modules.agent.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CostTotalDto {

    @ApiModelProperty(value = "是否可以点开详情")
    private Boolean isDetails;

    @ApiModelProperty(value = "平台费用")
    private BigDecimal cost = BigDecimal.ZERO;
    
    @ApiModelProperty(value = "服务费用")
    private BigDecimal serviceCost =BigDecimal.ZERO;
    
}