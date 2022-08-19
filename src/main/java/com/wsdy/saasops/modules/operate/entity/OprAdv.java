package com.wsdy.saasops.modules.operate.entity;

import lombok.Getter;
import lombok.Setter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;


@Setter
@Getter
@ApiModel(value = "opr_adv", description = "")
@Table(name = "opr_adv")
public class OprAdv implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "标题")
    private String title;

    @ApiModelProperty(value = "展示客户端, 0: pc, 1：移动端， 2 pc、移动均显示")
    private Integer clientShow;

    @ApiModelProperty(value = "广告类型,0:首页轮播图，1：左对联，2：右对联，3：弹窗，4：优惠页面banner")
    private Integer advType;

    @ApiModelProperty(value = "有效期（开始）")
    private String useStart;

    @ApiModelProperty(value = "有效期（结束）")
    private String useEnd;

    @ApiModelProperty(value = "状态：是否启用(0：禁用，1：启用")
    private Byte available;

    @ApiModelProperty(value = "创建人")
    private String creater;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "更新人")
    private String updater;

    @ApiModelProperty(value = "更新时间")
    private String updateTime;

    @Transient
    @ApiModelProperty(value = "广告ids")
    private List<Integer> ids;

    @Transient
    @ApiModelProperty(value = "展示客户端复选框")
    private List<Integer> clientShows;

    @Transient
    @ApiModelProperty(value = "广告类型复选框")
    private List<Integer> advTypes;

    @Transient
    @ApiModelProperty(value = "状态复选框")
    private List<Integer> availables;

    //按照创建时间进行过滤查询
    @Transient
    @ApiModelProperty(value = "开始时间")
    private String startTime;

    @Transient
    @ApiModelProperty(value = "结束时间")
    private String endTime;

    @ApiModelProperty(value = "广告子类型,0：空值，1：首页，2：真人，3：电子，4：体育，5：彩票，6： 手机  7 棋牌, 8 我的")
    private Integer advTypeChild;

    @ApiModelProperty(value = "广告子类型序号")
    private Integer advTypeChildNum;

    @Transient
    @ApiModelProperty(value = "图片信息")
    private List<OprAdvImage> imageList;
}