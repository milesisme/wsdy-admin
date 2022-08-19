package com.wsdy.saasops.modules.operate.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * @author daimon
 * 		查询set_gm_game， 优先获取set_gm_game的DepotName 传输类
 *
 */
@Data
@ApiModel(value = "GameDepotNameDto", description = "depotName传输类")
public class GameDepotNameDto implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "游戏名字")
    private String depotname;
    
    @ApiModelProperty(value = "游戏code")
    private String depotcode;
    
    @ApiModelProperty(value = "游戏类型code")
    private String catcode;

}