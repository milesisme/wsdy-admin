package com.wsdy.saasops.modules.member.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Table(name = "mbr_depot_trade_log")
public class MbrDepotTradeLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "数据中心交易号")
    private String outTradeno;

    @ApiModelProperty(value = "00成功")
    private String code;

    @ApiModelProperty(value = "参数字符串")
    private String param;

    @ApiModelProperty(value = "备注")
    private String memo;
}