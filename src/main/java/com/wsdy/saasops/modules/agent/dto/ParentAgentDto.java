package com.wsdy.saasops.modules.agent.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ParentAgentDto {

    @ApiModelProperty(value = "父级id")
    private Integer parentId;
    
    @ApiModelProperty(value = "代理父级账号")
    private String parentAccount;
    
    @ApiModelProperty(value = "")
    private Integer agyId;

    @ApiModelProperty(value = "代理线导出选择类型")
    private String agyAccount;
}