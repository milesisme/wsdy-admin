package com.wsdy.saasops.api.modules.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "电子分类查询传参", description = "")
public class ElecGameDto {
    @ApiModelProperty(value = "平台Id,必传")
    private Integer depotId;
    @ApiModelProperty(value = "游戏名称，可不传")
    private String lableName;
    @ApiModelProperty(value = "电子游艺分类类别或标签 类别 ,可不传")
    private Integer id;
    @ApiModelProperty(value = "分类或标签标识 如有传ID此参数必传  根据接口catLabelList中showType(1分类，2代表是标签)")
    private String showType;
    @ApiModelProperty(value = "排序标识,1人气，2，热度，可不传")
    private String sortMark;
    @ApiModelProperty("支持试玩,0或null不支试玩，1支持，可不传")
    private Byte enableTest;
    @ApiModelProperty("0 升序，1 降序,为空默认降序")
    private String sortWay;
    @ApiModelProperty(value = " 0代表PC端,1代表手机")
    private Byte terminal;
    @ApiModelProperty(value = "站点code" )
    private String siteCode;

	@ApiModelProperty(value = "是否t_gm_cat 的id")
	private Boolean isTGmCatId;
}
