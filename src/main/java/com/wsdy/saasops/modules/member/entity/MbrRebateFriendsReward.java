package com.wsdy.saasops.modules.member.entity;

import com.wsdy.saasops.common.constants.FriendRebateConstants;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Arrays;

/**
 * 好友推荐奖励发放记录
 */
@Data
@Table(name = "mbr_rebate_friends_reward")
public class MbrRebateFriendsReward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "活动ID")
    private Integer activityId;

    @ApiModelProperty(value = "会员帐号")
    private String loginName;

    @ApiModelProperty(value = "会员id")
    private Integer accountId;

    @ApiModelProperty(value = "数据类型")
    private Integer type;

    @ApiModelProperty(value = "奖励类型")
    private BigDecimal reward;

    @ApiModelProperty(value = "收益日")
    private String incomeTime;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "发放时间")
    private String giveOutTime;

    @ApiModelProperty(value = "操作类型，1手动， 0机器")
    private Integer operationType;

    @ApiModelProperty(value = "邀请数量")
    private  Integer inviteNum;

    @ApiModelProperty(value = "保存一些特殊数据")
    private String content;

    @ApiModelProperty(value = "状态")
    private Integer status;

    @ApiModelProperty(value = "创建着")
    private String creater;

    @ApiModelProperty(value = "账变ID")
    private Integer billDetailId;

    @ApiModelProperty(value = "备注")
    private String memo;

    @ApiModelProperty(value = "稽核倍数")
    @Transient
    private BigDecimal auditMultiple;

    @Override
    public String toString() {

        return "{" +
                "loginName='" + loginName + '\'' +
                ", accountId=" + accountId +
                ", type=" + Arrays.stream(FriendRebateConstants.values()).filter(item ->(item.getValue() == type)).findFirst().get().getName() +
                ", reward=" + reward +
                ", inviteNum=" + inviteNum +
                ", content='" + content + '\'' +
                '}';
    }

}
