package com.wsdy.saasops.modules.operate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class GameForCategoryDto {

	@ApiModelProperty(value = "游戏id")
	private Integer gameId;

	@ApiModelProperty(value = "排序")
	private Integer sortNum;

}
