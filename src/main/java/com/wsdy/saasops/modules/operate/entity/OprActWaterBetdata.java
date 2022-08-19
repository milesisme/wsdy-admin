package com.wsdy.saasops.modules.operate.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;


@Setter
@Getter
@ApiModel(value = "OprActWaterBetdata", description = "返水时间批次表")
@Table(name = "opr_act_water_betdate")
public class OprActWaterBetdata implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "活动ID")
    private Integer activityId;

    @ApiModelProperty(value = "返水统计开始时间")
    private String waterStart;

    @ApiModelProperty(value = "返水统计结束时间")
    private String waterEnd;

    @ApiModelProperty(value = "返水申请时间")
    private String applicationTime;
}