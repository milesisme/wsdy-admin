package com.wsdy.saasops.modules.system.pay.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@ApiModel(value = "StatisticsSucRateRespDto", description = "DTO对象")
public class StatisticsSucRateRespDto {
    @Transient
    @ApiModelProperty(value = "支付平台名称类型:支付宝1，微信2，网银支付3，极速支付4，其他支付5")
    private String type;

    @Transient
    @ApiModelProperty(value = "总笔数")
    private BigDecimal totalNumSum;

    @Transient
    @ApiModelProperty(value = "成功笔数")
    private BigDecimal sucNumSum;

    @Transient
    @ApiModelProperty(value = "成功率")
    private BigDecimal sucRateSum;

    @Transient
    @ApiModelProperty(value = "支付线路存款成功率统计list")
    List<StatisticsSucRateDto> list;
}
