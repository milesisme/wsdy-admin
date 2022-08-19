package com.wsdy.saasops.api.modules.transfer.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;


@Setter
@Getter
@ApiModel(value = "转帐DTO", description = "转帐DTO")
public class BillRequestDto {

    @ApiModelProperty(value = "资金转入平台")
    private Integer depotId;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "产生交易记录order")
    private String orderNo;

    @ApiModelProperty(value = "操作类型，0 支出1 收入")
    private Integer opType;

    @ApiModelProperty(value = "memo")
    private String memo;

    @ApiModelProperty(value = "游戏Id")
    private Integer gameId;

    @ApiModelProperty(value = "0代表PC端,1代表手机")
    private Byte terminal;

    @ApiModelProperty(value = "会员ID")
    private Integer accountId;

    @ApiModelProperty(value = "转帐金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "会员名")
    private String loginName;

    @ApiModelProperty(value = "游戏平台名字")
    private String depotName;

    @ApiModelProperty(value = "平台编码")
    private String depotCode;

    @ApiModelProperty(value = "ip", hidden = true)
    private String ip;

    @ApiModelProperty(value = "id", hidden = true)
    private Integer id;

    @ApiModelProperty(value = "平台操作前余额")
    private BigDecimal depotBeforeBalance;

    @ApiModelProperty(value = "dev：0 PC，3 H5")
    private Byte transferSource;

    @ApiModelProperty(value = "dev：0 PC，3 H5")
    private String dev;

    @ApiModelProperty(value = "分类ID")
    private Integer catId;

    @ApiModelProperty(value = "优惠ID")
    private Integer bonusId;

    @ApiModelProperty(value = "优惠金额")
    private BigDecimal bonusAmount = BigDecimal.ZERO;

    @ApiModelProperty(value = "转出是否判断优惠转出 true判断 false不用", hidden = true)
    private Boolean isTransferBouns = Boolean.TRUE;

    @ApiModelProperty(value = "转账平台集合", hidden = true)
    private List<Integer> depotIds;

    @ApiModelProperty(value = "转账金额+红利金额")
    private BigDecimal sumAmount = BigDecimal.ZERO;

    @ApiModelProperty(value = "特殊平台订单前缀")
    private String prefix;
}