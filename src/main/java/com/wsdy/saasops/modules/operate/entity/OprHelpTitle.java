package com.wsdy.saasops.modules.operate.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;


@Setter
@Getter
@ApiModel(value = "help_title", description = "")
@Table(name = "help_title")
public class OprHelpTitle implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "标题名称")
    private String titleName;

    @ApiModelProperty(value = "分类id")
    private Integer helpCategoryId;


    @ApiModelProperty(value = "创建人")
    private String creater;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "更新人")
    private String updater;

    @ApiModelProperty(value = "更新时间")
    private String updateTime;

    @ApiModelProperty(value = "状态：是否删除(0：未删除，1：删除")
    private Byte isdelete;

    @Transient
    @ApiModelProperty(value = "内容")
    private List<OprHelpContent> oprHelpContentList;




}