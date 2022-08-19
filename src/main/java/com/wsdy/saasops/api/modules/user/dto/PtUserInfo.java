package com.wsdy.saasops.api.modules.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "PT 会员信息")
public class PtUserInfo {
    @ApiModelProperty(value = "会员是否在线")
    private String ACCOUNTBUSINESSPHASE;
    @ApiModelProperty(value = "未知")
    private String ACCOUNTMIGRATED;
    @ApiModelProperty(value = "会员余额")
    private String BALANCE;
    @ApiModelProperty(value = "当前币种")
    private String CURRENCY;
}
