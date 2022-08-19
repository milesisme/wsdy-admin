package com.wsdy.saasops.modules.member.entity;

import com.wsdy.saasops.common.constants.FriendRebateConstants;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

/**
 *  好友推荐统计信息
 */
@Data
@Table(name = "mbr_rebate_friends")
public class MbrRebateFriends implements Serializable {

    private static final long serialVersionUID = 1L;
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

    @ApiModelProperty(value = "下级会员id")
    private Integer subAccountId;

    @ApiModelProperty(value = "下级会员名")
    private String subLoginName;

    @ApiModelProperty(value = "数据类型")
    private Integer type;

    @ApiModelProperty(value = "金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "奖励")
    private BigDecimal reward;

    @ApiModelProperty(value = "收益日")
    private String incomeTime;

    @ApiModelProperty(value = "事件时间")
    private String eventTime;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "操作类型，1手动， 0机器")
    private Integer operationType;

    @ApiModelProperty(value = "升级开始等级")
    private Integer fromVip;

    @ApiModelProperty(value = "到达等级")
    private Integer toVip;

    @Transient
    private Map<Integer, BigDecimal> validBetMap;

    @Transient
    private Integer originType;

    @Override
    public String toString() {
        return "MbrRebateFriends{" +
                "loginName='" + loginName + '\'' +
                ", accountId=" + accountId +
                ", subAccountId=" + subAccountId +
                ", subLoginName='" + subLoginName + '\'' +
                ", type=" + Arrays.stream(FriendRebateConstants.values()).filter(item ->(item.getValue() == type)).findFirst().get().getName() +
                ", amount=" + amount +
                ", reward=" + reward +
                ", eventTime='" + eventTime + '\'' +
                ", fromVip=" + fromVip +
                ", toVip=" + toVip +
                '}';
    }
}
