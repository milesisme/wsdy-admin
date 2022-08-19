package com.wsdy.saasops.agapi.modulesV2.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;


@Data
public class BillRecordListDto {

    @ApiModelProperty(value = "交易编号")
    private String orderNo;

    @ApiModelProperty(value = "时间")
    private String orderTime;

    @ApiModelProperty(value = "账户")
    private String loginName;

    @ApiModelProperty(value = "用户名称")
    private String realname;

    @ApiModelProperty(value = "类型")
    private String codename;

    @ApiModelProperty(value = "金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "操作类型，0 支出1 收入")
    private Integer opType;

    @ApiModelProperty(value = "操作后余额")
    private BigDecimal afterBalance;

    @ApiModelProperty(value = "执行人")
    private String createuser;

    private String financialcode;

    private Integer agentId;

    private String startTime;

    private String endTime;

    private String agyAccount;
}
