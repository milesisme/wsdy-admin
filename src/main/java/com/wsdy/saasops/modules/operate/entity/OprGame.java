package com.wsdy.saasops.modules.operate.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "前台游戏类别显示", description = "")
public class OprGame {
    @ApiModelProperty(value = "游戏Id")
    private Integer id;
    @ApiModelProperty(value = "平台Id")
    private Integer depotId;
    @ApiModelProperty(value = "游戏名称")
    private String gameName;
    @ApiModelProperty(value = "游戏图片url链接")
    private String logo;
    @ApiModelProperty(value = "游戏图片url链接")
    private String logo2;
    @ApiModelProperty(value = "人气值")
    private Integer clickNum;
    @ApiModelProperty(value = "人气值site")
    private Integer popularity;
    @ApiModelProperty(value = "推荐度")
    private Integer recRating;
    @ApiModelProperty(value = "平台名称")
    private String depotName;
    @ApiModelProperty(value = "游戏类型")
    private String catName;
    @ApiModelProperty(value = "奖金池1是，0否")
    private String enablePool;
    @ApiModelProperty(value = "试玩1是，0否")
    private String enableTest;
    @ApiModelProperty(value = "好评度")
    private Integer goodNum;
    @ApiModelProperty(value = "开启PC端 1->开启，0－>禁用")
    private Byte enablePc;
    @ApiModelProperty(value = "开启移动端 1->开启，0－>禁用")
    private Byte enableMb;
    @ApiModelProperty(value = "开启App 1->开启，0－>禁用")
    private Byte enableApp;
    @ApiModelProperty(value = "是否支持试玩  0 否  1是  默认0")
    private Integer isTry;
    
	@ApiModelProperty(value = "桌号,真人游戏专有")
	private String tableCode;

    // 平台图片相关
    @ApiModelProperty(value = "PC 个性化图片")
    private String picUrl;
    @ApiModelProperty(value = "H5 个性化图片")
    private String mbPicUrl;
    @ApiModelProperty(value = "APP 个性化图片")
    private String appPicUrl;
    @ApiModelProperty(value = "PC LOGO")
    private String logoPc;
    @ApiModelProperty(value = "APP LOGO")
    private String logoApp;
    @ApiModelProperty(value = "MB LOGO")
    private String logoMb;
    @ApiModelProperty(value = "平台背景图")
    private String depotBackgroundImage;

    @ApiModelProperty(value = "横竖屏标志： 0是不限制  1=竖屏  2=横屏")
    private Byte orientation;
}
