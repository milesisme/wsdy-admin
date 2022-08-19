package com.wsdy.saasops.modules.operate.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Table;
import javax.persistence.Transient;

@Setter
@Getter
@ApiModel(value = "t_opt_adv_banner", description = "活动banner模板")
@Table(name = "t_opt_adv_banner")
public class AdvBanner {
    @ApiModelProperty(value = "模板Id, 1-12值")
    private Integer evebNum;
    @ApiModelProperty(value = "0：pc，1：移动，2：移动pc均显示")
    private Integer clientShow;
    @ApiModelProperty(value = "0：pc，1：移动，2：移动pc均显示")
    private Integer advType;
    @ApiModelProperty(value = "图片跳转目标，0：站内；1：站外")
    private Integer picTarget;
    @ApiModelProperty(value = "活动分类")
    private Integer actId;
    @ApiModelProperty(value = "活动id")
    private Integer activityId;
    @ApiModelProperty(value = "站外路径")
    private String outStation;
    @ApiModelProperty(value = "广告路径")
    private String path;
    @ApiModelProperty(value = "广告路径")
    private String picPcPath;
    @ApiModelProperty(value = "移动广告路径")
    private String picMbPath;
    @Transient
    @ApiModelProperty(value = "标题")
    private String title;
    @Transient
    @ApiModelProperty(value = "站内跳转类型： 0 站内子页面； 1优惠活动 ")
    private Integer inType;
    @Transient
    @ApiModelProperty(value = "站内子页面类型：   1体育    3真人  5电子  6棋牌    9 电竞   12彩票     20 合营")
    private Integer inPageType;
}
