package com.wsdy.saasops.modules.mbrRebateAgent.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Setter
@Getter
@ApiModel(value = "MbrRebateAgentMonth", description = "")
@Table(name = "mbr_rebate_agent_month")
@ToString
public class MbrRebateAgentMonth implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "会员ID")
    private Integer accountId;
    @ApiModelProperty(value = "会员账号")
    private String loginName;
    @ApiModelProperty(value = "父结点")
    private Integer parentId;
    @ApiModelProperty(value = "父结点会员账号")
    private String loginNameParent;
    @ApiModelProperty(value = "全民代理标志 0非代理会员 1代理会员")
    private Integer agyflag;
    @ApiModelProperty(value = "代理会员级别id")
    private Integer agyLevelId;
    @ApiModelProperty(value = "计算时间 精确到时分秒")
    private String createTime;
    @ApiModelProperty(value = "统计时间 yyyy-mm")
    private String createTimeEx;
    @ApiModelProperty(value = "会员作为子节点深度")
    private Integer maxDepth;

    @ApiModelProperty(value = "有效投注")
    private BigDecimal validbet;
    @ApiModelProperty(value = "自身游戏派彩")
    private BigDecimal payout;
    @ApiModelProperty(value = "自身红利")
    private BigDecimal bonusAmount;
    @ApiModelProperty(value = "自身的有效派彩=自身游戏派彩-自身红利 正为0")
    private BigDecimal validPayoutForSelf;
    @ApiModelProperty(value = "贡献上级的有效派彩=自身游戏派彩+自身红利  可以为正可以为负")
    private BigDecimal validPayoutForParent;
    @ApiModelProperty(value = "下级直属会员(普通会员+代理会员)贡献的 有效派彩 合计")
    private BigDecimal validPayoutFromChildMember;
    @ApiModelProperty(value = "下级代理会员贡献的 有效派彩 合计")
    private BigDecimal validPayoutFromChildMemberAgent;
    @ApiModelProperty(value = "下级贡献的有效派彩=validpayoutfromchildmember+validpayoutfromchildmemberagent ")
    private BigDecimal validPayoutFromChild;

    @ApiModelProperty(value = "自身返利比例")
    private BigDecimal commissionRatio;
    @ApiModelProperty(value = "自身返利=自身的有效派彩 * 自身返利比例")
    private BigDecimal rebate;

    @ApiModelProperty(value = "下级提成比例")
    private BigDecimal commissionRatioSub;
    @ApiModelProperty(value = "贡献上级的奖金")
    private BigDecimal bonusAmountExForParent;
    @ApiModelProperty(value = "自身获得下级的奖金总计")
    private BigDecimal bonusAmountExfromChildTotal;
    @ApiModelProperty(value = "奖金百分比")
    private BigDecimal bonusPercent;

    @ApiModelProperty(value = "下级代理实发总计")
    private BigDecimal rebateChildTotal;
    @ApiModelProperty(value = "自身获得的实际下级佣金初算=下级贡献的有效派彩*(下级提成比例/100)")
    private BigDecimal rebateFromChild;
    @ApiModelProperty(value = "自身获得的实际下级佣金实际=初算-下级代理实发总计")
    private BigDecimal rebateFromChildActual;

    @ApiModelProperty(value = "自身实发=自身返利rebate + 自身获得下级的奖金总计bonusAmountExfromChildTotal + 自身获得的实际下级佣金实际rebateFromChildActual")
    private BigDecimal rebateTotal;
    @ApiModelProperty(value = "自身实发给上级计算值=自身返利rebate  + 自身获得的实际下级佣金实际rebateFromChildActual")
    private BigDecimal rebateTotalForParent;

    // 其他
    @ApiModelProperty(value = "活动ID")
    private Integer activityId;
    @ApiModelProperty(value = "活动规则ID")
    private Integer ruleId;
    @ApiModelProperty(value = "红利id")
    private Integer bonusId;

    @Transient
    @ApiModelProperty(value = "计算时用，子节点深度")
    private Integer depth;
    @Transient
    @ApiModelProperty(value = "计算时用，上级父节点，逗号分割")
    private String parentIds;

}