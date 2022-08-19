package com.wsdy.saasops.modules.operate.entity;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@ApiModel(value = "OprActCat", description = "活动分类")
@Table(name = "opr_act_cat")
public class OprActCat implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "")
    private String catName;

    @ApiModelProperty(value = "备注")
    private String catMemo;

    @ApiModelProperty(value = "1开启，0隐藏")
    private Byte available;

    @ApiModelProperty(value = "sort")
    private Integer sort;

    @ApiModelProperty(value = "建立时间")
    private String createTime;

    @ApiModelProperty(value = "1显示，0不显示")
    private String disable;

}