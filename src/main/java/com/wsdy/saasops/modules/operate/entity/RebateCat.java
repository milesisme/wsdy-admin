package com.wsdy.saasops.modules.operate.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Setter
@Getter
public class RebateCat implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "用户id")
    private Integer accountId;

    @ApiModelProperty(value = "分类id")
    private Integer catId;

    @ApiModelProperty(value = "有效投注额")
    private BigDecimal validBet;
}