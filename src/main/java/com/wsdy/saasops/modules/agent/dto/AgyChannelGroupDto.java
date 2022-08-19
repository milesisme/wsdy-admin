package com.wsdy.saasops.modules.agent.dto;

import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 渠道分组对象
 * </p>
 *
 * @author daimon
 * @since 2021-11-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AgyChannelGroupDto implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "id")
	private Integer id;

	@ApiModelProperty(value = "分组名称")
	private String name;

	@ApiModelProperty(value = "更新时间")
	private Date updateTime;

	@ApiModelProperty(value = "操作人")
	private String udapyeBy;

	@ApiModelProperty(value = "开始时间")
	private String startTime;

	@ApiModelProperty(value = "结束时间")
	private String endTime;

}
