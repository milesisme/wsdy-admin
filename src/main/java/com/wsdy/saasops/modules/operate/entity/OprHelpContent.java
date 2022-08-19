package com.wsdy.saasops.modules.operate.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;


@Setter
@Getter
@ApiModel(value = "help_content", description = "")
@Table(name = "help_content")
public class OprHelpContent implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "内容")
    private String helpContent;
    @ApiModelProperty(value = "内容标题")
    private String contentTitle;
    @ApiModelProperty(value = "分类id")
    private Integer helpCategoryId;

    @ApiModelProperty(value = "标题id")
    private Integer helpTitleId;

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






}