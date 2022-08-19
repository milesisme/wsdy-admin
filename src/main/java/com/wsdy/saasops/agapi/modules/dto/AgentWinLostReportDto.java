package com.wsdy.saasops.agapi.modules.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AgentWinLostReportDto {

    @ApiModelProperty(value = "null厅主 0股东 1总代 2代理  account 会员")
    private String level;

    @ApiModelProperty(value = "投注人数")
    private Long total;

    @ApiModelProperty(value = "投注金额")
    private BigDecimal betTotal;

    @ApiModelProperty(value = "有效投注额")
    private BigDecimal validbetTotal;

    @ApiModelProperty(value = "派彩")
    private BigDecimal payoutTotal;

    @ApiModelProperty(value = "类别")
    private String categoryTotal;

    @ApiModelProperty(value = "代理账户 or 会员账户")
    private String username;
    @ApiModelProperty(value = "转入")
    private BigDecimal depositAmount;
    @ApiModelProperty(value = "转出")
    private BigDecimal drawingAmount;
    @ApiModelProperty(value = "红利")
    private BigDecimal depositedAmount;
    @ApiModelProperty(value = "返利")
    private BigDecimal rebateAmount;


}

