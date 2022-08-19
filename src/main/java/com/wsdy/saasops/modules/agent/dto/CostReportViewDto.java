package com.wsdy.saasops.modules.agent.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CostReportViewDto {


    @ApiModelProperty(value = "下级代理")
    private DepotCostDto subordinateAgent;

    @ApiModelProperty(value = "直属会员")
    private DepotCostDto directAccount;

    @ApiModelProperty(value = "所有下级总计")
    private DepotCostDto allSubordinates;
}
