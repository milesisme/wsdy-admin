package com.wsdy.saasops.modules.agent.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 		渠道相關統計數據
 * </p>
 *
 * @author ${author}
 * @since 2021-11-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AgyChannelLogDto implements Serializable {

    private static final long serialVersionUID=1L;

    /**
	 * 自增id
	 */
	@ApiModelProperty(value = "id")
	private Integer id;

	@ApiModelProperty(value = "渠道编号")
	private String masterNum;

	@ApiModelProperty(value = "渠道副号")
	private String viceNum;

	@ApiModelProperty(value = "渠道名字")
	private String name;
	
	@ApiModelProperty(value = "渠道分组名字")
	private String groupName;

	@ApiModelProperty(value = "分组id")
	private Integer groupId;

	@ApiModelProperty(value = "扣量比例")
	private String deductRate;
	
	@ApiModelProperty(value = "总注册总量（主号+副号）")
	private Integer allRegisterTotal;
	
	@ApiModelProperty(value = "主号注册总量")
	private Integer registerTotal;
	
	@ApiModelProperty(value = "主号虚拟号注册总量")
	private Integer registerVirtualTotal;

	@ApiModelProperty(value = "主号模拟器注册总量")
	private Integer registerEmulatorTotal;
	
	@ApiModelProperty(value = "主号总充值量")
	private Integer rechargeTotal;
	
	/** 副号 ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓**/	
	@ApiModelProperty(value = "副号注册总量")
	private Integer viceRegisterTotal;
	
	@ApiModelProperty(value = "副号虚拟号注册总量")
	private Integer viceRegisterVirtualTotal;
	
	@ApiModelProperty(value = "副号模拟器注册总量")
	private Integer viceRegisterEmulatorTotal;
	
	@ApiModelProperty(value = "副号总充值量")
	private Integer viceRechargeTotal;
	/** 副号↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑**/	
	
	@ApiModelProperty(value = "是否打开 默认是")
	private Boolean isOpen;
	
	@ApiModelProperty(value = "是否打开 默认是")
	private String isOpenStr;

	@ApiModelProperty(value = "更新时间")
	private Date updateTime;

	@ApiModelProperty(value = "操作人")
	private String udapyeBy;
	
	@ApiModelProperty(value = "备注")
	private String remark;

	@ApiModelProperty(value = "开始时间")
	private String startTime;

	@ApiModelProperty(value = "结束时间")
	private String endTime;

}
