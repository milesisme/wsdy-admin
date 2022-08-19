package com.wsdy.saasops.modules.activity.entity;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Data
@Table(name = "mbr_rebate_hupeng")
public class MbrRebateHuPeng {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "活动ID")
    private Integer activityId;

    @ApiModelProperty(value = "会员帐号")
    private String loginName;

    @ApiModelProperty(value = "会员id")
    private Integer accountId;

    @ApiModelProperty(value = "子会员帐号")
    private String subLoginName;

    @ApiModelProperty(value = "子会员id")
    private Integer subAccountId;

    @ApiModelProperty(value = "金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "奖励")
    private BigDecimal reward;

    @ApiModelProperty(value = "收益日")
    private String incomeTime;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "发放时间")
    private String giveOutTime;

    @ApiModelProperty(value = "操作类型，1手动， 0机器")
    private Integer operationType;

    @ApiModelProperty(value = "下注著单数")
    private Integer betNum;

    @ApiModelProperty(value = "奖励注单数量")
    private Integer rewardNum;


    @ApiModelProperty(value = "创建着")
    private String creater;


    @ApiModelProperty(value = "备注")
    private String memo;

    @ApiModelProperty(value = "比率")
    private BigDecimal  rate;

}
