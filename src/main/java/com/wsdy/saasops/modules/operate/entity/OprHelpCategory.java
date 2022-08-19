package com.wsdy.saasops.modules.operate.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Setter
@Getter
@ApiModel(value = "help_category", description = "")
@Table(name = "help_category")
public class OprHelpCategory implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "分类名称")
    private String helpCategoryName;

    @ApiModelProperty(value = "前端排序id")
    private Integer outRangId;


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

    @ApiModelProperty(value = "状态：是否启用(0：未删除，1：删除")
    private Byte isdelete;

    @ApiModelProperty(value = "分类图片地址")
    private String picPath;

}