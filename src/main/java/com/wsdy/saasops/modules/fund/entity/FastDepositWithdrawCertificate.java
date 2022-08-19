package com.wsdy.saasops.modules.fund.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;


@Setter
@Getter
@ApiModel(value = "FastDepositWithdrawCertificate", description = "极速存取款")
@Table(name = "fast_depositwithdraw_certificate")
public class FastDepositWithdrawCertificate implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "会员ID")
    private Integer accountId;
    @ApiModelProperty(value = "取款订单号")
    private String withdrawOrderno;
    @ApiModelProperty(value = "存款订单号")
    private String depositorderno;
    @ApiModelProperty(value = "取款保证金")
    private BigDecimal withdrawEnsure;
    @ApiModelProperty(value = "保证金状态 0已扣除 1已退回")
    private Integer ensurestatus;
    @ApiModelProperty(value = "取款凭证图片 图片地址1,图片地址2")
    private String withdrawPictures;
    @ApiModelProperty(value = "存款凭证图片 图片地址1,图片地址2")
    private String depositPictures;

    @ApiModelProperty(value = "取款凭证图片 图片地址1,图片地址2")
    @Transient
    private String withdrawPicturesList;
    @ApiModelProperty(value = "存款凭证图片 图片地址1,图片地址2")
    @Transient
    private String depositPicturesList;

}