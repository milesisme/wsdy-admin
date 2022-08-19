package com.wsdy.saasops.modules.system.systemsetting.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Staff
 *	原生投注设置
 */
@Setter
@Getter
public class NativeSports {
	
    @ApiModelProperty("轮播模块是否启用")
    private String isOpenCarousel;
    
    @ApiModelProperty("公告模块是否启用")
    private String isOpenNotice;
}
