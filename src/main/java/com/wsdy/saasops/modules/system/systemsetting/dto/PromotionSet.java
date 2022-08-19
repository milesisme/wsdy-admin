package com.wsdy.saasops.modules.system.systemsetting.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PromotionSet {

    @ApiModelProperty("0会员 1代理 2渠道的推广域名")
    private Integer type;

    @ApiModelProperty("推广域名")
    private Integer siteUrlId;

    @ApiModelProperty("备注")
    private String memo;
}
