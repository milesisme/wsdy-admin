package com.wsdy.saasops.modules.agent.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

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
public class AgyChannelDto implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 自增id
	 */
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
	
	@ApiModelProperty(value = "渠道分组名字")
	private String groupName;

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
	private Date updateTime;

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

	@ApiModelProperty(value = "开始时间")
	private String startTime;

	@ApiModelProperty(value = "结束时间")
	private String endTime;
}
