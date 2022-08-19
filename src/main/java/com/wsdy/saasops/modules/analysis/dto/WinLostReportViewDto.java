package com.wsdy.saasops.modules.analysis.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class WinLostReportViewDto {


    @ApiModelProperty(value = "下级代理")
    private WinLostReportDto subordinateAgent;

    @ApiModelProperty(value = "直属会员")
    private WinLostReportDto directAccount;

    @ApiModelProperty(value = "所有下级")
    private WinLostReportDto allSubordinates;
}
