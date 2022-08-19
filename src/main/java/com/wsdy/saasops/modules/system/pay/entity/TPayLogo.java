package com.wsdy.saasops.modules.system.pay.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;


@Setter
@Getter
@ApiModel(value = "TPayLogo", description = "TPayLogo")
@Table(name = "t_pay_logo")
public class TPayLogo implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "支付所属:1 QQ 2微信 3京东 4网银 5支付宝 6同略云 7快捷支付 8银联扫码 9风云聚合 10BTP 11银行卡跳转 12个人二维码 13LBT 14卡转卡")
    private Integer paymentType;

    @ApiModelProperty(value = "logo")
    private String bankLogo;

    @ApiModelProperty(value = "扫码logo")
    private String ewmLogo;

    @ApiModelProperty(value = "不可用logo")
    private String disableLogo;

    @ApiModelProperty(value = "name")
    private String name;
}