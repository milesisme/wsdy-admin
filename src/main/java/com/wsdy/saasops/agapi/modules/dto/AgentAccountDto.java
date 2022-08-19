package com.wsdy.saasops.agapi.modules.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AgentAccountDto {

    @ApiModelProperty(value = "id")
    private Integer accountId;

    @ApiModelProperty(value = "代理账号")
    private String agyAccount;

    @ApiModelProperty(value = "手机号码")
    private String mobile;

    @ApiModelProperty(value = "qq")
    private String qq;

    @ApiModelProperty(value = "代理推广代码必须唯一")
    private String spreadCode;

    @ApiModelProperty(value = "真实名称")
    private String realName;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "微信")
    private String weChat;

    @ApiModelProperty(value = "佣金余额")
    private BigDecimal balance;

    @ApiModelProperty(value = "历史净盈利额")
    private BigDecimal netProfitBalance;

    @ApiModelProperty(value = "代理域名")
    private String domainUrl;

}
