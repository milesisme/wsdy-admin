package com.wsdy.saasops.modules.member.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WithdrawAuditDto {

    @ApiModelProperty(value = "orderNo")
    private String orderNo;

    @ApiModelProperty(value = "提款状态(2 4初审 3 5复审)")
    private Integer status;

    private List<AuditDetailDto> auditDetailDtos;
}
