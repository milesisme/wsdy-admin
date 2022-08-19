package com.wsdy.saasops.modules.member.entity;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 *  会员活动数据
 */
@Data
@Table(name = "mbr_rebate_act_data")
public class MbrRebateActData {


   public static final int  VIP = 1; // VIP数据

   public static final int  FRIEND = 2;// 好友数据

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    private Integer accountId;

    private Integer activityId;

    private Integer dataType;

    private Integer subAccountId;

    private Integer vipLevel;

    private Integer num;

    private BigDecimal minCharge;

}
