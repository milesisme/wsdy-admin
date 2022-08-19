package com.wsdy.saasops.modules.base.entity;

import lombok.Getter;
import lombok.Setter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;


@Setter
@Getter
@ApiModel(value = "OprAdv", description = "")
@Table(name = "opr_adv")
public class ToprAdv implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "标题")
    private String title;

    @ApiModelProperty(value = "1真人，2电子，3，彩票，4体育")
    private Byte gameCat;

    @ApiModelProperty(value = "平台id号")
    private Integer depotId;

    @ApiModelProperty(value = "展示客户端, 0: pc, 1：移动端， 2 pc、移动均显示")
    private Integer clientShow;

    @ApiModelProperty(value = "广告类型,0:首页轮播图，1：左对联，2：右对联，3：弹窗")
    private Integer advType;

    @ApiModelProperty(value = "pc广告路径")
    private String picPcPath;

    @ApiModelProperty(value = "移动广告路径")
    private String picMbPath;

    @ApiModelProperty(value = "有效期（开始）")
    private String useStart;

    @ApiModelProperty(value = "有效期（结束）")
    private String useEnd;

    @ApiModelProperty(value = "状态：是否启用(0：禁用，1：启用")
    private Byte available;

    @ApiModelProperty(value = "是否删除(0：否，1：是)")
    private Byte isDelete;

    @ApiModelProperty(value = "创建人")
    private String creater;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "更新人")
    private String updater;

    @ApiModelProperty(value = "更新时间")
    private String updateTime;

    @ApiModelProperty(value = "活动分类")
    private Integer actId;

    @ApiModelProperty(value = "活动")
    private Integer activityId;

    @ApiModelProperty(value = "站外路径")
    private String outStation;
    @Transient
    @ApiModelProperty(value = "广告ids")
    private Integer[] ids;

}