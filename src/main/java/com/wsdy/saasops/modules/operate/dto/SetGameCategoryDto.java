package com.wsdy.saasops.modules.operate.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "SetGameCategoryDto", description = "三级游戏分类传输类")
public class SetGameCategoryDto implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "分类id")
	private Integer id;

	@ApiModelProperty(value = "平台二级游戏id，对应set_gm_game.id")
	private Integer gamelogoid;

	@ApiModelProperty(value = "分类名")
	private String name;
	
	@ApiModelProperty(value = "排序字段")
	private Integer sortId;

	@ApiModelProperty(value = "站点code")
	private String siteCode;
	
}
