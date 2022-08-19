package com.wsdy.saasops.agapi.modules.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletFlowResponseDto {

    @ApiModelProperty(value = "时间")
    private String time;

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "ATK 用户提款,ACZ代充钱包上分，ADZ代理代充，" +
            "ASF代理上分，AYS 转入游戏钱包，ADC 转入代充钱包，ACK 存款 ，GA人工增加，GM人工减少,MSF会员上分")
    private String type;

    @ApiModelProperty(value = "账变金额(元)")
    private BigDecimal amount;

    @ApiModelProperty(value = "余额(元)")
    private BigDecimal balance;

    @ApiModelProperty(value = "状态 0拒绝 1通过 2处理中")
    private Integer status;

    @ApiModelProperty(value = "接收人会员")
    private String merAccount;
    
    @ApiModelProperty(value = "上分会员id")
    private String agyAccount;

    @ApiModelProperty(value = "上分账号")
    private String loginName;

    @ApiModelProperty(value = "备注")
    private String remarks;
}
