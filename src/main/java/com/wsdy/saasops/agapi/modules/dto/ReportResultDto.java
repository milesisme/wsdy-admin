package com.wsdy.saasops.agapi.modules.dto;

import com.wsdy.saasops.common.constants.Constants;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReportResultDto {

    @ApiModelProperty(value = "时间")
    private String time;

    @ApiModelProperty("新增会员")
    private Integer newMbrs = Constants.EVNumber.zero;

    @ApiModelProperty(value = "存款人数")
    private Integer totalDepositBalanceNum = Constants.EVNumber.zero;

    @ApiModelProperty(value = "首存人数：时间范围内首存人数")
    private Integer totalNewDeposits = Constants.EVNumber.zero;

    @ApiModelProperty(value = "存款金额：总存款")
    private BigDecimal totalDepositBalance = BigDecimal.ZERO;

    @ApiModelProperty(value = "提款人数")
    private Integer totalDrawAmountNum = Constants.EVNumber.zero;

    @ApiModelProperty(value = "提款金额：总提款")
    private BigDecimal totalDrawAmount = BigDecimal.ZERO;

    @ApiModelProperty(value = "有效投注额：总有效投注")
    private BigDecimal totalValidBets = BigDecimal.ZERO;

    @ApiModelProperty(value = "红利优惠")
    private BigDecimal totalBonusAmount = BigDecimal.ZERO;
    
    @ApiModelProperty(value = "任务返利")
    private BigDecimal totalTaskbonus = BigDecimal.ZERO;

    @ApiModelProperty(value = "净盈利")
    private BigDecimal totalProfit = BigDecimal.ZERO;

    @ApiModelProperty(value = "总派彩")
    private BigDecimal totalPayout = BigDecimal.ZERO;

    @ApiModelProperty(value = "代充次数")
    private Integer totalSurrogateNum = Constants.EVNumber.zero;

    @ApiModelProperty(value = "代充金额")
    private BigDecimal totalSurrogateAmount = BigDecimal.ZERO;
    
    @ApiModelProperty(value = "人工资金调整")
    private BigDecimal calculateProfit = BigDecimal.ZERO;

    @ApiModelProperty(value = "平台费用")
    private BigDecimal cost = BigDecimal.ZERO;
    
    @ApiModelProperty(value = "服务费用")
    private BigDecimal serviceCost =BigDecimal.ZERO;
}
