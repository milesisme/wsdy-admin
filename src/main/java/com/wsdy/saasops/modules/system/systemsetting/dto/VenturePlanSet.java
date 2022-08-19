package com.wsdy.saasops.modules.system.systemsetting.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author daimon
 *
 *	合营计划
 */
@Setter
@Getter
public class VenturePlanSet {

    @ApiModelProperty("合营计划图")
    private String venturePlanPic;
    
    @ApiModelProperty("定义行业图")
    private String defineIndustryPic;

}
