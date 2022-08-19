package com.wsdy.saasops.sysapi.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SptvBonusDto {

    @ApiModelProperty(value = "红利金额")
    private BigDecimal bonusAmount;

    @ApiModelProperty(value = "稽核倍數")
    private BigDecimal auditMultiple;

    @ApiModelProperty(value = "注单号")
    private String betNumber;

    private String siteCode;

    private String loginName;
}
