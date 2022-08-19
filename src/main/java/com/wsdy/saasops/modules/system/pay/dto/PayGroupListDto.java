package com.wsdy.saasops.modules.system.pay.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "PayGroupListDto", description = "支付分配")
public class PayGroupListDto {

    @ApiModelProperty(value = "会员组ID")
    private Integer groupId;

    @ApiModelProperty(value = "会员组name")
    private String groupName;

    @ApiModelProperty(value = "PayListDto")
    private PayListDto payListDto;
}
