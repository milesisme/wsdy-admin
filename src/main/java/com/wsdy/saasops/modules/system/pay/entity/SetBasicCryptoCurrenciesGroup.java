package com.wsdy.saasops.modules.system.pay.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Table;

@Setter
@Getter
@ApiModel(value = "SetBasicCryptoCurrenciesGroup", description = "数字货币会员组关联")
@Table(name = "set_basic_cryptocurrencies_group")
public class SetBasicCryptoCurrenciesGroup {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "数字货币支付id")
    private Integer currenciesId;

    @ApiModelProperty(value = "会员组")
    private Integer groupId;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "是否排队 0否 1是;不排队前端展示根据sort排序取第一个")
    private Integer isQueue;

}
