package com.wsdy.saasops.modules.member.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Setter
@Getter
@ApiModel(value = "会员加密货币钱包", description = "会员加密货币钱包")
@Table(name = "mbr_cryptocurrencies")
public class MbrCryptoCurrencies implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@ApiModelProperty(value = "自增长Id")
	private Integer id;
	@ApiModelProperty(value = "会员Id")
	private Integer accountId;
	@ApiModelProperty(value = "t_bs_bank表id")
	private Integer bankCardId;
	@ApiModelProperty(value = "钱包类型")
	private String walletName;
	@ApiModelProperty(value = "钱包地址")
	private String walletAddress;
	@ApiModelProperty(value = "货币类型 USDT")
	private String currencyCode;
	@ApiModelProperty(value = "协议类型 ERC20 TRC20")
	private String currencyProtocol;
	@ApiModelProperty(value = "1开启, 0禁用,2平台禁用改渠道<主要用于前端展示>")
	private Byte available;
	@ApiModelProperty(value = "")
	private String createTime;
	@ApiModelProperty(value = "1删除，0未删除")
	private Byte isDel;
	@ApiModelProperty(value = "钱包LogId")
	private Integer walletId;

	@Transient
	@ApiModelProperty(value = "会员Ids 删除送")
	private Integer[] ids;
	@Transient
	@ApiModelProperty(value = "钱包图标路径")
	private String bankLog;
	@Transient
	@ApiModelProperty(value = "钱包背景路径")
	private String walletBackImg;

	@Transient
	@ApiModelProperty(value="是否有提款 0没有 非0 有")
	private Integer isDraw;
}