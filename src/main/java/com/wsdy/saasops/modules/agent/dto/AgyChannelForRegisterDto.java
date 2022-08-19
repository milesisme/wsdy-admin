package com.wsdy.saasops.modules.agent.dto;

import java.io.Serializable;

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
public class AgyChannelForRegisterDto implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "渠道编号")
	private String num;
	
	@ApiModelProperty(value = "设备编号")
	private String deviceuuid;
	
    @ApiModelProperty(value = "是否模拟器注册，默认否")
   	private boolean isEmulator;
	
}
