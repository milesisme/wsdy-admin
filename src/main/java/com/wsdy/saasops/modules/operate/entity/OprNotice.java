package com.wsdy.saasops.modules.operate.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@ApiModel(value = "OprNotice", description = "运营管理公告通知")
@Table(name = "opr_notice")
public class OprNotice implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @Transient
    @ApiModelProperty(value = "删除")
    private Integer[] ids;

    @ApiModelProperty(value = "公告标题")
    private String noticeTitle;

    @ApiModelProperty(value = "公告内容")
    private String noticeContent;

    @ApiModelProperty(value = "开始时间")
    private String startTime;

    @ApiModelProperty(value = "结束时间")
    private String endTime;

    /*@ApiModelProperty(value = "公告(跑马灯)")
    private Byte enableNotice;

    @ApiModelProperty(value = "广播(弹窗)")
    private Byte enableAiring;*/
    @ApiModelProperty(value = "0：公告(跑马灯),1：广播(弹窗) 2全部")
    private String showType;

    @ApiModelProperty(value = "1开启，0禁用")
    private Byte available;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "更新人")
    private String updateUser;

    @ApiModelProperty(value = "更新时间")
    private String updateTime;

    @ApiModelProperty(value = "pc图片路径")
    private String pcPath;

    @ApiModelProperty(value = "移动图片路径")
    private String mbPath;

    @Transient
    @ApiModelProperty(value = "查询创建开始时间")

    private String createStart;
    @Transient
    @ApiModelProperty(value = "查询创建结束时间")
    private String createEnd;

    @Transient
    @ApiModelProperty(value = "状态复选框")
    private List<Integer> availables;

    @Transient
    @ApiModelProperty(value = "显示方式复选框")
    private String showTypes;


}