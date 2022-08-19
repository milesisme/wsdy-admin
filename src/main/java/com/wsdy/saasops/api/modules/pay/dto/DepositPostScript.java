package com.wsdy.saasops.api.modules.pay.dto;

import java.math.BigDecimal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel(value = "公司入款申请成功后信息显示", description = "公司入款申请成功后信息显示")
@Getter
@Setter
public class DepositPostScript {
    @ApiModelProperty(value = "银行名称")
    private String bankName;
    @ApiModelProperty(value = "银行卡号 或第三方支付的账号")
    private String bankAccount;
    @ApiModelProperty(value = "真实姓名")
    private String realName;
    @ApiModelProperty(value = "支行名称")
    private String bankBranch;
    @ApiModelProperty(value = "申请存款金额")
    private BigDecimal depositAmount;
    @ApiModelProperty(value = "附言单号")
    private String depositPostscript;
    @ApiModelProperty(value = "支付方式 0 银行卡转账 1二维码")
    private Integer urlMethod = 0;
    @ApiModelProperty(value = "二维码url")
    private String url;
    @ApiModelProperty(value = "存款人姓名")
    private String depositorsName;
    @ApiModelProperty(value = "存款人账号 LBT")
    private String depositUserAcc;

    // 极速存取款
    @ApiModelProperty(value = "提交状态 false没有匹配到银行卡 true匹配成功")
    private Boolean succeed;
    @ApiModelProperty(value = "错误信息")
    private String error;
    @ApiModelProperty(value = "订单号")
    private String orderNo;
    @ApiModelProperty(value = "签名")
    private String sign;
    @ApiModelProperty(value = "收款接收人")
    private String receiveUserName;
    @ApiModelProperty(value = "接收卡号")
    private String receiveCardNum;
    @ApiModelProperty(value = "订单ID")
    private Integer orderId;
}
