package com.wsdy.saasops.modules.member.entity;

import com.wsdy.saasops.modules.base.entity.BaseAuth;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Setter
@Getter
@ApiModel(value = "MbrGroup", description = "会员组")
@Table(name = "mbr_group")
public class MbrGroup implements Serializable {
	
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @Transient
    @ApiModelProperty(value = "会员ID,List")
    private Long[] ids;

    @ApiModelProperty(value = "会员组名")
    private String groupName;

    @ApiModelProperty(value = "备注")
    private String memo;

    @ApiModelProperty(value = "1开启，0禁用")
    private Byte available;

   /* @ApiModelProperty(value = "活动层级id")
    private Integer actLevelId;*/

    @ApiModelProperty(value = "0不是默认，1默认")
    @Transient
    private Byte isDef;
    @ApiModelProperty(value = "是否黑名单组 1是 0不是")
    private Byte isBlackGroup;

    // 当前组会员数
    @Transient
    private Integer mbrNums;

    // 会司入款最低限额
    @Transient
    private BigDecimal lowQuota;
    
    /**
     * 累计存款
     */
    private BigDecimal deposit;
    
    /**
     * 累计存款次数
     */
    private Integer depositTimes;
    
    /**
     * 累计公司输赢
     */
    private BigDecimal companyWinLose;
    
    /**
     * 累计有效投注
     */
    private BigDecimal validBet;

    /**
     * 1：锁定升级 0：不
     */
    private Integer isLockUpgrade;

    @ApiModelProperty(value = "1开启，0禁用 开启不同名银行卡")
    private Integer bankDifferentName;

    // 公司入款最高限额
    @Transient
    private BigDecimal topQuota;

    // 存款手续费
    @Transient
    private String depositFee;

    // 存款限免时间
    @Transient
    private Integer feeHours;

    // 存款限免次数
    @Transient
    private Integer feeTimes;

    // 每日取款限额
    @Transient
    private BigDecimal withDrawalQuota;

    // 单笔取款最低限额
    @Transient
    private BigDecimal wLowQuota;

    // 单笔取款最高限额CNY
    @Transient
    private BigDecimal wTopQuota;

    // 单笔支付宝取款最低限额
    @Transient
    private BigDecimal wLowAlipayQuota;

    // 单笔支付宝取款最高限额CNY
    @Transient
    private BigDecimal wTopAlipayQuota;

    // 单笔取款最低限额USDT
    @Transient
    private BigDecimal wLowUsdt;

    // 单笔取款最高限额
    @Transient
    private BigDecimal wTopUsdt;

    // 取款手续费
    @Transient
    private String withdrawalFee;

    // 单笔取款手续费 时限/小时
    @Transient
    private Integer wfeeHours;

    // 单笔取款限免次数
    @Transient
    private Integer wfeeTimes;

    // 存款详细设置Id
    @Transient
    private Integer did;

    // 取款详细设置Id
    @Transient
    private Integer wid;
    //是否完成(1完成，0未完成)
    @Transient
    private Integer groupDone;
    // 组Id
    @Transient
    private Integer groupId;
    @Transient
    private String groupIds;
    @Transient
    private BaseAuth baseAuth;


    @Transient
    @ApiModelProperty(value = "会员组 查询使用")
    private String groupIdList;

    @Transient
    @ApiModelProperty(value = "1开启，0禁用 查询使用")
    private String availableList;

    @Transient
    @ApiModelProperty(value = "是否收取手续费开关 1 收取 0不收取 ")
    private Byte chargeFeeAvailable;

    @Transient
    @ApiModelProperty(value = "不同名银行卡数量")
    private Integer bankDifferentNumber;

}