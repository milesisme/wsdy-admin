package com.wsdy.saasops.modules.member.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Table;

@Setter
@Getter
@Table(name = "mbr_tree")
public class MbrTree {

    @ApiModelProperty(value = "父结点")
    private Integer parentId;

    @ApiModelProperty(value = "子结点")
    private Integer childNodeId;

    @ApiModelProperty(value = "深度")
    private Integer depth;
}