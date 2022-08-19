package com.wsdy.saasops.modules.operate.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;


@Setter
@Getter
@ApiModel(value = "opr_adv_image", description = "广告图片")
@Table(name = "opr_adv_image")
public class OprAdvImage implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "广告id")
    private Integer advId;

    @ApiModelProperty(value = "pc图片路径")
    private String pcPath;

    @ApiModelProperty(value = "移动图片路径")
    private String mbPath;

    @ApiModelProperty(value = "图片跳转目标，0：站内；1：站外  2:不跳转  ")
    private Integer picTarget;

    @ApiModelProperty(value = "站内跳转类型： 0 站内子页面； 1优惠活动 ")
    private Integer inType;

    @ApiModelProperty(value = "站内子页面类型：   1体育    3真人  5电子  6棋牌    9 电竞   12彩票     20 合营")
    private Integer inPageType;

    @ApiModelProperty(value = "活动分类")
    private Integer actId;

    @ApiModelProperty(value = "活动")
    private Integer activityId;

    @ApiModelProperty(value = "站外路径")
    private String outStation;
}