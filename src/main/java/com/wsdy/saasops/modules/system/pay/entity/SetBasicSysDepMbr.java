package com.wsdy.saasops.modules.system.pay.entity;

import lombok.Getter;
import lombok.Setter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import javax.persistence.Table;


@Setter
@Getter
@ApiModel(value = "SetBasicSysDepMbr", description = "银行卡跟会员组关系")
@Table(name = "set_basic_sys_dep_mbr")
public class SetBasicSysDepMbr implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "depositId")
    private Integer depositId;

    @ApiModelProperty(value = "会员组")
    private Integer groupId;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "层数")
    private Integer tier;

    @ApiModelProperty(value = "是否排队 0否 1是;不排队前端展示根据sort排序取第一个")
    private Integer isQueue;
}