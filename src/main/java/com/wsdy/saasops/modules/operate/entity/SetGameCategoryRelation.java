package com.wsdy.saasops.modules.operate.entity;

import java.io.Serializable;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "SetGameCategoryRelation", description = "游戏分类关联表")
@Table(name = "set_game_category_relation")
public class SetGameCategoryRelation implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@ApiModelProperty(value = "自增id")
	private Integer id;

	@ApiModelProperty(value = "t_gm_game.id")
	private Integer gameid;

	@ApiModelProperty(value = "set_game_category.id")
	private Integer gamecategoryid;

	@ApiModelProperty(value = "排序")
	private Integer sortId;
	
	@Transient
	@ApiModelProperty(value = "游戏名查询t_gm_game的gamename")
	private String gamename;

}
