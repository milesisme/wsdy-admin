package com.wsdy.saasops.modules.task.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ApiModel(value = "TaskBonus", description = "")
@Table(name = "task_bonus")
public class TaskBonus implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "会员id")
    private Integer accountId;

    @ApiModelProperty(value = "会员名")
    private String loginName;

    @ApiModelProperty(value = "红利")
    private BigDecimal bonusAmount;

    @ApiModelProperty(value = "稽核点")
    private Integer discountAudit;

    @ApiModelProperty(value = "configid")
    private Integer configId;

    @ApiModelProperty(value = "创建时间")
    private String time;

    @ApiModelProperty(value = "天次数")
    private Integer num;

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @Transient
    @ApiModelProperty(value = "任务名称")
    private String taskName;

    @Transient
    @ApiModelProperty(value = "总领取人数")
    private String receiveNum;

    @Transient
    @ApiModelProperty(value = "点击量")
    private Long number;

    @Transient
    @ApiModelProperty(value = "直属代理ID")
    private Integer cagencyId;

    @Transient
    @ApiModelProperty(value = "startTime")
    private String startTime;

    @Transient
    @ApiModelProperty(value = "endTime")
    private String endTime;


    @Transient
    @ApiModelProperty(value = "1 正式代理  0测试代理")
    private Integer isCagency;

    @Transient
    @ApiModelProperty(value = "代理账号")
    private String agyAccount;

    @Transient
    private String financialcode;

//    @Transient
//    @ApiModelProperty(value = "总充值金额/导出用")
//    private BigDecimal rechargeAmount;
//
//    @Transient
//    @ApiModelProperty(value = "总参与人数/导出用")
//    private Integer partInNum;
//
//    @Transient
//    @ApiModelProperty(value = "总新增人数/导出用")
//    private Integer addNewNum;
}