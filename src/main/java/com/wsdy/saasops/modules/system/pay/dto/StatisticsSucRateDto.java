package com.wsdy.saasops.modules.system.pay.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Transient;
import java.math.BigDecimal;

@Setter
@Getter
@ApiModel(value = "StatisticsSucRateDto", description = "DTO对象")
public class StatisticsSucRateDto {

    @Transient
    @ApiModelProperty(value = "")
    private Integer id;

    @Transient
    @ApiModelProperty(value = "支付平台名称")
    private String payname;

    @Transient
    @ApiModelProperty(value = "总笔数")
    private BigDecimal totalNum;

    @Transient
    @ApiModelProperty(value = "成功笔数")
    private BigDecimal sucNum;

    @Transient
    @ApiModelProperty(value = "成功率")
    private BigDecimal sucRate;

    @Transient
    @ApiModelProperty(value = "支付平台名称类型:支付宝1，微信2，网银支付3，极速支付4，其他支付5")
    private String type;

    @Transient
    @ApiModelProperty(value = "开始时间")
    private String startTime;

    @Transient
    @ApiModelProperty(value = "结束时间")
    private String endTime;
}
