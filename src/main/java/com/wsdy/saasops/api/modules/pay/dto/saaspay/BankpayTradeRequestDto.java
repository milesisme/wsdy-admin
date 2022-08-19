package com.wsdy.saasops.api.modules.pay.dto.saaspay;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankpayTradeRequestDto extends PayTradeRequestDto{

    @ApiModelProperty(value = "收款卡的网银登录名，可以为空字符串")
    private String receiveUserName;

    @ApiModelProperty(value = "收款卡的卡号，不能为空")
    private String receiveCardNum;

    @ApiModelProperty(value = "收款卡的卡银行名称")
    private String bankName;

    @ApiModelProperty(value = "收款卡的卡支行名称")
    private String bankBranch;

//    @ApiModelProperty(value = "付款卡的网银登录名，可以为空字符串")
//    private String payUserName;

    @ApiModelProperty(value = "付款卡的卡号，不能为空")
    private String payCardNum;

    @ApiModelProperty(value = "附言，任意字符串")
    private String comment;

    @ApiModelProperty(value = "多语言站点使用code")
    private String verifyCode;
}
