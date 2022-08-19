package com.wsdy.saasops.modules.sys.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "会员统计权限")
public class StatMenuDto {

    @ApiModelProperty(value = "是否有会员入款查看统计，true是 false 否")
    private Boolean isDepositCount = Boolean.TRUE;

    @ApiModelProperty(value = "是否有提款初审查看统计，true是 false 否")
    private Boolean isWithdrawFirstCount = Boolean.TRUE;

    @ApiModelProperty(value = "是否有提款复审查看统计，true是 false 否")
    private Boolean isWithdrawReviewCount = Boolean.TRUE;

    @ApiModelProperty(value = "是否有红利申请查看统计，true是 false 否")
    private Boolean isBonusCount = Boolean.TRUE;
}
