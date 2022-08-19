package com.wsdy.saasops.modules.system.systemsetting.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
public class AiRecommend {

    @ApiModelProperty(" 0禁用 1启用")
    private Integer isEnble;

}
