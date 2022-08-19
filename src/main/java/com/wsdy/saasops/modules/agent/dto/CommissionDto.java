package com.wsdy.saasops.modules.agent.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CommissionDto {

    @ApiModelProperty(value = "阶段")
    private Integer stage;

    @ApiModelProperty(value = "平台")
    private List<CommDepotListDto> depotListDtos;

    @ApiModelProperty(value = "存款人数 /人")
    private Boolean isDeposit;

    @ApiModelProperty(value = "存款人数最小")
    private Integer depositMin;

    @ApiModelProperty(value = "存款人数最大")
    private Integer depositMax;

    @ApiModelProperty(value = "投注人数 /人")
    private Boolean isBet;

    @ApiModelProperty(value = "投注人数最小")
    private Integer betMin;

    @ApiModelProperty(value = "投注人数最大")
    private Integer betMax;

    @ApiModelProperty(value = "有效投注额")
    private Boolean isValidBet;

    @ApiModelProperty(value = "有效投注额元")
    private BigDecimal validBetMin;

    @ApiModelProperty(value = "有效投注额元")
    private BigDecimal validBetMax;

    @ApiModelProperty(value = "净盈利额")
    private Boolean isNetProfit;

    @ApiModelProperty(value = "净盈利额 /元 最小")
    private BigDecimal netProfitMin;

    @ApiModelProperty(value = "净盈利额 /元 最大")
    private BigDecimal netProfitMax;
}
