package com.wsdy.saasops.modules.member.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Setter
@Getter
@ApiModel(value = "会员银行卡历史记录", description = "会员银行卡历史记录")
@Table(name = "mbr_bankcard_history")
public class MbrBankcardHistory implements Serializable {
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

	@ApiModelProperty(value = "1开启, 0禁用")
	private Byte available;

	@ApiModelProperty(value = "")
	private String createTime;

	@ApiModelProperty(value = "1删除，0未删除")
	private Byte isDel;

	@ApiModelProperty(value = "1解绑，0未解绑")
	private Byte isUse;

	@ApiModelProperty(value = "更新人")
	private String updater;

	@ApiModelProperty(value = "更新时间")
	private String updateTime;

	@Transient
	@ApiModelProperty(value="用户名")
	private String loginName;

}