package com.wsdy.saasops.modules.member.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "MbrAccountLastBetDate", description = "投注大于100的最近一天传输类")
public class MbrAccountLastBetDate implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "会员账号")
    private String loginName;
    
    @ApiModelProperty(value = "投注大于100的最近一天的时间")
    private String betDate;

}