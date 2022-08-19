package com.wsdy.saasops.modules.operate.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@ApiModel(value = "JRedPacketDto", description = "红包")
public class JRedPacketDto {

    @ApiModelProperty(value = "活动范围")
    private ActivityScopeDto scopeDto;

    @ApiModelProperty(value = "是否审核 true是 false 否")
    private Boolean isAudit;

    @ApiModelProperty(value = "发放周期 每日1 每周7 自定义")
    private Integer period;

    @ApiModelProperty(value = "发放时间段")
    private List<RedPacketDateDto> dateDtos;

    @ApiModelProperty(value = "领取类型 0存款 1有效投注")
    private Integer drawType;

    @ApiModelProperty(value = "领取范围区间")
    private List<RedDrawScopeDto> scopeDtos;

    @ApiModelProperty(value = "红包类型 0固定金额 1随机金额")
    private Integer redType;

    @ApiModelProperty(value = "红包总金额")
    private BigDecimal redTotalAmount;

    @ApiModelProperty(value = "红包总数量")
    private BigDecimal redNumber;

    @ApiModelProperty(value = "活动规则")
    private List<RedPacketRuleDto> ruleDtos;
}
