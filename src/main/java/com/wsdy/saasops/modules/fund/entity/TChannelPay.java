package com.wsdy.saasops.modules.fund.entity;

import lombok.Getter;
import lombok.Setter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;


@Setter
@Getter
@ApiModel(value = "TChannelPay", description = "代付渠道配置表")
@Table(name = "t_channel_pay")
public class TChannelPay implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "最后一次修改人的账号")
    private String modifyUser;

    @ApiModelProperty(value = "最后一次修改时间")
    private String modifyTime;

    @ApiModelProperty(value = "状态　1开启，0禁用")
    private Integer available;

    @ApiModelProperty(value = "渠道例如：汇通代付")
    private String channelName;
    // 加密货币新增
    @ApiModelProperty(value = "提现方式：0银行卡， 1加密货币钱包  2银行卡+加密货币钱包")
    private Integer methodType;
    @ApiModelProperty(value = "货币类型 USDT")
    private String currencyCode;
    @ApiModelProperty(value = "协议类型 ERC20 TRC20")
    private String currencyProtocol;

    // LBT
    @ApiModelProperty(value = "平台代码 ")
    private String platformCode;
}