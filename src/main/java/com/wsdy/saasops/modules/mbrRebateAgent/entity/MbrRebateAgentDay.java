package com.wsdy.saasops.modules.mbrRebateAgent.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;

@Setter
@Getter
@ApiModel(value = "MbrRebateAgentDay", description = "")
@Table(name = "mbr_rebate_agent_day")
@ToString
public class MbrRebateAgentDay implements Serializable {
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
    @ApiModelProperty(value = "统计时间 yyyy-MM-dd")
    private String createTimeEx;
    @ApiModelProperty(value = "会员作为子节点深度")
    private Integer maxDepth;

    @ApiModelProperty(value = "昨日有效投注")
    private BigDecimal validbet;
    @ApiModelProperty(value = "昨日自身游戏派彩")
    private BigDecimal payout;
    @ApiModelProperty(value = "昨日自身红利")
    private BigDecimal bonusAmount;
    @ApiModelProperty(value = "昨日自身的有效派彩=自身游戏派彩-自身红利 正为0")
    private BigDecimal validPayoutForSelf;
    @ApiModelProperty(value = "昨日贡献上级的有效派彩=自身游戏派彩+自身红利  可以为正可以为负")
    private BigDecimal validPayoutForParent;
    @ApiModelProperty(value = "昨日下级直属会员(普通会员+代理会员)贡献的 有效派彩 合计")
    private BigDecimal validPayoutFromChildMember;
    @ApiModelProperty(value = "昨日下级代理会员贡献的 有效派彩 合计")
    private BigDecimal validPayoutFromChildMemberAgent;
    @ApiModelProperty(value = "昨日下级贡献的有效派彩=validpayoutfromchildmember+validpayoutfromchildmemberagent ")
    private BigDecimal validPayoutFromChild;

    @ApiModelProperty(value = "月累计有效投注")
    private BigDecimal validbetMonth;
    @ApiModelProperty(value = "月累计自身游戏派彩")
    private BigDecimal payoutMonth;
    @ApiModelProperty(value = "月累计自身红利")
    private BigDecimal bonusAmountMonth;
    @ApiModelProperty(value = "月累计自身的有效派彩=自身游戏派彩-自身红利 正为0")
    private BigDecimal validPayoutForSelfMonth;
    @ApiModelProperty(value = "月累计贡献上级的有效派彩=自身游戏派彩+自身红利  可以为正可以为负")
    private BigDecimal validPayoutForParentMonth;
    @ApiModelProperty(value = "月累计下级直属会员(普通会员+代理会员)贡献的 有效派彩 合计")
    private BigDecimal validPayoutFromChildMemberMonth;
    @ApiModelProperty(value = "月累计下级代理会员贡献的 有效派彩 合计")
    private BigDecimal validPayoutFromChildMemberAgentMonth;
    @ApiModelProperty(value = "月累计下级贡献的有效派彩=validPayoutFromChildMemberMonth+validPayoutFromChildMemberAgentMonth ")
    private BigDecimal validPayoutFromChildMonth;
}