package com.wsdy.saasops.modules.agent.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 *  	渠道对象
 * </p>
 *
 * @author daimon
 * @since 2021-11-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "agy_channel")
public class AgyChannel implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 自增id
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@ApiModelProperty(value = "id")
	private Integer id;

	/**
	 * 渠道编号
	 */
	@ApiModelProperty(value = "渠道编号")
	private String masterNum;

	/**
	 * 渠道副号
	 */
	@ApiModelProperty(value = "渠道副号")
	private String viceNum;

	/**
	 * 渠道名字
	 */
	@ApiModelProperty(value = "渠道名字")
	private String name;

	/**
	 * 分组id
	 */
	@ApiModelProperty(value = "分组id")
	private Integer groupId;

	/**
	 * 扣量比例
	 */
	@ApiModelProperty(value = "扣量比例")
	private BigDecimal deductRate;

	/**
	 * 是否打开 默认是
	 */
	@ApiModelProperty(value = "是否打开 默认是")
	private Boolean isOpen;

	/**
	 * 更新时间
	 */
	@ApiModelProperty(value = "更新时间")
	private String updateTime;

	/**
	 * 操作人
	 */
	@ApiModelProperty(value = "操作人")
	private String udapyeBy;
	
	/**
	 * 备注
	 */
	@ApiModelProperty(value = "备注")
	private String remark;
	
	@Transient
	@ApiModelProperty(value = "渠道推广域名")
	private String channelPromotionUrl;

}
