package com.wsdy.saasops.modules.agent.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 		通过渠道注册的用户注册记录
 * </p>
 *
 * @author daimon
 * @since 2021-11-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "agy_channel_log")
@ApiModel(value = "AgyChannelLog", description = "通过渠道注册的用户注册记录")
public class AgyChannelLog implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@ApiModelProperty(value = "id")
	private Integer id;

	@ApiModelProperty(value = "agy_channel.id")
	private Integer channelid;
	
	@ApiModelProperty(value = "accountid")
	private Integer accountId;

	@ApiModelProperty(value = "设备编号")
	private String deviceuuid;

	@ApiModelProperty(value = "用户第一次打开app时间")
	private Date registertime;

    @ApiModelProperty(value = "是否主号注册，默认是")
	private Boolean isMasterNum;
    
    @ApiModelProperty(value = "是否虚拟号注册，默认否")
   	private Boolean isVrtual;
    
    @ApiModelProperty(value = "虚拟号是否判断过，已经判断一次后不再判断")
    private Boolean isVrtualModif;
    
    @ApiModelProperty(value = "是否模拟器注册，默认否")
   	private Boolean isEmulator;


}
