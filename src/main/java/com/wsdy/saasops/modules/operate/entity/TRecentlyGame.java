package com.wsdy.saasops.modules.operate.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class TRecentlyGame implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;

    @ApiModelProperty(value = "会员id")
    private Integer userId;

    @ApiModelProperty(value = "会员名称")
    private String userName;

    @ApiModelProperty(value = "平台代码")
    private Integer depotId;

    @ApiModelProperty(value = "游戏ID")
    private String gameId;

    @ApiModelProperty(value = "游戏名称")
    private String gameName;

    @ApiModelProperty(value = "游戏代码")
    private String gameCode;

    @ApiModelProperty(value = "个性化图片")
    private String gameLogo;

    @ApiModelProperty(value = "游戏简单描述")
    private String gameTag;

    @ApiModelProperty(value = "1开启，0禁用")
    private Byte available;

    @ApiModelProperty(value = "个性化图片")
    private String logo;

    @ApiModelProperty(value = "详细备注")
    private String memo;

    @ApiModelProperty(value = "游戏名称(英文)")
    private String gameNameEn;

    @ApiModelProperty(value = "URL")
    private String url;

    @ApiModelProperty(value = "点击量")
    private Integer clickNum;

    @ApiModelProperty(value = "好评度")
    private Integer goodNum;

    @ApiModelProperty(value = "过去一个月这个游戏玩家数")
    private Integer monthPer;

    @ApiModelProperty(value = "昨天该游戏玩家数")
    private Integer lastdayPer;

    @ApiModelProperty(value = "游戏类别名称")
    private String catName;

    @ApiModelProperty(value = "游戏平台名称")
    private String depotName;

    @ApiModelProperty(value = "进入游戏时间")
    private String entryTime;
}