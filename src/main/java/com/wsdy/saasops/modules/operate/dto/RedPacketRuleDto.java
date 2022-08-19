package com.wsdy.saasops.modules.operate.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@ApiModel(value = "RedPacketRuleDto", description = "红包活动规则")
public class RedPacketRuleDto {

    @ApiModelProperty(value = "阶段")
    private Integer stage;

    @ApiModelProperty(value = "发放金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "本阶段发放数量")
    private Integer quantity;

    @ApiModelProperty(value = "内定")
    private List<String> accs;
}
