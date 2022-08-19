package com.wsdy.saasops.api.modules.user.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "转账记录返回DTO", description = "转账记录返回DTO")
public class TransferResponseDto {

    @ApiModelProperty(value = "时间")
    private String transferTime;

    @ApiModelProperty(value = "金额")
    private Double amount;

    @ApiModelProperty(value = "游戏平台")
    private String depotName;

    @ApiModelProperty(value = "类别 0 支出(主账户->平台帐号) 1 收入（主账户<-平台帐号)")
    private Integer opType;

    @ApiModelProperty(value = "1冻结（处理中） 2失败 3 成功")
    private Integer status;
}
