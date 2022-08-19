package com.wsdy.saasops.modules.member.entity;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "会员银行卡增加", description = "会员银行卡增加")
@Table(name = "mbr_bankcard")
public class MbrBankcard implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@ApiModelProperty(value = "自增长Id")
	private Integer id;

    @Transient
    @ApiModelProperty(value = "会员Id")
    private Integer[] ids;

	@ApiModelProperty(value = "会员Id")
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

	@ApiModelProperty(value = "开户时用户手机号 (目前只有支付宝使用)")
	private String bindTimePhoneNum;

	@ApiModelProperty(value = "1开启, 0禁用")
	private Byte available;

	@ApiModelProperty(value = "")
	private String createTime;

	@ApiModelProperty(value = "1删除，0未删除")
	private Byte isDel;


	@Transient
	@ApiModelProperty(value="银行LOG")
	private String bankLog;

	@Transient
	@ApiModelProperty(value="取款时显示的银行图片")
	private String backBankImg;

	@Transient
	@ApiModelProperty(value="是否有提款 0没有 非0 有")
	private Integer isDraw;

	@Transient
	@ApiModelProperty(value="用户名")
	private String loginName;

	@Transient
	@ApiModelProperty(value="银行类型 1银行卡 2支付宝")
	private Integer bankType;
}