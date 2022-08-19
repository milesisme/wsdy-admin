package com.wsdy.saasops.modules.member.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Table(name = "mbr_depot_trade")
public class MbrDepotTrade implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "会员id")
    private Integer accountId;

    @ApiModelProperty(value = "会员账号")
    private String loginName;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "数据中心交易号")
    private String outTradeno;

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "操作类型，0 支出1 收入")
    private Integer opType;

    @ApiModelProperty(value = "操作金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "平台code")
    private String depotCode;

    @ApiModelProperty(value = "财务类别代码")
    private String financialcode;

    @ApiModelProperty(value = "操作后余额")
    private BigDecimal afterBalance;

    @ApiModelProperty(value = "操作前的余额")
    private BigDecimal beforeBalance;
}