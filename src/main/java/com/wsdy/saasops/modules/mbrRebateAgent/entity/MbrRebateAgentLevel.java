package com.wsdy.saasops.modules.mbrRebateAgent.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Setter
@Getter
@ApiModel(value = "MbrRebateAgentLevel", description = "全民代理会员级别")
@Table(name = "mbr_rebate_agent_level")
public class MbrRebateAgentLevel implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "等级名称")
    private String tierName;
    @ApiModelProperty(value = "等级数字：0开始")
    private Integer accountLevel;
    @ApiModelProperty(value = "是否启用：1开启，0禁用")
    private Integer available;
    @ApiModelProperty(value = "创建时间")
    private String createTime;
    @ApiModelProperty(value = "创建人")
    private String createUser;
    @ApiModelProperty(value = "修改人")
    private String modifyUser;
    @ApiModelProperty(value = "修改时间")
    private String modifyTime;

    @Transient
    @ApiModelProperty(value = "该级别会员人数")
    private Integer memNum;
}