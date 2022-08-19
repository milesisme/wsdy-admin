package com.wsdy.saasops.modules.analysis.dto;

import com.wsdy.saasops.modules.analysis.entity.RptBetTotalModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AgentGameDateDto {

    @ApiModelProperty(value = "下级代理")
    private RptBetTotalModel subordinateAgent;

    @ApiModelProperty(value = "直属会员")
    private RptBetTotalModel directAccount;

    @ApiModelProperty(value = "所有下级")
    private RptBetTotalModel allSubordinates;
}
