package com.wsdy.saasops.api.modules.pay.dto.uxiangpay;

import com.wsdy.saasops.modules.system.pay.entity.SysDeposit;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class JuFuPayCreateOderReqDto implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "用户id")
    private Integer accountId;

	@ApiModelProperty(value = "在线支付通道id")
	private Integer onlinePayId;

	@ApiModelProperty(value = "用户平台商户号")
	private String merNo;

	@ApiModelProperty(value = "用户平台商户密钥")
	private String evbBankId;

	@ApiModelProperty(value = "支付金额")
	private String fee;

	@ApiModelProperty(value = "站点标识")
	private String siteCode;

	@ApiModelProperty(value = "站点标识")
	private Integer terminal;


}
