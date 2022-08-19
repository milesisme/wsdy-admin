package com.wsdy.saasops.modules.operate.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "set_game_category", description = "三级游戏分类表")
@Table(name = "set_game_category")
public class SetGameCategory implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@ApiModelProperty(value = "分类id")
	private Integer id;

	@ApiModelProperty(value = "平台二级游戏id，对应set_gm_game.id")
	private Integer gamelogoid;

	@ApiModelProperty(value = "分类名")
	private String name;
	
	@ApiModelProperty(value = "数据中心表t_gm_cat.id")
	private Integer tGmCatId;
	
	@ApiModelProperty(value = "针对数据中心t_gm_cat.id做删除标记，删除后不展示，站点分类数据做物理删除")
	private Boolean isDelete;

	@ApiModelProperty(value = "排序字段")
	private Integer sortId;

	@ApiModelProperty(value = "更新时间")
	private String updateTime;

	@ApiModelProperty(value = "操作人")
	private String updateBy;

	@Transient
    @ApiModelProperty(value = "游戏分类关联表")
    private List<SetGameCategoryRelation> gameCategoryRelations;
	
	@Transient
	@ApiModelProperty(value = "游戏名查询t_gm_game的gamename")
	private String gameName;
	
	@Transient
	@ApiModelProperty(value = "是否t_gm_cat 的id")
	private Boolean isTGmCatId;
	
	@Transient
	@ApiModelProperty(value = "站点code")
	private String siteCode;
	
}
