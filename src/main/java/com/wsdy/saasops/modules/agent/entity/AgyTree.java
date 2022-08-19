package com.wsdy.saasops.modules.agent.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import javax.persistence.Table;

@Setter
@Getter
@Table(name = "agy_tree")
public class AgyTree {

    @ApiModelProperty(value = "父结点")
    private Integer parentId;

    @ApiModelProperty(value = "子结点")
    private Integer childNodeId;

    @ApiModelProperty(value = "深度")
    private Integer depth;
}