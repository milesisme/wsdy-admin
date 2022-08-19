package com.wsdy.saasops.modules.operate.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "游戏类别", description = "")
public class TgmCatLabel {
	@ApiModelProperty(value = "标签id 为 游戏类别的名称或标签的id")
	private Integer id;// 游戏类别Id
	//private String catName;// 游戏类别名称
	//private String depotName;// 平台名称
	@JsonIgnore
	private Integer depotId;
	//private String labelId;// 标签Id
	@ApiModelProperty(value = "标签名称  为 游戏类别的名称或标签的名称")
	private String lableName;// 标签名称
	@ApiModelProperty(value = "标识这个是标签还是类别")
	private String showType;// 类别,标签
}
