package com.wsdy.saasops.modules.member.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;


@Setter
@Getter
@ApiModel(value = "MbrAuditFraud", description = "稽核违规记录表")
@Table(name = "mbr_audit_fraud")
public class MbrAuditFraud implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "开始时间")
    private String startTime;

    @ApiModelProperty(value = "结束时间")
    private String entTime;

    @ApiModelProperty(value = "平台Name")
    private String depotName;

    @ApiModelProperty(value = "分类Name")
    private String catName;

    @ApiModelProperty(value = "违规投注")
    private BigDecimal fraudValidBet;

    @ApiModelProperty(value = "派彩")
    private BigDecimal payOut;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "产生交易记录order")
    private Long orderNo;

}