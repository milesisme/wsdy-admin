package com.wsdy.saasops.modules.member.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@ApiModel(value = "MbrActivityLevel", description = "")
@Table(name = "mbr_activity_level")
public class MbrActivityLevel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "等级名称")
    private String tierName;
    @ApiModelProperty(value = "前台说明")
    private String description;
    @ApiModelProperty(value = "等级数字：0开始(vip0)")
    private Integer accountLevel;
    @ApiModelProperty(value = "是否启用：1开启，0禁用")
    private Integer available;

    // 晋级本级条件
    @ApiModelProperty(value = "晋级条件勾选状态：勾选 0 累计投注 1 累计充值 2全部勾选")
    private Integer promoteSign;
    @ApiModelProperty(value = "晋级条件 满足条件： 0 全部 1任意")
    private Integer conditions;
    @ApiModelProperty(value = "累计投注最小 大于")
    private BigDecimal validbetMin;
    @ApiModelProperty(value = "累计投注最大 小于等于 暂时不用")
    private BigDecimal validbetMax;
    @ApiModelProperty(value = "存款最小 大于")
    private BigDecimal depositMin;
    @ApiModelProperty(value = "存款最大 小于等于 暂时不用")
    private BigDecimal depositMax;

    // 降级上一等级条件
    @ApiModelProperty(value = "降至上一等级条件期间累计投注小于")
    private BigDecimal downgradeBet;
    
    @ApiModelProperty(value = "会员恢复等级投注额")
    private BigDecimal recoverBet;

    // 每日取款限额
    @ApiModelProperty(value = "是否取款限制：1 限制 0 不限制")
    private Byte feeAvailable;
    @ApiModelProperty(value = "每日充许取款次数")
    private Integer withDrawalTimes;
    @ApiModelProperty(value = "每日取款限额")
    private BigDecimal withDrawalQuota;

    @ApiModelProperty(value = "创建时间")
    private String createTime;
    @ApiModelProperty(value = "创建人")
    private String createUser;
    @ApiModelProperty(value = "修改人")
    private String modifyUser;
    @ApiModelProperty(value = "修改时间")
    private String modifyTime;

    // 校验条件：废弃
    @ApiModelProperty(value = "1是 0否")
    private Integer isName;
    @ApiModelProperty(value = "1是 0否")
    private Integer isBank;
    @ApiModelProperty(value = "1是 0否")
    private Integer isMobile;
    @ApiModelProperty(value = "1是 0否")
    private Integer isMail;

    @Transient
    @ApiModelProperty(value = "会员数")
    private Integer accountCount;
    @Transient
    @ApiModelProperty(value = "会员锁定数")
    private Integer accountLockCount;
    @Transient
    @ApiModelProperty(value = "等级ids")
    private List<Integer> ids;
}