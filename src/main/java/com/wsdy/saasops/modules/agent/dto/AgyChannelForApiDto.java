package com.wsdy.saasops.modules.agent.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 渠道对象 用户端返回专用
 * </p>
 *
 * @author daimon
 * @since 2021-11-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AgyChannelForApiDto implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "渠道编号")
	private String masterNum;

	@ApiModelProperty(value = "渠道副号")
	private String viceNum;

	@ApiModelProperty(value = "扣量比例")
	private BigDecimal deductRate;

}
