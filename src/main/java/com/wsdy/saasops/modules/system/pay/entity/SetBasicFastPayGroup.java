package com.wsdy.saasops.modules.system.pay.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;


@Setter
@Getter
@ApiModel(value = "SetBasicFastPayGroup", description = "快捷支付跟会员组的关系")
@Table(name = "set_basic_fastpay_group")
public class SetBasicFastPayGroup implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "fastPayId")
    private Integer fastPayId;

    @ApiModelProperty(value = "会员组")
    private Integer groupId;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "是否排队 0否 1是;不排队前端展示根据sort排序取第一个")
    private Integer isQueue;

    @Transient
    @ApiModelProperty(value = "支付所属:1 QQ 2微信 3京东 4网银 5支付宝 6同略云 7快捷支付 8银联扫码 9风云聚合 10BTP 11银行卡跳转 12个人二维码 13LBT 14卡转卡")
    private Integer paymentType;
}