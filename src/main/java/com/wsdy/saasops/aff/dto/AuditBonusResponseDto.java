package com.wsdy.saasops.aff.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AuditBonusResponseDto {

    @ApiModelProperty(value = "会员名")
    private String membercode;

    @ApiModelProperty(value = "收到日期")
    private String receiveddate;

    @ApiModelProperty(value = "扣款")
    private BigDecimal debit;

    @ApiModelProperty(value = "加款")
    private BigDecimal credit;

    @ApiModelProperty(value = "理由")
    private String reason;

    @ApiModelProperty(value = "备注")
    private String description;

    @ApiModelProperty(value = "订单状态 0 拒绝 1 通过 2待处理")
    private Integer status;

    private Integer id;

    private String orderNo;

    @ApiModelProperty(value = "1 其他优惠  2返水")
    private String type;

    @ApiModelProperty(value = "修改日期")
    private String updatedDate;
}
