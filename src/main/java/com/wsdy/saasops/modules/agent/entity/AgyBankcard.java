package com.wsdy.saasops.modules.agent.entity;

import lombok.Getter;
import lombok.Setter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import javax.persistence.*;


@Setter
@Getter
@ApiModel(value = "AgyBankcard", description = "AgyBankcard")
@Table(name = "agy_bankcard")
public class AgyBankcard implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "自增长Id")
    private Integer id;

    @ApiModelProperty(value = "代理id")
    private Integer accountId;

    @ApiModelProperty(value = "银行卡ID")
    private Integer bankCardId;

    @ApiModelProperty(value = "银行名称")
    private String bankName;

    @ApiModelProperty(value = "银行卡号")
    private String cardNo;

    @ApiModelProperty(value = "省")
    private String province;

    @ApiModelProperty(value = "市")
    private String city;

    @ApiModelProperty(value = "支行名称")
    private String address;

    @ApiModelProperty(value = "开户姓名")
    private String realName;

    @ApiModelProperty(value = "1开启, 0禁用")
    private Integer available;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "最后一次修改人的账号")
    private String modifyUser;

    @ApiModelProperty(value = "最后一次修改时间")
    private String modifyTime;

    @ApiModelProperty(value = "1删除，0未删除")
    private Integer isDel;


    @Transient
    @ApiModelProperty(value = "安全密码")
    private String securePwd;

    @Transient
    @ApiModelProperty(value = "银行卡号")
    private String cardNoEncryption;

    @Transient
    @ApiModelProperty(value="手机验证码 可选")
    private String mobileCaptchareg;

}