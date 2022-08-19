package com.wsdy.saasops.modules.system.pay.entity;

import lombok.Getter;
import lombok.Setter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import javax.persistence.Table;


@Setter
@Getter
@ApiModel(value = "SetBasicPaymbrGroupRelation", description = "线上支付跟会员组的关系")
@Table(name = "set_basic_paymbrgrouprelation")
public class SetBasicPaymbrGroupRelation implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "groupId")
    private Integer groupId;

    @ApiModelProperty(value = "")
    private Integer onlinePayId;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "是否排队 0否 1是;不排队前端展示根据sort排序取第一个")
    private Integer isQueue;
}