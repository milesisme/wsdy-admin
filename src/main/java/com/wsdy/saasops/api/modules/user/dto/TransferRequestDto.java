package com.wsdy.saasops.api.modules.user.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "转账记录入参DTO", description = "转账记录入参DTO")
public class TransferRequestDto {

    @ApiModelProperty(value = "开始时间")
    private String startTime;

    @ApiModelProperty(value = "结束时间")
    private String entTime;

    @ApiModelProperty(value = "平台ID")
    private Integer depotId;

    @ApiModelProperty(value = "类别 0 支出(主账户->平台帐号) 1 收入（主账户<-平台帐号)")
    private Integer opType;

    @ApiModelProperty(hidden = true)
    private Integer accountId;
}
