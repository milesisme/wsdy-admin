package com.wsdy.saasops.modules.member.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Setter
@Getter
@ApiModel(value = "好友转账", description = "")
public class MbrFriendTransDto implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "验证code!")
    private String code;

    @ApiModelProperty(value = "转账会员名称")
    private String transLoginName;

    @ApiModelProperty(value = "转账会员Id")
    private Integer transAccountId;

    @ApiModelProperty(value = "收款账户名称")
    private String receiptLoginName;

    @ApiModelProperty(value = "收款人会员Id")
    private Integer receiptAccountId;

    @ApiModelProperty(value = "转账金额")
    private BigDecimal transAmount;

    @ApiModelProperty(value = "会员手机号!")
    private String mobile;

    @ApiModelProperty("会员密码")
    private String password;


}
